package com.cisco.jimy.ws;

import java.io.IOException;
import java.net.HttpRetryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * WS for JSON data
 * 
 * @author Jindrich Myslivec
 */
public abstract class WSJSON extends WS {
    protected static Logger log = LogManager.getLogger(WSJSON.class);

    public WSJSON(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super(request, response);
    }

    protected abstract String getJSON() throws HttpRetryException, Exception;

    @Override
    protected void processWebService() throws HttpRetryException, Exception {
        response.setContentType("application/json;charset=" + ENCODING);
        // support for XHR requests
        response.setHeader("Access-Control-Allow-Origin", "*");

        // process concrete web service
        outWriter = response.getWriter();
        outWriter.write(getJSON());
    }

}
