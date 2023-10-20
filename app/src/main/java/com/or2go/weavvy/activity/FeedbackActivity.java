package com.or2go.weavvy.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.or2go.adapter.FeedbackAdapter;
import com.or2go.core.Or2GoStore;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderHistoryInfo;
import com.or2go.core.OrderItem;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FeedbackActivity extends AppCompatActivity {

    AppEnv gAppEnv;
    Context mContext;
    String  curorderid;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FeedbackAdapter feedbackAdapter;
    ArrayList<OrderItem> itemlist;
    Or2goOrderInfo moOr2goInfo;
    OrderHistoryInfo oHistoryInfo;
    TextView textViewName;
    Button btSend, btCancel;
    RatingBar storeRatingBar;
    String storeFeedBack;
    FeedbackAdapter.RateOnClickListener rateOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        getSupportActionBar().setTitle("Customer feedback");
        curorderid = getIntent().getStringExtra("orderid");
        moOr2goInfo = gAppEnv.getOrderManager().findOrder(curorderid);
        if (moOr2goInfo == null){
            oHistoryInfo = gAppEnv.getOrderHistoryManager().getCompletedOrder(curorderid);
        }
        recyclerView = (RecyclerView) findViewById(R.id.fd_recycleView);
        textViewName = (TextView) findViewById(R.id.store_name);
        storeRatingBar = (RatingBar) findViewById(R.id.store_rate);
        btSend = (Button) findViewById(R.id.bt_send_fedback);
        btCancel = (Button) findViewById(R.id.bt_cancel_fedback);
        storeRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                storeFeedBack = String.valueOf(ratingBar.getRating());
            }
        });
        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                postFeedback();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (moOr2goInfo == null){
                    finish();
                }else{
                    startActivity(new Intent(FeedbackActivity.this, MainActivity.class));
                }
            }
        });
        try {
            if(moOr2goInfo == null){
                Or2GoStore mStoreInfo = gAppEnv.getStoreManager().getStoreById(oHistoryInfo.oStore);
                textViewName.setText(mStoreInfo.getName());
                itemlist = oHistoryInfo.getItemList();
            }else{
                Or2GoStore mStoreInfo = gAppEnv.getStoreManager().getStoreById(moOr2goInfo.oStoreId);
                textViewName.setText(mStoreInfo.getName());
                itemlist = moOr2goInfo.getItemList();
            }
        }catch (Exception e){
            Toast.makeText(gAppEnv, "" + e, Toast.LENGTH_SHORT).show();
        }
        layoutManager = new LinearLayoutManager(FeedbackActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        rateOnClickListener  = new FeedbackAdapter.RateOnClickListener(){
            @Override
            public void onRatingChanged(Integer position, RatingBar ratingBar, float rating, boolean fromUser) {
                OrderItem item = itemlist.get(position);
                item.setRateNumber(ratingBar.getRating());
            }
        };
        if(moOr2goInfo == null)
            feedbackAdapter = new FeedbackAdapter(mContext, BuildConfig.OR2GO_SERVER, oHistoryInfo.oStore,itemlist, R.layout.dialog_order_complete, rateOnClickListener);
        else
            feedbackAdapter = new FeedbackAdapter(mContext, BuildConfig.OR2GO_SERVER, moOr2goInfo.oStoreId,itemlist, R.layout.dialog_order_complete, rateOnClickListener);
        recyclerView.setAdapter(feedbackAdapter);
    }

    private void postFeedback() {
        if(moOr2goInfo == null){
            gAppEnv.getOrderManager().postRating(oHistoryInfo.oId, oHistoryInfo.oStore, getFeedbackData());
            Toast.makeText(mContext, "your feedback is posted. Thank you.", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            gAppEnv.getOrderManager().postRating(moOr2goInfo.getId(), moOr2goInfo.getStoreId(), getFeedbackData());
            Toast.makeText(mContext, "your feedback is posted. Thank you.", Toast.LENGTH_SHORT).show();
            //move to main activity
            startActivity(new Intent(FeedbackActivity.this, MainActivity.class));
        }
    }

    private String getFeedbackData() {
        String ratingdesc="";
        JSONArray jsonArrFeedback = new JSONArray();
        //Store Service Feednack
        JSONObject jsonObjectFeedbackSerive = new JSONObject();
        try {
            jsonObjectFeedbackSerive.accumulate("product", "0");
            jsonObjectFeedbackSerive.accumulate("rating", storeFeedBack);
            jsonObjectFeedbackSerive.accumulate("feedback", "Good food");
            jsonArrFeedback.put(jsonObjectFeedbackSerive);
        }
        catch(Exception e) {
            gAppEnv.getGposLogger().d("Exception: "+e.getMessage());
        }
        //Add product feedbacks
        for(int i=0;i<itemlist.size();i++) {
            OrderItem item = itemlist.get(i);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("product", item.getId().toString());
                jsonObject.accumulate("rating", item.getRateNumber().toString());
                jsonObject.accumulate("feedback", item.getFeebackText().toString());
                jsonArrFeedback.put(jsonObject);
            }
            catch(Exception e) {
                gAppEnv.getGposLogger().d("Exception: "+e.getMessage());
            }
        }
        ratingdesc = jsonArrFeedback.toString();
        return ratingdesc;
    }
}