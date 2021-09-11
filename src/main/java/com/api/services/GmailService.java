package com.api.services;

import com.api.domain.EmailParameters;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@Service
public interface GmailService {
//    void setGmailCredentials(GmailCredentials gmailCredentials);

//    void setCredential(GoogleAuthorizationCodeFlow flow,String code,String redirectUri);

//    boolean sendMessage(List<EmailParameters> emails) throws MessagingException, IOException;

    void exchangeCode(String code);

    String authorize() throws Exception;

    boolean addEmail(EmailParameters emailParameterstoSend);
}
