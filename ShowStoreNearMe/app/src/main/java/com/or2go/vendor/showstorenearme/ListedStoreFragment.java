package com.or2go.vendor.showstorenearme;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.or2go.vendor.showstorenearme.storeList.StoreList;

import java.util.ArrayList;

public class ListedStoreFragment extends Fragment {

    Context context;
    AppEnv gAppEnv;
    RecyclerView recyclerView;
    ArrayList<StoreList> storeList;

    public ListedStoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_listed_store, container, false);
        context = getContext();
        gAppEnv = (AppEnv) getActivity().getApplicationContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_StoreList);

        storeList = gAppEnv.getStoreManager().getStoreList();
        for (int i = 0; i < storeList.size(); i++)
            System.out.println("StoreName: " + storeList.get(i).getStringName());



        return view;
    }
}