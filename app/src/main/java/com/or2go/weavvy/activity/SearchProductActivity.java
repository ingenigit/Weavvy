package com.or2go.weavvy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.or2go.adapter.SearchResultAdapter;
import com.or2go.core.SearchInfo;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.OrderListDividerDecoration;
import com.or2go.weavvy.R;
import com.or2go.weavvy.manager.ProductManager;
import com.or2go.weavvy.manager.SearchManager;

import java.util.ArrayList;

public class SearchProductActivity extends AppCompatActivity {

    AppEnv gAppEnv;
    Context mContext;
    String selVendorId, selVendorName;
    String selSearch;
    SearchManager searchMgr;
    ProductManager mProductMgr;
    //AutoCompleteTextView edsearch;
    SearchView edsearch;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<SearchInfo> mSearchResults;
    BottomNavigationView btNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext, "Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SearchProductActivity.this, SplashScreen.class));
        }
        selVendorId = getIntent().getStringExtra("vendorid");
        selVendorName = getIntent().getStringExtra("vendorname");
        selSearch = getIntent().getStringExtra("searchval");
        mProductMgr = gAppEnv.getStoreManager().getProductManager(selVendorId);
        searchMgr = gAppEnv.getSearchManager();
        mSearchResults = new ArrayList<SearchInfo>();

        edsearch = (SearchView) findViewById(R.id.edsearch);
        edsearch.setActivated(true);
        edsearch.setQueryHint("Search " + selVendorName + " products");
        edsearch.onActionViewExpanded();
        edsearch.setIconified(false);
        //edsearch.clearFocus();
        mRecyclerView = (RecyclerView) findViewById(R.id.vs_recycler_view);
        // Define a layout for RecyclerView
        mLayoutManager = new GridLayoutManager(mContext,1);
        mRecyclerView.setLayoutManager(mLayoutManager);

        SearchResultAdapter.RecyclerViewClickListener listener = new SearchResultAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                if (position >= 0) {
                    SearchInfo info = mSearchResults.get(position);
                    gAppEnv.getGposLogger().d("SearchResult: item slected  name= "+info.name);
                    Intent itemdetailsactivity = new Intent(SearchProductActivity.this, StoreProductsActivity.class);
                    itemdetailsactivity.putExtra("vendorID", selVendorId);
                    itemdetailsactivity.putExtra("productid", info.prodid);
                    startActivity(itemdetailsactivity);
                }
            }
        };
        mAdapter = new SearchResultAdapter(mContext, mSearchResults, R.layout.search_result_item, listener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new OrderListDividerDecoration(this, LinearLayoutManager.VERTICAL, 16));
        edsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                mProductMgr.getProductSearchedItems(query, mSearchResults);
                mAdapter.notifyDataSetChanged();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mProductMgr.getProductSearchedItems(newText, mSearchResults);
                gAppEnv.getGposLogger().i(" search result ciount= "+mSearchResults.size());
                mAdapter.notifyDataSetChanged();
                return false;
            }
        });

        if ((selSearch != null) && (!selSearch.isEmpty())) {
            edsearch.setQuery(selSearch,true);
            edsearch.clearFocus();
        }
        //Bottom Navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation_search);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Integer cartcnt = gAppEnv.getCartCaount();//getCartManager().getCartSize();
        if (cartcnt > 0) {
            navigation.getMenu().getItem(1).setTitle("Cart : (" + cartcnt + ")" );
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_dtl_nav_home:
                    Intent i = new Intent(SearchProductActivity.this, MainActivity.class);
                    startActivity(i);
                    return true;
                case R.id.item_dtl_nav_cart:
                    if (gAppEnv.getCartCaount() > 0) {
                        startActivity(new Intent(SearchProductActivity.this, OrderCartActivity.class));
                    } else
                        Toast.makeText(mContext, "Cart is empty. ", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };
}