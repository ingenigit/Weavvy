package com.or2go.weavvy.activity;

import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_DELIVERY;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_PICKUP;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_ITEM_DETAIL_REGULAR;
import static com.or2go.weavvy.manager.OrderCartManager.CART_STOCK_CHECK_COMPLETE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.or2go.adapter.AddressSelectorAdapter;
import com.or2go.adapter.CouponSelectorAdapter;
import com.or2go.adapter.OrderCartItemAdapter;
import com.or2go.core.CartItem;
import com.or2go.core.DeliveryAddrInfo;
import com.or2go.core.DiscountInfo;
import com.or2go.core.Or2GoStore;
import com.or2go.core.ProductSKU;
import com.or2go.weavvy.AddressSelectorDialog;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.CouponSelectorDialog;
import com.or2go.weavvy.CustomDailogView;
import com.or2go.weavvy.DeliveryChargeViewModel;
import com.or2go.weavvy.EditPackQuantityDialog;
import com.or2go.weavvy.OrderListDividerDecoration;
import com.or2go.weavvy.PublicNoticeDialog;
import com.or2go.weavvy.R;
import com.or2go.weavvy.manager.OrderCartManager;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderCartActivity extends AppCompatActivity implements AddressSelectorAdapter.RecyclerViewItemClickListener,
        CouponSelectorAdapter.RecyclerViewItemClickListener, EditPackQuantityDialog.DialogListener {

    Context mContext;
    AppEnv gAppEnv;
    Toolbar mToolbar;
    ArrayList<CartItem> itemlist;
    CustomDailogView customDailogView;
    CustomDailogView customdailogview;
    String geoTotalDistance;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    OrderCartManager mCartMgr;
    Or2GoStore mStoreInfo;
    private RequestQueue mRequestQueue;
    ImageLoader mImageLoader;
    ImageView imgQntyInc;
    ImageView imgQntyDec;
    ImageView imgDelete;
    ImageView imgEditQnty;
    TextView subTotalVal;
    TextView actualsubTotalVal;
    TextView tvDiscountVal;
    TextView exclTax;
    TextView tvDeliCharge;
    TextView tvGTotalVal;
    TextView tvAmountSave;
    TextView tvCouponInfo;
    LinearLayout linearLayoutSave;
    LinearLayout linearLayoutCoupon;
    ImageView btCouponControl;
    private EditText orderreqtime;
    ImageView btEditReqTime, btClearTime;
    TextView tvDeliAddr;
    TextView tvDeliContact;
    TextView tvDeliAddrName;
    Button btNewAddr;
    Button   btChangeAddr;
    ArrayList<DeliveryAddrInfo> mAddrInfoList;
    ArrayList<DiscountInfo> mCouponList;
    EditText edInstr;
    AddressSelectorDialog mAddrDialog=null;
    CouponSelectorDialog mCouponDialog=null;
    String SelectedCouponName;
    Integer getOrderType = 0;
    String sDeliCharge = "";
    DeliveryAddrInfo mDeliLocationInfo= null;
    int nEditQntyPos = -1;
    Currency currency = Currency.getInstance("INR");
    ProgressDialog progressDialog;
    Integer elapsedTime=0;
    String mOrderReqTime="";
    Boolean deliver = true;
    LinearLayout layoutoption, layoutaddress, layoutstoreaddress;
    TextView storeName, storeAddress, storecontact;
    Button buttonCallStore, buttonDirection;
    NestedScrollView scrollView;
    Integer nQnty;
    TextView tvorderType, tvchangeOrderType;
    private DeliveryChargeViewModel mDeliChargeViewModel;
    String storeaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cart);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.toolbar_line);
        TextView bartitle = (TextView) actionBar.getCustomView().findViewById(R.id.common2_toolbar_title);
        TextView barinfo = (TextView) actionBar.getCustomView().findViewById(R.id.common2_toolbar_info);
        ImageView imageView = (ImageView) actionBar.getCustomView().findViewById(R.id.imgcartvendlogo);
        tvorderType = (TextView) findViewById(R.id.tv_orderType);
        tvchangeOrderType = (TextView) findViewById(R.id.tv_changeOrderType);
        scrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrderCartActivity.this,SplashScreen.class)); }

        mCartMgr = gAppEnv.getCartManager();
        mStoreInfo = gAppEnv.getStoreManager().getStoreById(mCartMgr.getCurrentVendor());
        if (mCartMgr.getOrderType() == 0){
            showSelectOrderTypeDialog();
        }
        bartitle.setText(mStoreInfo.getName());
        barinfo.setText(mStoreInfo.vTagInfo);
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.blank_24)
                .error(R.drawable.blank_24);
        Glide.with(mContext)
                .load(BuildConfig.OR2GO_SERVER+"/storelogo"+"/LOGO"+mStoreInfo.getId()+".png")
                .apply(options)
                //.override(200, 200) // resizing
                //.fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
        itemlist = mCartMgr.getOrderList();
        mRecyclerView = (RecyclerView) findViewById(R.id.orderitemlistview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new OrderListDividerDecoration(this, LinearLayoutManager.VERTICAL, 16));
        OrderCartItemAdapter.RecyclerViewClickListener listener = new OrderCartItemAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position >= 0) {
                    if (view.getId() ==  R.id.btcartitemedit) {
                        CartItem edititem = itemlist.get(position);
                        nEditQntyPos = position;
                        ProductSKU pkinfo = edititem.getSKUInfo();
                        bottomSheetDialogShow(edititem.getImagePath(), edititem.getName(), edititem.getQnty(), edititem.getBrandName(),edititem.getName(), edititem.getId());
                    }
                    else if (view.getId() ==  R.id.btremoveitemstk) {
                        CartItem delitem = itemlist.get(position);
                        mCartMgr.deleteItem(delitem.getId(),delitem.getSKUId());
                        mAdapter.notifyDataSetChanged();
                        setTotal();
                    }
                }
            }
        };
        mAdapter = new OrderCartItemAdapter(mContext, BuildConfig.OR2GO_SERVER, mStoreInfo.getId(), mStoreInfo.getInventoryControl(),itemlist, R.layout.listview_ordercart_item, listener);
        mRecyclerView.setAdapter(mAdapter);

        subTotalVal = (TextView) findViewById(R.id.edordsubtotalval);//(TextView) findViewById(R.id.tvcartval);
        actualsubTotalVal = (TextView) findViewById(R.id.edordsubtotalactval);
        tvDiscountVal = (TextView) findViewById(R.id.tvorddiscval);
        exclTax = (TextView) findViewById(R.id.edordexcltaxval);
        tvDeliCharge = (TextView) findViewById(R.id.eddelicharge);
        tvGTotalVal = (TextView) findViewById(R.id.edordtotalval);
        tvAmountSave = (TextView) findViewById(R.id.edordsaveval);
        linearLayoutSave = (LinearLayout) findViewById(R.id.llordsave);
        linearLayoutCoupon = (LinearLayout) findViewById(R.id.llcouponinfo);
