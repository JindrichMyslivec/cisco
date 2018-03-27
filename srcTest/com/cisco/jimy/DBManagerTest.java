package com.cisco.jimy;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.jimy.db.DBManager;
import com.cisco.jimy.json.Car;

/**
 * Test cases for DBManager class.
 * 
 * @author jindrich.myslivec
 */
public class DBManagerTest implements Constants {
    static Logger log = LogManager.getLogger(DBManagerTest.class);

    @BeforeClass
    public static void init() {
        log.info("DBManagerTest init()");
        try {
            DBManager.init();
        } catch (SQLException exc) {
            log.error(exc.getMessage());
            Assert.fail(exc.getMessage());
        }
        log.info("DBManagerTest init() - END");
        log.info("--------------------------------------------------------------------------");
    }

    @AfterClass
    public static void close() {
        log.info("--------------------------------------------------------------------------");
        log.info("DBManagerTest close()");
        DBManager.close();
        log.info("DBManagerTest close() - END");
    }

    @Test
    public void testCars() {
        try {
            // insert
            Car car = new Car();
            car.setVIN("testVINCODE123");
            car.setBrand("testBrand");
            car.setModel("testModel");
            car.setPrice(123);

            DBManager.insertCar(car);

            List<Car> cars = DBManager.selectCars();

            Assert.assertNotNull(cars);
            Assert.assertEquals(1, cars.size());
            Assert.assertEquals(car.getVIN(), cars.get(0).getVIN());
            Assert.assertEquals(car.getBrand(), cars.get(0).getBrand());
            Assert.assertEquals(car.getModel(), cars.get(0).getModel());
            Assert.assertEquals(car.getPrice(), cars.get(0).getPrice());
        } catch (SQLException exc) {
            Assert.fail(exc.getMessage());
        } finally {
            try {
                DBManager.deleteCars();
            } catch (SQLException exc) {
                Assert.fail(exc.getMessage());
            }
        }
    }

    @Test
    public void testPrimaryKey() {
        try {
            Car car = new Car();
            car.setVIN("testVINCODE123");
            car.setBrand("testBrand");
            car.setModel("testModel");
            car.setPrice(123);
            try {
                DBManager.insertCar(car);
            } catch (SQLException exc) {
                Assert.fail(exc.getMessage());
            }
            try {
                DBManager.insertCar(car);
                Assert.fail("Inserted same car - same VIN code.");
            } catch (SQLException exc) {}
        } finally {
            try {
                DBManager.deleteCars();
            } catch (SQLException exc) {
                Assert.fail(exc.getMessage());
            }
        }
    }

    @Test
    public void testSQLInjection() {
        try {
            // insert
            DBManager.insertCar("';delete from cars;'", "", "", 123);
            Assert.fail("Inserted dangerous chars in SQL.");
        } catch (SQLException exc) {} finally {
            try {
                DBManager.deleteCars();
            } catch (SQLException exc) {
                Assert.fail(exc.getMessage());
            }
        }
    }
}
