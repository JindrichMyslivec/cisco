package com.cisco.jimy.ws;

import java.io.IOException;
import java.net.HttpRetryException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.jimy.Constants;
import com.cisco.jimy.db.DBManager;
import com.cisco.jimy.json.Car;

/**
 * Fetch all car items and return JSON.
 * 
 * @author jindrich.myslivec
 */
public class WSGetCars extends WSJSON implements Constants {
    protected static Logger log = LogManager.getLogger(WSGetCars.class);

    public WSGetCars(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super(request, response);
    }

    @Override
    protected String getJSON() throws HttpRetryException, Exception {
        List<Car> cars = DBManager.selectCars();
        log.debug("GET Cars: " + cars.size());
        return jsonMapper.writeValueAsString(cars);
    }
}
