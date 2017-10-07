package com.sk7software.rail.service;

import com.sk7software.rail.exception.InvalidTimeFormatException;
import com.sk7software.rail.model.DepartureBoard;
import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import com.thalesgroup.rtti._2017_02_02.ldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.Map;

public class GetDeparturesService extends AbstractBaseService {
    private static final Logger log = LoggerFactory.getLogger(GetDeparturesService.class);
    private static final String xmlRequestFile = "DeparturesRequest.xml";
    private DepartureBoard departures;

    public DepartureBoard getDepartureBoard(AccessToken token, String crs, String dest, int numRows, boolean later)
            throws InvalidTimeFormatException {

        try {
            LDBServiceSoap port = createService(token);
            GetBoardRequestParams _getDepartureBoard_parameters = new GetBoardRequestParams();
            _getDepartureBoard_parameters.setCrs(crs);
            _getDepartureBoard_parameters.setFilterCrs(dest);
            _getDepartureBoard_parameters.setNumRows(numRows);

            if (later) {
                _getDepartureBoard_parameters.setTimeOffset(119);
            } else {
                _getDepartureBoard_parameters.setTimeOffset(0);
            }

            ObjectFactory of = new ObjectFactory();
            JAXBElement<StationBoardResponseType> resp = of.createGetDepartureBoardResponse(
                    port.getDepartureBoard(_getDepartureBoard_parameters));
            StationBoardResponseType sb = resp.getValue();

            departures = new DepartureBoard();

            if (sb != null) {
                departures.addMessages(sb.getGetStationBoardResult().getNrccMessages());
                departures.addTrains(sb.getGetStationBoardResult().getLocationName(),
                        sb.getGetStationBoardResult().getTrainServices().getService(), null);
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
