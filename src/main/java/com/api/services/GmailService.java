package com.api.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public interface GmailService {
//    void setGmailCredentials(GmailCredentials gmailCredentials);

//    void setCredential(GoogleAuthorizationCodeFlow flow,String code,String redirectUri);

    boolean sendMessage(List< EmailParameters> emails) throws MessagingException, IOException;

    boolean  initialize() throws Exception;
}
