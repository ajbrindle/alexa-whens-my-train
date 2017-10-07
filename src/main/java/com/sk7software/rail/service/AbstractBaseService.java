package com.sk7software.rail.service;

import com.thalesgroup.rtti._2013_11_28.token.types.AccessToken;
import com.thalesgroup.rtti._2017_02_02.ldb.LDBServiceSoap;
import com.thalesgroup.rtti._2017_02_02.ldb.Ldb;

public abstract class AbstractBaseService {

    public LDBServiceSoap createService(AccessToken token) {
        Ldb ss = new Ldb();
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(token);
        ss.setHandlerResolver(handlerResolver);
        LDBServiceSoap port = ss.getLDBServiceSoap();
        return port;
    }
}
