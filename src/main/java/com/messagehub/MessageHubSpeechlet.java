package com.messagehub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.amazon.speech.ui.SimpleCard;

/**
 * This sample shows how to create a simple speechlet for handling intent requests and managing
 * session interactions.
 */
public class MessageHubSpeechlet implements Speechlet {
	private static final String NAME_KEY = "NAME";
    private static final String NAME_SLOT = "Name";

    private List<String> intents = new ArrayList<String>();

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {


        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        if ("GetMessages".equals(intentName)) {
            return retrieveMessages();
        } else if ("SendMessage".equals(intentName)) {
            return sendMessage();
        } else if ("DayLook".equals(intentName)) {
        	return dayLook();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse retrieveMessages() {
    		return null;
    }

    private SpeechletResponse sendMessage() {
		return null;
    }
    private SpeechletResponse dayLook() {
		return null;
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechOutput =
                "Welcome to the Message Hub, "
                        + "Please tell me your name by saying, my name is Sam";

        // Here we are setting shouldEndSession to false to not end the session and
        // prompt the user for input
        return buildSpeechletResponse("Welcome", speechOutput, false);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted name in the
     * Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse setNameInSessionAndSayHello(final Intent intent, final Session session) {
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
            speechOutput =
                    String.format("Hello %s, now I can remember your name, "
                            + "you can ask me your name by saying, whats my name?", name);
        } else {
            // Render an error since we don't know what the users name is.
            speechOutput = "I'm not sure what your name is, please try again";
        }

        // Here we are setting shouldEndSession to false to not end the session and
        // prompt the user for input
        return buildSpeechletResponse(intent.getName(), speechOutput, false);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's name from the Session.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
    private SpeechletResponse getNameFromSessionAndSayHello(final Intent intent,
            final Session session) {
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

        return buildSpeechletResponse(intent.getName(), speechOutput, shouldEndSession);
    }

    /**
     * Creates and returns the visual and spoken response with shouldEndSession flag
     *
     * @param title
     *            title for the companion application home card
     * @param output
     *            output content for speech and companion application home card
     * @param shouldEndSession
     *            should the session be closed
     * @return SpeechletResponse spoken and visual response for the given input
     */
    private SpeechletResponse buildSpeechletResponse(final String title, final String output,
            final boolean shouldEndSession) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle(String.format("SessionSpeechlet - %s", title));
        card.setSubtitle(String.format("SessionSpeechlet - Sub Title"));
        card.setContent(String.format("SessionSpeechlet - %s", output));

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(output);

        // Create the speechlet response.
        SpeechletResponse response = new SpeechletResponse();
        response.setShouldEndSession(shouldEndSession);
        response.setOutputSpeech(speech);
        response.setCard(card);
        return response;
    }
}
