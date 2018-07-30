package com.dokitari.tekomaps.models;

public class AlertLocation {
    private double lati, longi;
    private String address;

    public AlertLocation() {
    }

    public AlertLocation(double lati, double longi, String address) {
        this.lati = lati;
        this.longi = longi;
        this.address = address;
    }

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}