//        btSelCoupon = (Button) findViewById(R.id.btSelCoupon);
        tvCouponInfo = (TextView)findViewById(R.id.tvDiscountInfo);
        btCouponControl = (ImageView) findViewById(R.id.btCouponControl);
        mCouponList = gAppEnv.getDiscountManager().getAvailableCoupons(mCartMgr.getCurrentVendor());
        if (mCouponList != null) {
            for (int c = 0; c > mCouponList.size(); c++){
                gAppEnv.gAppSettings.removeSharePerUsedCoupon("coupon_key", mCouponList.get(c).mdName);
            }
        }
        actualsubTotalVal.setPaintFlags(actualsubTotalVal.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

        if (mCartMgr.isCouponSelected()) {
            DiscountInfo discinfo = mCartMgr.getSelectedCoupon();
            linearLayoutCoupon.setEnabled(false);
            tvCouponInfo.setText("Selected Coupon: " + discinfo.mdName);
            btCouponControl.setImageResource(R.drawable.ic_outline_cancel_24);
            setTotal();
        }
        else if (mCouponList != null)
            tvCouponInfo.setText("Available Coupons are: " + mCouponList.size());
        else
            linearLayoutCoupon.setEnabled(false);
        linearLayoutCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NewAddressDialog();
                couponSelectorDialog();
            }
        });
        btCouponControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCartMgr.isCouponSelected()) {
                    mCartMgr.clearSelectedCoupon();
                    setTotal();
                    if(SelectedCouponName == null)
                        SelectedCouponName = "";
                    tvCouponInfo.setText("Available Coupon: " + mCouponList.size());
                    btCouponControl.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24);
                    linearLayoutCoupon.setEnabled(true);
                }
                else if (mCouponList != null)
                    couponSelectorDialog();
            }
        });

        layoutoption = (LinearLayout) findViewById(R.id.lltypeinfo);
        layoutaddress = (LinearLayout) findViewById(R.id.lladdrinfo);
        layoutstoreaddress = (LinearLayout) findViewById(R.id.llstoraddrinfo);
        storeName = (TextView) findViewById(R.id.tvStoreAddrName);
        storeAddress = (TextView) findViewById(R.id.tvStoreAddr);
        storecontact = (TextView) findViewById(R.id.tvStoreContact);
        buttonCallStore = (Button) findViewById(R.id.btCallStore);
        buttonDirection = (Button) findViewById(R.id.btWayToStore);
        btNewAddr = (Button) findViewById(R.id.btCartNewAddr);
        btChangeAddr = (Button) findViewById(R.id.btChangeAddr);
        tvDeliAddrName = (TextView) findViewById(R.id.tvDeliAddrName);
        tvDeliAddr = (TextView) findViewById(R.id.tvDeliAddr);
        tvDeliContact = (TextView) findViewById(R.id.tvDeliContact);
        edInstr = (EditText) findViewById(R.id.edordinfoinstruct);
        storeName.setText(mStoreInfo.vName);
        storecontact.setText(mStoreInfo.vContact);
        mAddrInfoList = gAppEnv.getDeliveryManager().getAddrList();
        mDeliChargeViewModel = new ViewModelProvider(this).get(DeliveryChargeViewModel.class);
        final Observer<String> delichargeObserver = new Observer<String>() {
            @Override
            public void onChanged(String newcharge) {
                mCartMgr.setDeliveryChargeVal(Float.valueOf(newcharge));
                setTotal();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            if ( getOrderType == OR2GO_ORDERTYPE_DELIVERY && (Float.parseFloat(newcharge) < 0 || mCartMgr.getDeliveryChargeVal() < 0))
                                ShowAddressNotSuitable();
                        }catch (Exception e){
                            gAppEnv.getGposLogger().e("run: " + e);
                        }
                    }
                });
                thread.start();
            }
        };
        mDeliChargeViewModel.getDeliveryCharge().observe(this, delichargeObserver);
        tvchangeOrderType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectOrderTypeDialog();
            }
        });
        orderreqtime = (EditText) findViewById(R.id.eddelireqtime);
        orderreqtime.setEnabled(false);
        btClearTime = (ImageView) findViewById(R.id.imageViewClearTime);
        btEditReqTime = (ImageView)  findViewById(R.id.btEditDeliReqtime);
        btEditReqTime.setEnabled(true);
        btEditReqTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showOrderTimeSelectionDialog();
            }
        });
        btClearTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderreqtime.setText("");
                btClearTime.setVisibility(View.GONE);
            }
        });
        if (mCartMgr.getOrderType() == OR2GO_ORDERTYPE_DELIVERY) {
            DeliverySelected();
        }
        else if (mCartMgr.getOrderType() == OR2GO_ORDERTYPE_PICKUP) {
            PickupSelected();
        }
        else {
            btEditReqTime.setEnabled(true);
            layoutstoreaddress.setVisibility(View.GONE);
            layoutaddress.setVisibility(View.VISIBLE);
            layoutaddress.setBackgroundColor(Color.parseColor("#eeeeee"));
            btChangeAddr.setEnabled(false);
            btNewAddr.setEnabled(false);
        }
        buttonCallStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!storecontact.getText().toString().trim().equals("")){
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + mStoreInfo.vContact));
                        startActivity(callIntent);
                    }else{
                        Toast.makeText(mContext, "Contact not available", Toast.LENGTH_SHORT).show();
                    }
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(mContext, "" + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+mStoreInfo.getGeoLoc());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        if (mCartMgr.isDeliveryAddrNameSet()) {
            gAppEnv.getGposLogger().d("OrderCartActivity: delivery address name: "+mCartMgr.getDeliveryAddrName());
            mDeliLocationInfo = gAppEnv.getDeliveryManager().getAddrInfo(mCartMgr.getDeliveryAddrName());
            if (mDeliLocationInfo == null)
                gAppEnv.getGposLogger().d("OrderCartActivity: delivery address info not found: ERROR!!!");
            else {
                tvDeliAddrName.setText(mDeliLocationInfo.getAddrName());
                tvDeliAddr.setText(mDeliLocationInfo.getAddress() + " : " + mDeliLocationInfo.place);
                tvDeliContact.setText(mDeliLocationInfo.getAltcontact());
                gAppEnv.getGposLogger().d("OrderCartActivity: setting delivery to=" + mDeliLocationInfo.getAddrName());
                mCartMgr.updateDeliveryCharge(mDeliChargeViewModel);
            }
        }
        else
            setTotal();

        btNewAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deliaddractivity = new Intent(OrderCartActivity.this, NewAddressActivity.class);
                deliaddractivity.putExtra("caller", "cart" );
                startActivity(deliaddractivity);
            }
        });
        btChangeAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NewAddressDialog();
                addressSelectorDialog();
            }
        });

        if (gAppEnv.getSPManager().isClosedToday()) {
            if (!gAppEnv.getSPClosedNoticeDone()) {
                gAppEnv.setSPClosedNoticeDone(true);
                //publicNotice();
                PublicNoticeDialog noticediag = new PublicNoticeDialog(this, "ShutDown Notice", BuildConfig.APP_NAME+" is closed now. Sorry for the inconvenience.");
                noticediag.show();
            }
            else
                getSupportActionBar().setTitle(BuildConfig.APP_NAME+" - Service Closed");
        }

        if (gAppEnv.getSPManager().isClosedToday()) {
            if (!gAppEnv.getSPClosedNoticeDone()) {
                gAppEnv.setSPClosedNoticeDone(true);
                //publicNotice();
                PublicNoticeDialog noticediag = new PublicNoticeDialog(this, "ShutDown Notice", BuildConfig.APP_NAME+" is closed now. Sorry for the inconvenience.");
                noticediag.show();
            }
            else
                getSupportActionBar().setTitle(BuildConfig.APP_NAME+" - Service Closed");
        }

        boolean stockcheck = mCartMgr.validateStockAvailability();
        if (stockcheck == true)
            monitorStockCheck();
        String address = mStoreInfo.geolocation;
        String[] addressParts = address.split(",");
        String lati = addressParts[0].toString().trim();
        String longi = addressParts[1].toString().trim();
        LatLng latLng = new LatLng(Double.parseDouble(lati), Double.parseDouble(longi));
        storeaddress = getAddress(latLng);
        storeAddress.setText(storeaddress);
        if (mAddrInfoList.size() == 1 && mCartMgr.getOrderType() == OR2GO_ORDERTYPE_DELIVERY){
            mDeliLocationInfo = mAddrInfoList.get(0);
            tvDeliAddrName.setText(mAddrInfoList.get(0).getAddrName());
            tvDeliAddr.setText(mAddrInfoList.get(0).getAddress()+ " : "+mAddrInfoList.get(0).place);
            tvDeliContact.setText(mAddrInfoList.get(0).getAltcontact());
            //sDeliAddrName=data.nickname;
            //sDeliAddr = data.getAddress();
            //sDeliLoc = data.place;
            mDeliLocationInfo = mAddrInfoList.get(0);
            mCartMgr.setDeliveryAddrName(mAddrInfoList.get(0).nickname, mDeliChargeViewModel);
            gAppEnv.getGposLogger().d("OrderCartActivity: setting delivery to="+mAddrInfoList.get(0).nickname);
            mCartMgr.updateDeliveryCharge(mDeliChargeViewModel);
        }

        //Bottom Navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.cartnavigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.cartnavi_home:
                    Intent i = new Intent(OrderCartActivity.this, MainActivity.class);
                    startActivity(i);
                    return true;
                case R.id.cartnavi_clear:
                    mCartMgr.clearCart();
                    itemlist.clear();
                    mAdapter.notifyDataSetChanged();
                    startActivity(new Intent(OrderCartActivity.this, MainActivity.class));
                    return true;
                case R.id.cartnavi_add:
                    Intent intent = new Intent(OrderCartActivity.this, StoreProductsActivity.class);
                    intent.putExtra("vendorID", mStoreInfo.getId());
                    startActivity(intent);
                    return true;
                case R.id.cartnavi_order:
                    if (gAppEnv.getSPManager().isClosedToday())
                    {
                        Toast.makeText(mContext, "Server is closed now.", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    if (mCartMgr.isCartEmpty())
                    {
                        Toast.makeText(mContext, "The cart is empty!!", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    if (getOrderType == 0) {
                        Toast.makeText(mContext, "Please select order type.", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if(getOrderType == OR2GO_ORDERTYPE_DELIVERY) {
                        if (mAddrInfoList.size() <= 0) {
                            String title = "Add Address";
                            String body = "Please add profile address. It is required used for ordering.";
                            String positive = "OK";
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
                            customDailogView = new CustomDailogView(OrderCartActivity.this, title, body, positive, "", visible, onclick);
                            customDailogView.show();
                            return false;
                        }
                        if (mCartMgr.getDeliveryChargeVal() < 0)
                        {
                            String title = "Delivery Address Error";
                            String body = "Delivery to the given address is not supported. Please add a valid delivery address.";
                            String positive = "OK";
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
                            customDailogView = new CustomDailogView(OrderCartActivity.this, title, body, positive, "", visible, onclick);
                            customDailogView.show();
                            return false;
                        }

                    }
                    else if(getOrderType == OR2GO_ORDERTYPE_PICKUP)
                        mDeliLocationInfo = new DeliveryAddrInfo("", "", "", "", "", "", "", "");

                    if (mCartMgr.getStockCheckStatus() == CART_STOCK_CHECK_COMPLETE){
                        for(int j = 0; j < itemlist.size(); j++){
                            Integer stockValue = Math.round(itemlist.get(j).getCurStock());
                            if (mStoreInfo.getInventoryControl() > 0 && (stockValue <= 0 || (int) Float.parseFloat(itemlist.get(j).getQnty()) > stockValue)){
                                Toast.makeText(mContext, itemlist.get(j).getName() + " is out of Stock", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                    }

                    String vminord = mStoreInfo.getMinOrder();
                    if (Float.parseFloat(mCartMgr.getCartItemTotal()) < Float.parseFloat(vminord)) {
                        float remainAmt = Float.parseFloat(vminord) - Float.parseFloat(mCartMgr.getCartItemTotal());
                        String title = "Add more";
                        String body = "Order amount is less than " + currency.getSymbol() + Float.parseFloat(vminord) + " .Add " + currency.getSymbol() + remainAmt + " to place an order";
                        String positive = "OK";
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
                        customDailogView = new CustomDailogView(OrderCartActivity.this, title, body, positive, "", visible, onclick);
                        customDailogView.show();
//                        Toast.makeText(getApplicationContext(), "The order total amount is less than minimum order value of this vendor. Please add items.", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    if (!gAppEnv.isInternetOn())
                    {
                        String title = "Internet Connection";
                        String body = "No Internet Connection. Please connect to internet for online ordering.";
                        String positive = "OK";
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
                        customDailogView = new CustomDailogView(OrderCartActivity.this, title, body, positive, "", visible, onclick);
                        customDailogView.show();
//                        Toast.makeText(getApplicationContext(), "Please connect to internet for online operation.", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    if (!gAppEnv.isLoggedIn())
                    {
                        String title = "Register?";
                        String body = "Please register to order.";
                        String positive = "OK";
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
                        customDailogView = new CustomDailogView(OrderCartActivity.this, title, body, positive, "", visible, onclick);
                        customDailogView.show();
                        if (!gAppEnv.isRegistered())
                            RegisterDialog();
                        else
                            Toast.makeText(mContext, "Logging in.. Please order again after login is complete", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (mCartMgr.isStockNotAvailable(mStoreInfo.getInventoryControl())) {
                        Toast.makeText(mContext, "Some ordered items not available.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    boolean postres = postOrderRequest();
                    return true;
            }
            return false;
        }
    };

    private boolean postOrderRequest() {
        String sCustReq = edInstr.getText().toString();
        String reqtime=mOrderReqTime;
        String delicontact = "";
        if (mDeliLocationInfo.getAltcontact().isEmpty())
            delicontact = gAppEnv.gAppSettings.getUserName()+":"+gAppEnv.gAppSettings.getUserId();
        else
            delicontact= mDeliLocationInfo.getAltcontact();
        String deliaddr = mDeliLocationInfo.getAddress()+" Landmark-"+mDeliLocationInfo.getLandmark()+" PIN-"+mDeliLocationInfo.getZipCode()+" Contact-"+delicontact;
        final Integer reqid = mCartMgr.placeOrder(mDeliLocationInfo, sCustReq, reqtime,OR2GO_ORDER_ITEM_DETAIL_REGULAR);
        progressDialog = new ProgressDialog(OrderCartActivity.this, R.style.Theme_Or2goProgressDialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Placing order...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Handler mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            public void run() {
                elapsedTime +=1;
                if (mCartMgr.isOrderRequestComplete(reqid)) {
                    progressDialog.dismiss();
                    mProgressHandler.removeCallbacks(null);
                    gAppEnv.gAppSettings.setCouponUsed(SelectedCouponName,mStoreInfo.getId());
                    mCartMgr.clearCart();
                    startActivity(new Intent(OrderCartActivity.this,OrderListActivity.class));
                }
                else if (mCartMgr.isStockCheckComplete()){
                    startActivity(new Intent(OrderCartActivity.this,OrderCartActivity.class));
                }
                else {
                    if (elapsedTime > 50) {
                        mProgressHandler.removeCallbacks(null);
                        progressDialog.dismiss();
                        NetworkErrorDialog();
                    }
                    else
                        mProgressHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
        return true;
    }

    private void NetworkErrorDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CompatAlertDialogStyle);
        builder.setTitle("SERVER COMMUNICATION ERROR");
        builder.setMessage("The order can not be placed with the server beacuse of netwrok problem. Please try again.");
        builder.setCancelable(false);
        builder.setPositiveButton("Later",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton("Retry",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (gAppEnv.isInternetOn())
                            dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void deleteItem(int pos) {
        CartItem item = itemlist.get(pos);
        mCartMgr.deleteItem(item.getId(), item.getSKUId());
        mAdapter.notifyDataSetChanged();
        setTotal();
    }
    private void RegisterDialog() {
        final Dialog dialog = new Dialog(mContext, R.style.OtpDialog);
        dialog.setContentView(R.layout.dialog_register_msg);
        dialog.setTitle("Register");
        dialog.setCancelable(false);
        Button okButton = (Button) dialog.findViewById(R.id.btRegOk);
        Button cancelButton = (Button) dialog.findViewById(R.id.btRegCancel);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OrderCartActivity.this, UserRegisterActivity.class));
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setTotal() {
        if (mCartMgr.isCartEmpty()) {
            mCartMgr.clearCart();
            Toast.makeText(mContext, "Cart is empty!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrderCartActivity.this, MainActivity.class));
        }
        else {
            if (sendFloatValue(mCartMgr.getCartItemTotal()).equals("0.00"))
                subTotalVal.setText(currency.getSymbol() + Math.round(Float.parseFloat(mCartMgr.getCartItemTotal())));
            else
                subTotalVal.setText(currency.getSymbol() + mCartMgr.getCartItemTotal());
            tvDiscountVal.setText("- " + currency.getSymbol() + mCartMgr.getDiscountAmount().toString());
            exclTax.setText("+ " + currency.getSymbol() + mCartMgr.getExclusiveTaxTotal());
            if (mCartMgr.getOrderType() == OR2GO_ORDERTYPE_PICKUP && (mCartMgr.getDeliveryCharge().equals("0.0") || mCartMgr.getDeliveryCharge().equals("-1.0")))
                tvDeliCharge.setText("Free");
            else if (mCartMgr.getDeliveryChargeVal() < 0) {
                tvDeliCharge.setText("");
            }
            else
                tvDeliCharge.setText("+ " + currency.getSymbol()+mCartMgr.getDeliveryCharge());
            tvGTotalVal.setText(currency.getSymbol() + mCartMgr.getCartTotal());
            if(mCartMgr.getCartActualItemTotal() == ""){
                actualsubTotalVal.setVisibility(View.GONE);
                linearLayoutSave.setVisibility(View.GONE);
            }else{
                float actualAmount = Float.parseFloat(mCartMgr.getCartActualItemTotal());
                float payAmount = Float.parseFloat(mCartMgr.getCartItemTotal());
                if (sendFloatValue(mCartMgr.getCartActualItemTotal()).equals("0.00"))
                    actualsubTotalVal.setText(currency.getSymbol() + (int) actualAmount);
                else
                    actualsubTotalVal.setText(currency.getSymbol() + mCartMgr.getCartActualItemTotal());
                float valueSave = Float.parseFloat(mCartMgr.getCartActualItemTotal()) - Float.parseFloat(mCartMgr.getCartItemTotal());
                if (actualAmount > payAmount){
                    if (sendFloatValue(String.valueOf(valueSave)).equals("0.0"))
                        tvAmountSave.setText(currency.getSymbol() + String.valueOf(Math.round(valueSave) + mCartMgr.getDiscountAmount()));
                    else
                        tvAmountSave.setText(currency.getSymbol() + String.valueOf(valueSave + mCartMgr.getDiscountAmount()));
                }else{
                    actualsubTotalVal.setVisibility(View.GONE);
                    linearLayoutSave.setVisibility(View.GONE);
                }
            }
        }
    }
    public String sendFloatValue(String value){
        String ss = value;
        BigDecimal bigDecimal = new BigDecimal(ss);
        int i = bigDecimal.intValue();
        String ll = bigDecimal.subtract(new BigDecimal(i)).toPlainString();
        return ll;
    }

    private void ShowAddressNotSuitable() {
        if (mAddrInfoList.size() <= 1){
            String title = "Address Not Suitable !";
            String body = "Selected address is cleared for deliver. Please add new Address.";
            String positive = "Add New";
            String cancel = "Cancel";
            boolean visible = true;
            CustomDailogView.onClickButton onclick = new CustomDailogView.onClickButton() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.positive_Btn:
                            Intent deliaddractivity = new Intent(OrderCartActivity.this, NewAddressActivity.class);
                            deliaddractivity.putExtra("caller", "cart" );
                            startActivity(deliaddractivity);
                            customDailogView.dismiss();
                            break;
                        case R.id.negative_Btn:
                            PickupSelected();
                            customDailogView.dismiss();
                            break;
                    }
                }
            };
            customDailogView = new CustomDailogView(OrderCartActivity.this, title, body, positive, cancel, visible, onclick);
            customDailogView.show();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(OrderCartActivity.this);
            builder.setMessage("The address you provide is not suitable. Please change address or edit this address.");
            builder.setTitle("Address Not Suitable !");
            builder.setCancelable(false);
            builder.setPositiveButton("Choose Address", (DialogInterface.OnClickListener) (dialog, which) -> {
                addressSelectorDialog();
                dialog.cancel();
            });
            builder.setNegativeButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
                PickupSelected();
                dialog.cancel();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void PickupSelected() {
        gAppEnv.gAppSettings.setUseGeoDistance(false);
        btEditReqTime.setEnabled(true);
        layoutstoreaddress.setVisibility(View.VISIBLE);
        layoutaddress.setVisibility(View.GONE);
        layoutstoreaddress.setBackgroundColor(Color.parseColor("#eeeeee"));
        mCartMgr.setOrderType(OR2GO_ORDERTYPE_PICKUP);
        mCartMgr.clearDeliveryAddress();
        tvDeliAddrName.setText("");
        tvDeliAddr.setText("");
        tvDeliContact.setText("");
        setTotal();
        getOrderType = OR2GO_ORDERTYPE_PICKUP;
        tvorderType.setText("Pickup");
    }

    private void checkGeoUsedOrNot(String geoDistance) {
        if (gAppEnv.gAppSettings.getUseGeoDistance())
            ShowUseGeoDistanceFee(geoDistance);
    }

    private void showOrderTimeSelectionDialog() {
        new SingleDateAndTimePickerDialog.Builder(mContext)
                .minutesStep(30)
                .mustBeOnFuture()
                .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                    @Override
                    public void onDisplayed(SingleDateAndTimePicker picker) {
                        //retrieve the SingleDateAndTimePicker
                    }

                    @Override
                    public void onClosed(SingleDateAndTimePicker picker) {
                        // On dialog closed
                    }
                })

                .title("Select Delivery Time")
                .listener(new SingleDateAndTimePickerDialog.Listener() {
                    @Override
                    public void onDateSelected(Date date) {
                        DateFormat saveFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        DateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");
                        String delitime = displayFormat.format(date);
                        if (deliver){
                            orderreqtime.setText(delitime);
                            mOrderReqTime = saveFormat.format(date);
                            btClearTime.setVisibility(View.VISIBLE);
                        }else{
                            orderreqtime.setText("");
                            btClearTime.setVisibility(View.GONE);
                            mOrderReqTime = "";
                        }
                    }
                })
                .display();
    }

    private void bottomSheetDialogShow(int imagepath, String name, String qnty, String brandName, String pname, int id){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_edit_qnty_whole);
        bottomSheetDialog.setCancelable(false);
        TextView textViewTitle = (TextView) bottomSheetDialog.findViewById(R.id.textview_pName);
        TextView textViewQty = (TextView) bottomSheetDialog.findViewById(R.id.edittext_qty);
        ImageView close = (ImageView) bottomSheetDialog.findViewById(R.id.imageview_close);
        ImageView productimage = (ImageView) bottomSheetDialog.findViewById(R.id.imageview_proimage);
        ImageView buttonDecrease = (ImageView) bottomSheetDialog.findViewById(R.id.imageview_decrease);
        ImageView buttonIncrease = (ImageView) bottomSheetDialog.findViewById(R.id.imageview_increase);
        Button buttondelete = (Button) bottomSheetDialog.findViewById(R.id.button_delete);
        Button buttonupdate = (Button) bottomSheetDialog.findViewById(R.id.button_update);
        Float fqnty = Float.parseFloat(qnty);
        nQnty = fqnty.intValue();
        //load image
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.blankitem)
                .error(R.drawable.blankitem);
        if(imagepath == 0){
            Glide.with(mContext)
                    .load(BuildConfig.OR2GO_SERVER+"prodimage/"+prodNameToImagePath(brandName, pname) + ".jpg")
                    .apply(options)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(productimage);
        }else if (imagepath == 1){
            Glide.with(mContext)
                    .load(BuildConfig.OR2GO_SERVER+"vendorprodimage/"+mStoreInfo.getId()+"/"+id+ ".jpg")
                    .apply(options)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(productimage);
        }
        textViewTitle.setText(name);
        textViewQty.setText(nQnty.toString());
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nQnty>0) nQnty--;
                textViewQty.setText(nQnty.toString());
            }
        });
        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nQnty++;
                textViewQty.setText(nQnty.toString());
            }
        });
        buttondelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinishEditQntyDialog("0");
                bottomSheetDialog.dismiss();
            }
        });
        buttonupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinishEditQntyDialog(textViewQty.getText().toString());
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    private void couponSelectorDialog() {
        CouponSelectorAdapter mCouponAdapter = new CouponSelectorAdapter(mCouponList, R.layout.cardview_coupon_selector, this);
        mCouponDialog = new CouponSelectorDialog(OrderCartActivity.this, mCouponAdapter);
        mCouponDialog.setCanceledOnTouchOutside(false);
        mCouponDialog.show();
    }

    private String prodNameToImagePath(String brand, String name) {
        String lname = name.toLowerCase();
        String sname;
        if (brand != null && (!brand.isEmpty()) && (!brand.equals("null"))) {
            String lbrand = brand.toLowerCase();
            if (lname.contains(lbrand))
                sname=lname;
            else
                sname = lbrand + "_" + lname;
        }
        else
            sname = lname;
        String bname = sname.replace(" ", "_");
        String fname = bname.replace("&", "_");
        String rname = fname.replace(",", "_");
        return (rname);
    }

    private void monitorStockCheck() {
        progressDialog = new ProgressDialog(OrderCartActivity.this, R.style.Theme_Or2goProgressDialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Checking stock availability...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final Handler mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            public void run() {
                elapsedTime +=1;
                if (mCartMgr.isStockCheckComplete()) {
                    progressDialog.dismiss();
                    mProgressHandler.removeCallbacks(null);
                    for(int i=0;i<itemlist.size();i++) {
                        CartItem oitem = itemlist.get(i);
                    }
                    mAdapter.notifyDataSetChanged();
                }
                else if (mCartMgr.isStockCheckError()) {
                    mProgressHandler.removeCallbacks(null);
                    progressDialog.dismiss();
                    NetworkErrorDialog();
                }
                else {
                    if (elapsedTime > 100) {
                        mProgressHandler.removeCallbacks(null);
                        progressDialog.dismiss();
                        NetworkErrorDialog();
                    }
                    else
                        mProgressHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void addressSelectorDialog() {
        AddressSelectorAdapter mAdapter = new AddressSelectorAdapter(mAddrInfoList, R.layout.cardview_address_selector,this);
        mAddrDialog = new AddressSelectorDialog(OrderCartActivity.this, mAdapter);
        mAddrDialog.setCanceledOnTouchOutside(false);
        mAddrDialog.show();
    }

    private void ShowUseGeoDistanceFee(String distance) {
        String body = "Delivery charge set according to store address and customer address.";
        boolean visible = false;
        CustomDailogView.onClickButton onClick = new CustomDailogView.onClickButton() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.positive_Btn:
                        gAppEnv.gAppSettings.setUseGeoDistance(false);
                        customdailogview.dismiss();
                        break;
                    default:
                        customdailogview.dismiss();
                        break;
                }
            }
        };
        customdailogview = new CustomDailogView(mContext, "Delivery Charge", body, "ok", "", visible, onClick);
        customdailogview.show();
    }

    private String getAddress(LatLng latLng) {
        String storeAddress = "";
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try{
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            storeAddress = addresses.get(0).getAddressLine(0);
        }catch (Exception e){
            e.printStackTrace();
        }
        return storeAddress;
    }

    private void DeliverySelected(){
        tvorderType.setText("Delivery");
        getOrderType = OR2GO_ORDERTYPE_DELIVERY;
        btEditReqTime.setEnabled(true);
        layoutstoreaddress.setVisibility(View.GONE);
        layoutaddress.setVisibility(View.VISIBLE);
        layoutaddress.setBackgroundColor(Color.parseColor("#eeeeee"));
        btChangeAddr.setEnabled(true);
        btNewAddr.setEnabled(true);
        mCartMgr.setOrderType(OR2GO_ORDERTYPE_DELIVERY);
        if(!mCartMgr.getDeliveryAddrName().isEmpty()) {
            mCartMgr.updateDeliveryCharge(mDeliChargeViewModel);
            setTotal();
        }
        else {
            mCartMgr.setDeliveryChargeVal(Float.valueOf("-1"));
            setTotal();
        }
    }

    private void showSelectOrderTypeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dailog_select_order_type);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        RadioGroup radioGroupDT = (RadioGroup)dialog.findViewById(R.id.radioGroup);;
        MaterialButton okay_button = dialog.findViewById(R.id.material_button_OK);
        okay_button.setEnabled(false);
        radioGroupDT.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radiobuttonP) {
                    getOrderType = OR2GO_ORDERTYPE_PICKUP;
                    okay_button.setEnabled(true);
                }
                else if (checkedId == R.id.radiobuttonD) {
                    getOrderType = OR2GO_ORDERTYPE_DELIVERY;
                    okay_button.setEnabled(true);
                }
            }
        });
        okay_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getOrderType == OR2GO_ORDERTYPE_PICKUP){
                    PickupSelected();
                    dialog.dismiss();
                }
                else{
                    DeliverySelected();
                    addressSelectorDialog();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =  intent.getAction();
            gAppEnv.getGposLogger().d(gAppEnv.gAppSettings.getGeoTotalDistance() + "Geo Distance: "+action);
            geoTotalDistance = gAppEnv.gAppSettings.getGeoTotalDistance();
            if (customdailogview == null){
                checkGeoUsedOrNot(gAppEnv.gAppSettings.getGeoTotalDistance());
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, new IntentFilter("UsedGeoCode"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, new IntentFilter("updateDistance"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void clickOnItem(DeliveryAddrInfo data) {
        mDeliLocationInfo = data;
        tvDeliAddrName.setText(data.getAddrName());
        tvDeliAddr.setText(data.getAddress()+ " : "+data.place);
        tvDeliContact.setText(data.getAltcontact());
        mDeliLocationInfo = data;
        mCartMgr.setDeliveryAddrName(data.nickname, mDeliChargeViewModel);
        gAppEnv.getGposLogger().d("OrderCartActivity: setting delivery to="+data.nickname);
        mCartMgr.updateDeliveryCharge(mDeliChargeViewModel);
        if (mAddrDialog != null){
            mAddrDialog.dismiss();
            mAddrDialog = null;
            tvorderType.setText("Delivery");
        }
    }

    @Override
    public void onCouponSelection(DiscountInfo coupon) {
        if (coupon != null) {
            gAppEnv.getGposLogger().i(" Coupon Selected="+coupon.mdName);
            if ((coupon.minsaleamnt != null) && (Float.valueOf(mCartMgr.getCartItemTotal()) < coupon.minsaleamnt)) {
                String title = "Cart";
                String body = "Cart amount is less than the required coupon minimum amount.";
                String positive = "OK";
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
                customDailogView = new CustomDailogView(OrderCartActivity.this, title, body, positive, "", visible, onclick);
                customDailogView.show();
            }
            else {
                mCartMgr.setSelectedCoupon(coupon);
                setTotal();
                tvCouponInfo.setText("Selected Coupon: " + coupon.mdName);
                if (coupon.mdScope == 2){
                    SelectedCouponName = coupon.mdName;
                }
                btCouponControl.setImageResource(R.drawable.ic_outline_cancel_24);
                linearLayoutCoupon.setEnabled(false);
            }
        }
        if (mCouponDialog != null){
            mCouponDialog.dismiss();
            mCouponDialog = null;
        }
    }

    @Override
    public void onFinishEditQntyDialog(String qnty) {
        gAppEnv.getGposLogger().i(" Edit cart item quantity to="+qnty);
        Float nqnty = Float.parseFloat(qnty);
        if (nEditQntyPos < 0) return;;
        CartItem edititem = itemlist.get(nEditQntyPos);
        if (nqnty == 0)
            deleteItem(nEditQntyPos);
        else {
            edititem.setQnty(nqnty);
            mCartMgr.saveItemQnty(edititem.getId().toString(), edititem.getSKUId(), qnty);
            mCartMgr.updateSubTotal();
            if (mCartMgr.isCouponSelected()) {
                mCartMgr.recalculateCoupon();
                if (!mCartMgr.isCouponSelected()) {
                    tvCouponInfo.setText("Applied Coupon: " + mCouponList.size());
                    btCouponControl.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24);
                    linearLayoutCoupon.setEnabled(true);
                }
            }
            mAdapter.notifyDataSetChanged();
        }
        setTotal();
        nEditQntyPos = -1;
    }
}