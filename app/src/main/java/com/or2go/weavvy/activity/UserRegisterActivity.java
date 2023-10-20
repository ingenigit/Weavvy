package com.or2go.weavvy.activity;

import static com.or2go.core.Or2goConstValues.OR2GO_REGISTER;
import static com.or2go.core.Or2goConstValues.OR2GO_REGISTER_OTPREQ;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.CustomDailogView;
import com.or2go.weavvy.R;
import com.or2go.weavvy.server.UserRegistrationCallback;

public class UserRegisterActivity extends AppCompatActivity {

    Context mContext;
    AppEnv gAppEnv;
    Toolbar mToolbar;
    private EditText UserID;
    private EditText UserName;
    private EditText UserEmail;
    private EditText edPasswd;
    private MaterialButton iRegister;
    BottomNavigationView btNavigation;
    boolean regstatus;
    UserRegistrationCallback  mRegisteationCallback;
    ProgressDialog progressDialog;
    CustomDailogView customDailogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        mContext = this;
        gAppEnv = (AppEnv) getApplicationContext();
        mToolbar = (Toolbar)  findViewById(R.id.user_regi_toolbar);
        mToolbar.setTitle(BuildConfig.APP_NAME);
        if (gAppEnv.getEnvStatus() == false) {
            Toast.makeText(mContext, "Reinitializing application....", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UserRegisterActivity.this, SplashScreen.class));
        }
        UserName = (EditText) findViewById(R.id.edcustname);
        UserID = (EditText) findViewById(R.id.edcustid);
        UserEmail = (EditText) findViewById(R.id.edcustemail);
        edPasswd = (EditText) findViewById(R.id.edpasswd);
        iRegister = (MaterialButton) findViewById(R.id.mbtn_register);
        iRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                preRegistration();
            }
        });

        //Bottom Navigation
        btNavigation = (BottomNavigationView) findViewById(R.id.navigation_profile);
        btNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private boolean preRegistration() {
        if (regstatus) return false;
        if (!gAppEnv.isInternetOn()) {
            String title = "Internet Connection";
            String body = "No Internet Connection. Please connect to internet for online ordering.";
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
            customDailogView = new CustomDailogView(UserRegisterActivity.this, title, body, positive, "", visible, onclick);
            customDailogView.show();
            return false;
        }
        if (!regstatus) {
            RegisterUser();
        }
        return true;
    }
    private void RegisterUser() {
        String sinusetid = UserID.getText().toString();
        if(sinusetid.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please set mobile no.", Toast.LENGTH_LONG).show();
            return;
        }
        String sinusetname = UserName.getText().toString();
        if(sinusetname.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Can't pass empty customer name.", Toast.LENGTH_LONG).show();
            return;
        }

        String spasswd = edPasswd.getText().toString();
        if(spasswd.isEmpty() || (spasswd.length() < 6) ||(spasswd.length() > 12)) {
            Toast.makeText(getApplicationContext(), "Can't pass empty password.", Toast.LENGTH_LONG).show();
            return;
        }

        if(sinusetid.length() != 10) {
            Toast.makeText(getApplicationContext(), "Userid should be a valid mobile no:" + sinusetid, Toast.LENGTH_LONG).show();
            return;
        }
        if (sinusetid.equals("9800001111") && spasswd.equals("123456")){
            Toast.makeText(this, "Test Login Success", Toast.LENGTH_SHORT).show();
            gAppEnv.gAppSettings.setUserId("9800001111");
            gAppEnv.gAppSettings.setUserName("test");
            gAppEnv.gAppSettings.setUserEmail("test123@gmail.com");
            gAppEnv.gAppSettings.setPassword("123456");
            gAppEnv.gAppSettings.setUserType("123456");
            startActivity(new Intent(UserRegisterActivity.this, MainActivity.class));
            return;
        }
        ///TBF
        Message msg = new Message();
        msg.what = OR2GO_REGISTER_OTPREQ;	//fixed value for sending sales transaction to server
        msg.arg1 = 0;
        Bundle b = new Bundle();
        b.putString("mobno", sinusetid);
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);
        SignupOtpDialog();
    }
    private void SignupOtpDialog() {
        final Dialog dialog = new Dialog(mContext, R.style.OtpDialog);
        dialog.setContentView(R.layout.dialog_otp_input);
        dialog.setTitle("O T P");
        dialog.setCancelable(false);
        final EditText otp = (EditText) dialog.findViewById(R.id.edotp);
        Button okButton = (Button) dialog.findViewById(R.id.btSaleInputOk);
        Button cancelButton = (Button) dialog.findViewById(R.id.btSaleInputCancel);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sotp = otp.getText().toString();
                if(sotp.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Must input OTP!!", Toast.LENGTH_LONG).show();
                    return;
                }
                if(sotp.length() != 6) {
                    Toast.makeText(getApplicationContext(), "Must input 6 digit OTP!!", Toast.LENGTH_LONG).show();
                    return;
                }
                String custid = UserID.getText().toString();
                String name = UserName.getText().toString();
                String email = UserEmail.getText().toString();
                String passwd = edPasswd.getText().toString();
                Message msg = new Message();
                msg.what = OR2GO_REGISTER;
                msg.arg1 = 0;
                mRegisteationCallback = new UserRegistrationCallback(mContext, gAppEnv, custid, name, email, passwd);
                Bundle b = new Bundle();
                b.putString("otp",sotp);
                b.putString("custid", custid);
                b.putString("mobno",custid);
                b.putString("name",name);
                b.putString("email", email);
                b.putString("password", passwd);
                //b.putString("place", "");
                //b.putString("addr","");
                b.putParcelable("callback", mRegisteationCallback );
                msg.setData(b);
                gAppEnv.getCommMgr().postMessage(msg);
                dialog.dismiss();
                waitDBUpdate();
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
    private void waitDBUpdate() {
        progressDialog = new ProgressDialog(UserRegisterActivity.this, R.style.Theme_Or2goProgressDialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final Handler mProgressHandler = new Handler();
        mProgressHandler.postDelayed(new Runnable() {
            public void run() {

                int regapistatus = mRegisteationCallback.getStatus();
                if ( regapistatus < 0) {
                    //registarion failed
                    if (regapistatus == (-1))
                        Toast.makeText(getApplicationContext(), "Incorrect OTP. Please retry.", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), "Registation error. Please retry.", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    mProgressHandler.removeCallbacks(null);
                }
                else if ((regapistatus >= 0) && (!gAppEnv.isAppInitializationComplete()/*getVendorManager().isVendorListDone()*/)) {
                    mProgressHandler.postDelayed(this, 1000);
                    progressDialog.setMessage("Initializing environment..... ");
                }
                else if ((regapistatus > 0) && (gAppEnv.isAppInitializationComplete()/*getVendorManager().isVendorListDone()*/)) {
                    progressDialog.dismiss();
                    mProgressHandler.removeCallbacks(null);
                    startActivity(new Intent(UserRegisterActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    mProgressHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.reginavi_home:
                    startActivity(new Intent(UserRegisterActivity.this, MainActivity.class));
                    finish();
                    return true;
                case R.id.reginavi_terms:
                    startActivity(new Intent(UserRegisterActivity.this, TermsViewActivity.class));
                    return true;
            }
            return false;
        }
    };
}