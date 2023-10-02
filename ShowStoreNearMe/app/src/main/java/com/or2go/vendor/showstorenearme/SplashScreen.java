package com.or2go.vendor.showstorenearme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {
    AppEnv gAppEnv;
    Context mContext;
    private Thread mInitThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        gAppEnv = (AppEnv) getApplicationContext();
        mContext = this;

        getData();
    }

    private void getData() {
        System.out.println("dkfdfndfdkjfndjk");
        mInitThread = new Thread(new AppInitThread());
        mInitThread.start();
        if (!gAppEnv.isInternetOn())
            Toast.makeText(gAppEnv, "no inteernrt", Toast.LENGTH_SHORT).show();
        else
            runSplash();
    }

    private void runSplash() {
        final Handler mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressHandler.removeCallbacks(null);
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
            }
        },2000);
    }

    class AppInitThread implements Runnable {
        @Override
        public void run() {
            gAppEnv.startEnv();
        }
    }

}