package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.or2go.adapter.MultiPackSelectorAdapter;
import com.or2go.adapter.SalesSelectItemAdapter;
import com.or2go.adapter.SalesSubCatListAdapter;
import com.or2go.adapter.SalesvwTypeListAdapter;
import com.or2go.core.Or2GoStore;
import com.or2go.core.ProductSKU;
import com.or2go.core.SalesSelectInfo;
import com.or2go.core.UnitManager;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.CustomDailogView;
import com.or2go.weavvy.MultiPackSelectorDialog;
import com.or2go.weavvy.OrderListDividerDecoration;
import com.or2go.weavvy.R;
import com.or2go.weavvy.SplashScreen;
import com.or2go.weavvy.manager.OrderCartManager;
import com.or2go.weavvy.manager.ProductManager;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

public class StoreProductsActivity extends AppCompatActivity implements MultiPackSelectorAdapter.RecyclerViewItemClickListener{

    Context mContext;
    AppEnv gAppEnv;
    ArrayList<SalesSelectInfo> salesItems;
    private RecyclerView mRecyclerView;
    private SalesSelectItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    BottomNavigationView btNavigation;
    ProductManager mProductMgr;
    OrderCartManager mCartMgr;
    Or2GoStore mStoreInfo;
    CustomDailogView customDailogView;
    //category list
    private RecyclerView mCategoryRecyclerView;
    private SalesvwTypeListAdapter mCategoryListAdapter;
    private RecyclerView.LayoutManager mCategoryLayoutManager;
    Handler mProgressHandler=null;
    int elapsedTime=0;
    MultiPackSelectorDialog mMultiPackSelectorDialog=null;
    Integer mMultiPackSelectPosition=-1;
    TextView textViewStoreName, textViewStoreAddress;
    private ArrayList<String> productTypes;
    HashMap<String, List<String>> productTypesChild;
    String selType="";
    String selVendor="";
    String selVendorId;
    String selCategory="";
    String selSubCategory="";
    int selProductId=-1;
    UnitManager mUnitMgr;
    ImageView imageViewStoreStatus;
    ShapeableImageView storeImageView;
    LinearLayout nodataLayout;
    LinearLayout linearLayoutSub;
    CardView cardViewselecteSub;
    TextView textViewCatName;
    RecyclerView recyclerViewSub;
    SalesSubCatListAdapter subCatListAdapter;
    SkeletonScreen skeletonScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_products);

        getWindow().setAllowEnterTransitionOverlap(false);
        Slide slide = new Slide(Gravity.RIGHT);
        getWindow().setReturnTransition(slide);
        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext,"Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(StoreProductsActivity.this, SplashScreen.class));
        }
        mCartMgr = gAppEnv.getCartManager();
        mUnitMgr = new UnitManager();
        selVendorId = getIntent().getStringExtra("vendorID");
        //selVendor = getIntent().getStringExtra("vendor");
        selCategory=getIntent().getStringExtra("category");
        selSubCategory=getIntent().getStringExtra("subcategory");
        selProductId=getIntent().getIntExtra("productid", -1);

        if (selCategory==null) selCategory="";
        if (selSubCategory==null) selSubCategory="";

        mStoreInfo = gAppEnv.getStoreManager().getStoreById(selVendorId);
        mProductMgr = gAppEnv.getStoreManager().getProductManager(selVendorId);
        gAppEnv.gAppSettings.setStoreIdName(mStoreInfo.vName);
        textViewStoreName = (TextView) findViewById(R.id.tv_store_name);
        textViewStoreAddress = (TextView) findViewById(R.id.tv_store_address);
        imageViewStoreStatus = (ImageView) findViewById(R.id.storeStatusImage);
        storeImageView = (ShapeableImageView) findViewById(R.id.imageview_storeimage);
        selVendor = mStoreInfo.vName;
        textViewStoreName.setText(selVendor);
        textViewStoreAddress.setText(mStoreInfo.vLocality+", "+mStoreInfo.vPlace);
        if(!mStoreInfo.isOpen())
            imageViewStoreStatus.setColorFilter(mContext.getResources().getColor(R.color.red));
        //storeImage
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.blankitem)
                .error(R.drawable.blankitem);
        Glide.with(mContext)
                .load(BuildConfig.OR2GO_SERVER+"storelogo"+"/LOGO"+mStoreInfo.vId+".png")
                .apply(options)
                //.override(200, 200) // resizing
                //.fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(storeImageView);

        productTypes = (ArrayList<String>) mProductMgr.getProductTypes();// get category
        System.out.println(mProductMgr + "kdsfjndjg " + mStoreInfo.getId() + productTypes.size());
        productTypesChild = mProductMgr.getProductSubCategories(); //get subcategory
        //Type List View
        mCategoryRecyclerView = (RecyclerView)findViewById(R.id.salesprodcategorylist);
        //mCategoryLayoutManager = new LinearLayoutManager(getApplicationContext());
        mCategoryRecyclerView.setLayoutManager(mCategoryLayoutManager);
        //sub
        linearLayoutSub = (LinearLayout) findViewById(R.id.showItemSubcategory);
        cardViewselecteSub = (CardView) findViewById(R.id.sales_select_category_cardview);
        textViewCatName = (TextView) findViewById(R.id.tvcategoryname);
        recyclerViewSub = (RecyclerView) findViewById(R.id.salesprosubcategorylist);
        nodataLayout = (LinearLayout) findViewById(R.id.noDataLinearLayout);

        SalesvwTypeListAdapter.RecyclerViewClickListener catlistener = new SalesvwTypeListAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                if (position >= 0) {
                    String newType = productTypes.get(position);
                    gAppEnv.getGposLogger().i(" selected category: " + newType + "   position=" + position+ "current caegory:"+selType);
                    List<String> subCat = productTypesChild.get(newType);
                    if (subCat == null){
                        if (newType.equals(selType)) {
                            selType="";
                            nodataLayout.setVisibility(View.GONE);
                            mCategoryListAdapter.clearSelectedCategory(position);
                            clearTypeSelection();
                        }else{
                            selType = newType;
                            mCategoryListAdapter.setSelectedCategory(position);
                            mProductMgr.getCategoryItems(selType, salesItems);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    else {
                        String selTypes = newType;
                        mCategoryListAdapter.setSelectedCategory(position);
                        mProductMgr.getCategoryItems(selTypes, salesItems);
                        mAdapter.notifyDataSetChanged();
                        //show subcategory
                        linearLayoutSub.setVisibility(View.VISIBLE);
                        mCategoryRecyclerView.setVisibility(View.GONE);
                        textViewCatName.setText(selTypes);
                        cardViewselecteSub.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selType="";
                                nodataLayout.setVisibility(View.GONE);
                                mCategoryListAdapter.clearSelectedCategory(position);
                                clearTypeSelection();
                                mAdapter.notifyDataSetChanged();
                                linearLayoutSub.setVisibility(View.GONE);
                                mCategoryRecyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                        ArrayList<String> productTypeSub = new ArrayList<>(subCat);
                        SalesSubCatListAdapter.RecyclerViewClickListener catsublistener = new SalesSubCatListAdapter.RecyclerViewClickListener(){
                            @Override
                            public void onClick(View view, int position) {
                                String subType = productTypeSub.get(position);
                                String highlight = "";
                                if (subType.equals(highlight)){
                                    highlight="";
                                    subCatListAdapter.clearSelectedCategory(position);
                                }else{
                                    highlight = subType;
                                    subCatListAdapter.setSelectedCategory(position);
                                    mProductMgr.getSubCategoryItems(selTypes, subType, salesItems);
                                    mAdapter.notifyDataSetChanged();
                                }
                                if (salesItems.size() == 0)
                                    nodataLayout.setVisibility(View.VISIBLE);
                                else
                                    nodataLayout.setVisibility(View.GONE);
                            }
                        };
                        subCatListAdapter = new SalesSubCatListAdapter(mContext, productTypeSub, R.layout.listview_sales_category, catsublistener);
                        mCategoryLayoutManager = new LinearLayoutManager(StoreProductsActivity.this, LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewSub.setLayoutManager(mCategoryLayoutManager);
                        recyclerViewSub.setAdapter(subCatListAdapter);
                    }
                }
            }
        };
        mCategoryListAdapter = new SalesvwTypeListAdapter(mContext, productTypes, R.layout.listview_sales_category, catlistener);
        mCategoryLayoutManager = new LinearLayoutManager(StoreProductsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mCategoryRecyclerView.setLayoutManager(mCategoryLayoutManager);
        mCategoryRecyclerView.setAdapter(mCategoryListAdapter);
        mRecyclerView = (RecyclerView) findViewById(R.id.ssitem_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        salesItems = new ArrayList<>();
        if (mProductMgr != null) {
            if((!selCategory.isEmpty()) && (!selSubCategory.isEmpty())) ////get selected subcategory items
                mProductMgr.getSearchedProduct(selCategory, selSubCategory, selProductId, salesItems);
            else if((!selCategory.isEmpty()) && (selSubCategory.isEmpty())) //get selected category items
                mProductMgr.getCategoryItems(selCategory, salesItems);
            else
                mProductMgr.getAllItems(salesItems);
        }
        SalesSelectItemAdapter.RecyclerViewClickListener listener = new SalesSelectItemAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                if ((position >= 0) && (mCartMgr!=null)){
                    //check if there is already order for other vendor in cart
                    String cartvendor = mCartMgr.getCurrentVendor();
                    SalesSelectInfo oritem = salesItems.get(position);
                    //ProductPriceInfo packinfo = oritem.getSelectedPriceInfo();
                    ProductSKU packinfo = oritem.getSelectedSKUInfo();
                    if(v.getId() == R.id.cardViewAddItem) {
                        gAppEnv.getGposLogger().i(" sales select item: " + oritem.getName());
                        if (packinfo==null) {
                            Toast.makeText(mContext, "It's price is missing", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if ( (!cartvendor.isEmpty())  && (!selVendorId.equals(cartvendor)) ) {
                            //Show alert message
                            String title = "Already Exit !";
                            String body = "Products of different vendor already exist in cart.";
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
                            customDailogView = new CustomDailogView(StoreProductsActivity.this, title, body, positive, "", visible, onclick);
                            customDailogView.show();
                            return;
                        }
                        v.setVisibility(View.GONE);
                        gAppEnv.getGposLogger().i(" sales select adding new item: " + oritem.getProduct().name + "   id=" + oritem.getProduct().id);
                        mCartMgr.addNewItem(oritem.getId(), mStoreInfo.getId(), oritem.getSelectedSKUInfo());
                        oritem.incQuantity(oritem.mSKUSelectId);
                        mAdapter.notifyDataSetChanged();
                        updateCartTotal();
                    }
                    else if (v.getId() == R.id.imgmultipacksel) {
                        mMultiPackSelectPosition=position;
                        gAppEnv.getGposLogger().i(" show multi pack item list..: ");
                        showMultiPackSelectorDialog(oritem.getProduct().name, oritem.getSKUList());
                    }
                    else if(v.getId() == R.id.gimgprodprop){
                        if (packinfo==null) return;
                        
                        ImageView imageViewdummy = findViewById(v.getId());
                        Intent intent = new Intent(StoreProductsActivity.this, ItemDetailsActivity.class);
                        intent.putExtra("position", position);
                        intent.putExtra("oritemID", oritem.getId());
                        intent.putExtra("vendorID", selVendorId);
                        //SharedAnimation
                        Pair[] pairs = new Pair[1];
                        pairs[0] = new Pair<View, String>(imageViewdummy, "ViewImage");
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext, pairs);
                        //end
                        startActivity(intent, options.toBundle());
                    }
                    else {
                        if (v.getId() == R.id.cardViewItemDec) {

                            if (oritem.isQntyEmpty(oritem.mSKUSelectId)) return;

                            mCartMgr.decItemQnty(oritem.getId(), oritem.getSelectedSKU());//mCartMgr.decItemQnty(oritem.getName());
                            oritem.decQuantity(oritem.mSKUSelectId);
                            mAdapter.notifyDataSetChanged();
                            updateCartTotal();

                        }
                        else if (v.getId() == R.id.cardViewItemAdd) {

                            if (oritem.isQntyEmpty(oritem.mSKUSelectId)) {
                                ///gAppEnv.getGposLogger().i(" sales select adding new item: " + oritem.getName() + "   id=" + oritem.getId());

                                mCartMgr.addNewItem(oritem.getId(), selVendorId, oritem.getSelectedSKUInfo());
                            } else {
                                ///TBF
                                ///gAppEnv.getGposLogger().i(" sales select increasing item: " + oritem.getName() + "   id=" + oritem.getId());
                                mCartMgr.incItemQnty(oritem.getId(), oritem.getSelectedSKU());//mCartMgr.incItemQnty(oritem.getName());
                            }
                            oritem.incQuantity(oritem.mSKUSelectId);
                            mAdapter.notifyDataSetChanged();
                            //update
                            updateCartTotal();
                            //((OrderItemAdapter) mAdapter).updateData(gridItems);
                        }
                    }
                }
            }
        };
        mAdapter = new SalesSelectItemAdapter(mContext, BuildConfig.OR2GO_SERVER, selVendorId, mStoreInfo.getInventoryControl(),salesItems, R.layout.listview_category_item, listener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new OrderListDividerDecoration(this, LinearLayoutManager.VERTICAL, 16));

        if (selProductId>=0)
            mLayoutManager.scrollToPosition(findSelectedProductPoistion(selProductId));

        //Bottom Navigation
        btNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        updateCartTotal();

        if (mStoreInfo.isDownloadRequired()) {
            gAppEnv.getDataSyncManager().startDataDownload(mStoreInfo); // requestVendorDataSync(mStoreInfo.vId);
            skeletonScreen = Skeleton.bind(mRecyclerView)
                    .adapter(mAdapter)
                    .count(6)
                    .load(R.layout.skeleton_sales_select_item)
                    .duration(500)
                    .shimmer(true)
                    .angle(30)
                    .frozen(false)
                    .show();
            monitorCommProgress();
        }
    }

    private void monitorCommProgress() {
        mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            public void run() {
                elapsedTime +=1;
                //if (mVendorInfo.isServerProductListUpdateDone() )
                //if (isDBUpdateDone()){
                if(gAppEnv.getDataSyncManager().isSyncDone(mStoreInfo)){
                    mProgressHandler.removeCallbacks(null);
                    if((!selCategory.isEmpty()) && (!selSubCategory.isEmpty()))
                        mProductMgr.getSearchedProduct(selCategory, selSubCategory, selProductId, salesItems);
                    else if((!selCategory.isEmpty()) && (selSubCategory.isEmpty()))
                        mProductMgr.getCategoryItems(selCategory, salesItems);
                    else
                        mProductMgr.getAllItems(salesItems);
                    mAdapter.notifyDataSetChanged();
                    productTypes = (ArrayList<String>) mProductMgr.getProductTypes();//
                    mCategoryListAdapter.notifyDataSetChanged();
                    skeletonScreen.hide();
                    mProgressHandler = null;
                }
                else if(gAppEnv.getDataSyncManager().isDownloadError(mStoreInfo)){
                    mProgressHandler.removeCallbacks(null);
                    mCategoryListAdapter.notifyDataSetChanged();
                    skeletonScreen.hide();
                    mProgressHandler = null;
                    Toast.makeText(mContext,"download error", Toast.LENGTH_SHORT).show();
                }
                else if (salesItems.size() == 0){
                    skeletonScreen.hide();
                    nodataLayout.setVisibility(View.VISIBLE);
                    mProgressHandler.removeCallbacks(null);
                    mProgressHandler = null;
                }
                else {
                    if (elapsedTime > 200) {
                        elapsedTime =0;
                        NetworkErrorDialog();
                        mProgressHandler.removeCallbacks(null);
                    }
                    else
                        mProgressHandler.postDelayed(this, 1000);
                }
                Log.i("SalesSelectActivity", "Vendor product status="+mStoreInfo.vProdStatus);
            }
        }, 1000);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent dashid = new Intent(StoreProductsActivity.this, MainActivity.class);
                startActivity(dashid);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(StoreProductsActivity.this, MainActivity.class));
                    return true;
                case R.id.navi_prod_search:
                    Intent searchactivity = new Intent(StoreProductsActivity.this, SearchProductActivity.class);
                    searchactivity.putExtra("vendorid", mStoreInfo.vId);
                    searchactivity.putExtra("vendorname", mStoreInfo.vName);
                    startActivity(searchactivity);
                    return true;
                case R.id.navigation_cart:
                    if (!gAppEnv.isRegistered()) {
                        RegisterDialog();
                        return false;
                    }
                    else if (mCartMgr.isCartEmpty()) {
                        Toast.makeText(mContext, "Cart is empty! ", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    startActivity(new Intent(StoreProductsActivity.this, OrderCartActivity.class));
                    return true;
            }
            return false;
        }
    };

    private void NetworkErrorDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CompatAlertDialogStyle);
        builder.setTitle("Network Error");
        builder.setMessage("The connection is lost or very slow. Please check your internet connectivity and retry.");
        builder.setCancelable(false);
        builder.setPositiveButton("Exit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gAppEnv.appExit();
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton("Retry",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (gAppEnv.isInternetOn()) {
                            gAppEnv.InitServerComm();
                            monitorCommProgress();
                            dialog.dismiss();
                        }
                    }
                });
        builder.show();
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
                startActivity(new Intent(StoreProductsActivity.this, UserRegisterActivity.class));
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

    private int findSelectedProductPoistion(int prdid) {
        for (int i=0; i<salesItems.size(); i++) {
            SalesSelectInfo griditem = salesItems.get(i);
            if (griditem.getProduct().id == prdid)
                return i;
        }
        return 0;
    }

    private void showMultiPackSelectorDialog(String itemname, ArrayList<ProductSKU> SKUList) {
        if (SKUList == null) {
            gAppEnv.getGposLogger().i(" showMultiPackSelectorDialog pack list null ERROR!!!");
            return;
        }
        else
            gAppEnv.getGposLogger().i(" showMultiPackSelectorDialog price count=" + SKUList.size()+ " SKU count"+ SKUList.size());
        MultiPackSelectorAdapter mAdapter = new MultiPackSelectorAdapter(SKUList, R.layout.listview_multipack_item, this);
        mMultiPackSelectorDialog = new MultiPackSelectorDialog(StoreProductsActivity.this, mAdapter, itemname);
        mMultiPackSelectorDialog.setCanceledOnTouchOutside(false);
        mMultiPackSelectorDialog.show();
    }

    private void updateCartTotal() {
        if (mCartMgr == null) return;
        Integer cartcnt = mCartMgr.getCartSize();
        Currency currency = Currency.getInstance("INR");
        if (cartcnt <=0 ) {
            btNavigation.getMenu().getItem(2).setTitle("Cart");
            btNavigation.removeBadge(R.id.navigation_cart);
            return;
        }
        //update subtotal value
        String SalesSubtotalVal = mCartMgr.getCartItemTotal();
        btNavigation.getMenu().getItem(2).setTitle(currency.getSymbol()+SalesSubtotalVal);
        btNavigation.getOrCreateBadge(R.id.navigation_cart);
        //btNavigation.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        BadgeDrawable cartBadge = btNavigation.getBadge(R.id.navigation_cart);
        cartBadge.setNumber(cartcnt);
    }

    private void clearTypeSelection() {
        mProductMgr.getAllItems(salesItems);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onMultiPackSelectItem(ProductSKU data) {
        gAppEnv.getGposLogger().i(" MultiPackSelect =" + data.mAmount);
        ///TBF
        if (mMultiPackSelectorDialog != null){
            mMultiPackSelectorDialog.dismiss();
            mMultiPackSelectorDialog = null;
            SalesSelectInfo salesViewInfo = salesItems.get(mMultiPackSelectPosition);
            salesViewInfo.mSKUSelectId = data.mSKUId;
            mMultiPackSelectPosition=-1;
            mAdapter.notifyDataSetChanged();
        }
    }

    //update cart changes
    final BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action =  intent.getAction();
            gAppEnv.getGposLogger().d("Broadcast Receiver: message from ItemDetailsActivity = "+action);
            //update data
            mProductMgr.getAllItems(salesItems);
            mAdapter.notifyDataSetChanged();
            //set cart value here.
            updateCartTotal();
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        registerStatusUpdateReceiver();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterStatusUpdateReceiver();
    }

    private void unregisterStatusUpdateReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void registerStatusUpdateReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("Order_CartManager_Update"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("Order_Cart_Update"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}