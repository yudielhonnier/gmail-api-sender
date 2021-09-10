package com.api.controllers;

import java.io.FileInputStream;
import java.util.*;

import javax.annotation.Resource;

import com.api.services.EmailParameters;
import com.api.services.GmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.web.servlet.view.RedirectView;
import com.google.api.services.gmail.GmailScopes;

@Controller
@RestController
public class GoogleMailController {

     private static List<EmailParameters> emails = new ArrayList<>(Arrays.asList());
//
//    @Value("${gmail.client.clientId}")
//    private String clientId;
//
//    @Value("${gmail.client.clientSecret}")
//    private String clientSecret;
//
//    @Value("${gmail.client.redirectUri}")
//    private String redirectUri;
//
//    @Value("${gmail.client.userAuthorizationUri}")
//    private String authUri;
//
//    @Value("${gmail.client.accessTokenUri}")
//    private String tokenAuthUri;
//
//    @Value("${gmail.client.accessType}")
//    private String accessType;
//
//    @Value("${gmail.client.scopes}")
//    private String scopesAplicationProperties;

    @Resource
    private GmailService gmailService;


    @RequestMapping(value = "/email/send", method = RequestMethod.GET, consumes = "application/json")
    public ResponseEntity googleConnectionStatus(@RequestBody EmailParameters emailParametersData) throws Exception {

        emails.add( emailParametersData);
       if( gmailService.sendMessage(emails)) {
           return new ResponseEntity(HttpStatus.CREATED);
       }
       return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/email/initialize", method = RequestMethod.GET)
    public ResponseEntity googleInitialize() throws Exception {

        gmailService.initialize();
        return new ResponseEntity(HttpStatus.OK);
    }
}
