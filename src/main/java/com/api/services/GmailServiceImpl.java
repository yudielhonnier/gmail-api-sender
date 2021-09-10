package com.api.services;


import com.api.GmailApplication;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
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

    private static final String APPLICATION_NAME = "PortalFromSpring";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final File DATA_STORE_DIR =
            new File(GmailApplication.class.getResource("/").getPath(), "credentials");

    // port of redirect_uri http://localhost:8082/Callback
//    private static final int LOCAL_RECEIVER_PORT = 78547;

    Credential credential;

    Gmail userGmail;

    private static final List<String> SCOPES = new ArrayList<>(Arrays.asList(GmailScopes.GMAIL_SEND, GmailScopes.MAIL_GOOGLE_COM));
    private List<Integer> emailsNoSended = new ArrayList<>(Arrays.asList());

    @Override
    public boolean initialize() throws Exception {
        userGmail = createGmail();
        userGmail.users().messages().list("laflechahonnyone@gmail.com");
        return true;
    }

    private Gmail createGmail() throws Exception {
        NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        credential = authorize(netHttpTransport);
        System.out.println("credentials created");
        return new Gmail.Builder(credential.getTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize(final NetHttpTransport HTTP_TRANSPORT) throws Exception {

        // Load client_secret.json file
        FileInputStream fileInputStream = new FileInputStream("client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(fileInputStream));
        System.out.println("client secret loaded");
//         Generate the url that will be used for the consent dialog.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES
                )
                        .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
                        .setAccessType("offline")
                        .setApprovalPrompt("auto")
                        .build();
        System.out.println("flow  loaded");

//         Exchange an authorization code for  refresh token

//        System.out.println("receiver----" + receiver.getRedirectUri());
//        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        AuthorizationCodeRequestUrl authorizationUrl;
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri("https://gmail-api-sender.herokuapp.com/Callback");

        System.out.println("Please copy and paste this authorizationUrl in your browser->" + authorizationUrl);

        return credential;
    }

    @Override
    public void setCredential(GoogleAuthorizationCodeFlow flow, String code, String redirectUri) {
        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            credential = flow.createAndStoreCredential(response, "userId");

        } catch (Exception e) {

            System.out.println("exception cached ");
            e.printStackTrace();
        }

    }

    @Override
    public boolean sendMessage(List<EmailParameters> emails) {

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
