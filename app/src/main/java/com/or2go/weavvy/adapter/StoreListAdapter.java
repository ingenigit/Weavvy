package com.or2go.weavvy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.R;
import com.or2go.weavvy.activity.StoreProductsActivity;
import com.or2go.weavvy.model.StoreList;

import java.util.ArrayList;

public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.ViewHolder> {
    Context context;
    ArrayList<StoreList> storeListArrayList;

    public StoreListAdapter(Context context, ArrayList<StoreList> storeList) {
        this.context = context;
        this.storeListArrayList = storeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.store_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreList storeList = storeListArrayList.get(position);
        holder.textViewName.setText(storeList.getStringName());
        holder.textViewContact.setText(storeList.getvContact());
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.blankitem)
                .error(R.drawable.blankitem);
        Glide.with(context)
                .load(BuildConfig.OR2GO_SERVER+"/storelogo"+"/LOGO"+storeList.stringID+".png")
                .apply(options)
                //.override(200, 200) // resizing
                //.fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.imageView);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StoreProductsActivity.class);
                intent.putExtra("vendorID", storeList.getStringID());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storeListArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView textViewName, textViewContact;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardview_store);
            imageView = (ImageView) itemView.findViewById(R.id.image_store);
            textViewName = (TextView) itemView.findViewById(R.id.text_view_store_name);
            textViewContact = (TextView) itemView.findViewById(R.id.text_view_store_contact);
        }
    }
}
