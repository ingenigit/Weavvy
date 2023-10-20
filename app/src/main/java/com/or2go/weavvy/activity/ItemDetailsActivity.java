package com.or2go.weavvy.activity;

import static com.or2go.core.Or2goConstValues.OR2GO_PRODUCT_TAG_FOOD_NONVEG;
import static com.or2go.core.Or2goConstValues.OR2GO_PRODUCT_TAG_FOOD_VEG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.or2go.core.CartItem;
import com.or2go.core.Or2GoStore;
import com.or2go.core.ProductInfo;
import com.or2go.core.ProductSKU;
import com.or2go.core.SalesSelectInfo;
import com.or2go.core.UnitManager;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.R;
import com.or2go.weavvy.adapter.SelectPackSizeAdapter;
import com.or2go.weavvy.manager.OrderCartManager;
import com.or2go.weavvy.manager.ProductManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;

public class ItemDetailsActivity extends AppCompatActivity {

    AppEnv gAppEnv;
    ImageView imageViewImage;
    TextView textViewName, textViewDesc;
    String vendorid;
    Or2GoStore mStoreInfo;
    Integer elapsedTime=0;
    ProgressDialog progressDialog;
    ImageView imageViewPurity;
    Currency currency = Currency.getInstance("INR");
    DecimalFormat df = new DecimalFormat("0");
    OrderCartManager mCartMgr;
    RecyclerView recyclerViewPackSize;
    RecyclerView.LayoutManager layoutManager;
    SelectPackSizeAdapter selectPackSizeAdapter;
    SalesSelectInfo salesSelectInfo;
    //ProductPriceInfo productPriceInfo;
    ProductSKU productSKUInfo;
    ProductManager productManager;
    UnitManager mUnitMgr= new UnitManager();
    ProductInfo prdInfo;
    LocalBroadcastManager localBroadcastManager;
    Intent CartChange, localIntent;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        gAppEnv = (AppEnv) getApplicationContext();
        //share Animation
        getWindow().setAllowEnterTransitionOverlap(false);
        Slide slide = new Slide(Gravity.RIGHT);
        getWindow().setReturnTransition(slide);
        Intent intent = getIntent();
        Integer integerId = intent.getIntExtra("oritemID",-1);
        vendorid = intent.getStringExtra("vendorID");
        String vendorName = intent.getStringExtra("storename");
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        CartChange = new Intent("Order_CartManager_Update");
        localIntent = new Intent("Order_Cart_Update");
        mCartMgr = gAppEnv.getCartManager();
        if (vendorName != null) {
            mStoreInfo = gAppEnv.getStoreManager().getStoreByName(vendorName);
            vendorid = mStoreInfo.getId();
        }else
            mStoreInfo = gAppEnv.getStoreManager().getStoreById(vendorid);

        ProductManager prdmgr = gAppEnv.getStoreManager().getProductManager(vendorid);
        prdInfo = prdmgr.getProductInfo(integerId);
        if (prdInfo == null){
            NoDialog("Not Available", "Sorry! currently this product is not available.");
            return;
        }
        salesSelectInfo = new SalesSelectInfo(prdmgr.getProductInfo(integerId));
        productManager = gAppEnv.getStoreManager().getProductManager(vendorid);
        productSKUInfo= salesSelectInfo.getSelectedSKUInfo();
        if (productSKUInfo == null){
            NoDialog("No SKU", "This product didn't have any SKU.");
            return;
        }
        updateQuantityFromCart();
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.appmaintoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(prdInfo.name);
        getSupportActionBar().setSubtitle(mStoreInfo.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageViewImage = (ImageView) findViewById(R.id.imageViewImage);
        textViewName = (TextView) findViewById(R.id.tvitemName);
        textViewDesc = (TextView) findViewById(R.id.tvitemDesc);
        imageViewPurity = (ImageView) findViewById(R.id.ImagePurity);
        recyclerViewPackSize = (RecyclerView) findViewById(R.id.packVariant);

        //item image
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.blankitem)
                .error(R.drawable.blankitem);
        if(salesSelectInfo.getProduct().getImagepath() == 0){
            Glide.with(this)
                    .load(BuildConfig.OR2GO_SERVER+"prodimage/"+prodNameToImagePath(salesSelectInfo.getBrand(), salesSelectInfo.getName()) + ".jpg")
                    .apply(options)
                    //.override(200, 200) // resizing
                    //.fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(imageViewImage);
        }else if (salesSelectInfo.getProduct().getImagepath() == 1){
            Glide.with(this)
                    .load(BuildConfig.OR2GO_SERVER+"vendorprodimage/"+mStoreInfo.vId+"/"+salesSelectInfo.getId()+ ".jpg")
                    .apply(options)
                    //.override(200, 200) // resizing
                    //.fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(imageViewImage);
        }else
            Toast.makeText(this, "No Product Image", Toast.LENGTH_SHORT).show();

