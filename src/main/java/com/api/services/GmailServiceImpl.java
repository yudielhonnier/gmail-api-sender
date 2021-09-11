package com.api.services;


import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;

@Service
public final class GmailServiceImpl implements GmailService {

    private static final String APPLICATION_NAME = "GMAIL API SENDER";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport httpTransport;

    @Value("${gmail.client.clientId}")
    private String clientId;

    @Value("${gmail.client.clientSecret}")
    private String clientSecret;

    @Value("${gmail.client.redirectUri}")
    private String redirectUri;

    @Value("${gmail.client.userAuthorizationUri}")
    private String authUri;

    @Value("${gmail.client.accessTokenUri}")
    private String tokenAuthUri;

    @Value("${gmail.client.accessType}")
    private String accessType;

//    @Value("${gmail.client.scopes}")
//    private String scopesAplicationProperties;

    @Value("${gmail.client.javascript_origins}")
    private String javascript_origins;

    @Value("${gmail.client.approvalPromt}")
    private String approvalPromt;

    @Value("${gmail.client.emailFrom}")
    private String emailFrom;

    Credential credential;
    Gmail userGmail;
    GoogleAuthorizationCodeFlow flow;
    GoogleClientSecrets clientSecrets;

    private List<Integer> emailsNoSended = new ArrayList<>(Arrays.asList());
    private static final List<String> SCOPES=new ArrayList<>(Arrays.asList(GmailScopes.GMAIL_SEND, GmailScopes.MAIL_GOOGLE_COM));

    @Override
    public String authorize() throws Exception {

        if (flow == null) {
            Details web = new Details();
            web.setClientId(clientId);
            web.setClientSecret(clientSecret);
            clientSecrets = new GoogleClientSecrets().setWeb(web);
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                    SCOPES)
                    .setAuthorizationServerEncodedUrl(authUri)
                    .setTokenServerUrl(new GenericUrl(tokenAuthUri))
                    .setAccessType(accessType)
                    .setApprovalPrompt(approvalPromt)
                    .build();

        }

        System.out.println("flow  loaded");

        AuthorizationCodeRequestUrl authorizationUrl;
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);

        System.out.println("authorizationUrl --->\n" + authorizationUrl);
        return authorizationUrl.build();
    }



    @Override
    public String initialize() throws Exception {
      return "authorized";

    }

    @Override
    public void exchangeCode(String code) {
        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            credential = flow.createAndStoreCredential(response, "userId");
            System.out.println(credential.getTransport().toString());
            userGmail = createGmail();
            userGmail.users().messages().list(emailFrom);

        } catch (Exception e) {

            System.out.println("exception cached ");
            e.printStackTrace();
        }

    }

    private Gmail createGmail()  {
        return new Gmail.Builder(credential.getTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public boolean sendMessage(List<EmailParameters> emails) throws IOException {
        emails.stream().forEach((emailParametersToSend) -> {

                    Message message = null;
                    try {
                        message = createMessageWithEmail(
                                createEmail(emailParametersToSend.getRecipientAddress()
                                        , emailParametersToSend.getFrom()
                                        , emailParametersToSend.getSubject()
                                        , emailParametersToSend.getBody()));
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("getBody ---" + emailParametersToSend.getBody());

                    try {
                        System.out.println("userGmail ---" + userGmail.toString());
                        if (!userGmail.users()
                                .messages()
                                .send(emailParametersToSend.getFrom(), message)
                                .execute()
                                .getLabelIds().contains("SENT")) {
                            System.out.println("Problem sendind mesagge tu user  " + emailParametersToSend.getRecipientAddress());
                            emailParametersToSend.setEnviado("false");

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        return true;
    }



    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);

        return new Message()
                .setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
    }


}
