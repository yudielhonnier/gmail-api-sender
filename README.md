# google-gmail-api

Demo Project for Gmail API  Using Spring Boot Rest API with OAuth2.

1) Rest API
2) Google Gmail API
3) OAuth2

<h2>What do I do?</h2>
This api is not a mail server, it only allows you to send by mail from time to time a list of messages written for example in the contact section of your website.




<h2>Interesting reads about this topic</h2>

####Creating and managing projects in Google Cloud
https://cloud.google.com/resource-manager/docs/creating-managing-projects?visit_id=637669197043753578-2757882342&rd=1

####Java Quickstart 
https://developers.google.com/gmail/api/guides/sending

####Sending Email Guide  
https://developers.google.com/gmail/api/quickstart/java

####Usar OAuth 2.0 para acceder a las API de Google
https://developers.google.com/identity/protocols/oauth2

####Gmail API Rest Resources
https://developers.google.com/gmail/api/reference/rest

<h2>Get it up and runnning</h2>

<h2>Maven Dependency</h2>


<!--		 https://mvnrepository.com/artifact/com.google.apis/google-api-services-gmail-->
		<dependency>
			<groupId>com.google.apis</groupId>
			<artifactId>google-api-services-gmail</artifactId>
			<version>v1-rev103-1.22.0</version>
			<exclusions>
				<exclusion>
					<groupId>com.google.guava</groupId>
					<artifactId>guava-jdk5</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

<!-- https://mvnrepository.com/artifact/com.google.auth/google-auth-library-oauth2-http -->
		<dependency>
			<groupId>com.google.auth</groupId>
			<artifactId>google-auth-library-oauth2-http</artifactId>
			<version>1.1.0</version>
		</dependency>
		
		
<!-- https://mvnrepository.com/artifact/javax.mail/mail -->
		<dependency>
			<groupId>javax.mail</groupId>
			<artifactId>mail</artifactId>
			<version>1.4.7</version>
		</dependency>

<!-- https://mvnrepository.com/artifact/com.google.oauth-client/google-oauth-client -->
		<dependency>
			<groupId>com.google.oauth-client</groupId>
			<artifactId>google-oauth-client-java6</artifactId>
			<version>1.31.0</version>
		</dependency>		
		



That’s it. Thank you for reading this post.

Enjoy!!!

