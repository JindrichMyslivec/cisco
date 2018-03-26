package com.cisco.jimy.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpRetryException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.jimy.Constants;
import com.cisco.jimy.json.Error;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WebService resource
 */
public abstract class WS implements Constants {
    protected static Logger log = LogManager.getLogger(WS.class);

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ObjectMapper jsonMapper = new ObjectMapper();

    /** Text output stream for JSON resource */
    protected PrintWriter outWriter = null;

    protected Map<String, String> parameters = new HashMap<String, String>();

    /**
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    public WS(HttpServletRequest request, HttpServletResponse response, boolean getUser) throws IOException {
        this.request = request;
        this.response = response;
        // Disable caching
        if (this.response != null) {
            this.response.addHeader("ETag", Long.toString(System.currentTimeMillis()));
            this.response.addHeader("Pragma", "no-cache");
            this.response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            this.response.addHeader("Expires", "-1");
        }
        if (this.request != null) {
            try {
                this.request.setCharacterEncoding(ENCODING);
            } catch (UnsupportedEncodingException exc) {
                log.warn("Cannot set '" + ENCODING + "' encoding for HTTP request.", exc);
            }
        }
        jsonMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    /**
     * Implementation of concrete web service.
     * 
     * @return
     * @throws IOException
     */
    protected abstract void processWebService() throws HttpRetryException, Exception;

    /**
     * Process web service resource and handle exceptions.
     * 
     * @throws ServletException
     */
    public void process() throws ServletException, IOException {
        long time = System.currentTimeMillis();
        try {
            processWebService();
            response.setStatus(200);
        } catch (HttpRetryException exc) {
            log.error("Error: " + exc.responseCode() + ": " + exc.getMessage());
            response.setStatus(exc.responseCode());
            Error error = new Error(exc.getMessage());
            if (outWriter != null) {
                outWriter.write(jsonMapper.writeValueAsString(error));
            }
        } catch (Throwable exc) {
            log.error("Error: " + exc.getMessage(), exc);
            response.setStatus(500);
            StringWriter result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            exc.printStackTrace(printWriter);
            Error error = new Error(exc.getMessage(), result.getBuffer().toString());
            if (outWriter != null) {
                outWriter.write(jsonMapper.writeValueAsString(error));
            }
        } finally {
            if (outWriter != null) {
                outWriter.close();
            }
        }
        log.debug("Process Time: " + ((System.currentTimeMillis() - time) / 1000.0d) + "s");
    }

}
