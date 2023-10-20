package com.or2go.weavvy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.or2go.core.Or2GoStore;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderListViewHolder>{

    private Context mContext;
    public AppEnv gAppEnv;
    ArrayList<Or2goOrderInfo> mOrderList;
    RecyclerViewClickListener mListener;
    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//("yyyy-MM-dd HH:mm:ss");
    DateFormat outputFormat = new SimpleDateFormat("EEE, dd/MMM/yyyy");
    DateFormat outputFormatTimeOnly = new SimpleDateFormat("h:mm a");

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    public class OrderListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private RecyclerViewClickListener mListener;
        public TextView ordno, ordstatus, orddate, ordtime, ordvendor;
        public OrderListViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            ordno = (TextView) view.findViewById(R.id.ordno);
            ordstatus = (TextView) view.findViewById(R.id.ordsts);
            orddate = (TextView) view.findViewById(R.id.orddate);
            ordtime = (TextView) view.findViewById(R.id.ordtime);
            ordvendor = (TextView) view.findViewById(R.id.ordvendor);
            mListener = listener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(view, getAdapterPosition());
        }
    }


    public OrderListAdapter(Context gAppEnv, Context context, ArrayList<Or2goOrderInfo> orderList, RecyclerViewClickListener listener)
    {
        this.gAppEnv = (AppEnv) gAppEnv;
        this.mContext = context;
        this.mOrderList = orderList;

        this.mListener = listener;
    }

    @Override
    public OrderListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.listview_orderlist_item, parent, false);
        return new OrderListViewHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(final OrderListViewHolder holder, int position) {
        Or2goOrderInfo oritem = mOrderList.get(position);
        Log.i("OrderListAdapter"," order vendor = "+oritem.getVendorName()+ " vid"+oritem.oStoreId);
        holder.ordno.setText(oritem.getId());
        holder.ordstatus.setText(oritem.getStatusText());
        if (Objects.equals(oritem.getStatusText(), "Order Confirmed"))
            holder.ordstatus.setTextColor(Color.parseColor("#367E18"));
        else if (Objects.equals(oritem.getStatusText(), "Order Canceled"))
            holder.ordstatus.setTextColor(Color.parseColor("#CC3636"));
        else if (Objects.equals(oritem.getStatusText(), "Order is Ready"))
            holder.ordstatus.setTextColor(Color.parseColor("#fcc874"));
        else
            holder.ordstatus.setTextColor(Color.GRAY);
        Or2GoStore mStoreInfo = gAppEnv.getStoreManager().getStoreById(oritem.oStoreId);
        holder.ordvendor.setText(mStoreInfo.getName());
        try{
            Date date = inputFormat.parse(oritem.getOrderTime());
            holder.orddate.setText(outputFormat.format(date));
            holder.ordtime.setText(outputFormatTimeOnly.format(date));
        }catch(ParseException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }
}
