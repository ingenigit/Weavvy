package com.or2go.vendor.weavvy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.or2go.vendor.weavvy.storeList.StoreList;

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
    }

    @Override
    public int getItemCount() {
        return storeListArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewContact;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.text_view_store_name);
            textViewContact = (TextView) itemView.findViewById(R.id.text_view_store_contact);
        }
    }
}
