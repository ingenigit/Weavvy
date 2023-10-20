package com.or2go.weavvy;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

public class ShortNoticeDialog extends Dialog implements View.OnClickListener{
    private Context mContext;
    private Dialog dialog;
    private ProgressBar progressBar;
    private ImageView imageView;
    Integer prog = 0;

    public ShortNoticeDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().getAttributes().windowAnimations = R.style.animation;
        setContentView(R.layout.custom_suggestion_dialog);
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM);
        windowParams.copyFrom(window.getAttributes());
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(windowParams);
        setCancelable(false);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setMax(100);
        progressBar.setProgress(prog);
        imageView = (ImageView) findViewById(R.id.imageViewCancel);
        imageView.setOnClickListener(this);
        new UpdateProgressBar().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageViewCancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    public class UpdateProgressBar extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            prog = 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while(prog < 100){
                prog += 3;
                publishProgress(prog);
                SystemClock.sleep(100);
            }
            dismiss();
            return null;
        }
    }
}