        layoutManager = new LinearLayoutManager(this);
        recyclerViewPackSize.setLayoutManager(layoutManager);
        SetTextValue(prdInfo.name, productSKUInfo.mAmount.toString(), mUnitMgr.getUnitName(productSKUInfo.mUnit),
                prdInfo.desc, productSKUInfo.mPrice, productSKUInfo.getMRPStr(), productSKUInfo.getDiscountValue(),
                productSKUInfo.mMRP, productSKUInfo.mPrice, productSKUInfo.mUnit);

        SelectPackSizeAdapter.RecyclerViewItemClickListener listener = new SelectPackSizeAdapter.RecyclerViewItemClickListener() {
            @Override
            public void onMultiPackSelectItem(View view, ProductSKU data, int position) {
                if ((position >= 0) && (mCartMgr!=null)) {
                    String cartvendor = mCartMgr.getCurrentVendor();
                    if ((!cartvendor.isEmpty()) && (!vendorid.equals(cartvendor))) {
                        //Show alert message
                        Toast.makeText(ItemDetailsActivity.this, "Order for different vendor exist in cart.", Toast.LENGTH_SHORT).show();
                    }else{
                        if(view.getId() == R.id.buttonAdd){
                            //selectPackSizeAdapter.setSelectedPosition(data.mPriceId);
                            salesSelectInfo.mSKUSelectId = data.mSKUId;
//                            getSelectedSKUInfo(data.mSKUId);
                            SetTextValue(prdInfo.name, data.mAmount.toString(), mUnitMgr.getUnitName(data.mUnit), prdInfo.desc, data.mPrice, data.getMRPStr(),
                                    data.getDiscountValue(), data.mMRP, data.mPrice, data.mUnit);
                            mCartMgr.addNewItem(integerId, vendorid, getSelectedSKUInfo(data.mSKUId)); //, prdInfo.getSKU(integerId)
                            salesSelectInfo.incQuantity(data.mSKUId);
                            selectPackSizeAdapter.notifyDataSetChanged();
//                            updateQuantity(salesSelectInfo.isQntyEmpty(salesSelectInfo.mPriceSelectId),salesSelectInfo.getViewQnty(salesSelectInfo.mPriceSelectId));
                            localBroadcastManager.sendBroadcast(localIntent);
                            updateCartTotal();
                        }else{
                            if(view.getId() == R.id.itmadd){
//                                Toast.makeText(gAppEnv, "" + salesSelectInfo.getSelectedPrice(), Toast.LENGTH_SHORT).show();
                                if (salesSelectInfo.isQntyEmpty(data.mSKUId)) {
                                    mCartMgr.addNewItem(integerId, vendorid,data);
                                }else {
                                    mCartMgr.incItemQnty(integerId, data.mSKUId);
                                    salesSelectInfo.incQuantity(data.mSKUId);
                                    salesSelectInfo.mSKUSelectId = data.mSKUId;
                                    selectPackSizeAdapter.notifyDataSetChanged();
//                                    updateQuantity(salesSelectInfo.isQntyEmpty(salesSelectInfo.mPriceSelectId),salesSelectInfo.getViewQnty(salesSelectInfo.mPriceSelectId));
                                    updateCartTotal();
                                }
                            }else if(view.getId() == R.id.itmdec){
                                if (salesSelectInfo.isQntyEmpty(data.mSKUId)) return;
                                mCartMgr.decItemQnty(integerId, data.mSKUId);//mCartMgr.decItemQnty(oritem.getName());
                                salesSelectInfo.decQuantity(data.mSKUId);
                                selectPackSizeAdapter.notifyDataSetChanged();
//                                updateQuantity(salesSelectInfo.isQntyEmpty(salesSelectInfo.mPriceSelectId),salesSelectInfo.getViewQnty(salesSelectInfo.mPriceSelectId));
                                updateCartTotal();
                            }
                        }
                    }
                }
            }
        };
        selectPackSizeAdapter = new SelectPackSizeAdapter(prdInfo.mSKUList,mStoreInfo.vInventoryControl, salesSelectInfo, mStoreInfo.vId, gAppEnv, R.layout.pack_size_list, listener);
        recyclerViewPackSize.setAdapter(selectPackSizeAdapter);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.itemDtlBottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectListener);
        Integer cartcnt = gAppEnv.getCartCaount();//getCartManager().getCartSize();
        if (cartcnt > 0) {
            //String SalesSubtotalVal = gAppEnv.getCartManager().getCartTotal();
            bottomNavigationView.getMenu().getItem(1).setTitle("Cart : (" + cartcnt + ")" );
        }
        //check stock
        boolean checkStock = productManager.validateStockAvailability(vendorid, prdInfo.mSKUList);
        if (checkStock == true)
            moniterStockCheck();

    }

    private void updateQuantityFromCart() {
        ArrayList<ProductSKU> skuList = prdInfo.mSKUList;
        if (skuList!= null) {
            int pkcnt = skuList.size();
            for (int i = 0; i < pkcnt; i++) {
                ProductSKU isku = skuList.get(i);
                CartItem cartp = mCartMgr.getOrderPackItemById(prdInfo.id, isku.mSKUId);
                if (cartp != null) {
                    salesSelectInfo.mapQuantity.put(isku.mSKUId, cartp.getQntyVal());
                }
            }
        }
    }

    public ProductSKU getSelectedSKUInfo(int mSKUSelectId) {
        if ((prdInfo.getSKUList() ==null) || (prdInfo.getSKUList().size()==0)) return null;
        int plistsize=prdInfo.getSKUList().size();
        for(int i=0;i<plistsize;i++) {
            productSKUInfo = prdInfo.getSKUList().get(i);
            if (productSKUInfo.mSKUId.equals(mSKUSelectId))
                return productSKUInfo;
        }
        return prdInfo.getSKUList().get(0);
    }

    private void NoDialog(String title, String desc) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(desc)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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

    private void SetTextValue(String stringName, String amount, String unitName, String stringDesc, Float price, String mrpStr,
                              Float discountValue, Float mMaxPrice, Float mSalePrice, Integer mUnit) {
        textViewName.setText(stringName + ", " + amount + unitName);
        textViewDesc.setText(stringDesc);
        if (salesSelectInfo.mFoodType == OR2GO_PRODUCT_TAG_FOOD_VEG) {
            imageViewPurity.setVisibility(View.VISIBLE);
            imageViewPurity.setImageResource(R.drawable.veg_24);}
        else if (salesSelectInfo.mFoodType == OR2GO_PRODUCT_TAG_FOOD_NONVEG) {
            imageViewPurity.setVisibility(View.VISIBLE);
            imageViewPurity.setImageResource(R.drawable.non_veg_24);
        }
        else {
            imageViewPurity.setVisibility(View.GONE);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectListener = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.item_dtl_nav_home:
                    startActivity(new Intent(ItemDetailsActivity.this, MainActivity.class));
                    return true;
                case R.id.item_dtl_nav_cart:
                    if (!gAppEnv.isRegistered())
                        return false;
                    if (mCartMgr.isCartEmpty()) {
                        Toast.makeText(ItemDetailsActivity.this, "Cart is empty.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    startActivity(new Intent(ItemDetailsActivity.this,OrderCartActivity.class));
                    return true;
            }
            return false;
        }
    };

    private void moniterStockCheck() {
        progressDialog = new ProgressDialog(ItemDetailsActivity.this, R.style.Theme_Or2goProgressDialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Checking stock availability...");
        progressDialog.setCancelable(false);

        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                elapsedTime +=1;
                if (mCartMgr.isStockCheckComplete()) {
                    handler.removeCallbacks(null);
                    progressDialog.dismiss();
                    selectPackSizeAdapter.notifyDataSetChanged();
                }
                else if (mCartMgr.isStockCheckError()) {
                    handler.removeCallbacks(null);
                    progressDialog.dismiss();
                    if (gAppEnv.gAppSettings.getUserType().equals(""))
                        NetworkErrorDialog();
                    else
                        ProperLoginDialog();
                }else {
                    if (elapsedTime > 100) {
                        handler.removeCallbacks(null);
                        progressDialog.dismiss();
                        NetworkErrorDialog();
                    }else
                        handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
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

    private void ProperLoginDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CompatAlertDialogStyle);
        builder.setTitle("Login required!");
        builder.setMessage("Login with your mobile no. to see stock quantity.. ");
        builder.setCancelable(false);
        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(ItemDetailsActivity.this, UserRegisterActivity.class));
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void updateCartTotal(){
        if (mCartMgr == null) return;
        Integer cartcnt = mCartMgr.getCartSize();
        if (cartcnt <=0 ) {
            bottomNavigationView.getMenu().getItem(1).setTitle("Cart");
            bottomNavigationView.removeBadge(R.id.item_dtl_nav_cart);
            return;
        }
        //update subtotal value
        String SalesSubtotalVal = mCartMgr.getCartItemTotal();

        //update cartManager
        boolean brresult = localBroadcastManager.sendBroadcast(CartChange);

        bottomNavigationView.getMenu().getItem(1).setTitle(currency.getSymbol()+SalesSubtotalVal);
        bottomNavigationView.getOrCreateBadge(R.id.item_dtl_nav_cart);
        BadgeDrawable cartBadge = bottomNavigationView.getBadge(R.id.item_dtl_nav_cart);
        cartBadge.setNumber(cartcnt);
    }

}