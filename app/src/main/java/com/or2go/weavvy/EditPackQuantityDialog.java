package com.or2go.weavvy;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class EditPackQuantityDialog extends DialogFragment {
    String qnty;
    String name;
    int pkAmt;
    String unit;
    Integer nQnty;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_qnty_whole, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.animation;
        if (getArguments() != null ) {
            name = getArguments().getString("name");
            qnty = getArguments().getString("quantity");
            pkAmt = getArguments().getInt("pkamt");
            unit = getArguments().getString("unit");
            Float fqnty = Float.parseFloat(qnty);
            nQnty = fqnty.intValue();
        }
        final TextView edname = view.findViewById(R.id.title);
        final EditText edqnty = view.findViewById(R.id.edqnty);
        final ImageView imginc = view.findViewById(R.id.qntyincreasebt);
        final ImageView imgdec = view.findViewById(R.id.qntydecreasebt);
        edname.setText(name + " ["+pkAmt+unit+"]");
        edqnty.setText(nQnty.toString());
        imginc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nQnty++;
                edqnty.setText(nQnty.toString());
            }
        });

        imgdec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nQnty>0) nQnty--;
                edqnty.setText(nQnty.toString());
            }
        });

        Button btnDone = view.findViewById(R.id.edOK);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogListener dialogListener = (DialogListener) getActivity();
                dialogListener.onFinishEditQntyDialog(edqnty.getText().toString());
                dismiss();
            }
        });

        ImageView btnDelete = view.findViewById(R.id.edDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogListener dialogListener = (DialogListener) getActivity();
                dialogListener.onFinishEditQntyDialog("0");
                dismiss();
            }
        });

        Button btnCancel = view.findViewById(R.id.edCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface DialogListener {
        void onFinishEditQntyDialog(String qnty);
    }
}
