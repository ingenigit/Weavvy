package com.or2go.weavvy;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MultiPackSelectorDialog extends Dialog implements View.OnClickListener{

    public Activity activity;
    public Dialog dialog;
    public Button cancel;
    TextView title;
    RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter adapter;
    String mName;

    public MultiPackSelectorDialog(Activity a, RecyclerView.Adapter adapter, String name) {
        super(a);
        this.activity = a;
        this.adapter = adapter;
        mName  = name;
        setupLayout();
    }

    private void setupLayout() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_multipack_selector);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        cancel = (Button) findViewById(R.id.selCancel);
        //no = (Button) findViewById(R.id.no);
        title = findViewById(R.id.tvtop);
        recyclerView = findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        cancel.setOnClickListener(this);
        //no.setOnClickListener(this);

        title.setText(mName);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //case R.id.yes:
            //Do Something
            //    break;
            case R.id.selCancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
