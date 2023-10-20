package com.or2go.weavvy;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PublicNoticeDialog extends Dialog {

    public Activity activity;
    //public Dialog dialog;
    public Button cancel;
    TextView dtitle, dmsg;

    String mTitle, mMessage;

    public PublicNoticeDialog(Activity a, String title, String msg)
    {
        super(a);
        this.activity = a;
        mTitle = title;
        mMessage = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_public_notice);
        cancel = (Button) findViewById(R.id.selCancel);
        //no = (Button) findViewById(R.id.no);
        dtitle = findViewById(R.id.title);
        dmsg = findViewById(R.id.msg);

        dtitle.setText(mTitle);
        dmsg.setText(mMessage);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }



}
