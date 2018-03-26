package com.cisco.jimy;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.jimy.db.DBManager;
import com.cisco.jimy.ws.WS;
import com.cisco.jimy.ws.WSGetCars;
import com.cisco.jimy.ws.WSGetDocumentation;
import com.cisco.jimy.ws.WSGetHealthy;
import com.cisco.jimy.ws.WSPostCars;

/**
 * HTTP servlet to route request to Java classes
 */
public class WSServlet extends HttpServlet implements Constants {

    static Logger log = LogManager.getLogger(WSServlet.class);

    private static String version = null;

    /**
     * Initialize event.
     */
    @Override
    public void init() throws ServletException {
        log.info("--------------------------------------------------");
        log.info("CISCO server init");

        // java version
        log.info("OS name: " + System.getProperty("os.name") + ", version: " + System.getProperty("os.version") + ", arch: "
                + System.getProperty("os.arch"));
        log.info("Java version: " + System.getProperty("java.version") + ", vendor: " + System.getProperty("java.vendor"));
        Runtime runtime = Runtime.getRuntime();
        log.debug("Memory max size: " + runtime.maxMemory() / MB + "MB, total: " + runtime.totalMemory() / MB + "MB, used: "
                + (runtime.totalMemory() - runtime.freeMemory()) / MB + "MB, free: " + runtime.freeMemory() / MB + "MB");

        // load version
        try {
            Properties prop = new Properties();
            prop.load(getClass().getResourceAsStream("/version.properties"));
            version = prop.getProperty("version");
            log.info("CISCO version: " + version);
        } catch (Exception exc) {
            log.warn("Cannot get KORE version.", exc);
        }

        // initialize DB
        try {
            DBManager.init();
        } catch (SQLException exc) {
            log.error("Cannot connect to DB.", exc);
            throw new ServletException(exc);
        }
        super.init();
        
        log.info("CISCO server ready");
    };

    /**
     * Destroy event. Close all connections.
     */
    @Override
    public void destroy() {
        log.info("CISCO server destroy");
        DBManager.close();
        log.info("--------------------------------------------------");
        super.destroy();
    }

    /**
     * Get KORE version
     * 
     * @return
     */
    public static String getVersion() {
        return version;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt("GET", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt("POST", request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt("DELETE", request, response);
    }

    /**
     * Main servlet method which process demanded web service.
     * 
     * @param method
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void doIt(String method, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        log.debug(method + " Requested url: " + uri);
        uri = URLDecoder.decode(uri.substring(ctx.length()), "UTF-8");
        WS ws = null;
        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (uri.equals("/")) {
                    ws = new WSGetDocumentation(request, response);
                } else if (uri.equals(COMMAND_GET_HEALTHY)) {
                    ws = new WSGetHealthy(request, response);
                } else if (uri.equals(COMMAND_GET_CARS)) {
                    ws = new WSGetCars(request, response);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                if (uri.startsWith(COMMAND_POST_CARS)) {
                    ws = new WSPostCars(request, response);
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
            }
            if (ws != null) {
                ws.process();
            } else {
                log.info("Unknown command '" + uri + "'");
                throw new IOException("Unknown command.");
            }
        } catch (Throwable exc) {
            log.error("Cannot process web service.", exc);
            throw exc;
        }
    }
}
