package com.or2go.weavvy.manager;

import android.content.Context;
import android.util.Log;

import com.or2go.core.SPInfo;
import com.or2go.mylibrary.SPDBHelper;
import com.or2go.weavvy.AppEnv;

public class SPManager {
    private Context mContext;
    AppEnv gAppEnv;

    SPInfo mSPInfo;
    SPDBHelper mSPDB;

    public SPManager(Context context) {
        mContext = context;
        gAppEnv = (AppEnv) context;// getApplicationContext();

        Log.i("SPManager", "Initializing Service Provider Info From DB");
        mSPDB = new SPDBHelper(mContext);

        mSPInfo = mSPDB.getSPInfo();

        //mSPInfo.dumpSPInfo();

        //SPDBTest();
    }

    public void setSPInfo(SPInfo spinfo)
    {
        boolean dbres;
        //make sure sp table is empty..
        if ((mSPInfo!= null) && (mSPDB.isSPInfoSet()))
            dbres= mSPDB.updateSPInfo(spinfo);
        else
            dbres= mSPDB.insertSPInfo(spinfo);

        if (dbres) mSPInfo = spinfo;
        else
            System.out.println("Error in inserting SP Info !!!");
    }

    public boolean setShutdownInfo(String from, String till, String cause)
    {
        if (mSPInfo==null) return false;

        mSPDB.setShutdownInfo(mSPInfo.spname, from, till, cause);

        boolean ret = mSPInfo.setShutdownInfo(from, till, cause);
        mSPInfo.dumpSPInfo();

        return ret;
    }

    public boolean clearShutdownInfo()
    {
        if (mSPInfo==null) return false;

        mSPDB.clearShutdownInfo(mSPInfo.spname);

        boolean ret =  mSPInfo.clearShutdownInfo();
        mSPInfo.dumpSPInfo();

        return ret;
    }

    public boolean isClosedToday()
    {
        if (mSPInfo==null) return false;

        return mSPInfo.isClosedToday();
    }
}
