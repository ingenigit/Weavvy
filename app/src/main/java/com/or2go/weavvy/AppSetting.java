package com.or2go.weavvy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppSetting {
	private Context mContext;
	  // Get Application super class for global data
	AppEnv gAppEnv;
	Properties mAppProperties;
	SharedPreferences sharedPref;// = PreferenceManager.getDefaultSharedPreferences(mContext);
	OnSharedPreferenceChangeListener listener;

	AppSetting(Context context ) {
		mContext =context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
		setPropertyBool("Pref_FTU", false);
	}
	
	public String getProperty(String key) {
		String appPref;
		//SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (key.equals("discount_preference") || key.equals("global_tax_preference"))
			appPref = sharedPref.getString(key, "0");		
		else
			appPref = sharedPref.getString(key, "");
		return appPref;
	}
	
	public boolean getBoolProperty(String key) {
		//SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean appPref = sharedPref.getBoolean(key, false);
		return appPref;
	}
	
	public boolean setProperty(String key, String value) {
		//SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString(key, value);
		prefEditor.commit();   
		return true;
	}
	
	public boolean setPropertyInt(String key, Integer value) {
		//SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor prefEditor = sharedPref.edit();
		prefEditor.putInt(key, value);
		prefEditor.commit();   
		return true;
	}
	
	public boolean setPropertyBool(String key, Boolean value) {
		//SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor prefEditor = sharedPref.edit();
		prefEditor.putBoolean(key, value);//(key, value);
		prefEditor.commit();   
		return true;
	}

	public String getUserId() {
		String devid = sharedPref.getString("Pref_UserId", "");
		return devid;
	}

	public boolean setUserId(String uid) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_UserId", uid);
		prefEditor.commit();
		return true;
	}

	public String getUserName() {
		String devid = sharedPref.getString("Pref_UserName", "");
		return devid;
	}

	public boolean setUserType(String type) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_UserType", type);
		prefEditor.commit();
		return true;
	}

	public String getUserType() {
		String userType = sharedPref.getString("Pref_UserType", "");
		return userType;
	}

	public boolean setAddressPage(boolean addresspage) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putBoolean("AddressPageUse", addresspage);
		prefEditor.commit();
		return true;
	}
	public Boolean getAddressPage() {
		return sharedPref.getBoolean("AddressPageUse", false);
	}

	public boolean setServerSPDBVersion(int ver) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putInt("Pref_SPDBVersion", ver);
		prefEditor.commit();
		return true;
	}

	public int getServerSPDBVersion()
	{
		return sharedPref.getInt("Pref_SPDBVersion", 0);
	}

	public boolean setUserName(String name) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_UserName", name);
		prefEditor.commit();
		return true;
	}

	public String getUserEmail() {
		String devid = sharedPref.getString("Pref_UserEmail", "");

		return devid;
	}

	public boolean setUserEmail(String email) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_UserEmail", email);
		prefEditor.commit();
		return true;
	}

	public String getUserAddress() {
		String devid = sharedPref.getString("Pref_UserAddress", "");

		return devid;
	}

	public boolean setUserAddress(String addr) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_UserAddress", addr);
		prefEditor.commit();
		return true;
	}

	public String getUserLocation() {
		String devid = sharedPref.getString("Pref_UserLocation", "");
		return devid;
	}

	public boolean setUserLocation(String loc) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_UserLocation", loc);
		prefEditor.commit();
		return true;
	}

	public String getServerID() {
		String devid = sharedPref.getString("Pref_ServerID", "");
		return devid;
	}
	
	public boolean setServerID(String devid) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_ServerID", devid);
		prefEditor.commit();   
		return true;
	}
	
	public boolean setDeviceName(String devid) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_DeviceName", devid);
		prefEditor.commit();   
		return true;
	}
	
	public String getDeviceName() {
		String devid = sharedPref.getString("Pref_DeviceName", "");
		
		return devid;
	}
	
	public boolean setPassword(String devid) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_Password", devid);
		prefEditor.commit();   
		return true;
	}
	
	public String getPassword()
	{
		return sharedPref.getString("Pref_Password", "");
	}

	public boolean setOrderReqId(Integer reqid) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putInt("Pref_OrderReqId", reqid);
		prefEditor.commit();
		return true;
	}

	public Integer getOrderReqId()
	{
		return sharedPref.getInt("Pref_OrderReqId", 0);
	}

	public boolean setPublicNoticeId(Integer noticeid) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putInt("Pref_PublicNoticeId", noticeid);
		prefEditor.commit();
		return true;
	}

	public Integer getPublicNoticeId()
	{
		return sharedPref.getInt("Pref_PublicNoticeId", 0);
	}

	public void initSettings(String mode) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.commit(); 
		
	}

	///App and Vendor settings
	public boolean setAppName(String name) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_AppName", name);
		prefEditor.commit();
		return true;
	}

	public String getAppName() {
		String storid = sharedPref.getString("Pref_AppName", "");
		return storid;
	}

	public boolean setAppContact(String contact) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_AppContact", contact);
		prefEditor.commit();
		return true;
	}

	public String getAppContact() {
		String storid = sharedPref.getString("Pref_AppContact", "");
		return storid;
	}
	public boolean setAppEmail(String email) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_AppEmail", email);
		prefEditor.commit();
		return true;
	}

	public String getAppEmail() {
		String storid = sharedPref.getString("Pref_AppEmail", "");
		return storid;
	}

	public boolean setAppDescription(String desc) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_AppDesc", desc);
		prefEditor.commit();
		return true;
	}

	public String getAppDescription() {
		String storid = sharedPref.getString("Pref_AppDesc", "");
		return storid;
	}

	public boolean setAppTheme(int th) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putInt("Pref_AppTheme", th);
		prefEditor.commit();
		return true;
	}

	public int getAppTheme() {
		int storid = sharedPref.getInt("Pref_AppTheme", -1);
		return storid;
	}

	public boolean setStoreIdName(String storeName) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("Pref_StoreName", storeName);
		prefEditor.commit();
		return true;
	}

	public String getStoreIdName() {
		String storid = sharedPref.getString("Pref_StoreName", "");
		return storid;
	}

	//Save use coupon
	public class Coupon{
		public String storeid, couponname;
		public Coupon(String storeid, String couponname) {
			this.storeid = storeid;
			this.couponname = couponname;
		}
	}
	public boolean setCouponUsed(String couponname, String storeid) {
		List<Coupon> couponArrayList = new ArrayList<Coupon>();
		couponArrayList.add(new Coupon(storeid, couponname));
		String json = new Gson().toJson(couponArrayList);
		Editor editor = sharedPref.edit();
		editor.putString("coupon_key", json);
		editor.commit();
		return true;
	}
	public ArrayList<Coupon> getCouponUsed(){
		String json = sharedPref.getString("coupon_key", "");
		Type type = new TypeToken<List<Coupon>>(){}.getType();
		return new Gson().fromJson(json, type);
	}

	//remove coupon which is not available (check from all couponlist name == saved coupon name)
	public boolean removeSharePerUsedCoupon(String key, String couponname){
		Editor editor = sharedPref.edit();
		ArrayList<Coupon> set = new ArrayList<Coupon>();
		set = getCouponUsed();
		if (set != null) {
			for (int i = 0; i < set.size(); i++){
				if(!set.get(i).couponname.equals(couponname)){
					set.remove(i);
					break;
				}
			}
		}
		String json = new Gson().toJson(set);
		editor.putString(key, json);
		editor.commit();
		return true;
	}

	public boolean setGeoAddress(String getAddressLatLong) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("addressGeo", getAddressLatLong);
		prefEditor.commit();
		return true;
	}
	public String getGeoAddress() {
		return sharedPref.getString("addressGeo", "");
	}

	//user exit not allow to use onetime coupon
	public boolean setExistPersonCoupon(boolean yesNo) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putBoolean("NoOneTimeCoupon", yesNo);
		prefEditor.commit();
		return true;
	}
	public boolean getExitPersonCoupon() {
		return sharedPref.getBoolean("NoOneTimeCoupon", false);
	}

	//set use of geo or not
	public boolean setUseGeoDistance(boolean yesNo) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putBoolean("UseGeoDistanceCharge", yesNo);
		prefEditor.commit();
		return true;
	}
	public boolean getUseGeoDistance() {
		return sharedPref.getBoolean("UseGeoDistanceCharge", false);
	}

	public boolean setGeoTotalDistance(String getTotalDistance) {
		Editor prefEditor = sharedPref.edit();
		prefEditor.putString("UsedGeoDistance", getTotalDistance);
		prefEditor.commit();
		return true;
	}
	public String getGeoTotalDistance() {
		return sharedPref.getString("UsedGeoDistance", "");
	}

}
