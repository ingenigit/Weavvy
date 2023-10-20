package com.or2go.weavvy;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PaymentSelectorDialog extends Dialog implements View.OnClickListener{

    public PaymentSelectorDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public PaymentSelectorDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public Activity activity;
    public Dialog dialog;
    public Button cancel;
    TextView title;
    RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter adapter;



    public PaymentSelectorDialog(Activity a, RecyclerView.Adapter adapter) {
        super(a);
        this.activity = a;
        this.adapter = adapter;
        setupLayout();
    }

    private void setupLayout() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_payment_selector);
        cancel = (Button) findViewById(R.id.selCancel);
        //no = (Button) findViewById(R.id.no);
        title = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);


        recyclerView.setAdapter(adapter);
        cancel.setOnClickListener(this);
        //no.setOnClickListener(this);

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
