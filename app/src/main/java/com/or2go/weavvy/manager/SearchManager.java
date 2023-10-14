package com.or2go.weavvy.manager;

import android.content.Context;

import com.or2go.core.SearchInfo;
import com.or2go.mylibrary.SearchDBHelper;
import com.or2go.weavvy.AppEnv;

import java.util.ArrayList;

public class SearchManager {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    SearchDBHelper searchdb;

    public SearchManager(Context context) {
        mContext = context;
        gAppEnv = (AppEnv) context;
        searchdb = new SearchDBHelper(context);
        searchdb.InitDB();
    }

    public boolean addSearchData(String store, String name, String brand, String tag, Integer id) {
        return searchdb.insertData(name, brand, store, tag, id);
    }

    public ArrayList<SearchInfo> getSearchDataList()
    {
        return searchdb.getSearchDataList();
    }

    public ArrayList<String> getSearchNames()
    {
        return searchdb.getSearchNames();
    }

    public ArrayList<SearchInfo> getSearchInfo(String name)
    {
        return searchdb.getSearchInfo(name);
    }

    public boolean getSearchInfo(String name,  ArrayList<SearchInfo>list ) {
        return searchdb.getSearchInfo(name, list);
    }
}
