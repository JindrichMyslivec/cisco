package com.cisco.jimy.json;

/**
 * JSON structure
 * 
 * @author Jindrich Myslivec
 */
public class Car {

    private String vin;
    private String brand;
    private String model;
    private int price;

    public Car() {
    }
    
    public String getVIN() {
        return vin;
    }

    public void setVIN(String vin) {
        this.vin = vin;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    
    @Override
    public String toString() {
        return getBrand() + " (" + getVIN() + ") " + getModel() + ": " + getPrice() + "CZK";
    }

}
