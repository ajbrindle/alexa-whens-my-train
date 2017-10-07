package com.sk7software.rail.service;

import com.sk7software.rail.exception.InvalidTimeFormatException;
import com.sk7software.rail.model.DepartureBoard;
import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import com.thalesgroup.rtti._2017_02_02.ldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.JAXBElement;
import javax.xml.soap.SOAPException;
import java.util.HashMap;
import java.util.Map;

public class GetDepartureBoardWithStopsService extends AbstractBaseService {
    private static final Logger log = LoggerFactory.getLogger(GetDepartureBoardWithStopsService.class);
    private static final String SOAP_ACTION = "http://thalesgroup.com/RTTI/2015-05-14/ldb/GetDepBoardWithDetails";
    private static final String xmlRequestFile = "DepartureBoardRequest.xml";
    private DepartureBoard departures;

    public DepartureBoard getDepartureBoard(AccessToken token, String crs, int numRows, boolean later)
        throws InvalidTimeFormatException {

        try {
            Map<String, String> tokens = new HashMap<>();
            tokens.put("-STATION-", "DVN");
            tokens.put("-ROWS-", "10");

            if (later) {
                tokens.put("-OFFSET-", "119");
            } else {
                tokens.put ("-OFFSET-", "0");
            }

            XMLServiceCall xmlService = new XMLServiceCall();
            xmlService.createHttpPost(xmlService.getXMLRequestFromFile(xmlRequestFile, tokens), SOAP_ACTION);
            xmlService.createHttpClient();
            xmlService.fetchXMLResponse();
            JAXBElement<StationBoardWithDetailsResponseType> sb = xmlService.unmarshallResponse(
                    "GetDepBoardWithDetailsResponse", StationBoardWithDetailsResponseType.class);

            departures = new DepartureBoard();

            if (sb != null) {
                departures.addMessages(sb.getValue().getGetStationBoardResult().getNrccMessages());
                departures.addTrains(sb.getValue().getGetStationBoardResult().getLocationName(),
                        sb.getValue().getGetStationBoardResult().getTrainServices().getService(), xmlService);
            } else {
                log.info("Unable to find train services");
            }
        } catch (Exception e) {
            log.info("Error calling departure board service " + e.getMessage());
            e.printStackTrace();
        }
        return departures;
    }
}
