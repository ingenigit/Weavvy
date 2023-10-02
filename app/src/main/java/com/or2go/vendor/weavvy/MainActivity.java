package com.or2go.vendor.weavvy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout.setupWithViewPager(viewPager);
        SetUpViewPager(viewPager);
    }

    private void SetUpViewPager(ViewPager viewpager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.AddFragment(new ListedStoreFragment(), "Listed Store");
        viewPagerAdapter.AddFragment(new GoogleStoreFragment(), "Google Store");
        viewpager.setAdapter(viewPagerAdapter);
    }
    //inner class
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> pageTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitle.get(position);
        }

        public void AddFragment(Fragment fragment, String storeName) {
            fragmentList.add(fragment);
            pageTitle.add(storeName);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}