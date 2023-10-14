package com.or2go.weavvy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestOptions;
import com.or2go.weavvy.activity.MainActivity;

public class SplashScreen extends AppCompatActivity {

    AppEnv gAppEnv;
    Context mContext;
    private Thread mInitThread;
    ImageView mAppLogo;
    Integer elapsedTime=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        gAppEnv = (AppEnv) getApplicationContext();
        mContext = this;

        mAppLogo =  (ImageView)findViewById(R.id.imgviewAppLogo);
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.blankitem)
                .error(R.drawable.blankitem);
//        Glide.with(mContext)
//                .load(BuildConfig.OR2GO_SERVER+"applogo/"+ BuildConfig.OR2GO_VENDORID+".png")
//                .apply(options)
////                .override(200, 200) // resizing
////                .fitCenter()
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                .into(mAppLogo);

        getData();
    }

    private void getData() {
        mInitThread = new Thread(new AppInitThread());
        mInitThread.start();
        if (!gAppEnv.isInternetOn())
            InternetCheckDialog();
        else
            runSplash();
    }

    private void InternetCheckDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CompatAlertDialogStyle);
        builder.setTitle("Connection Error");
        builder.setMessage("Please check your internet connectivity and retry.");
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

    private void monitorCommProgress() {
        final Handler mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            public void run() {
                elapsedTime +=1;
                if ((gAppEnv.isRegistered()) && (gAppEnv.getStoreManager() != null) && (gAppEnv.getStoreManager().isVendorListDone()) && (gAppEnv.isAppInitializationComplete())) {
                    gAppEnv.initCartManager();
                    mProgressHandler.removeCallbacks(null);
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                }
                else if ((!gAppEnv.isRegistered()) && (gAppEnv.getStoreManager() != null) && (gAppEnv.getStoreManager().isVendorListDone())) {
                    gAppEnv.initCartManager();
                    mProgressHandler.removeCallbacks(null);
                    startActivity(new Intent(SplashScreen.this,MainActivity.class));
                }
                else {
                    if (elapsedTime > 50) {
                        elapsedTime =0;
                        NetworkErrorDialog();
                        mProgressHandler.removeCallbacks(null);
                    }
                    else
                        mProgressHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

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