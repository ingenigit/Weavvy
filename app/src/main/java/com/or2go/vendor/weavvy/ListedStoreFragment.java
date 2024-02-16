package com.or2go.vendor.weavvy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.or2go.vendor.weavvy.storeList.StoreList;

import java.util.ArrayList;

public class ListedStoreFragment extends Fragment {
    Context context;
    AppEnv gAppEnv;
    RecyclerView recyclerView;
    EditText editTextSearch;
    ImageView imageViewClear;
    ProgressBar progressBar;
    FloatingActionButton fabOpenMap;
    ArrayList<StoreList> storeList;
    StoreListAdapter storeListAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    //timer
    long delay = 1000; // 1 seconds after user stops typing
    long last_text_edit = 0;
    String inputText = "";
    Handler handler = new Handler();

    public ListedStoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_listed_store, container, false);
        context = getContext();
        gAppEnv = (AppEnv) getActivity().getApplicationContext();
        editTextSearch = (EditText) view.findViewById(R.id.ed_search);
        imageViewClear = (ImageView) view.findViewById(R.id.img_clear);
        progressBar = (ProgressBar) view.findViewById(R.id.progrssbar);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_StoreList);
        fabOpenMap = (FloatingActionButton) view.findViewById(R.id.fab_open_map);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        storeList = gAppEnv.getStoreManager().getStoreList();
        for (int i = 0; i < storeList.size(); i++)
            System.out.println("StoreName: " + storeList.get(i).getStringName());
        storeListAdapter = new StoreListAdapter(context, storeList);
        recyclerView.setAdapter(storeListAdapter);
        fabOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ListedStore.class));
            }
        });
        return view;
    }
}