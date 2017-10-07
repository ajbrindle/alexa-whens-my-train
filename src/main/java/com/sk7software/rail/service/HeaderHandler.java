package com.sk7software.rail.service;


import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import com.thalesgroup.rtti._2013_11_28.token.types.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringReader;
import java.util.Properties;
import java.util.Set;

public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {

    private AccessToken token;

    public HeaderHandler(AccessToken token) {
        this.token = token;
    }

    public boolean handleMessage(SOAPMessageContext context) {

        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        SOAPMessage message = context.getMessage();

        if (outbound) {
            // Embed access token in outbound message
            try {
                final ObjectFactory objectFactory = new ObjectFactory();

                Marshaller marshaller = JAXBContext.newInstance(AccessToken.class).createMarshaller();

                SOAPHeader header = message.getSOAPHeader();
                SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();

                if (header == null) {
                    header = envelope.addHeader();
                }

                marshaller.marshal(objectFactory.createAccessToken(token), header);
                message.saveChanges();
//                prettyPrint(message.getSOAPPart().getContent());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                prettyPrint(message.getSOAPPart().getContent());
            } catch (SOAPException se) {
                System.err.println("Invalid SOAP Response");
                se.printStackTrace();
            }
        }

        return outbound;
    }

    public Set getHeaders() {
        return null;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
    }

    public static void prettyPrint(Source source) {
        try {
            System.out.println("-----------------------------------");
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.transform(source, new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
