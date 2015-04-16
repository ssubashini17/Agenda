package com.example;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.gmail.GmailScopes;

public class CredentialsProvider {
	private GoogleCredential credential;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();

	/** E-mail address of the service account. */
	private static final String SERVICE_ACCOUNT_EMAIL = "918593848967-1f5evctnfp865gavbhq9g0nvuivuq6sn@developer.gserviceaccount.com";

	private static final String FILE_NAME = "./resources/Echo Hack-2bc576fb05e1.p12";

	public CredentialsProvider() {
		setCredential();
	}

	public void setCredential() {
		HttpTransport httpTransport;
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			this.credential = new GoogleCredential.Builder()
					.setTransport(httpTransport)
					.setJsonFactory(JSON_FACTORY)
					.setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
					.setServiceAccountScopes(
							Collections.unmodifiableList(Arrays.asList(
									GmailScopes.GMAIL_COMPOSE,
									CalendarScopes.CALENDAR)))
					.setServiceAccountPrivateKeyFromP12File(new File(FILE_NAME))
					.build();

		} catch (GeneralSecurityException e) {
			throw new RuntimeException(
					"Could not create google oauth credential\n Caused By:"
							+ e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(
					"Could not create google oauth credential\n Caused By:"
							+ e.getMessage());
		}
	}

	public GoogleCredential getCredential() {
		return this.credential;
	}
}
