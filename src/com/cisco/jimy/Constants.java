package com.cisco.jimy;

import java.nio.charset.Charset;

/**
 * Constants for Cisco server.
 * 
 * @author jindrich.myslivec
 */
public interface Constants {

    public static final String COMMAND_GET_HEALTHY = "/healthy";
    public static final String COMMAND_POST_CARS = "/cars";
    public static final String COMMAND_GET_CARS = "/cars";

    public static final String PARAM_NAME = "name";
    public static final String PARAM_DATA = "data";
    public static final String PARAM_ID = "id";

    public static final String LS = System.getProperty("line.separator");
    public static int MB = 1024 * 1024;

    public static final int BUFFER_SIZE = 4096;
    public static final String ENCODING = "UTF-8";
    public static final Charset ENCODING_CHARSET = Charset.forName(ENCODING);

}
