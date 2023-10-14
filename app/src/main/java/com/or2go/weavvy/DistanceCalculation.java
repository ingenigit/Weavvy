package com.or2go.weavvy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.or2go.core.DeliveryAddrInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class DistanceCalculation implements Runnable{
    AppEnv gAppEnv;
    Context mContext;
    public String vendorloc;
    public String customerloc;
    DeliveryAddrInfo mDeliveryAddrInfo;
    String mModelJsonData;
    DeliveryChargeViewModel mDeliViewModel;
    public String getTotalDistance;
    LocalBroadcastManager localBroadcastManager;
    Intent intent;
    public DistanceCalculation(Context mContext, String vendorloc, String customerloc) {
        this.gAppEnv = (AppEnv) mContext;
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        intent = new Intent("updateDistance");
        this.mContext = mContext;
        this.vendorloc = vendorloc;
        this.customerloc = customerloc;
    }

    @Override
    public void run() {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/distancematrix/json")
                .buildUpon()
                .appendQueryParameter("origins", vendorloc)
                .appendQueryParameter("destinations", customerloc)
                .appendQueryParameter("sensor", "false")
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", "AIzaSyAnhTf79xLDcS0zj_cl_rjAVbx-cIBfwa8")
                .toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        JSONArray jsonArray = response.getJSONArray("rows");
                        JSONObject jsonObjectDistance = jsonArray
                                .getJSONObject(0)
                                .getJSONArray("elements")
                                .getJSONObject(0)
                                .getJSONObject("distance");
                        JSONObject jsonObjectTime = jsonArray
                                .getJSONObject(0)
                                .getJSONArray("elements")
                                .getJSONObject(0)
                                .getJSONObject("duration");
                        String tDistance = jsonObjectDistance.getString("text").toString();
                        String tTime = jsonObjectTime.getString("text").toString();
                        findDistanceCharge(mDeliveryAddrInfo, tDistance, mModelJsonData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void setDeliveryInfo(DeliveryAddrInfo addr, String modeldata, DeliveryChargeViewModel delimodel)
    {
        mDeliveryAddrInfo= addr;
        mModelJsonData =modeldata;
        mDeliViewModel = delimodel;
    }

    /*private String findDistanceCharge(DeliveryAddrInfo addr, String distance, String mParams) {

        System.out.println("DistanceCalculation : findDistanceCharge : distance ="+distance);

        String retval = "-1";
        try{
            JSONArray jsonArray = new JSONArray(mParams);
            int freDistance = 0, basDistacne = 0, basCharge = 0, ratperKM = 0;
            String[] getdistance = distance.split(" ");
            boolean hasComma = getdistance[0].contains(",");
            int num;
            if (hasComma)
                num = Integer.parseInt(getdistance[0].replaceAll(",", ""));
            else
                num = Integer.parseInt(getdistance[0]);
            int tdis = num;
            for (int i = 0; i < jsonArray.length(); i++){
                String stringData = jsonArray.getString(i);
                JSONObject childobj = new JSONObject(stringData);
                Iterator<String> list = childobj.keys();
                String name = list.next().trim();
                String charge = childobj.getString(name);
                if (name.equals("FreeDistance"))
                    freDistance = Integer.parseInt(charge);
                else if (name.equals("BaseDistance"))
                    basDistacne = Integer.parseInt(charge);
                else if (name.equals("BaseCharge"))
                    basCharge = Integer.parseInt(charge);
                else if (name.equals("RateperKM"))
                    ratperKM = Integer.parseInt(charge);
            }
            Integer m2delicharge;
            if (tdis > basDistacne) {
                int difference = tdis - basDistacne;
                m2delicharge = basCharge + (difference * ratperKM);
                //return String.valueOf(totalCharge);
            }
            else
                m2delicharge = basCharge;

            mDeliViewModel.getDeliveryCharge().setValue(m2delicharge.toString());

        }catch (JSONException e){
            e.printStackTrace();
        }
        return retval;
    }*/

    private String findDistanceCharge(DeliveryAddrInfo addr, String distance, String mParams) {
        Float freDistance = Float.parseFloat("0");
        Float basDistance = Float.parseFloat("0");
        Float basCharge = Float.parseFloat("0");
        Float ratperKM = Float.parseFloat("0");
        String sFreeDist="", sBaseDist="", sBaseCharge="", sRate="";
        System.out.println("DistanceCalculation : findDistanceCharge : distance ="+distance);
        getTotalDistance = distance.toString();
        String retval = "-1";
        try{
            JSONArray jsonArray = new JSONArray(mParams);

            JSONObject objFreeDistance = jsonArray.getJSONObject(0);
            JSONObject objBaseDistance = jsonArray.getJSONObject(1);
            JSONObject objBaseCharge = jsonArray.getJSONObject(2);
            JSONObject objKMRate = jsonArray.getJSONObject(3);

            System.out.println("DistanceModel : FreeKM ="+objFreeDistance.toString());
            System.out.println("DistanceModel : BaseKM ="+objBaseDistance.toString());
            System.out.println("DistanceModel : BaseCharge ="+objBaseCharge.toString());
            System.out.println("DistanceModel : Rate ="+objKMRate.toString());

            for (int i = 0; i < jsonArray.length(); i++){
                String stringData = jsonArray.getString(i);
                JSONObject childobj = new JSONObject(stringData);
                Iterator<String> list = childobj.keys();
                String name = list.next().trim();
                String value = childobj.getString(name);
                if (name.equals("FreeDistance"))
                    sFreeDist = value;
                else if (name.equals("BaseDistance"))
                    sBaseDist = value;
                else if (name.equals("BaseCharge"))
                    sBaseCharge = value;
                else if (name.equals("RateperKM"))
                    sRate = value;
            }

            freDistance = Float.parseFloat(sFreeDist);
            basDistance = Float.parseFloat(sBaseDist);
            basCharge = Float.parseFloat(sBaseCharge);
            ratperKM = Float.parseFloat(sRate);

            String[] getdistance = distance.split(" ");
            boolean hasComma = getdistance[0].contains(",");

            Float actdist = Float.parseFloat(getdistance[0].replaceAll(",",""));

            Float chargedist = actdist -freDistance;

            Float calcdist;
            if (chargedist < basDistance)
                calcdist = Float.valueOf(basDistance);
            else
                calcdist = chargedist;

            Float calccharge = calcdist * ratperKM;

            Float actcharge;
            if (calccharge < basCharge)
                actcharge = basCharge;
            else
                actcharge = calccharge;

            Integer idelicharge = actcharge.intValue();
            gAppEnv.gAppSettings.setGeoTotalDistance(getTotalDistance);
            localBroadcastManager.sendBroadcast(intent);
            mDeliViewModel.getDeliveryCharge().setValue(idelicharge.toString());

        }catch (JSONException e){
            e.printStackTrace();
        }
        return retval;
    }
}
