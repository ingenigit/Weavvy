package com.or2go.vendor.weavvy;

public class MarkerData {
    Double latitude, longitude;
    String title, type;
    String snippet;

    public MarkerData(Double latitude, Double longitude, String title, String type, String snippet) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.type = type;
        this.snippet = snippet;
    }
}

