package com.or2go.weavvy;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

public class CustomDailogView extends Dialog implements View.OnClickListener {

    String title, body;
    TextView dialogTitle, dialogBody;
    Button positiveButton, negativeButton;
    Context mContext;
    onClickButton onClickButton;
    CardView negCardview;
    String pos, neg;
    Window window;
    boolean visible;

    public interface onClickButton {
        void onClick(View view);
    }

    public CustomDailogView(@NonNull Context context, String title, String body, String posText, String negText, boolean show, onClickButton clickButton) {
        super(context);
        this.mContext = context;
        this.title = title;
        this.body = body;
        this.pos = posText;
        this.neg = negText;
        this.visible = show;
        this.onClickButton = clickButton;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.getWindow().getAttributes().windowAnimations = R.style.animation;
        setContentView(R.layout.custom_dailog_view);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        window = this.getWindow();
        lp.copyFrom(window.getAttributes());
        //dialog with full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        setCancelable(false);
        initComponent();
        this.dialogTitle.setText(title);
        this.dialogBody.setText(body);
        this.positiveButton.setText(pos);
        this.negativeButton.setText(neg);
        if (!this.visible)
            this.negCardview.setVisibility(View.GONE);
        positiveButton.setOnClickListener(this);
        negativeButton.setOnClickListener(this);
    }

    private void initComponent() {
        dialogTitle = (TextView) findViewById(R.id.dailog_Title);
        dialogBody = (TextView) findViewById(R.id.dailog_Body);
        positiveButton = (Button) findViewById(R.id.positive_Btn);
        negativeButton = (Button) findViewById(R.id.negative_Btn);
        negCardview = (CardView) findViewById(R.id.negCarview);
    }

    @Override
    public void onClick(View v) {
        onClickButton.onClick(v);
//        switch (v.getId()) {
//            case R.id.positive_Btn:
//                Toast.makeText(mContext, "OK", Toast.LENGTH_SHORT).show();
//                break;
//            default:
//                break;
//        }
//        dismiss();
    }
}
