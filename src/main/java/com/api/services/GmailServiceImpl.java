package com.api.services;


import com.api.GmailApplication;
import com.google.api.client.auth.oauth2.Credential;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
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
    private static final int LOCAL_RECEIVER_PORT = 8082;

    Credential credential;

    private static final List<String> SCOPES=new ArrayList<>(Arrays.asList(GmailScopes.GMAIL_SEND,GmailScopes.MAIL_GOOGLE_COM));

    private Credential authorize(final NetHttpTransport HTTP_TRANSPORT) throws Exception {

        // Load client_secret.json file
        FileInputStream fileInputStream=new FileInputStream("client_secret.json");

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(fileInputStream));

//         Generate the url that will be used for the consent dialog.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT ,
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES
                        )
                        .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
                        .setAccessType("offline")
                        .setApprovalPrompt("auto")
                        .build();

//         Exchange an authorization code for  refresh token
        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder().setPort(LOCAL_RECEIVER_PORT).build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return credential;
    }


    private List<Integer> emailsNoSended=new ArrayList<>(Arrays.asList());

    @Override
    public boolean sendMessage(List<EmailParameters> emails)  {

            emails.stream().forEach((emailParametersToSend) ->{

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
                        System.out.println("getBody ---"+emailParametersToSend.getBody());

                        try {
                            Gmail userGmail=createGmail();
                            userGmail.users().messages().list("laflechahonnyone@gmail.com");
                            if (! userGmail.users()
                                    .messages()
                                    .send(emailParametersToSend.getFrom(), message)
                                    .execute()
                                    .getLabelIds().contains("SENT")){
                                System.out.println("Problem sendind mesagge tu user  "+emailParametersToSend.getRecipientAddress());
                               emailParametersToSend.setEnviado("false");

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    );

        return true;
    }

    private Gmail createGmail() throws Exception {
       NetHttpTransport netHttpTransport= GoogleNetHttpTransport.newTrustedTransport();
        credential=authorize(netHttpTransport);
        return new Gmail.Builder(credential.getTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
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
