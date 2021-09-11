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

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;
import java.io.*;
import java.util.*;

@Service
public final class GmailServiceImpl implements GmailService {

    private static final String APPLICATION_NAME = "GMAIL API SENDER";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport httpTransport;
    private static boolean serverOn=false;
    private static boolean sendingMessages=false;

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

//    @Value("${sheduler.configuration.cron}")
//    private Long shedulerCron;


    Credential credential;
    Gmail userGmail;
    GoogleAuthorizationCodeFlow flow;
    GoogleClientSecrets clientSecrets;

    private List<Message> messageList= new ArrayList<>(Arrays.asList());
    private List<Message> messagesNoSendeds= new ArrayList<>(Arrays.asList());
    // TODO CREATE EMAIL BD

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
               System.out.println(message.getId());
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
            userGmail.users().messages().list(emailFrom);
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

    @Scheduled(cron ="0 0/15 * * * ?" )
    public void sendMessage()  {
        if(serverOn&&!messageList.isEmpty()&&!sendingMessages) {
            sendingMessages=true;
            System.out.println("Server authorized status "+ serverOn);
            System.out.println("MessageList size "+ messageList.size());
            messageList.stream().forEach((message) -> {

                try {
                                userGmail.users()
                                        .messages()
                                        .send(emailFrom,message)
                                        .execute()
                                        .getLabelIds().contains("SENT");

                                message.getLabelIds().stream().forEach(s -> System.out.println("labels "+s));

                        } catch (Exception e) {
                            messagesNoSendeds.add(message);
                            e.printStackTrace();
                        }
                    }

            );
            messageList.clear();

        }
    }


    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to email address of the receiver
     * @param from email address of the sender, the mailbox account
     * @param subject subject of the email
     * @param bodyText body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     */
    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties(), null));
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException
     * @throws MessagingException
     */
    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
//TODO SE THE WAY TO UPDATE THE BASE64 CLASS
        return new Message()
                .setRaw(Base64.encodeBase64URLSafeString(buffer.toByteArray()));
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to Email address of the receiver.
     * @param from Email address of the sender, the mailbox account.
     * @param subject Subject of the email.
     * @param bodyText Body text of the email.
     * @param file Path to the file to be attached.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException
     */

    public static MimeMessage createEmailWithAttachment(String to,
                                                        String from,
                                                        String subject,
                                                        String bodyText,
                                                        File file)
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText, "text/plain");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        mimeBodyPart = new MimeBodyPart();
        DataSource source = (DataSource) new FileDataSource(file);

        mimeBodyPart.setDataHandler(new DataHandler((javax.activation.DataSource) source));
        mimeBodyPart.setFileName(file.getName());

        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart);

        return email;
    }

//    public List<Message> listMessagesMatchingQuery(String userId, String query) throws IOException {
//        ListMessagesResponse response = createGmail().users().messages().list(userId).setQ(query).execute();
//        List<Message> messages = new ArrayList<Message>();
//
//        while (response.getMessages() != null) {
//            messages.addAll(response.getMessages());
//            if (response.getNextPageToken() != null) {
//                String pageToken = response.getNextPageToken();
//                response = createGmail().users().messages().list(userId).setQ(query).setPageToken(pageToken).execute();
//            }
//            else {
//                break;
//            }
//        }
//
//        // for (Message message : messages) {
//        //     System.out.println(message.toPrettyString());
//        // }
//
//        return messages;
//    }
//
//    public String getMessageContent(String id){
//        String result = "";
//        try {
//            //Message msg = createGmail().users().messages().get("me", id).setFormat("full").execute();
//            Message msg = createGmail().users().messages().get("manhattan.project.1939@gmail.com", id).setFormat("full").execute();
//            result = StringUtils.newStringUtf8(Base64.decodeBase64(msg.getPayload().getParts().get(0).getBody().getData()));
//        }
//        catch (IOException ioe){
//            result = "";
//        }
//        return result;
//    }
//
//    public void getMessage2(String id) throws IOException{
//        Message msg = createGmail().users().messages().get("manhattan.project.1939@gmail.com", id).setFormat("full").execute();
//
//        for (MessagePartHeader header : msg.getPayload().getHeaders()) {
//            if (header.getName().contains("Date") || header.getName().contains("From") || header.getName().contains("To") || header.getName().contains("Subject"))
//                System.out.println(header.getName() + ":" + header.getValue());
//        }
//
//        // Displaying Message Body as a Plain Text
//        for (MessagePart msgPart : msg.getPayload().getParts()) {
//            if (msgPart.getMimeType().contains("text/plain"))
//                System.out.println(new String(Base64.decodeBase64(msgPart.getBody().getData())));
//        }
//
//        byte[] bodyBytes = Base64.decodeBase64(msg.getPayload().getParts().get(0).getBody().getData().trim().toString()); // get body
//        String body = new String(bodyBytes, "UTF-8");
//        System.out.println("---> body: "+body);
//    }
//
//
//    public Message getMessage(String userId, String messageId) throws IOException {
//        Message message = createGmail().users().messages().get(userId, messageId).execute();
//        System.out.println("Message snippet: " + message.getSnippet());
//        return message;
//    }

}
