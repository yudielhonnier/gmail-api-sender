//package com.api.utils;
//
//import com.google.api.client.auth.oauth2.Credential;
//
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.auth.oauth2.UserCredentials;
//import com.google.common.collect.ImmutableList;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.GeneralSecurityException;
//import java.util.List;
//
//// Sample to authenticate by using a user credential
//public class AuthUserFlow {
//
//    private static final File DATA_STORE_DIR =
//            new File(AuthUserFlow.class.getResource("/").getPath(), "credentials");
//    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//    // i.e redirect_uri http://localhost:8081/Callback
//    private static final int LOCAL_RECEIVER_PORT = 8081;
//
//    public static void runAuthUserFlow() throws IOException {
//
//        /**
//         * Download your OAuth2 configuration from the Google Developers Console API Credentials page.
//         * https://console.cloud.google.com/apis/credentials
//         */
//        Path credentialsPath = Paths.get("path/to/your/client_secret.json");
//        List<String> scopes = ImmutableList.of("https://www.googleapis.com/auth/bigquery");
//        authUserFlow(credentialsPath, scopes);
//    }
//
//    public static void authUserFlow(Path credentialsPath, List<String> selectedScopes) throws IOException {
//        // Reading credentials file
//        try (InputStream inputStream = Files.newInputStream(credentialsPath)) {
//
//            // Load client_secret.json file
//            GoogleClientSecrets clientSecrets =
//                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));
//            String clientId = clientSecrets.getDetails().getClientId();
//            String clientSecret = clientSecrets.getDetails().getClientSecret();
//
//            // Generate the url that will be used for the consent dialog.
//            GoogleAuthorizationCodeFlow flow =
//                    new GoogleAuthorizationCodeFlow.Builder(
//                            GoogleNetHttpTransport.newTrustedTransport(),
//                            JSON_FACTORY,
//                            clientSecrets,
//                            selectedScopes)
//                            .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
//                            .setAccessType("offline")
//                            .setApprovalPrompt("auto")
//                            .build();
//
//            // Exchange an authorization code for  refresh token
//            LocalServerReceiver receiver =
//                    new LocalServerReceiver.Builder().setPort(LOCAL_RECEIVER_PORT).build();
//            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//
//            // OAuth2 Credentials representing a user's identity and consent
//            GoogleCredentials credentials =
//                    UserCredentials.newBuilder()
//                            .setClientId(clientId)
//                            .setClientSecret(clientSecret)
//                            .setRefreshToken(credential.getRefreshToken())
//                            .build();
//
//
//    } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        }
//    }}