package com.or2go.vendor.weavvy.storeList;

import java.io.Serializable;

public class StoreList  implements Serializable {
    String stringID, stringName, stringType, geolocation, vContact;
    boolean isSelected;

    public StoreList(String stringID, String stringName, String stringType, String contact, String geoLocation, boolean isSelected) {
        this.stringID = stringID;
        this.stringName = stringName;
        this.stringType = stringType;
        this.vContact = contact;
        this.geolocation = geoLocation;
        this.isSelected = isSelected;
    }
    public StoreList (String stringID, String stringName){
        this.stringID = stringID;
        this.stringName = stringName;
    }

    public String getStringID() {
        return stringID;
    }

    public String getStringName() {
        return stringName;
    }

    public String getStringType() {
        return stringType;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public String getvContact() {
        return vContact;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
