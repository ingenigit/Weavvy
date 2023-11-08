package com.or2go.weavvy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.or2go.weavvy.R;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private String mImagePath;
    private String OR2GO_SERVER;
    private String OR2GO_SP_CODE;
    private int layout;

    public ImageSliderAdapter(Context context, String imgpath, String server, String or2goVendorid, int layout) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mImagePath =imgpath;
        this.OR2GO_SERVER = server;
        this.OR2GO_SP_CODE = or2goVendorid;
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(layout, parent, false);
        return new ImageSliderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Integer selpos = position+1;
        holder.pagerimg.setScaleType(ImageView.ScaleType.FIT_XY);
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.blankitem)
                .error(R.drawable.blankitem);
        Glide.with(mContext)
                .load(OR2GO_SERVER+"appimage/"+OR2GO_SP_CODE+"/"+mImagePath+"/"+selpos.toString()+".png")
                .apply(options)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.pagerimg);
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView pagerimg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pagerimg = (ImageView) itemView.findViewById(R.id.imgPager);
        }
    }
}
