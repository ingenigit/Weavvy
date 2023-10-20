package com.or2go.weavvy.activity;

import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_HISTORY_INFO;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_FORCE_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_REJECTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.or2go.adapter.Or2goItemListAdapter;
import com.or2go.core.Or2GoStore;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderItem;
import com.or2go.core.ProductInfo;
import com.or2go.core.ProductSKU;
import com.or2go.core.SalesSelectInfo;
import com.or2go.mylibrary.CartDBHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.CustomDailogView;
import com.or2go.weavvy.R;
import com.or2go.weavvy.manager.OrderCartManager;
import com.or2go.weavvy.manager.ProductManager;
import com.or2go.weavvy.server.OrderInfoDetailCallback;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Objects;

public class OrderHistoryDetailsActivity extends AppCompatActivity {

    Context mContext;
    AppEnv gAppEnv;
    String curorderid="";
    String rejectItems;
    BottomSheetDialog bottomSheetDialog;
    Or2goOrderInfo moOr2goInfo;
    Or2GoStore mStoreInfo;
    OrderCartManager mCartMgr;
    SalesSelectInfo salesSelectInfo;
    ProductSKU productSKUInfo;
    CartDBHelper cartDB;
    TextView iordid;
    TextView iordstore;
    TextView iorddtime;
    TextView iordtime;
    TextView iordsubtotal;
    TextView iordtotal;
    TextView idelcharge;
    TextView idiscount;
    TextView itax;
    TextView idaddress;
    TextView ipaysts;
    LinearLayout linearLayoutAddress;
    Or2goItemListAdapter itemlistadapter;
    ArrayList<OrderItem> itemlist;
    ProgressDialog progressDialog;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    CustomDailogView customDailogView;
    BottomNavigationView btNavigation;
    Button rateOrder, reOrder;
    OrderInfoDetailCallback cbOrderInfoCallback;
    Currency currency;
    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy h:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_details);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        cartDB = new CartDBHelper(mContext);
        cartDB.InitCartDB();
        if (!gAppEnv.getEnvStatus()) {
            Toast.makeText(mContext,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrderHistoryDetailsActivity.this, MainActivity.class));
        }
        currency = Currency.getInstance("INR");
        curorderid = getIntent().getStringExtra("orderid");
        rejectItems = getIntent().getStringExtra("rejres");
        mCartMgr = gAppEnv.getCartManager();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        iordid = (TextView) findViewById(R.id.tvorderiddata);
        iordstore = (TextView) findViewById(R.id.tvstorename);
        iordtime = (TextView) findViewById(R.id.tvtimedata);
        iorddtime = (TextView) findViewById(R.id.tvdtimedata);
        iordsubtotal = (TextView) findViewById(R.id.tvsubtotaldata);
        iordtotal = (TextView) findViewById(R.id.tvtotaldata);
        idelcharge = (TextView) findViewById(R.id.tvdeliverychargedata);
        idiscount = (TextView) findViewById(R.id.tvdiscountdata);
        itax = (TextView) findViewById(R.id.tvtaxdata);
        idaddress = (TextView) findViewById(R.id.tvdeliadddata);
        ipaysts = (TextView) findViewById(R.id.tvpaystsdata);
        rateOrder = (Button) findViewById(R.id.rateHisOrd);
        reOrder = (Button) findViewById(R.id.reOrderHisOrd);
        mRecyclerView = (RecyclerView) findViewById(R.id.orderitemlist);
        linearLayoutAddress = (LinearLayout) findViewById(R.id.linearlayoutAddress);
        mLayoutManager = new GridLayoutManager(mContext,1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        itemlist = new ArrayList<OrderItem>();
        getOrderHistoryInfo(curorderid);
        btNavigation = (BottomNavigationView) findViewById(R.id.salesdetailsbottomnavi);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Integer cartcnt = gAppEnv.getCartCaount();//getCartManager().getCartSize();
        if (cartcnt > 0) {
            //String SalesSubtotalVal = gAppEnv.getCartManager().getCartTotal();
            btNavigation.getMenu().getItem(2).setTitle(currency.getSymbol() + mCartMgr.getCartItemTotal());
            btNavigation.getOrCreateBadge(R.id.history_cart);
            BadgeDrawable cartBadge = btNavigation.getBadge(R.id.history_cart);
            cartBadge.setNumber(cartcnt);
        }else{
            btNavigation.getMenu().getItem(2).setTitle("Cart");
            btNavigation.removeBadge(R.id.navigation_cart);
        }
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        monitorCommProgress();
        rateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderHistoryDetailsActivity.this, FeedbackActivity.class);
                intent.putExtra("orderid", curorderid);
                startActivity(intent);
            }
        });
        reOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gAppEnv.getCartCaount() > 0){
                    Toast.makeText(mContext, "Cart already has items.", Toast.LENGTH_SHORT).show();
                }else{
                    String cartvendor = mCartMgr.getCurrentVendor();
//                    if ( (!cartvendor.isEmpty())  && (!orderHistoryInfo.oStore.equals(cartvendor)) )
                    if ( (!cartvendor.isEmpty())  && (!moOr2goInfo.oStoreId.equals(cartvendor)) )
                    {
                        //Show alert message
                        String title = "Already Exist!";
                        String body = "Products of different vendor already exist in cart.";
                        String positive = "Ok";
                        boolean visible = false;
                        CustomDailogView.onClickButton onclick = new CustomDailogView.onClickButton() {
                            @Override
                            public void onClick(View view) {
                                switch (view.getId()) {
                                    case R.id.positive_Btn:
                                        customDailogView.dismiss();
                                        break;
                                }
                            }
                        };
                        customDailogView = new CustomDailogView(OrderHistoryDetailsActivity.this, title, body, positive, "", visible, onclick);
                        customDailogView.show();
                    }else {
                        for (int i = 0; i< moOr2goInfo.getItemList().size(); i++){
                            int id = moOr2goInfo.getItemList().get(i).getId();
                            ProductManager productManager = gAppEnv.getStoreManager().getProductManager(moOr2goInfo.oStoreId);
                            ProductInfo productInfo = productManager.getProductInfo(id);
                            salesSelectInfo = new SalesSelectInfo(productManager.getProductInfo(id));
                            productSKUInfo = salesSelectInfo.getSelectedSKUInfo();
                            mCartMgr.addNewItem(id, moOr2goInfo.oStoreId, productSKUInfo);
                            salesSelectInfo.incQuantity(id);
                            startActivity(new Intent(OrderHistoryDetailsActivity.this, OrderCartActivity.class));
                        }
                    }
                }
            }
        });
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.history_home:
                    startActivity(new Intent(OrderHistoryDetailsActivity.this, MainActivity.class));
                    return true;
                case R.id.history_feedback:
                    Intent intent = new Intent(OrderHistoryDetailsActivity.this, FeedbackActivity.class);
                    intent.putExtra("orderid", curorderid);
                    startActivity(intent);
                    return true;
                case R.id.history_cart:
                    if (gAppEnv.getCartCaount() > 0) {
                        startActivity(new Intent(OrderHistoryDetailsActivity.this, OrderCartActivity.class));
                    } else
                        Toast.makeText(mContext, "Cart is empty!", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(OrderHistoryDetailsActivity.this, OrderHistoryActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOrderInfo() {
        itemlist = moOr2goInfo.getItemList();
        itemlistadapter = new Or2goItemListAdapter(this, R.layout.listview_orderhistory_item, itemlist);
        mRecyclerView.setAdapter(itemlistadapter);
        mStoreInfo = gAppEnv.getStoreManager().getStoreById(moOr2goInfo.oStoreId);
        iordid.setText(moOr2goInfo.oOr2goId);
        iordstore.setText(mStoreInfo.getName());

        try {
            if (Objects.equals(moOr2goInfo.oTime, "") || Objects.equals(moOr2goInfo.oDeliveryTime, "")){
                iordtime.setText(moOr2goInfo.oTime);
                iorddtime.setText(moOr2goInfo.getCompletionTime());
            }else{
                Date date = inputFormat.parse(moOr2goInfo.oTime);
                iordtime.setText(outputFormat.format(date));
                Date date1 = inputFormat.parse(moOr2goInfo.getCompletionTime());
                iorddtime.setText(outputFormat.format(date1));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(moOr2goInfo.oType == 2)
            linearLayoutAddress.setVisibility(View.GONE);
        iordtotal.setText(currency.getSymbol() + moOr2goInfo.oTotal);
        idelcharge.setText( "+" + currency.getSymbol() + moOr2goInfo.oDeliveryCharge);
        idiscount.setText("-" + currency.getSymbol() + moOr2goInfo.oDiscount);
        itax.setText("+" + currency.getSymbol() + moOr2goInfo.oTax);
        if (sendFloatValue(moOr2goInfo.oSubTotal).equals("0.0"))
            iordsubtotal.setText(currency.getSymbol() + (int) Float.parseFloat(moOr2goInfo.oSubTotal));
        else
            iordsubtotal.setText(currency.getSymbol() + moOr2goInfo.oSubTotal);
        idaddress.setText(moOr2goInfo.oAddress);
        switch (moOr2goInfo.oPayMode){
            case 0:
                ipaysts.setText("Pay Status None");
                break;
            case 1:
                ipaysts.setText("Pay Status Online Complete");
                break;
            case 3:
                ipaysts.setText("Pay Status Complete");
                break;
            default:
                ipaysts.setText("Pay Status");
                break;
        }
        if (moOr2goInfo.getStatus() == ORDER_STATUS_COMPLETE){
            bottomSheetDialogShow(0);
        }
        else if (moOr2goInfo.getStatus() == ORDER_STATUS_CANCELLED)
        {
            bottomSheetDialogShow(1);
        }
        else if ((moOr2goInfo.getStatus() == ORDER_STATUS_REJECTED) || (moOr2goInfo.getStatus() == ORDER_STATUS_FORCE_CANCELLED)){
            if (rejectItems == null)
                bottomSheetDialogShow(2);
            else
                orderRejectDialog();
        }
    }

    private void bottomSheetDialogShow(int i) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);
        TextView textViewTitle = (TextView) bottomSheetDialog.findViewById(R.id.tv_order_Title);
        Button buttonfedback = (Button) bottomSheetDialog.findViewById(R.id.buttonRate);
        Button buttonclose = (Button) bottomSheetDialog.findViewById(R.id.btClose);
        buttonfedback.setVisibility(View.GONE);
        if (i == 0)
            textViewTitle.setText("Order Completed");
        else if (i == 1)
            textViewTitle.setText("Order Cancelled");
        else if (i == 2)
            textViewTitle.setText("Order Declined");
        buttonclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    private void orderRejectDialog() {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_order_reject);
        dialog.setCancelable(false);
        TextView ordmsg = (TextView) dialog.findViewById(R.id.tv_ordrej_msg);
        ListView listview = (ListView) dialog.findViewById(R.id.listViewRejection);

        if (moOr2goInfo.getStatus() == ORDER_STATUS_REJECTED) {
            ordmsg.setText(moOr2goInfo.getStatusDescription());
            String[] rejected = rejectItems.split(",");
            ArrayList<String> rejData = new ArrayList<String>();
            for (int i = 1; i < rejected.length; i++){//3
                rejData.add(rejected[i]);
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rejData);
            listview.setAdapter(adapter);
        }
        else if (moOr2goInfo.getStatus() == ORDER_STATUS_FORCE_CANCELLED)
            ordmsg.setText(moOr2goInfo.getCancelCodeText());
        Button okButton = (Button) dialog.findViewById(R.id.btOrdRejOk);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gAppEnv.getOrderManager().removeOrder(moOr2goInfo);
                gAppEnv.getNotificationManager().clearAllNotifications();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public String sendFloatValue(String value){
        String ss = value;
        BigDecimal bigDecimal = new BigDecimal(ss);
        int i = bigDecimal.intValue();
        String ll = bigDecimal.subtract(new BigDecimal(i)).toPlainString();
        return ll;
    }

    private void getOrderHistoryInfo(String curorderid) {
        Message msg = new Message();
        msg.what = OR2GO_ORDER_HISTORY_INFO;    //fixed value for sending sales transaction to server
        msg.arg1 = 0;
        cbOrderInfoCallback = new OrderInfoDetailCallback(mContext);
        cbOrderInfoCallback.setViewAdapter(moOr2goInfo, itemlistadapter);
        Bundle b = new Bundle();
        b.putString("orderid", curorderid);
        b.putParcelable("callback", cbOrderInfoCallback);
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
    }

    private void monitorCommProgress() {
        final Handler mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            public void run() {
                if (cbOrderInfoCallback.getDataStatus()) {
                    progressDialog.dismiss();
                    gAppEnv.getGposLogger().d("AppMain : Order history info now ready...updating order data");
                    mProgressHandler.removeCallbacks(null);
                    moOr2goInfo = cbOrderInfoCallback.gerOrderInfo();
                    setOrderInfo();
                }
                else {
                    mProgressHandler.postDelayed(this, 2000);
                }
            }
        }, 2000);
    }
}