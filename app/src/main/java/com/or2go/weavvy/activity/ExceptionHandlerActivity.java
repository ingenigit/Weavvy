package com.or2go.weavvy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.or2go.weavvy.R;
import com.or2go.weavvy.SplashScreen;

public class ExceptionHandlerActivity extends AppCompatActivity {

    Context context;
    private Button btQuit;
    private Button btRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_handler);

        context = this;
        btQuit = (Button)findViewById(R.id.btAppExit);
        btQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });

        btRetry = (Button)findViewById(R.id.btAppRetry);
        btRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExceptionHandlerActivity.this, SplashScreen.class));
            }
        });
    }
}