package com.sk7software.rail.model;

import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestGetBoard {

    private AccessToken token;

//    @Rule
//    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089).httpsPort(8443));

    @Before
    public void setup() {
        token = new AccessToken();
        token.setTokenValue("7d2b005e-2019-4535-8431-89122d5bb8be");
    }

    @Test
    public void testGetBoard() throws Exception {
//        stubFor(any(anyUrl())
//                .withRequestBody(matchingXPath("//GetDepBoardWithDetailsRequest")
//                        .withXPathNamespace("ldb", "http://thalesgroup.com/RTTI/2017-02-02/ldb/"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "text/xml")
//                        .withBodyFile("GetDepartureBoardWithDetails.response")));
//        GetDepartureBoardService service = new GetDepartureBoardService();
//        DepartureBoard board = service.getDepartureBoard(token, "MAN", 10);
//        assertTrue("Manchester Piccadilly".equals(board.getTrains().get(0).getStation().getName()));
    }
}
