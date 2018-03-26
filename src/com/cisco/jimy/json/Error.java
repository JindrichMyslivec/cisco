package com.cisco.jimy.json;

/**
 * JSON structure for error messages.
 * 
 * @author jindrich.myslivec
 */
public class Error {

    private String message;
    private String stacktrace;
    
    public Error() {
        this(null, null);
    }
    
    public Error(String message) {
        this(message, null);
    }
    
    public Error(String message, String stacktrace) {
        this.message = message;
        this.stacktrace = stacktrace;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

}
