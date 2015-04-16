package com.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

/**
 * Root resource (exposed at "myresource" path)
 */
public class SendEmail {

	private static final String APPLICATION_NAME = "SendEmail";

	/** Global instance of the HTTP transport. */
	private static HttpTransport httpTransport;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();

	private static Gmail client;

	private static String from = "subashini.echo.hack@gmail.com";

	private static Map<String, String> contacts = new HashMap<>();

	static {
		contacts.put("mani", "manikandangupta.rs@gmail.com");
	}

	@POST
	@Path("/send")
	@Produces(MediaType.TEXT_PLAIN)
	public String sendMail(@QueryParam("to")String to, @QueryParam("msg")String message) {
		String result = "";

		System.out.println("Request received to send email.");

		if (!contacts.containsKey(to)) return "Contact not available";

		try {
			try {

				httpTransport = GoogleNetHttpTransport.newTrustedTransport();

				CredentialsProvider provider = new CredentialsProvider();
				GoogleCredential credential = provider.getCredential();

				credential.refreshToken();

				client = new Gmail.Builder(httpTransport, JSON_FACTORY,
						credential).setApplicationName(APPLICATION_NAME)
						.build();
				System.out.println("Sending Email. Gmail client creation sucess");

				sendMessage(from,
						createEmail(contacts.get(to), from, message, message));

				return "Email sent";

			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return result;
	}

	/**
	   * Create a MimeMessage using the parameters provided.
	   *
	   * @param to Email address of the receiver.
	   * @param from Email address of the sender, the mailbox account.
	   * @param subject Subject of the email.
	   * @param bodyText Body text of the email.
	   * @return MimeMessage to be used to send email.
	   * @throws MessagingException
	   */
	  private MimeMessage createEmail(String to, String from, String subject,
	      String bodyText) throws MessagingException {
	    Properties props = new Properties();
	    Session session = Session.getDefaultInstance(props, null);

	    MimeMessage email = new MimeMessage(session);
	    InternetAddress tAddress = new InternetAddress(to);
	    InternetAddress fAddress = new InternetAddress(from);

	    email.setFrom(new InternetAddress(from));
	    email.addRecipient(javax.mail.Message.RecipientType.TO,
	                       new InternetAddress(to));
	    email.setSubject(subject);
	    email.setText(bodyText);
	    return email;
	  }

	/**
	 * Send an email from the user's mailbox to its recipient.
	 *
	 * @param service
	 *            Authorized Gmail API instance.
	 * @param userId
	 *            User's email address. The special value "me" can be used to
	 *            indicate the authenticated user.
	 * @param email
	 *            Email to be sent.
	 * @throws MessagingException
	 * @throws IOException
	 */
	private void sendMessage(String userId, MimeMessage email)
			throws MessagingException, IOException {
		Message message = createMessageWithEmail(email);
		message = client.users().messages().send(userId, message).execute();
	}

	/**
	 * Create a Message from an email
	 *
	 * @param email
	 *            Email to be set to raw of message
	 * @return Message containing base64url encoded email.
	 * @throws IOException
	 * @throws MessagingException
	 */
	private Message createMessageWithEmail(MimeMessage email)
			throws MessagingException, IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		email.writeTo(bytes);
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes
				.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

}
