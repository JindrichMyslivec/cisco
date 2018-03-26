package com.cisco.jimy.ws;

import java.io.IOException;
import java.net.HttpRetryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cisco.jimy.db.DBManager;

/**
 * Get some HTML page.
 * 
 * @author jindrich.myslivec
 */
public class WSGetHealthy extends WS {

    public WSGetHealthy(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super(request, response, false);
    }

    @Override
    protected void processWebService() throws HttpRetryException, Exception {
        response.setContentType("text/html;charset=UTF-8");
        // support for XHR requests
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        if (DBManager.getInstance().isAlive()) {
            response.setStatus(200);
        } else {
            response.setStatus(400);
        }
        response.getWriter().println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        response.getWriter().println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        response.getWriter().println("<head>");
        response.getWriter().println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
        response.getWriter().println("<title>CISCO server is healthy</title>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        response.getWriter().println("<h1>CISCO server is healthy</h1>");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }
}
