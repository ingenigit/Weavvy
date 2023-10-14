package com.or2go.weavvy;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeliveryChargeViewModel extends ViewModel {

    private MutableLiveData<String> mDeliCharge;

    public MutableLiveData<String> getDeliveryCharge()
    {
        if (mDeliCharge==null)
        {
            mDeliCharge = new MutableLiveData<String>();
            mDeliCharge.setValue("-1");
        }

        return mDeliCharge;
    }
}
