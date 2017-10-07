/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.sk7software.rail;

import com.sk7software.rail.model.DepartureBoard;
import com.sk7software.rail.model.StationStop;
import com.sk7software.rail.model.TrainService;
import com.sk7software.rail.service.GetDepartureBoardWithStopsService;
import com.sk7software.rail.service.GetDeparturesService;
import com.sk7software.rail.service.GetServiceDetailsService;
import com.sk7software.rail.util.TimeUtil;
import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

import java.util.List;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class WhensMyTrainSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(WhensMyTrainSpeechlet.class);
    private static final String TOKEN_VALUE = "7d2b005e-2019-4535-8431-89122d5bb8be";

    private static final int MODE_FROM_HOME = 1;
    private static final int MODE_TO_HOME = 2;

    private AccessToken token;
    private DepartureBoard departures;
    private boolean gotLater = false;
    private int mode;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        token = new AccessToken();
        token.setTokenValue(TOKEN_VALUE);
        mode = MODE_FROM_HOME;
        gotLater = false;
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("WhensMyTrainIntent".equals(intentName)) {
            gotLater = false;
            mode = MODE_FROM_HOME;
            return getWelcomeResponse();
        } else if ("StopsIntent".equals(intentName)) {
            return getStopsResponse(intent);
        } else if ("TrainsHomeIntent".equals(intentName)) {
            gotLater = false;
            mode = MODE_TO_HOME;
            return getTrainsHomeResponse();
        } else if ("LaterTrainsIntent".equals(intentName)) {
            return getLaterTrainsIntent();
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        StringBuilder speechText = new StringBuilder();

        try {
            GetDepartureBoardWithStopsService service = new GetDepartureBoardWithStopsService();
            departures = service.getDepartureBoard(token, "MAN", 20, gotLater);
            log.info("Fetched departures");
            speechText.append(departures.getSpokenText());
        } catch (Exception e) {
            speechText.append("There was an error looking up the departure board");
        }
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("When's My Train");
        card.setContent(speechText.toString());

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("Anything else?");
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private SpeechletResponse getStopsResponse(Intent intent) {
        String time = intent.getSlots().get("time").getValue();
        StringBuilder speechText = new StringBuilder();

        try {
            for (TrainService t : departures.getTrains()) {
                if (t.getStation().getScheduled().equals(time)) {
                    List<StationStop> stops = t.getStops();
                    if (stops == null || stops.size() == 0) {
                        // Stops have not been fetched so do it now
                        GetServiceDetailsService service = new GetServiceDetailsService();
                        stops = service.getServiceStops(token, t.getServiceId());
                    }
                    speechText.append("The ");
                    speechText.append(t.getStation().getScheduled());
                    speechText.append(" service from ");
                    speechText.append(t.getStation().getName());
                    speechText.append(" stops at: ");

                    int numStops = stops.size();
                    int stopsSoFar = 0;

                    for (StationStop s : stops) {
                        if (stopsSoFar++ == numStops - 1 && numStops > 1) {
                            speechText.append(" and ");
                        }
                        speechText.append(s.getName());
                        if (numStops == 1) {
                            speechText.append(" only");
                        }

                        if (TimeUtil.isDelayed(s.getScheduled(), s.getEstimated())) {
                            int delay = TimeUtil.calcDelayMinutes(s.getScheduled(), s.getEstimated());
                            if (delay > 0) {
                                speechText.append(" ");
                                speechText.append(delay);
                                speechText.append(" minutes late");
                            }
                            speechText.append(" at ");
                            speechText.append(s.getEstimated());
                            speechText.append(". ");
                        } else {
                            speechText.append(" at ");
                            speechText.append(s.getScheduled());
                            speechText.append(". ");
                        }
                    }
                }
            }
        } catch (Exception e) {
            speechText.append("Unable to look up the stops for that service.");
        }
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("Anything else?");
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse getTrainsHomeResponse() {
        StringBuilder speechText = new StringBuilder();

        try {
            GetDeparturesService service = new GetDeparturesService();
            departures = service.getDepartureBoard(token, "MAN", "DVN", 50, gotLater);
            log.info("Fetched departures");
            speechText.append(departures.getSpokenText());
        } catch (Exception e) {
            speechText.append("There was an error looking up the departure board");
        }
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("When's My Train");
        card.setContent(speechText.toString());

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText("Anything else?");
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getLaterTrainsIntent() {
        String speechText;

        if (!gotLater) {
            gotLater = true;
            if (mode == MODE_FROM_HOME) {
                return getWelcomeResponse();
            } else {
                return getTrainsHomeResponse();
            }
        } else {
            speechText = "I can't get any trains later than this.";
        }

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech);
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say hello to me!";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }
}
