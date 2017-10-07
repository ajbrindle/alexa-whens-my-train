package com.sk7software.rail.service;

import com.sk7software.rail.exception.InvalidTimeFormatException;
import com.sk7software.rail.model.DepartureBoard;
import com.sk7software.rail.model.StationStop;
import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import com.thalesgroup.rtti._2017_02_02.ldb.*;
import com.thalesgroup.rtti._2017_02_02.ldb.types.CallingPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetServiceDetailsService extends AbstractBaseService {
    private static final Logger log = LoggerFactory.getLogger(GetServiceDetailsService.class);
    private static final String SOAP_ACTION = "http://thalesgroup.com/RTTI/2012-01-13/ldb/GetServiceDetails";
    private static final String xmlRequestFile = "ServiceRequest.xml";
    private List<StationStop> stops;

    public List<StationStop> getServiceStops(AccessToken token, String serviceId)
            throws InvalidTimeFormatException {
        try {
            Map<String, String> tokens = new HashMap<>();
            tokens.put("-SERVICE_ID-", serviceId);
            XMLServiceCall xmlService = new XMLServiceCall();
            xmlService.createHttpPost(xmlService.getXMLRequestFromFile(xmlRequestFile, tokens), SOAP_ACTION);
            xmlService.createHttpClient();
            xmlService.fetchXMLResponse();
            return(xmlService.extractStops(null));
        } catch (Exception e) {
            log.info("Error calling departure board service " + e.getMessage());
            e.printStackTrace();
        }

        return stops;
    }

    public static String toXml(JAXBElement element) {
        try {
            JAXBContext jc = JAXBContext.newInstance(element.getValue().getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(element, baos);
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
