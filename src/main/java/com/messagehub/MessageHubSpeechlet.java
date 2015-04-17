package com.messagehub;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;

/**
 * This sample shows how to create a simple speechlet for handling intent
 * requests and managing session interactions.
 */
public class MessageHubSpeechlet implements Speechlet {
	private static final String NAME_KEY = "NAME";
	private static final String NAME_SLOT = "Name";

	private static final DynamoDBMapper mapper = new DynamoDBMapper(
			new AmazonDynamoDBClient());

	@Override
	public void onSessionStarted(final SessionStartedRequest request,
			final Session session) throws SpeechletException {
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request,
			final Session session) throws SpeechletException {
		return getWelcomeResponse();
	}

	@Override
	public SpeechletResponse onIntent(final IntentRequest request,
			final Session session) throws SpeechletException {

		// Get intent from the request object.
		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;

		// Note: If the session is started with an intent, no welcome message
		// will be rendered;
		// rather, the intent specific response will be returned.
		if ("GetMessages".equals(intentName)) {
			return retrieveMessages(intent, session);
		} else if ("SendMessage".equals(intentName)) {
			return sendMessage(intent, session);
		} else if ("DayLook".equals(intentName)) {
			return dayLook();
		} else {
			throw new SpeechletException("Invalid Intent");
		}
	}

	private SpeechletResponse retrieveMessages(Intent intent, Session session) {
		String name = session.getAttribute(NAME_KEY).toString();

		Map<Class<?>, List<KeyPair>> itemsToGet = new HashMap<Class<?>, List<KeyPair>>();
		itemsToGet.put(MessageHub.class,
				Arrays.asList(new KeyPair().withHashKey(name)));

		Map<String, List<Object>> messages = mapper.batchLoad(itemsToGet);

		return processMessage(name, messages);

	}

	private SpeechletResponse processMessage(String name,
			Map<String, List<Object>> messages) {

		if (messages.isEmpty())
			return buildSpeechletResponse(name, "No messages for you", false);

		List<Object> messagePerUser = messages.get(name);

		if (messagePerUser.isEmpty())
			return buildSpeechletResponse(name, "No messages for you", false);

		StringBuilder builder = new StringBuilder(String.format(
				"You have got %s messages.", messagePerUser.size()));

		int i = 1;
		for (Object message : messagePerUser) {
			MessageHub msg = (MessageHub) message;
			builder.append(convert(i) + " Message.");
			builder.append(msg.getMessage() + ".");
			msg.setStatus(Status.read);
			mapper.save(msg);
			i++;
		}
		return buildSpeechletResponse(name, builder.toString(), false);

	}

	private String convert(int i) {
		switch (i) {
		case 1:
			return "First";
		case 2:
			return "Second";
		case 3:
			return "Third";
		default:
			return "Four";
		}
	}

	private SpeechletResponse sendMessage(Intent intent, Session session) {
		MessageHub message = new MessageHub();
		message.setId(UUID.randomUUID().toString());
		String name = session.getAttribute(NAME_KEY).toString();
		message.setName(name);
		message.setStatus(Status.unread);
		Slot messageContent = intent.getSlot("message");
		message.setMessage(messageContent.getValue());
		mapper.save(message);
		return buildSpeechletResponse(intent.getName(), "message saved", false);
	}

	private SpeechletResponse dayLook() {
		return null;
	}

	@Override
	public void onSessionEnded(final SessionEndedRequest request,
			final Session session) throws SpeechletException {
	}

	/**
	 * Creates and returns a {@code SpeechletResponse} with a welcome message.
	 *
	 * @return SpeechletResponse spoken and visual welcome message
	 */
	private SpeechletResponse getWelcomeResponse() {
		// Create the welcome message.
		String speechOutput = "Welcome to the Message Hub, "
				+ "Please tell me your name by saying, my name is Sam";

		// Here we are setting shouldEndSession to false to not end the session
		// and
		// prompt the user for input
		return buildSpeechletResponse("Welcome", speechOutput, false);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the intent and stores the
	 * extracted name in the Session.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response the given intent
	 */
	private SpeechletResponse setNameInSessionAndSayHello(final Intent intent,
			final Session session) {
		// Get the slots from the intent.
		Map<String, Slot> slots = intent.getSlots();

		// Get the name slot from the list slots.
		Slot nameSlot = slots.get(NAME_SLOT);
		String speechOutput = "";

		// Check for name and create output to user.
		if (nameSlot != null) {
			// Store the user's name in the Session and create response.
			String name = nameSlot.getValue();
			session.setAttribute(NAME_KEY, name);
			speechOutput = String
					.format("Hello %s, now I can remember your name, "
							+ "you can ask me your name by saying, whats my name?",
							name);
		} else {
			// Render an error since we don't know what the users name is.
			speechOutput = "I'm not sure what your name is, please try again";
		}

		// Here we are setting shouldEndSession to false to not end the session
		// and
		// prompt the user for input
		return buildSpeechletResponse(intent.getName(), speechOutput, false);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the intent and get the user's
	 * name from the Session.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response for the intent
	 */
	private SpeechletResponse getNameFromSessionAndSayHello(
			final Intent intent, final Session session) {
		String speechOutput = "";
		boolean shouldEndSession = false;

		// Get the user's name from the session.
		String name = (String) session.getAttribute(NAME_KEY);

		// Check to make sure user's name is set in the session.
		if (StringUtils.isNotEmpty(name)) {
			speechOutput = String.format("Your name is %s, goodbye", name);
			shouldEndSession = true;
		} else {
			// Since the user's name is not set render an error message.
			speechOutput = "I'm not sure what your name is, you can say, my name is Sam";
		}

		return buildSpeechletResponse(intent.getName(), speechOutput,
				shouldEndSession);
	}

	/**
	 * Creates and returns the visual and spoken response with shouldEndSession
	 * flag
	 *
	 * @param title
	 *            title for the companion application home card
	 * @param output
	 *            output content for speech and companion application home card
	 * @param shouldEndSession
	 *            should the session be closed
	 * @return SpeechletResponse spoken and visual response for the given input
	 */
	private SpeechletResponse buildSpeechletResponse(String name,
			final String output, final boolean shouldEndSession) {

		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(output);

		// Create the speechlet response.
		SpeechletResponse response = new SpeechletResponse();
		response.setShouldEndSession(shouldEndSession);
		response.setOutputSpeech(speech);
		return response;
	}
}
