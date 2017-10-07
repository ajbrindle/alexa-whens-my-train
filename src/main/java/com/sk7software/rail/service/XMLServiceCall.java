package com.sk7software.rail.service;

import com.sk7software.rail.model.StationStop;
import com.thalesgroup.rtti._2017_02_02.ldb.StationBoardWithDetailsResponseType;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XMLServiceCall {
    private static final Logger log = LoggerFactory.getLogger(XMLServiceCall.class);
    private static final String LDBWS_URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/ldb10.asmx";
    public static final int MODE_START = 0;
    public static final int MODE_SERVICE = 1;
    public static final int MODE_CP = 2;

    private HttpPost httpPost;
    private HttpClient httpClient;

    private List<StationStop> stops;
    private String xmlResponse;

    public XMLServiceCall() {}

    public void createHttpPost(String xmlRequest, String soapAction) {
        StringEntity entity = new StringEntity(xmlRequest, ContentType.create(
                "text/xml", Consts.UTF_8));
        entity.setChunked(true);
        httpPost = new HttpPost(LDBWS_URL);
        httpPost.addHeader("SOAPAction", soapAction);
        httpPost.setEntity(entity);
    }

    public void createHttpClient() {
        httpClient = HttpClients.createDefault();
    }

    public void fetchXMLResponse() throws IOException {
        HttpResponse response = httpClient.execute(httpPost);
        InputStream xmlInStream = response.getEntity().getContent();
        xmlResponse = IOUtils.toString(xmlInStream, Consts.UTF_8);
    }

    public <T> JAXBElement<T> unmarshallResponse(String elementName, Class<T> clazz) {
        try {
            StringReader reader = new StringReader(xmlResponse);
            XMLInputFactory xif = XMLInputFactory.newFactory();
            StreamSource xml = new StreamSource(reader);
            XMLStreamReader xsr = xif.createXMLStreamReader(xml);
            while (xsr.hasNext()) {
                if (xsr.isStartElement() && xsr.getLocalName().equals(elementName)) {
                    break;
                }
                xsr.next();
            }
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<T>  t = unmarshaller.unmarshal(xsr, clazz);
            return t;
        } catch (XMLStreamException e) {
            return null;
        } catch (JAXBException e) {
            return null;
        }
    }

    public List<StationStop> extractStops(String serviceID) {
        stops = new ArrayList<>();
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xmlResponse.getBytes("utf-8"))));

            if (doc.hasChildNodes()) {
                printNote(doc.getChildNodes(), serviceID, (serviceID == null ? MODE_SERVICE : MODE_START));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return stops;
    }

    private void printNote(NodeList nodeList, String serviceID, int mode) throws Exception {

        StationStop stop = new StationStop();

        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                String[] names = tempNode.getNodeName().split(":");
                if (contains(names, "serviceID")) {
                    if (serviceID.equals(tempNode.getTextContent())) {
                        mode = MODE_SERVICE;
                    } else {
                        mode = MODE_START;
                    }
                } else if (mode == MODE_SERVICE) {
                    if (contains(names, "callingPoint")) {
                        mode = MODE_CP;
                    }
                } else if (mode == MODE_CP) {
                    if (contains(names, "locationName")) {
                        stop.setName(tempNode.getTextContent());
                    } else if (contains(names, "st")) {
                        stop.setScheduled(tempNode.getTextContent());
                    } else if (contains(names,"et")) {
                        stop.setEstimated(tempNode.getTextContent());
                        stops.add(stop);
                    }
                }

                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes
                    printNote(tempNode.getChildNodes(), serviceID, mode);
                }

                if (contains(names, "callingPointList")) {
                    mode = MODE_START;
                }
            }
        }
    }

    private boolean contains(String[] names, String value) {
        for (String n : names) {
            if (value.equals(n)) {
                return true;
            }
        }

        return false;
    }

    public String getXMLRequestFromFile(String filename, Map<String, String> tokens) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File xmlFile = new File(classLoader.getResource(filename).getFile());
            String xml = new String(Files.readAllBytes(Paths.get(classLoader.getResource(filename).getFile())));
            for (String key : tokens.keySet()) {
                String value = tokens.get(key);
                xml = xml.replaceAll(key, value);
            }
            log.info(xml);
            return xml;
        } catch (IOException e) {
            return null;
        }
    }
}
