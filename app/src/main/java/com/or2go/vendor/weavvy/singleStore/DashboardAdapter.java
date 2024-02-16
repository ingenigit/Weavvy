package com.or2go.vendor.weavvy.singleStore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.or2go.vendor.weavvy.R;

import java.util.List;

public class DashboardAdapter extends BaseAdapter {

    Context mContext;
    List<String> mCategoryList;
    List<Integer> mOrderImage;
    List<Integer> mOrderCountList;
    LayoutInflater inflter;
    private RequestQueue mRequestQueue;
    ImageLoader mImageLoader;
    int cacheSize = 1 * 1024 * 1024; // 4MiB

    public DashboardAdapter(Context context, List<Integer> mOrderCatImage, List<String> mOrderCatList, List<Integer> mOrderCatCount) {
        this.mContext = context;
        this.mOrderImage = mOrderCatImage;
        this.mCategoryList = mOrderCatList;
        this.mOrderCountList = mOrderCatCount;
        inflter = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return mCategoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflter.inflate(R.layout.cardview_dashboard, null);
        TextView ordcat = (TextView) convertView.findViewById(R.id.dashlabel);
        TextView ordcnt = (TextView) convertView.findViewById(R.id.dashinfo1);
        ImageView catimg = (ImageView) convertView.findViewById(R.id.martcatimg);

        ordcat.setText(mCategoryList.get(position));
        ordcnt.setText(mOrderCountList.get(position).toString());
        catimg.setImageResource(mOrderImage.get(position));

        return convertView;
    }
}
