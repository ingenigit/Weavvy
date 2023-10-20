package com.or2go.weavvy.activity;

import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_DETAILS;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_EXT_NEFT;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_EXT_UPI;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_EXT_WALLET;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_EXTPAY_CONFIRMATION_REQ;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_FAILED_ONLINE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_LOCAL_COMPLETE;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_STATUS_ONLINE_COMPLETE_APP;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_FORCE_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_REJECTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.or2go.adapter.OrderDetailsItemAdapter;
import com.or2go.adapter.PaymentSelectorAdapter;
import com.or2go.core.Or2GoStore;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.OrderItem;
import com.or2go.core.PaymentMethodInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.CustomDailogView;
import com.or2go.weavvy.OrderListDividerDecoration;
import com.or2go.weavvy.PaymentSelectorDialog;
import com.or2go.weavvy.R;
import com.or2go.weavvy.RazorPayHelper;
import com.or2go.weavvy.server.OrderDetailsCallback;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity implements PaymentResultWithDataListener,
        PaymentSelectorAdapter.RecyclerViewItemClickListener{

    Context mContext;
    AppEnv gAppEnv;
    String mVendorName="";
    String  curorderid, rejectItems;
    String notifymsg;
    Or2goOrderInfo moOr2goInfo;
    TextView iordsts;
    TextView iordtime;
    TextView iordtype;
    TextView ireqtime;
    TextView iordsubtotal;
    TextView iordtotal;
    TextView idelcharge;
    TextView idiscount;
    TextView idtax;
    TextView iordaddrname;
    TextView iordaddr;
    TextView icustreq;
    TextView ipaysts;
    LinearLayout linearLayoutOTP, linearLayouttime, linearLayoutagent, linearLayoutDAddress;
    TextView pickupotp;
    Button btPay;
    boolean GPS;
    TextView idelitime;
    TextView ideliman;
    ImageView icalldeli;
    ArrayList<OrderItem> itemlist;
    CustomDailogView customDailogView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    String mDeliAddrName="";
    BottomSheetDialog bottomSheetDialog;
    BottomNavigationView btNavigation;
    PaymentSelectorDialog mPaymentSelectorDialog=null;
    ArrayList<PaymentMethodInfo> mPayMethodList;
    boolean isPaymentInProgress;
    private String sUpiTransactionMsg="";
    Currency currency;
    SwipeRefreshLayout refreshLayout;
    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");;//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext, "Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrderDetailsActivity.this, MainActivity.class));
        }
        currency = Currency.getInstance("INR");
        isPaymentInProgress = false;

        if (gAppEnv.getOrderManager() == null) {
            gAppEnv.ShutdownAppEnv();
            Intent i = new Intent(OrderDetailsActivity.this, MainActivity.class);
            startActivity(i);
            return;
        }
        curorderid = getIntent().getStringExtra("orderid");
        rejectItems = getIntent().getStringExtra("rejres");
        if (curorderid.isEmpty()) {
            gAppEnv.getGposLogger().d("OrderDetails : !!!!  ordid error !!!!!" + curorderid);
            Intent i = new Intent(OrderDetailsActivity.this, OrderListActivity.class);
            startActivity(i);
            return;
        } else
            gAppEnv.getGposLogger().d("OrderDetails : ordid=" + curorderid);

        moOr2goInfo = gAppEnv.getOrderManager().findOrder(curorderid);
        if (moOr2goInfo == null) {
            gAppEnv.getGposLogger().d("OrderDetails : !!!!  ordid error !!!!!" + curorderid);
            Intent i = new Intent(OrderDetailsActivity.this, OrderListActivity.class);
            startActivity(i);
            return;
        }

        mVendorName = null;
        gAppEnv.getNotificationManager().clearAllNotifications();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.toolbar_order_details);

        View barview = getSupportActionBar().getCustomView();
        TextView bartitle = (TextView) barview.findViewById(R.id.order_details_bar_title);//
        TextView barinfo = (TextView) barview.findViewById(R.id.order_details_bar_info1);//
        String title = "Order Id #";
        bartitle.setText(title + moOr2goInfo.oOr2goId);
        Or2GoStore mStoreInfo = gAppEnv.getStoreManager().getStoreById(moOr2goInfo.oStoreId);
        barinfo.setText(mStoreInfo.getName());

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        iordsts = (TextView) findViewById(R.id.tvstatusdata);
        iordtime = (TextView) findViewById(R.id.tvtimedata);
        iordtype = (TextView) findViewById(R.id.tvordtypedata);
        iordsubtotal = (TextView) findViewById(R.id.tvsubtotaldata);
        iordtotal = (TextView) findViewById(R.id.tvtotaldata);
        idelcharge = (TextView) findViewById(R.id.tvdeliverychargedata);
        idiscount = (TextView) findViewById(R.id.tvdiscountdata);
        idtax = (TextView) findViewById(R.id.tvtaxdata);
        iordaddrname = (TextView) findViewById(R.id.tvaddrname);
        iordaddr = (TextView) findViewById(R.id.tvaddrdata);
        linearLayoutDAddress = (LinearLayout) findViewById(R.id.deliveryAddressLL);
        icustreq = (TextView) findViewById(R.id.tvcustreqdata);
        ipaysts = (TextView) findViewById(R.id.tvpaystsdata);
        btPay = (Button)findViewById(R.id.btPayOrder);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.reqrefresh);
        linearLayoutOTP = (LinearLayout) findViewById(R.id.pickupOTP);
        linearLayouttime = (LinearLayout) findViewById(R.id.deliTime);
        linearLayoutagent = (LinearLayout) findViewById(R.id.deliAgent);
        pickupotp = (TextView) findViewById(R.id.OTP);
        if (moOr2goInfo.getType() == 2){
            linearLayoutOTP.setVisibility(View.VISIBLE);
            pickupotp.setText(moOr2goInfo.getPickupOTP());
            linearLayouttime.setVisibility(View.GONE);
            linearLayoutagent.setVisibility(View.GONE);
            linearLayoutDAddress.setVisibility(View.GONE);
            iordaddr.setVisibility(View.GONE);
        }else{
            linearLayoutOTP.setVisibility(View.GONE);
            linearLayouttime.setVisibility(View.VISIBLE);
            linearLayoutagent.setVisibility(View.VISIBLE);
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                gAppEnv.getOrderManager().getActiveOrders();
                Intent intent = new Intent(OrderDetailsActivity.this, OrderDetailsActivity.class);
                intent.putExtra("orderid",moOr2goInfo.oOr2goId );
                intent.putExtra("orderstatus", moOr2goInfo.oStatus );
                startActivity(intent);
                finish();
                refreshLayout.setRefreshing(false);
            }
        });

        idelitime = (TextView) findViewById(R.id.tvdelitimedata);
        ideliman = (TextView) findViewById(R.id.tvdelimandata);
        icalldeli = (ImageView) findViewById(R.id.ivdelicall);
        if (!moOr2goInfo.getAddress().isEmpty())
            mDeliAddrName = gAppEnv.getDeliveryManager().getAddrNameFromAddr(moOr2goInfo.getAddress());

        if (!moOr2goInfo.getDAContact().equals(""))
            icalldeli.setVisibility(View.VISIBLE);
        setOrderInfo();

        mRecyclerView = (RecyclerView) findViewById(R.id.orderitemlist);
        itemlist = moOr2goInfo.getItemList();
        // Define a layout for RecyclerView
        mLayoutManager = new GridLayoutManager(mContext,1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new OrderListDividerDecoration(this, LinearLayoutManager.VERTICAL, 16));

        OrderDetailsItemAdapter.RecyclerViewClickListener listener = new OrderDetailsItemAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position >= 0) {
                    gAppEnv.getGposLogger().i("order list item clicked");
                }
            }
        };

        mAdapter = new OrderDetailsItemAdapter(mContext,itemlist, R.layout.listview_orderdetails_item, listener);
        mRecyclerView.setAdapter(mAdapter);
        //Digital Payment
        if (moOr2goInfo.isCancellable()) btPay.setEnabled(false);
        btPay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (moOr2goInfo.isCancellable()) {
                    String title = "Not Confirm !";
                    String body = "";
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
                    customDailogView = new CustomDailogView(OrderDetailsActivity.this, title, body, positive, "", visible, onclick);
                    customDailogView.show();
                }
                else if ((moOr2goInfo.getPayStatus() == OR2GO_PAY_STATUS_ONLINE_COMPLETE_APP) || (moOr2goInfo.getPayStatus() == OR2GO_PAY_STATUS_COMPLETE)) {
                    String title = "Payment";
                    String body = "Your order payment is already done!!";
                    String positive = "Ok";
                    boolean visible = false;
                    CustomDailogView.onClickButton onclick = new CustomDailogView.onClickButton() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.positive_Btn:
                                    customDailogView.dismiss();
                                    break;
                                case R.id.negative_Btn:
                                    customDailogView.dismiss();
                            }
                        }
                    };
                    customDailogView = new CustomDailogView(OrderDetailsActivity.this, title, body, positive, "", visible, onclick);
                    customDailogView.show();
                }
                else {
                    startRazorpayProcess();
                }
            }
        });

        btNavigation = (BottomNavigationView) findViewById(R.id.salesdetailsbottomnavi);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (moOr2goInfo.getStatus() == ORDER_STATUS_COMPLETE) {
            bottomSheetDialogShow(0);
        }
        else if (moOr2goInfo.getStatus() == ORDER_STATUS_CANCELLED) {
        }
        else if ((moOr2goInfo.getStatus() == ORDER_STATUS_REJECTED) || (moOr2goInfo.getStatus() == ORDER_STATUS_FORCE_CANCELLED)) {
            orderRejectDialog();
        }

        icalldeli.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + moOr2goInfo.getDAContact()));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Log.e("Calling a Phone Number", "Call failed", activityException);
                }
            }
        });

        if (itemlist.size() == 0) {
            gAppEnv.getGposLogger().i("calling order details API");
            Message msg = new Message();
            msg.what = OR2GO_ORDER_DETAILS;    //fixed value for sending sales transaction to server
            msg.arg1 = 0;

            OrderDetailsCallback ordercb = new OrderDetailsCallback(mContext, gAppEnv.getStoreManager().getProductManager(moOr2goInfo.oStoreId));
            ordercb.setViewAdapter(itemlist, (OrderDetailsItemAdapter) mAdapter);
            Bundle b = new Bundle();
            b.putString("orderid", moOr2goInfo.getId());
            b.putParcelable("callback", ordercb);
            msg.setData(b);
            gAppEnv.getCommMgr().postMessage(msg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent nextact;
                if (gAppEnv.getOrderManager().getOrderCount() > 1)
                    nextact= new Intent(OrderDetailsActivity.this,OrderListActivity.class);
                else
                    nextact = new Intent(OrderDetailsActivity.this, MainActivity.class);
                startActivity(nextact);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected( MenuItem item) {
            switch (item.getItemId()) {
                case R.id.detailsnavi_home:
                    if (!isPaymentInProgress)
                        startActivity(new Intent(OrderDetailsActivity.this, MainActivity.class));
                    return true;
                case R.id.detailsnavi_cancel:
                    if (isPaymentInProgress) return true;
                    if (moOr2goInfo.isCancellable()) {
                        OrderCancelDialog();
                    }
                    else {
                        Toast.makeText(mContext, "Confirmed Order can not be cancelled." , Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case R.id.detailsnavi_track:
                    if (isPaymentInProgress) return true;
                    Intent dalocactivity = new Intent(OrderDetailsActivity.this, OrderTrackActivity.class);
                    dalocactivity.putExtra("orderId", moOr2goInfo.oOr2goId);
                    dalocactivity.putExtra("orderStatus", moOr2goInfo.getStatus());
                    dalocactivity.putExtra("orderType", moOr2goInfo.getType());
                    dalocactivity.putExtra("totalItem", moOr2goInfo.getItemCount());
                    dalocactivity.putExtra("totalAmount", moOr2goInfo.getTotal());
                    startActivity(dalocactivity);
                    return true;
            }
            return false;
        }
    };

    private void statusCheck() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GPS = false;
            buildAlertMessageNoGps();
        }else
            GPS = true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void setOrderInfo() {
        String ordtime="";
        try {
            Date date = inputFormat.parse(moOr2goInfo.getOrderTime());
            ordtime = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        gAppEnv.getGposLogger().i( "Order Request orderid:" + " orig order time"+moOr2goInfo.getOrderTime()+"formatted order time="+ordtime);
        gAppEnv.getGposLogger().d("OrderDetails : Order status="+moOr2goInfo.oStatus+ "  text="+moOr2goInfo.getStatusText());
        iordtime.setText(ordtime);
        iordsts.setText(moOr2goInfo.getStatusText());
        iordtype.setText(moOr2goInfo.getOrderTypeDescription());
        iordtotal.setText(currency.getSymbol()+moOr2goInfo.getTotal());
        idelcharge.setText("+" + currency.getSymbol()+moOr2goInfo.getDeliveryCharge());
        idiscount.setText("-" + currency.getSymbol()+moOr2goInfo.getDiscount());
        idtax.setText("+" + currency.getSymbol()+moOr2goInfo.getTax());
        if (sendFloatValue(moOr2goInfo.getSubTotal()).equals("0.0"))
            iordsubtotal.setText(currency.getSymbol()+ Math.round(Float.parseFloat(moOr2goInfo.getSubTotal())));
        else
            iordsubtotal.setText(currency.getSymbol()+moOr2goInfo.getSubTotal());

        if (!moOr2goInfo.isCancellable()) btPay.setEnabled(true);

        if (moOr2goInfo.getPayStatus() == OR2GO_PAY_STATUS_COMPLETE) {
            ipaysts.setText(moOr2goInfo.getPayStatusText() + " " + mContext.getResources().getString(R.string.sym_tick));
            ipaysts.setTextColor(getResources().getColor(R.color.colorFontGreen));
            btPay.setEnabled(false);
        }else
            ipaysts.setText(moOr2goInfo.getPayStatusText());

        iordaddrname.setText(mDeliAddrName);
        iordaddr.setText(moOr2goInfo.getAddress()+" , "+moOr2goInfo.getLocation());
        icustreq.setText(moOr2goInfo.getCustReq());
        gAppEnv.getGposLogger().d("OrderDetailsActivity : Order request=" + moOr2goInfo.getCustReq());
        if (moOr2goInfo.isOnDelivery()){
            idelitime.setText(moOr2goInfo.getoDeliveryTime());
            gAppEnv.getGposLogger().d("OrderDetailsActivity : Order ON DELIVERY=" +moOr2goInfo.getDAName()+ " : "+moOr2goInfo.getDAContact());
            ideliman.setText(moOr2goInfo.getDAName() + " : "+ moOr2goInfo.getDAContact());
            icalldeli.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_local_phone_24));
        }
        else {
            idelitime.setText("NA");
            ideliman.setText("NA");
            icalldeli.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.blank_24));
            icalldeli.setEnabled(false);
        }
    }

    public String sendFloatValue(String value){
        BigDecimal bigDecimal = new BigDecimal(value);
        int i = bigDecimal.intValue();//180
        String ll = bigDecimal.subtract(new BigDecimal(i)).toPlainString();
        return ll;
    }

    private void bottomSheetDialogShow(int i) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);
        bottomSheetDialog.setCancelable(false);
        TextView textViewTitle = (TextView) bottomSheetDialog.findViewById(R.id.tv_order_Title);
        Button buttonfedback = (Button) bottomSheetDialog.findViewById(R.id.buttonRate);
        Button buttonclose = (Button) bottomSheetDialog.findViewById(R.id.btClose);
        if (i == 0)
            textViewTitle.setText(R.string.order_completed);
        else if (i == 1)
            textViewTitle.setText(R.string.order_cancelled);
        else if (i == 2)
            textViewTitle.setText(R.string.order_declined);
        buttonfedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetailsActivity.this, FeedbackActivity.class);
                intent.putExtra("orderid", curorderid);
                startActivity(intent);
                bottomSheetDialog.dismiss();
            }
        });
        buttonclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete the order from order manager ...no more use of it
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action =  intent.getAction();
            String updatetype = intent.getStringExtra("update");
            gAppEnv.getGposLogger().d("OrderDetails Broadcast Receiver: message from Order Manager  Update Type="+updatetype);
            setOrderInfo();
            itemlist = moOr2goInfo.getItemList();
            mAdapter.notifyDataSetChanged();
            gAppEnv.getNotificationManager().clearAllNotifications();//clearNotification(curorderid);
            if (moOr2goInfo.getStatus() == ORDER_STATUS_COMPLETE) {
                gAppEnv.getOrderManager().completeOrder(moOr2goInfo);//save to history
                bottomSheetDialogShow(0);
            }
            else if (moOr2goInfo.getStatus() == ORDER_STATUS_CANCELLED) {
            }
            else if (moOr2goInfo.getStatus() == ORDER_STATUS_REJECTED) {
                orderRejectDialog();
            }
            else if (moOr2goInfo.getStatus() == ORDER_STATUS_FORCE_CANCELLED) {
                orderRejectDialog();
            }
