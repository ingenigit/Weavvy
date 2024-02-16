package com.or2go.vendor.weavvy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Properties;

public class AppSetting {
    private Context mContext;
    SharedPreferences sharedPref;// = PreferenceManager.getDefaultSharedPreferences(mContext);
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    AppSetting(Context context ) {
        mContext =context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean setSPID(String uid) {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("Pref_SPID", uid);
        prefEditor.commit();
        return true;
    }
    public String getSPID()
    {
        return sharedPref.getString("Pref_SPID", "");
    }

    public boolean setRegister(String regis){
        SharedPreferences.Editor perference = sharedPref.edit();
        perference.putString("Pref_Regis", regis);
        perference.commit();
        return true;
    }
    public String getRegister(){
        return sharedPref.getString("Pref_Regis", "");
    }

    public boolean setVendorType(String regis){
        SharedPreferences.Editor perference = sharedPref.edit();
        perference.putString("Pref_Vendor_Type", regis);
        perference.commit();
        return true;
    }
    public String getVendorType(){
        return sharedPref.getString("Pref_Vendor_Type", "");
    }

    public boolean setVendorId(String id) {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("Pref_VENDID", id);
        prefEditor.commit();
        return true;
    }
    public String getVendorId()
    {
        return sharedPref.getString("Pref_VENDID", "");
    }

    public boolean setStoreId(String id) {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("Pref_STOREID", id);
        prefEditor.commit();
        return true;
    }
    public String getStoreId()
    {
        return sharedPref.getString("Pref_STOREID", "");
    }

    public boolean setVenodorName(String name) {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("Pref_VendorName", name);
        prefEditor.commit();
        return true;
    }
    public String getVendorName()
    {
        return sharedPref.getString("Pref_VendorName", "");
    }

    public boolean setMobileNo(String name) {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("Pref_Mobile", name);
        prefEditor.commit();
        return true;
    }
    public String getMobileNo() {
        return sharedPref.getString("Pref_Mobile", "");
    }

    public boolean setPassocde(String name) {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("Pref_Passcode", name);
        prefEditor.commit();
        return true;
    }
    public String getPassocde() {
        return sharedPref.getString("Pref_Passcode", "");
    }
}
