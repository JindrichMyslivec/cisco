package com.cisco.jimy.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpRetryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get server documentation in HTML.
 * 
 * @author jindrich.myslivec
 */
public class WSGetDocumentation extends WS {

    public WSGetDocumentation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super(request, response);
    }

    @Override
    protected void processWebService() throws HttpRetryException, Exception {
        response.setContentType("text/html;charset=UTF-8");
        // support for XHR requests
        response.setHeader("Access-Control-Allow-Origin", "*");

        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream("/index.html");
            if (inputStream == null) throw new IOException("Cannot read documentation file.");
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.getWriter().println(line);
            }
        } finally {
            if (inputStream != null) inputStream.close();
            if (reader != null) reader.close();
        }
    }
}
