package com.or2go.vendor.showstorenearme;

public class MarkerData {
    Double latitude, longitude;
    String title;
    String snippet;

    public MarkerData(Double latitude, Double longitude, String title, String snippet) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.snippet = snippet;
    }
}

