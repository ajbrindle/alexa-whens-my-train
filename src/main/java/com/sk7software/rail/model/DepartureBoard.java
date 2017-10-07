package com.sk7software.rail.model;

import com.sk7software.rail.exception.InvalidTimeFormatException;
import com.sk7software.rail.exception.SpeechException;
import com.sk7software.rail.service.XMLServiceCall;
import com.thalesgroup.rtti._2012_01_13.ldb.types.ArrayOfNRCCMessages;
import com.thalesgroup.rtti._2012_01_13.ldb.types.NRCCMessage;
import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import com.thalesgroup.rtti._2017_02_02.ldb.types.ArrayOfServiceItemsWithCallingPoints;
import com.thalesgroup.rtti._2017_02_02.ldb.types.ServiceItem;
import com.thalesgroup.rtti._2017_02_02.ldb.types.ServiceItemWithCallingPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DepartureBoard {
    private static final Logger log = LoggerFactory.getLogger(DepartureBoard.class);

    private List<String> messages;
    private List<TrainService> trains;

    public DepartureBoard() {
        messages = new ArrayList<>();
        trains = new ArrayList<>();
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<TrainService> getTrains() {
        return trains;
    }

    public void setTrains(List<TrainService> trains) {
        this.trains = trains;
    }

    public String getSpokenText() throws SpeechException {
        StringBuilder boardInformation = new StringBuilder();

        // Get service messages
        if (messages.size() > 0) {
            for (String m : messages) {
                boardInformation.append(m);
                boardInformation.append(". ");
            }
        }

        if (trains.size() > 0) {
            for (TrainService s : trains) {
                boardInformation.append(s.getDepartureString());
            }
        } else {
            boardInformation.append("There are no trains after this time.");
        }

        return boardInformation.toString();
    }

    public void addMessages(ArrayOfNRCCMessages messages) {
        if (messages != null) {
            for (NRCCMessage m : messages.getMessage()) {
                this.messages.add(m.getValue().replaceAll("\\<[^>]*>", ""));
            }
        }
    }

    public void addTrains(String locationName, List<? extends ServiceItem> services,
                          XMLServiceCall xmlService) throws InvalidTimeFormatException {
        for (ServiceItem s : services) {
            TrainService t = new TrainService();
            t.setOperator(s.getOperator());
            t.setServiceId(s.getServiceID());
            t.setPlatform(s.getPlatform());
            t.setStation(new StationStop(locationName, s.getStd(), s.getEtd()));
            t.setOrigin(s.getOrigin().getLocation().get(0).getLocationName());
            t.setDestination(s.getDestination().getLocation().get(0).getLocationName());
            t.setDelayReason(s.getDelayReason());
            if (s.isIsCancelled() != null) {
                t.setCancelled(s.isIsCancelled());
                t.setCancelReason(s.getCancelReason());
            } else {
                t.setCancelled(false);
            }

            log.info("Train: " + t.getStation().getScheduled() + " " + t.getOrigin() + " to " + t.getDestination());

            if (xmlService != null) {
                t.setStops(xmlService.extractStops(s.getServiceID()));
            }

            trains.add(t);
        }
    }
}
