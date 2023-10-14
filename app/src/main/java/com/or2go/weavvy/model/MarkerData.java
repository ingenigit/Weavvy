package com.or2go.weavvy.model;

public class MarkerData {
    public Double latitude, longitude;
    public String title, type;
    public String snippet;

    public MarkerData(Double latitude, Double longitude, String title, String type, String snippet) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.type = type;
        this.snippet = snippet;
    }
}

