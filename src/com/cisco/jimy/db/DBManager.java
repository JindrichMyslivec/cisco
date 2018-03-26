package com.cisco.jimy.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cisco.jimy.Constants;
import com.cisco.jimy.json.Car;

/**
 * Singleton class to access HSQL database.
 * 
 * @author jindrich.myslivec
 *
 */
public class DBManager implements Constants {
    private static DBManager instance = null;
    private static Logger log = LogManager.getLogger(DBManager.class);

    public static final int DB_VALIDATION_TIMEOUT = 3; // seconds

    private Connection conn;
    private PreparedStatement psInsertCar;
    private PreparedStatement psDeleteCars;

    /**
     * Create singleton instance.
     */
    private DBManager() {
    }

    /**
     * Get singleton instance.
     * 
     * @return
     */
    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    /**
     * Initialize all variables
     * 
     * @param rb
     * @throws SQLException
     */
    synchronized public static void init() throws SQLException {
        log.info("DBManager init");

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (Exception e) {
            log.error("Failed to load HSQLDB JDBC driver.");
            throw new SQLException("Failed to load HSQLDB JDBC driver.");
        }
        connect();
    }

    /**
     * Connect to DB
     * 
     * @throws SQLException
     */
    synchronized public static void connect() throws SQLException {
        log.info("DBManager connect");
        long time = System.currentTimeMillis();

        getInstance().conn = DriverManager.getConnection("jdbc:hsqldb:mem:ciscodb", "SA", "");
        getInstance().conn.setAutoCommit(false);

        DatabaseMetaData meta = getInstance().conn.getMetaData();
        log.info("Connected database: " + meta.getDatabaseProductName() + ", "
                + meta.getDatabaseProductVersion().replace(LS, " "));
        log.debug("Database driver: " + meta.getDriverName() + " " + meta.getDriverVersion() + ", JDBC: "
                + meta.getJDBCMajorVersion()
                + "." + meta.getJDBCMinorVersion());

        log.info("DB connected in " + ((System.currentTimeMillis() - time) / 1000.0d) + "s");

        // create schema
        Statement stmt = null;
        try {
            stmt = getInstance().conn.createStatement();
            stmt.executeUpdate(
                    "CREATE TABLE cars (vin VARCHAR(50) NOT NULL, brand VARCHAR(50) NOT NULL, model VARCHAR(50), price INT, PRIMARY KEY (vin))");
        } catch (SQLException exc) {
            log.error("Cannot create schema.", exc);
        } finally {
            closeStatements(stmt);
        }

        // initialize prepared statements
        getInstance().psInsertCar = getInstance().conn.prepareStatement("insert into cars (vin, brand, model, price) values (?, ?, ?, ?)");
        getInstance().psDeleteCars = getInstance().conn.prepareStatement("delete from cars");
    }

    /**
     * Get DB connection
     * 
     * @return
     */
    public static Connection getConnection() {
        return getInstance().conn;
    }

    /**
     * Close all DBWorkers and then connect to DB again.
     * 
     * @throws SQLException
     */
    public synchronized static void reconnect() throws SQLException {
        log.info("Reconnecting database");
        close();
        connect();
    }

    /**
     * Shutdown thread executor and close all DB workers with all DB connections.
     */
    public synchronized static void close() {
        log.info("DBManager close");

        // close all prepared statements
        closeStatements(getInstance().psInsertCar, getInstance().psDeleteCars);

        // close all DB connections
        try {
            getConnection().close();
        } catch (SQLException exc) {
            log.warn("Cannot close DB connection.", exc);
        }
        // close DB
        try {
            DriverManager.getConnection("jdbc:hsqldb:mem:ciscodb;shutdown=true", "SA", "");
        } catch (SQLException exc) {
            log.warn("Cannot shutdown DB.", exc);
        }
    }

    /**
     * Check main DB connection if it is still alive.
     */
    public boolean isAlive() {
        try {
            return getInstance().conn.isValid(DB_VALIDATION_TIMEOUT);
        } catch (SQLException exc) {
            log.warn("Cannot execute DB connection isValid()", exc);
        }
        return false;
    }

    /**
     * Select all car records from DB.
     * 
     * @return
     * @throws SQLException
     */
    public static synchronized List<Car> selectCars() throws SQLException {
        log.info("select cars - start");
        List<Car> cars = new ArrayList<Car>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = getConnection().createStatement();
            rs = stmt.executeQuery("select vin, brand, model, price from cars");
            while (rs.next()) {
                Car car = new Car();
                car.setVIN(rs.getString(1));
                car.setBrand(rs.getString(2));
                car.setModel(rs.getString(3));
                car.setPrice(rs.getInt(4));
                cars.add(car);
            }
        } finally {
            closeResultSets(rs);
            closeStatements(stmt);
            log.info("select cars - end - " + cars.size());
        }
        return cars;
    }

    /**
     * Insert one new car record into DB.
     * 
     * @param car
     * @throws SQLException
     */
    public static synchronized void insertCar(Car car) throws SQLException {
        insertCar(car.getVIN(), car.getBrand(), car.getModel(), car.getPrice());
    }

    /**
     * Insert one new car record into DB.
     * 
     * @param vin
     * @param brand
     * @param model
     * @param price
     * @return
     * @throws SQLException
     */
    public static synchronized void insertCar(String vin, String brand, String model, int price) throws SQLException {
        log.info("insert car - start");
        checkParameters(vin, brand, model);
        getInstance().psInsertCar.setString(1, vin);
        getInstance().psInsertCar.setString(2, brand);
        getInstance().psInsertCar.setString(3, model);
        getInstance().psInsertCar.setInt(4, price);
        int rows = getInstance().psInsertCar.executeUpdate();
        if (rows != 1) throw new SQLException(
                "Cannot insert car record into DB. Returned rows '" + rows + "'.");
        log.info("insert car - end - " + vin);
    }

    /**
     * Delete all car records from DB.
     * 
     * @return
     * @throws SQLException
     */
    public static synchronized int deleteCars() throws SQLException {
        log.info("delete cars - start");
        int rows = getInstance().psDeleteCars.executeUpdate();
        log.info("delete cars - end - " + rows);
        return rows;
    }

    /**
     * Close all SQL result sets and ignore SQLException
     * 
     * @param resultSets
     */
    public static void closeResultSets(ResultSet... resultSets) {
        for (ResultSet rs : resultSets) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLRecoverableException exc) {
                    // no problem
                } catch (SQLException exc) {
                    log.warn("Cannot close SQL result set. " + exc.getMessage(), exc);
                }
            }
        }
    }

    /**
     * Close all SQL statements and ignore SQLException
     * 
     * @param statements
     */
    public static void closeStatements(Statement... statements) {
        for (Statement stmt : statements) {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLRecoverableException exc) {
                    // no problem
                } catch (SQLException exc) {
                    log.warn("Cannot close SQL statement. " + exc.getMessage(), exc);
                }
            }
        }
    }

    /**
     * Check input parameters to prevent SQL injection.
     * 
     * @param params
     * @throws SQLException
     */
    private static void checkParameters(String... params) throws SQLException {
        final char[] SQL_INJECTION_CHARS = new char[] { '\0', '\b', '\n', '\r', '\t', '\f', '\"', '\'', '\\', ';' };
        if (params == null) return;
        for (String param : params) {
            if (param == null) continue;
            for (char c : SQL_INJECTION_CHARS) {
                if (param.indexOf(c) >= 0) throw new SQLException("Illegal characters.");
            }
        }
    }
}
