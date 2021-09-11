package com.api.services;


import com.api.domain.EmailParameters;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private static boolean serverOn=false;
    private static boolean sendeds=false;

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

    @Value("${gmail.client.tokenExpiresIn}")
    private Long tokenExpiresIn;


    private Message message;

    Credential credential;
    Gmail userGmail;
    GoogleAuthorizationCodeFlow flow;
    GoogleClientSecrets clientSecrets;

    private List<Message> messageList= new ArrayList<>(Arrays.asList());;
    // TODO CREATE EMAIL BD
    private List<Integer> emailsNoSended = new ArrayList<>(Arrays.asList());
    private static final List<String> SCOPES=new ArrayList<>(Arrays.asList(GmailScopes.GMAIL_SEND, GmailScopes.MAIL_GOOGLE_COM));

    @Override
    public String authorize() throws Exception {

     if(!serverOn){
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
        System.out.println("The server was already on ");
       return "/";
    }

    @Override
    public boolean addEmail(EmailParameters emailParametersToSend) throws IOException {

        Message message = null;
       if(serverOn) {
           try {
               message = createMessageWithEmail(
                       createEmail(emailParametersToSend.getRecipientAddress()
                               , emailParametersToSend.getFrom()
                               , emailParametersToSend.getSubject()
                               , emailParametersToSend.getBody()));

              return messageList.add(message);
           } catch (MessagingException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
        return false;
    }

    @Override
    public void exchangeCode(String code) {
        try {

            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute().setExpiresInSeconds(tokenExpiresIn);
            credential = flow.createAndStoreCredential(response, "userId");
            System.out.println("The token expire in -------"+ response.getExpiresInSeconds());
            userGmail = createGmail();

            serverOn=true;
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

    @Scheduled(cron = "0/15 * * * * ?")
    public void sendMessage() throws IOException {
        if(serverOn&&!messageList.isEmpty()) {
            System.out.println("Server authorized status "+ serverOn);
            System.out.println("MessageList size "+ messageList.size());
            userGmail.users().messages().list(emailFrom);
            messageList.stream().forEach((message) -> {

                        try {
                            System.out.println("getBody ---" + message.toPrettyString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            if (!userGmail.users()
                                    .messages()
                                    .send(message.getId(), this.message)
                                    .execute()
                                    .getLabelIds().contains("SENT")) {

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            );
        }
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
//TODO SE THE WAY TO UPDATE THE BASE64 CLASS
        return new Message()
                .setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
    }


}
