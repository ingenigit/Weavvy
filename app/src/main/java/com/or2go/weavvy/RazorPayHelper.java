package com.or2go.weavvy;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.or2go.core.Or2goOrderInfo;
import com.razorpay.Checkout;

import org.json.JSONObject;

import java.util.Currency;

public class RazorPayHelper {

    Context mContext;
    AppEnv gAppEnv;
    Activity mActivity = null;

    int payorderid;

    Or2goOrderInfo payOr2goInfo;
    Currency currency;
    //TextView orerid;
    //TextView orderamt;

    public RazorPayHelper(Context context, AppEnv appenv, Activity activity, Or2goOrderInfo or2goinfo)
    {
        mContext = context;
        gAppEnv = appenv;

        mActivity=activity;

        payOr2goInfo = or2goinfo;

         /*
         To ensure faster loading of the Checkout form,
          call this method as early as possible in your checkout flow.
         */
        Checkout.preload(mActivity.getApplicationContext());
    }

    public void startPayment() {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */

        final Checkout co = new Checkout();
        co.setImage(R.drawable.nodatafound);

        try {
            JSONObject options = new JSONObject();
            options.put("name", BuildConfig.APP_NAME);
            options.put("description", "Charges for Order#"+ payOr2goInfo.getId());
            options.put("currency", "INR");
            options.put("amount", getRazorPayTotal());

            JSONObject preFill = new JSONObject();
            preFill.put("email", gAppEnv.gAppSettings.getUserEmail());
            preFill.put("contact", gAppEnv.gAppSettings.getUserId());

            options.put("prefill", preFill);

            co.open(mActivity, options);


        } catch (Exception e) {
            Toast.makeText(mContext, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }


    /**
     * The name of the function has to be
     * onPaymentSuccess
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    /*
    @SuppressWarnings("unused")
    public void onPaymentSuccess(String razorpayPaymentID,  PaymentData paymentData) {
        try {
            JSONObject paymentobject = paymentData.getData();
            Log.i("RazorPay", "Payment successfull: result data:" + paymentobject.toString());

            String resOrderId = paymentData.getOrderId();
            String resSignature = paymentData.getSignature();
            Log.i("RazorPay", "Payment successfull: orderid:" + resOrderId+ " signature:"+resSignature);
            //Toast.makeText(mActivity, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

            payOr2goInfo.setPayStatus(OR2GO_PAY_STATUS_LOCAL_COMPLETE);
            gAppEnv.getOrderManager().orderPaymentComplete(payOr2goInfo.getId(), OR2GO_PAY_STATUS_COMPLETE, razorpayPaymentID);


            PaymentResultDialog(true);


        } catch (Exception e) {
            Log.e("RazorPay", "Exception in onPaymentSuccess", e);
        }
    }*/

    /**
     * The name of the function has to be
     * onPaymentError
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    /*
    @SuppressWarnings("unused")
    public void onPaymentError(int code, String response,  PaymentData paymentData) {
        try {
            JSONObject paymentobject = paymentData.getData();
            Log.i("RazorPay", "Payment failure: result data:" + paymentobject.toString());
            //Toast.makeText(mActivity, "Payment failed: " + code + " " + response, Toast.LENGTH_SHORT).show();

            payOr2goInfo.setPayStatus(OR2GO_PAY_STATUS_FAILED_ONLINE);

            PaymentResultDialog(false);


        } catch (Exception e) {
            Log.e("RazorPay", "Exception in onPaymentError", e);
        }
    }*/

    private String getRazorPayTotal()
    {
        String retval="";
        String totval = payOr2goInfo.getTotal();
        gAppEnv.getGposLogger().i("Order value"+ totval );

        int sepidx = totval.indexOf(".");

        if (sepidx < 0) {
            gAppEnv.getGposLogger().i("Order value has no paisa val" );
            retval = totval+"00";

        }
        else
        {
            String valarr[] = totval.split("\\.");

            String pval = valarr[1];
            if(pval.length() == 1)
            {
                retval = valarr[0]+pval+"0";
            }
            else if (pval.length() == 2)
            {
                retval = valarr[0]+pval;
            }
            else if (pval.length() > 2)
            {
                retval = valarr[0]+pval.substring(0,1);
            }

        }

        gAppEnv.getGposLogger().i("RazorPay payment value"+retval );
        /*
        String valarr[] = totval.split(".");
        String pval = valarr[1];
        */
        return retval;
    }

    /*
    private void PaymentResultDialog(boolean result)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CompatAlertDialogStyle);
        builder.setTitle("DIGITAL PAYMENT");
        if (result)
        builder.setMessage("Your payment for is successfull. Thsnk you!");
        else
            builder.setMessage("Online payment failed.!!! Please verify.");
        builder.setCancelable(false);

        //builder.setPositiveButton("Exit", null);
        //builder.setNegativeButton("Retry", null);

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        builder.show();

    }*/
}
