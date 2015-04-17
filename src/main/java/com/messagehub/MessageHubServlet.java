package com.messagehub;

import com.amazon.speech.speechlet.servlet.SpeechletServlet;

public class MessageHubServlet extends SpeechletServlet {

	public MessageHubServlet() {
		setSpeechlet(new MessageHubSpeechlet());
	}

}
