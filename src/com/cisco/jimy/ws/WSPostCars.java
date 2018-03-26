package com.cisco.jimy.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpRetryException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.jimy.Constants;
import com.cisco.jimy.db.DBManager;
import com.cisco.jimy.json.Car;
import com.cisco.jimy.json.Status;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Parse JSON and save all cars into DB.
 * 
 * @author jindrich.myslivec
 */
public class WSPostCars extends WSJSON implements Constants {
    protected static Logger log = LogManager.getLogger(WSPostCars.class);

    public WSPostCars(HttpServletRequest request, HttpServletResponse response) throws IOException {
        super(request, response);
    }

    @Override
    protected String getJSON() throws HttpRetryException, Exception {
        StringBuffer json = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                json.append(line);
        } catch (IOException exc) {
            log.error("Cannot read JSON from request.", exc);
            throw new HttpRetryException("Cannot read JSON from request.", 400);
        }

        if (json == null || json.toString().trim().isEmpty()) throw new HttpRetryException("Missing required JSON.", 400);

        log.info("POST /cars JSON: " + json);

        List<Car> cars = jsonMapper.readValue(json.toString(), new TypeReference<List<Car>>() {
        });
        synchronized (DBManager.getInstance()) {
            try {
                for (Car car : cars) {
                    DBManager.insertCar(car.getVIN(), car.getBrand(), car.getModel(), car.getPrice());
                }
                log.debug("DB commit");
                DBManager.getConnection().commit();
            } catch (SQLIntegrityConstraintViolationException exc) {
                log.error("Cannot insert car - VIN already exists", exc);
                log.debug("DB rollback");
                DBManager.getConnection().rollback();
                throw new HttpRetryException("Car VIN already exists", 409);
            } catch (SQLException exc) {
                log.error("Cannot insert car", exc);
                log.debug("DB rollback");
                DBManager.getConnection().rollback();
                throw exc;
            }
        }

        log.debug("POST /cars - inserted " + cars.size() + " cars");
        return jsonMapper.writeValueAsString(new Status(Status.STATUS_SAVED, "Successfully inserted " + cars.size() + " cars"));
    }
}
