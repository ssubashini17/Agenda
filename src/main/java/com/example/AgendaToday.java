package com.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.util.*;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("agendatoday")
public class AgendaToday {
	private static final String APPLICATION_NAME = "CalendarFeed";

	/** Global instance of the HTTP transport. */
	private static HttpTransport httpTransport;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();

	private static final String CALENDAR_ID = "subashini.echo.hack@gmail.com";

	private static Calendar client;

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get() {
		String result = "";
		try {
			try {
				com.google.api.client.util.DateTime currentTime = new com.google.api.client.util.DateTime(
						new Date(), TimeZone.getDefault());
				httpTransport = GoogleNetHttpTransport.newTrustedTransport();

				CredentialsProvider provider = new CredentialsProvider();
				GoogleCredential credential = provider.getCredential();

				credential.refreshToken();

				client = new Calendar.Builder(httpTransport, JSON_FACTORY,
						credential).setApplicationName(APPLICATION_NAME)
						.build();

				Events events = client.events().list(CALENDAR_ID)
						.setTimeMin(currentTime).execute();

				for (Event event : events.getItems()) {
					result = result + "\n" + event.getSummary() + " from "
							+ event.getStart().getDateTime()
							+ " till " + event.getEnd().getDateTime();
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return result;
	}

}
