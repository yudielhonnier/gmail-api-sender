package com.api.controllers;

import java.util.*;

import javax.annotation.Resource;

import com.api.domain.EmailParameters;
import com.api.services.GmailService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RestController
public class GoogleMailController {

     private static List<EmailParameters> emails = new ArrayList<>(Arrays.asList());

    @Value("${gmail.client.redirectUri}")
    private String redirectUri;

    @Resource
    private GmailService gmailService;

    @RequestMapping(value = "/email/send", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity sendEmail(@RequestBody EmailParameters emailParametersData)   {

       if( gmailService.addEmail(emailParametersData)) {
           return new ResponseEntity(HttpStatus.CREATED);
       }
       return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/Callback", method = RequestMethod.GET, params = "code")
    public ResponseEntity<String> oauth2Callback(@RequestParam(value = "code") String code) {
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();

        System.out.println("Look jose the method entered");
        // String message;
        try {
            gmailService.exchangeCode( code);
                  json.put("response", arr);
        } catch (Exception e) {
            System.out.println("exception cached ");
            e.printStackTrace();
        }
        return new ResponseEntity<>(json.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/email/authorize", method = RequestMethod.GET)
    public RedirectView googleAuthorize() throws Exception {

        return new RedirectView(gmailService.authorize());
    }
}