//            else if (moOr2goInfo.isActionRequired()) {
//                Intent ordintent =  new Intent(OrderDetailsActivity.this, OrderActionActivity.class);
//                ordintent.putExtra("orderid", moOr2goInfo.getId()); //curorderid
//                startActivity(ordintent);
//            }
            if ((updatetype!= null) && (updatetype.equals("paystatus"))) {
                if (moOr2goInfo.getPayStatus() == OR2GO_PAY_STATUS_COMPLETE) {
                    ipaysts.setText(moOr2goInfo.getPayStatusText() + " " + mContext.getResources().getString(R.string.sym_tick));
                    ipaysts.setTextColor(getResources().getColor(R.color.colorFontGreen));
                    btPay.setEnabled(false);
                }
                else
                    ipaysts.setText(moOr2goInfo.getPayStatusText());
            }
        }
    };
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
                startActivity(new Intent(OrderDetailsActivity.this,MainActivity.class));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void OrderCancelDialog() {

        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_order_cancel);
        dialog.setTitle("Order cancellation");
        ImageView cancelimage = (ImageView) dialog.findViewById(R.id.cancel_ixon);
        Button okButton = (Button) dialog.findViewById(R.id.btOrdRCancel);
        Button cancelButton = (Button) dialog.findViewById(R.id.btOrdOk);
        cancelimage.setColorFilter(Color.rgb(255,0,0));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gAppEnv.getOrderManager().cancelOrder(moOr2goInfo.getId(), moOr2goInfo.getStoreId());
                dialog.dismiss();
                gAppEnv.getOrderManager().completeOrder(moOr2goInfo);
                gAppEnv.getOrderHistoryManager().broadcastStatusUpdate();
                startActivity(new Intent(OrderDetailsActivity.this, OrderListActivity.class));
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

    private void startRazorpayProcess() {
        isPaymentInProgress =true;
        RazorPayHelper payHelper = new RazorPayHelper(mContext,gAppEnv, OrderDetailsActivity.this, moOr2goInfo);
        payHelper.startPayment();
    }

    private void extPayDialog() {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_ext_payment);
        dialog.setTitle("Payment Confirmation");
        final EditText edPayId = (EditText) dialog.findViewById(R.id.edExtPayId);
        final Spinner sppaymethod = (Spinner) dialog.findViewById(R.id.spExtPayMethod);
        List<String> paymethodlist = new ArrayList<String>();
        paymethodlist.add(getString(R.string.pay_method_UPI));
        paymethodlist.add(getString(R.string.pay_method_wallet));
        paymethodlist.add(getString(R.string.pay_method_netbank));
        final ArrayAdapter<String> adp = new ArrayAdapter<String>(OrderDetailsActivity.this, android.R.layout.simple_spinner_item,paymethodlist);
        sppaymethod.setAdapter(adp);

        Button okButton = (Button) dialog.findViewById(R.id.btExtPayOk);
        Button cancelButton = (Button) dialog.findViewById(R.id.btExtPayCancel);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer paymode=OR2GO_PAY_MODE_EXT_UPI;
                int selidx = sppaymethod.getSelectedItemPosition();
                if (selidx == 0)
                    paymode=OR2GO_PAY_MODE_EXT_UPI;
                else if (selidx==1)
                    paymode=OR2GO_PAY_MODE_EXT_WALLET;
                else if (selidx == 2) //(sppaymethod.getSelectedItem().equals("NetBanking"))
                    paymode=OR2GO_PAY_MODE_EXT_NEFT;

                moOr2goInfo.setPayStatus(OR2GO_PAY_STATUS_EXTPAY_CONFIRMATION_REQ);
                gAppEnv.getOrderManager().orderPaymentstatusUpdate(moOr2goInfo.getId(), moOr2goInfo.getStatus(), OR2GO_PAY_STATUS_EXTPAY_CONFIRMATION_REQ, OR2GO_PAY_MODE_EXT_UPI,edPayId.getText().toString());

                ipaysts.setText(moOr2goInfo.getPayStatusText());

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gAppEnv.getOrderManager().orderRespondDeliveryCharge(moOr2goInfo.getId(), false);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerStatusUpdateReceiver();
    }

    private void registerStatusUpdateReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("ORDER_STATUS_UPDATE"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterStatusUpdateReceiver();
    }

    private void unregisterStatusUpdateReceiver() {
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void onPaymentSuccess(String razorpayPaymentID,  PaymentData paymentData) {
        isPaymentInProgress = false;
        try {
            JSONObject paymentobject = paymentData.getData();
            gAppEnv.getGposLogger().i("RazorPay Payment successfull: result data:" + paymentobject.toString());

            String resOrderId = paymentData.getOrderId();
            String resSignature = paymentData.getSignature();
            gAppEnv.getGposLogger().i("RazorPay Payment successfull: orderid:" + resOrderId+ " signature:"+resSignature);
            //Toast.makeText(mActivity, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

            moOr2goInfo.setPayStatus(OR2GO_PAY_STATUS_LOCAL_COMPLETE);
            gAppEnv.getOrderManager().orderPaymentComplete(moOr2goInfo.getId(), OR2GO_PAY_STATUS_COMPLETE, razorpayPaymentID);

            ipaysts.setText(moOr2goInfo.getPayStatusText()+" "+ mContext.getResources().getString(R.string.sym_tick));
            ipaysts.setTextColor(getResources().getColor(R.color.colorFontGreen));
            btPay.setEnabled(false);
            PaymentResultDialog(true);
        } catch (Exception e) {
            Log.e("RazorPay", "Exception in onPaymentSuccess", e);
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        isPaymentInProgress = false;
        try {
            JSONObject paymentobject = paymentData.getData();
            //Log.i("RazorPay", "Payment failure: result data:" + paymentobject.toString());
            //Toast.makeText(mActivity, "Payment failed: " + code + " " + response, Toast.LENGTH_SHORT).show();

            moOr2goInfo.setPayStatus(OR2GO_PAY_STATUS_FAILED_ONLINE);

            ipaysts.setText(moOr2goInfo.getPayStatusText());
            PaymentResultDialog(false);
        } catch (Exception e) {
            Log.e("RazorPay", "Exception in onPaymentError", e);
        }
    }

    private void PaymentResultDialog(boolean result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CompatAlertDialogStyle);
        builder.setTitle("DIGITAL PAYMENT");

        if (result)
            builder.setMessage("Your payment for is successful. Thank you!");
        else
            builder.setMessage("Online payment failed.!!! Please verify.");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();

    }

    @Override
    public void clickOnItem(PaymentMethodInfo data) {
        mPaymentSelectorDialog.dismiss();
        if (data.sMethod.equals("Digital Payment")) {
            startRazorpayProcess();
        }
        else {
            extPayDialog();
        }
    }
}