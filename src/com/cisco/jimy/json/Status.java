package com.cisco.jimy.json;

/**
 * JSON structure for returning status.
 * 
 * @author jindrich.myslivec
 */
public class Status {

    public static final String STATUS_SAVED = "Saved";
    
    private String status;
    private String message;
    
    public Status() {
        this(null, null);
    }
    
    public Status(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
