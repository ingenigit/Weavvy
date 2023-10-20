package com.or2go.weavvy.adapter;

import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CANCELLED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_REJECTED;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.or2go.core.Or2GoStore;
import com.or2go.core.OrderHistoryInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {
    public AppEnv gAppEnv;
    private Context mContext;
    ArrayList<OrderHistoryInfo> mOrderList;
    RecyclerViewClickListener mListener;
    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//("yyyy-MM-dd HH:mm:ss");
    DateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy h:mm a");

    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    public OrderHistoryAdapter(Context context, Context mContext, ArrayList<OrderHistoryInfo> orderList, RecyclerViewClickListener listener) {
        this.gAppEnv = (AppEnv) context;
        this.mContext = mContext;
        this.mOrderList = orderList;
        this.mListener = listener;
    }

    @Override
    public OrderHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.listview_h_orderlist_items, parent, false);
        return new OrderHistoryViewHolder(itemView, mListener);
    }

    @Override
    public void onBindViewHolder(final OrderHistoryViewHolder holder, int position) {
        final int selpos = position;
        OrderHistoryInfo oritem = mOrderList.get(position);
        //Integer orid = oritem.oId;
        holder.ordno.setText(oritem.oId);
        //holder.ordtime.setText(oritem.oTime);
        if (oritem.oStatus == ORDER_STATUS_COMPLETE)
            holder.ordstatus.setTextColor(Color.parseColor("#099c09"));
        else if(oritem.oStatus == ORDER_STATUS_CANCELLED || oritem.oStatus == ORDER_STATUS_REJECTED)
            holder.ordstatus.setTextColor(Color.parseColor("#9c0953"));
        holder.ordstatus.setText(oritem.getStatusText());
        Or2GoStore mStoreInfo = gAppEnv.getStoreManager().getStoreById(oritem.oStore);
        holder.ordvendor.setText(mStoreInfo.getName());
        try {
            Date date = inputFormat.parse(oritem.oTime);
            holder.ordtime.setText(outputFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }

    public class OrderHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private RecyclerViewClickListener mListener;
        public TextView ordno, ordtime, ordvendor, ordstatus;
        public ImageView orderckout, itemedit;
        public RecyclerView.LayoutManager mLayoutManager;

        public OrderHistoryViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            ordno = (TextView) view.findViewById(R.id.ordno);
            ordstatus = (TextView) view.findViewById(R.id.ordstatus);
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
}
