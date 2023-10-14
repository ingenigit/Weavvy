package com.or2go.weavvy.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.or2go.core.ProductSKU;
import com.or2go.core.SalesSelectInfo;
import com.or2go.core.UnitManager;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.R;
import com.or2go.weavvy.manager.OrderCartManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;

public class SelectPackSizeAdapter extends RecyclerView.Adapter<SelectPackSizeAdapter.ViewHolder> {
    //ArrayList<ProductPriceInfo> priceInfos;
    ArrayList<ProductSKU> productSKUS;
    SalesSelectInfo mOrderItemList;
    Currency currency = Currency.getInstance("INR");
    RecyclerViewItemClickListener recyclerViewItemClickListener;
    DecimalFormat df = new DecimalFormat("0");
    private int layout;
    private final DecimalFormat decfor = new DecimalFormat("0.00");
    //int mSelectedItem;
    UnitManager mUnitManager= new UnitManager();
    Integer inventoryControl;
    AppEnv appEnv;
    String storeID;
    public SelectPackSizeAdapter(ArrayList<ProductSKU> arrayList1, int invControl, SalesSelectInfo oritem,
                                 String storeId, AppEnv gAppEnv, int layout, RecyclerViewItemClickListener listener) {
        //this.priceInfos = arrayList;
        this.productSKUS = arrayList1;
        this.inventoryControl = invControl;
        //this.mSelectedItem = selectedPosition;
        this.storeID = storeId;
        this.mOrderItemList = oritem;
        this.appEnv = (AppEnv) gAppEnv;
        this.layout = layout;
        this.recyclerViewItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //ProductPriceInfo packInfo = priceInfos.get(position);

        /*if (packInfo.mPriceId == mSelectedItem){
            holder.relativeLayout.setBackgroundResource(R.drawable.highlight_select_bg);
        }else{
            holder.relativeLayout.setBackgroundResource(R.drawable.pack_size_bg);
        }*/
//        holder.radioButton.setChecked(position == mSelectedItem);

        ProductSKU skuinfo = productSKUS.get(position);
        OrderCartManager mCartMgr = appEnv.getCartManager();
        Float fQnty= mOrderItemList.mapQuantity.get(skuinfo.mSKUId);
        System.out.println("SelectPackAdapter: priceid="+skuinfo.mSKUId+"  Qnty="+fQnty);
        System.out.println("SelectPackAdapter: Stock Available = "+ skuinfo.mStockStatus );
        //String totalQTY; //mOrderItemList.getViewQnty();

        if (skuinfo.mColor.equals("")) {
            holder.textViewColor.setVisibility(View.GONE);
        }else {
            holder.textViewColor.setVisibility(View.VISIBLE);
            String text = "Color: " + skuinfo.mColor;
            String[] words = text.split(" ");
            words[1] = words[1].toUpperCase();
            holder.textViewColor.setText(String.join(" ", words));
        }
        if (skuinfo.mSize.equals("")){
            holder.textViewSize.setVisibility(View.GONE);
        }else{
            holder.textViewSize.setVisibility(View.VISIBLE);
            holder.textViewSize.setText("Size: " + skuinfo.mSize);
        }

        if (fQnty==null)
            fQnty=Float.parseFloat("0");
        //else
        //    totalQTY=fQnty.toString();
        //if (Integer.parseInt(totalQTY) == 0)

        System.out.println(mCartMgr.getCurrentVendor() + fQnty + "/////" + storeID);
        if (fQnty!=0 && (mCartMgr.getCurrentVendor().equals(storeID))){
            //if (packInfo.mPriceId == mSelectedItem){
            holder.addItem.setVisibility(View.GONE);
            holder.itemqnty.setText(fQnty.toString());//(totalQTY);
            //}else{
//                holder.addItem.setVisibility(View.VISIBLE);
//                holder.itemqnty.setText("");
            //}
        }else{
            System.out.println(mCartMgr.getCurrentVendor() + "/////" + storeID);
            holder.addItem.setVisibility(View.VISIBLE);
            holder.itemqnty.setText("");
        }

        if (inventoryControl != 0){
            if (skuinfo.mStockStatus <= 0) {
                holder.proOutStock.setVisibility(View.VISIBLE);
                holder.showStock.setVisibility(View.GONE);
                holder.addItem.setEnabled(false);

            }else {
                holder.proOutStock.setVisibility(View.GONE);
                holder.showStock.setVisibility(View.VISIBLE);
                holder.addItem.setEnabled(true);
                holder.showStock.setText(skuinfo.mStockStatus + " left");
                GradientDrawable drawable = (GradientDrawable)holder.showStock.getBackground();
                if (skuinfo.mStockStatus > 10)
                    drawable.setStroke ((int) 1.5, Color.GREEN);
                else
                    drawable.setStroke ((int) 1.5, Color.RED);
            }
        }else{
            holder.proOutStock.setVisibility(View.GONE);
            holder.showStock.setVisibility(View.GONE);
        }


        /*int skuid = packInfo.mSKUId;
        ProductSKU SKUInfo = null;
        if (skuid!=0) {
            for (int j = 0; j < productSKUS.size(); j++) {
                if (productSKUS.get(j).mSKUId == skuid) {
                    SKUInfo = productSKUS.get(j);
                    break;
                }
            }
        }*/

        holder.textViewSP.setText(currency.getSymbol() +skuinfo.mPrice.toString());
        if (skuinfo !=null)
            holder.textViewQty.setText(skuinfo.mAmount.toString()+ mUnitManager.getUnitName(skuinfo.mUnit));
        else
            holder.textViewQty.setText(skuinfo.mAmount.toString()+ mUnitManager.getUnitName(skuinfo.mUnit));
        String MRP = skuinfo.mMRP.toString();
        if(MRP.isEmpty()){
            holder.textViewDic.setText("");
            holder.textViewDic.setVisibility(View.GONE);
        }else{
            holder.textViewMRP.setText(currency.getSymbol() +skuinfo.mMRP.toString());
            Float discamnt= getDiscountValue(skuinfo);
            if (discamnt != null) {
                if (discamnt > 5) {
                    holder.textViewDic.setText(df.format(discamnt) + "% Off");
                    holder.textViewDic.setVisibility(View.VISIBLE);
                } else {
                    Float discrs = skuinfo.mMRP - skuinfo.mPrice;
                    if (discrs >= 1) {
                        holder.textViewDic.setText(currency.getSymbol() + df.format(discrs) + " Off");
                        holder.textViewDic.setVisibility(View.VISIBLE);
                    } else {
                        holder.textViewDic.setText("");
                        holder.textViewDic.setVisibility(View.GONE);
                    }
                }
            } else {
                holder.textViewDic.setText("");
                holder.textViewDic.setVisibility(View.GONE);
            }
        }

        float SP = skuinfo.mPrice;
        String QUnit = mUnitManager.getUnitName(skuinfo.mUnit);//packInfo.getUnitName();
        int aam = Math.round(skuinfo.mAmount);
        System.out.println("sbs " + aam + QUnit);
        if (aam >= 1 && QUnit.equals("Kg")){
            float aak = (SP/aam);
            float amk = (aak/4);
            holder.textViewPerQty.setText(currency.getSymbol() + decfor.format(amk) + "/250g");
            System.out.println(amk);
        }else if(aam >= 1 && QUnit.equals("g")){
            float aak = (SP/aam);
            float amk = (aak*25);
            holder.textViewPerQty.setText(currency.getSymbol() + decfor.format(amk) + "/25g");
            System.out.println(amk);
        }else if(QUnit.equals("L")){
            float aak = (SP/aam);
            float amk = (aak/4);
            holder.textViewPerQty.setText(currency.getSymbol() + decfor.format(amk) + "/250l");
            System.out.println(amk);
        }else if(QUnit.equals("ml")){
            float aak = (SP/aam);
            float amk = (aak*25);
            holder.textViewPerQty.setText(currency.getSymbol() + decfor.format(amk) + "/25ml");
            System.out.println(amk);
        }else{
            System.out.println("sorry");
            holder.textViewPerQty.setVisibility(View.GONE);
        }
        holder.textViewMRP.setPaintFlags(holder.textViewMRP.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public int getItemCount() {
        return productSKUS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout linearLayout;
        RelativeLayout relativeLayout;
        TextView textViewMRP, textViewSP, textViewDic, textViewQty, textViewPerQty;
        TextView itemqnty, showStock, proOutStock;
        Button addItem;
        MaterialButton itemAdd, itemDelete;
        TextView textViewColor, textViewSize;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.selectedOne);
//            showStock = (TextView) itemView.findViewById(R.id.showStockAvailable);
//            proOutStock = (TextView) itemView.findViewById(R.id.proOutOfStock);
//            linearLayout = (LinearLayout) itemView.findViewById(R.id.showQtyWant);
//            textViewColor = (TextView) itemView.findViewById(R.id.textViewColor);
//            textViewSize = (TextView) itemView.findViewById(R.id.tv_ProSize);
//            textViewMRP = (TextView) itemView.findViewById(R.id.tv_mrp);
//            textViewSP = (TextView) itemView.findViewById(R.id.tv_sp);
//            textViewDic = (TextView) itemView.findViewById(R.id.tv_dic);
//            textViewQty = (TextView) itemView.findViewById(R.id.tv_qty);
//            textViewPerQty = (TextView) itemView.findViewById(R.id.tv_per_qty);
//            itemqnty = (TextView) itemView.findViewById(R.id.itmqnty);
//            itemAdd = (MaterialButton) itemView.findViewById(R.id.itmadd);
//            itemDelete = (MaterialButton) itemView.findViewById(R.id.itmdec);
//            addItem = (Button) itemView.findViewById(R.id.buttonAdd);
            itemView.setOnClickListener(this);
            addItem.setOnClickListener(this);
            itemAdd.setOnClickListener(this);
            itemDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recyclerViewItemClickListener.onMultiPackSelectItem(v, productSKUS.get(this.getAdapterPosition()), getAdapterPosition());
        }
    }

    /*public void setSelectedPosition(int position)
    {
        if (mSelectedItem >= 0) notifyItemChanged(mSelectedItem);
        mSelectedItem = position;
        notifyItemChanged(position);
    }*/

    public interface RecyclerViewItemClickListener {
        void onMultiPackSelectItem(View view, ProductSKU data, int position);
    }

    public Float getDiscountValue(ProductSKU skuinfo)
    {
        if (skuinfo.mMRP == null) return null;
        if (skuinfo.mMRP <= skuinfo.mPrice) return null;

        //Float offAmnt = mMaxPrice-mSalePrice;
        Float discPerc = ((skuinfo.mMRP-skuinfo.mPrice)/skuinfo.mMRP) *100;

        return discPerc;
    }
}
