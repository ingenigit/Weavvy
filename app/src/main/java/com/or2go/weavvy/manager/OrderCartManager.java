package com.or2go.weavvy.manager;

import static com.or2go.core.Or2GoMsgValues.OR2GO_MSG_CUSTOMER_ORDER_PLACE;
import static com.or2go.core.Or2goConstValues.DISCOUNT_AMOUNT_TYPE_VALUE;
import static com.or2go.core.Or2goConstValues.OR2GO_COMM_ASYNC_API;
import static com.or2go.core.Or2goConstValues.OR2GO_ITEM_STOCK_VAL;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_DELIVERY;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDERTYPE_PICKUP;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_IMG_STATUS_PENDING;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_ITEM_DETAIL_IMG;
import static com.or2go.core.Or2goConstValues.OR2GO_ORDER_REQ;
import static com.or2go.core.Or2goConstValues.OR2GO_PAY_MODE_COD;
import static com.or2go.core.Or2goConstValues.ORDER_CART_STATUS_COMPLETE;
import static com.or2go.core.Or2goConstValues.ORDER_CART_STATUS_NONE;
import static com.or2go.core.Or2goConstValues.ORDER_CART_STATUS_REQUEST;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_CONFIRMED;
import static com.or2go.core.Or2goConstValues.ORDER_STATUS_REQUEST;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.or2go.core.CartItem;
import com.or2go.core.DeliveryAddrInfo;
import com.or2go.core.DiscountInfo;
import com.or2go.core.Or2GoStore;
import com.or2go.core.Or2goOrderInfo;
import com.or2go.core.ProductInfo;
import com.or2go.core.ProductSKU;
import com.or2go.core.UnitManager;
import com.or2go.mylibrary.CartDBHelper;
import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.or2go.weavvy.DeliveryChargeViewModel;
import com.or2go.weavvy.activity.OrderDetailsActivity;
import com.or2go.weavvy.server.OrderRequestCallback;
import com.or2go.weavvy.server.StockStatusCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderCartManager {

    private Context mContext;
    // Get Application super class for global data
    AppEnv gAppEnv;
    Integer gOrderRequestId;
    private final ArrayList<CartItem> gOrderList;
    private DiscountInfo selCoupon=null;
    private Float mSubTotal;
    private Float mActualSubTotal;
    private Float mDiscount;
    private Float mTax;
    private Float mDeliveryCharge;
    private Integer mOrderType;         //Delivery or Pickup
    private String mDeliveryAddrName="";
    private String mDeliveryLocation="";
    private boolean mOrderRequestPending=false;
    private int nCurRequestId;
    private String sCurStore = "";
    private Integer nOrderRequestState=ORDER_CART_STATUS_NONE;
    private Or2goOrderInfo mReqOrderInfo=null;
    private CartDBHelper cartDB;
    private int mCartUpdateStatus;
    Integer mStockCheckStatus;
    UnitManager mUnitMgr = new UnitManager();
    //formatting sales amounts
    DecimalFormat df;
    public static final int CART_STOCK_CHECK_NONE = 0;
    public static final int CART_STOCK_CHECK_REQUEST = 1;
    public static final int CART_STOCK_CHECK_ERROR = 2;
    public static final int CART_STOCK_CHECK_COMPLETE = 3;

    public OrderCartManager(Context context) {
        mContext =context;
        //Get global application
        gAppEnv = (AppEnv)context;
        gOrderList = new ArrayList <CartItem>();
        gOrderRequestId = gAppEnv.gAppSettings.getOrderReqId();
        mSubTotal = new Float(0);
        mActualSubTotal = new Float(0);
        mTax = new Float(0);
        mDiscount = new Float(0);
        mDeliveryCharge = new Float(-1);
        mOrderType = 0;  //Not selected
        nCurRequestId = 0;
        mStockCheckStatus=CART_STOCK_CHECK_NONE;
        df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_DOWN);
        cartDB = new CartDBHelper(mContext);
        cartDB.InitCartDB();
        updatePendingVendor();
    }

    private void updatePendingVendor() {
        int itemcnt = getSavedCartCount();
        if ( itemcnt> 0) {
            sCurStore = cartDB.getCartStore();
            Log.i("OrderCartManager", "pending cart ...  vendor="+sCurStore);
            Or2GoStore storeinfo = gAppEnv.getStoreManager().getStoreById(sCurStore);
            Log.i("OrderCartManager", "updating pending vendor="+sCurStore);
            updatePendingCart();
            String deliname = cartDB.getDeliveryAddrName();
            if (!deliname.isEmpty()) {
                mOrderType=OR2GO_ORDERTYPE_DELIVERY;
                mDeliveryAddrName = deliname;
                DeliveryAddrInfo deliaddr = gAppEnv.getDeliveryManager().getAddrInfo(deliname);
                if (deliaddr!= null) {
                    mDeliveryAddrName = deliname;
                    mDeliveryLocation = deliaddr.place;
                }
            }
            else {
                mOrderType=OR2GO_ORDERTYPE_PICKUP;
            }
        }
    }

    public void updatePendingCart() {
        Log.i("OrderCartManager", "updatependingcart called");
        int itemcnt = getSavedCartCount();
        if ( itemcnt> 0) {
            ArrayList <CartItem> savedlist = cartDB.getCartItems();
            String vendor = cartDB.getCartStore();
            ProductManager prdMgr = gAppEnv.getStoreManager().getProductManager(vendor);
            for(int i=0;i < itemcnt; i++) {
                CartItem sitem = savedlist.get(i);
                ProductInfo prdInfo = prdMgr.getProductInfo(sitem.getId());
                if (prdInfo == null) {
                    Log.i("OrderCartManager", "pending cart item not found ....clearing cart!!!");
                    clearCart();
                }
                else {
                    sitem.setProductInfo(prdInfo);
                    gOrderList.add(sitem);
                }
            }
            updateSubTotal();
        }
    }

    private int getSavedCartCount()
    {
        return cartDB.getItemCount();
    }

    public synchronized boolean addNewItem(int id, String storeid, /*ProductPriceInfo priceinfo,*/ ProductSKU skuinfo) {
        ProductManager prdMgr = gAppEnv.getStoreManager().getProductManager(storeid);
        ProductInfo prdInfo = prdMgr.getProductInfo(id);
        if (prdInfo==null)
            System.out.println("Order Item Product Info NULL ERROR !!!");
        int skuid;
        if (skuinfo == null) skuid =0;
        else
            skuid = skuinfo.mSKUId;

        CartItem newitem = new CartItem(id, prdInfo.name, skuinfo.mPrice, Float.valueOf("1"),
                                            skuinfo.mUnit, skuid, prdInfo.imagepath, prdInfo.taxincl, prdInfo.taxrate);
        newitem.setProductInfo(prdInfo);
        gOrderList.add(newitem);
        Float itemtotal = newitem.getItemTotal();
        boolean insres = cartDB.insertItem(newitem.getId().toString(),newitem.getName(),
                newitem.getPrice().toString(),
                newitem.getQnty(), mUnitMgr.getUnitName(newitem.getOrderUnit()),
                newitem.getSKUId(),
                newitem.getImagePath(),
                newitem.taxInclusive, newitem.getTaxRate());
        if (insres==true) {
            if (sCurStore.isEmpty()) {
                sCurStore = storeid;
                cartDB.insertStore(storeid);
            }
            updateSubTotal();
            return true;
        }
        else {
            System.out.println("Cart Manager : Add item failed !!!");
            gOrderList.remove(newitem);
            return false;
        }
    }

    public boolean incItemQnty(int itemid, int skuid) {
        int i=0;
        int salelistsz = gOrderList.size();
        for(i=0;i<salelistsz;i++) {
            CartItem saleitem = gOrderList.get(i);
            System.out.println("CartManager IncQnty list itemid="+saleitem.getId()+" skuid="+saleitem.getSKUId());
            if ((saleitem.getId() == itemid) && (saleitem.getSKUId() == skuid)) {
                saleitem.addQuantity(Float.valueOf("1"), saleitem.getOrderUnit());
                saveItemQnty(saleitem.getId().toString(), skuid, saleitem.getQnty());
                updateSubTotal();
                return true;
            }
        }
        return false;
    }

    public boolean decItemQnty(int itemid, int skuid) {
        int i=0;
        int salelistsz = gOrderList.size();
        for(i=0;i<salelistsz;i++) {
            CartItem saleitem = gOrderList.get(i);
            //if present decrease quantity
            if ((saleitem.getId() == itemid) && (saleitem.getSKUId() == skuid)) {
                saleitem.removeQuantity(Float.valueOf("1"), saleitem.getOrderUnit());
                saveItemQnty(saleitem.getId().toString(), skuid, saleitem.getQnty());
                if (saleitem.getQntyVal() == 0) deleteItem(saleitem.getId(), skuid);
                updateSubTotal();
                return true;
            }
        }
        return false;
    }

    public boolean saveItemQnty(String itemid, Integer packid, String qnty) {
        gAppEnv.getGposLogger().i(" Saving Cart Items="+itemid + "packid="+packid+"quantity to="+qnty);
        cartDB.updateItemQnty(itemid, packid, qnty);
        return true;
    }

    public boolean deleteItem(int itemid, int skuid) {
        int i=0;
        int salelistsz = gOrderList.size();
        //check if the item is already present
        for(i=0;i<salelistsz;i++) {
            CartItem saleitem = gOrderList.get(i);
            if ((saleitem.getId() == itemid) && (saleitem.getSKUId() == skuid)){
                gOrderList.remove(i);
                cartDB.deleteItem(saleitem.getId().toString(), skuid);
                break;
            }
        }
        updateSubTotal();
        if (isCartEmpty()) {
            sCurStore = "";
            clearCart();
        }
        return true;
    }

    public boolean updateSubTotal() {
        int itemcnt = gOrderList.size();
        mSubTotal = Float.parseFloat("0");
        mActualSubTotal = Float.parseFloat("0");
        for (int i=0;i<itemcnt;i++) {
            CartItem item = gOrderList.get(i);
            mSubTotal += item.getItemTotal();
            mActualSubTotal += item.getItemActualTotal();
        }
        if (selCoupon!= null) mDiscount = calcDiscount();
        mTax = calcExlusiveTax();
        return true;
    }

    public String getCartItemTotal()
    {
        return df.format(mSubTotal);//mSubTotal.toString();
    }

    public String getCartActualItemTotal()
    {
        return df.format(mActualSubTotal);
    }

    public String getCartTotal() {
        Float  tot;
        DecimalFormat dftot = new DecimalFormat("0");
        dftot.setRoundingMode(RoundingMode.HALF_DOWN);
        if (mDeliveryCharge < 0)
            tot = mSubTotal + mTax - mDiscount;
        else
            tot = mSubTotal + mTax + mDeliveryCharge - mDiscount;
        return dftot.format(tot);//tot.toString();
    }

    public int getCartSize()
    {
        return gOrderList.size();
    }

    public Float getExclusiveTaxTotal() {
        return mTax;
    }

    public ArrayList<CartItem> getOrderList()
    {
        return gOrderList;
    }

    //Order Type
    public void setOrderType(Integer ordtype) { mOrderType=ordtype;}

    public Integer getOrderType() { return mOrderType;}

    //Discount Coupon
    public void setSelectedCoupon(DiscountInfo discinfo) {
        selCoupon = discinfo;
        //calculate discount
        mDiscount = calcDiscount();
        //adjust exclusive tax after discount
        mTax = calcExlusiveTax();
    }

    public void clearSelectedCoupon() {
        selCoupon = null;
        mDiscount = Float.parseFloat("0");
        //adjust exclusive tax after discount
        mTax = calcExlusiveTax();
    }

    public DiscountInfo getSelectedCoupon()
    {
        return selCoupon;
    }

    public boolean isCouponSelected()
    {
         return ((selCoupon == null) ? false: true);
    }

    public boolean recalculateCoupon() {
        if (mSubTotal < selCoupon.minsaleamnt) {
            clearSelectedCoupon();
            return false;
        }
        mDiscount = calcDiscount();
        mTax = calcExlusiveTax();
        return true;
    }

    public boolean isDeliveryAddrNameSet() {
        if (mDeliveryAddrName.isEmpty())
            return false;
        else
            return true;
    }

    public String getDeliveryAddrName(){ return mDeliveryAddrName; }

    public boolean setDeliveryAddrName(String locname, DeliveryChargeViewModel chargeviewmodel) {
        DeliveryAddrInfo deliaddr = gAppEnv.getDeliveryManager().getAddrInfo(locname);
        if (deliaddr!= null) {
            if (!mDeliveryAddrName.isEmpty()) {
                cartDB.clearDeliveryAddrName();
            }
            mDeliveryAddrName = locname;
            mDeliveryLocation = deliaddr.place;
            Or2GoStore storeinfo = gAppEnv.getStoreManager().getStoreById(sCurStore);
            String sDeliCharge = gAppEnv.getDeliveryManager().getDeliveryCharge(chargeviewmodel,storeinfo.geolocation, deliaddr, 0);
            System.out.println("Deliver charge for address=" + locname + "  charge=" + sDeliCharge);
            if (!sDeliCharge.isEmpty()) {
                    mDeliveryCharge = Float.parseFloat(sDeliCharge);
            }
            cartDB.insertDeliveryAddrName(locname);
            return true;
        }
        System.out.println("not valid delivery name...."+locname);
        return false;
    }

    public boolean clearDeliveryAddress() {
        mDeliveryAddrName="";
        mDeliveryLocation="";
        mDeliveryCharge=Float.valueOf("0");
        cartDB.clearDeliveryAddrName();
        return true;
    }

    public void setDeliveryChargeVal(Float delicharge) {mDeliveryCharge=delicharge;}

    public String getDeliveryCharge()
    {
            return mDeliveryCharge.toString();
    }

    public Float getDeliveryChargeVal()
    {
        return mDeliveryCharge;
    }

    public void updateDeliveryCharge(DeliveryChargeViewModel chargeviewmodel) {
        if (!mDeliveryAddrName.isEmpty()) {
            DeliveryAddrInfo deliaddr = gAppEnv.getDeliveryManager().getAddrInfo(mDeliveryAddrName);
            Or2GoStore storeinfo = gAppEnv.getStoreManager().getStoreById(sCurStore);

            if (deliaddr != null) {
                //String sDeliCharge = gAppEnv.getDeliveryManager().getDeliveryCharge(deliaddr.place, sCurStore, gOrderList);
                String sDeliCharge = gAppEnv.getDeliveryManager().getDeliveryCharge(chargeviewmodel,storeinfo.geolocation, deliaddr, 0);
                if (!sDeliCharge.isEmpty()) {
                    mDeliveryCharge = Float.parseFloat(sDeliCharge);
                    //mDeliChargeFinal = true;
                }
            }

        }
    }
    ///Private calculation functions
    private Float calcDiscount() {
        if (selCoupon == null) return Float.valueOf("0");

        if (selCoupon.mdAmntType == DISCOUNT_AMOUNT_TYPE_VALUE) {
            return selCoupon.mdValue;
        }
        else {
            Float discval;
            Float roundedval = Float.parseFloat(df.format((mSubTotal/100) * selCoupon.mdValue));
            if ((selCoupon.maxdiscamnt > 0) && (roundedval > selCoupon.maxdiscamnt))
                discval = selCoupon.maxdiscamnt;
            else
                discval = roundedval;
            return discval;
        }
    }

    private Float calcExlusiveTax() {
        int itemcnt = gOrderList.size();
        System.out.println("cal Exlusice Tax" + itemcnt);
        Float extax = Float.parseFloat("0");
        for (int i=0;i<itemcnt;i++) {
            System.out.println("cal Exlusice Tax" + gOrderList.get(i).isTaxInclusive());
            System.out.println("cal rate" + gOrderList.get(i).getTaxRate());
            CartItem item = gOrderList.get(i);
            if (!item.isTaxInclusive()) {
                Float rate = item.getTaxRate();
                Float subtot = item.getItemTotal();
                System.out.println("rate" + rate);
                Float itemdiscper = (item.getItemTotal()/mSubTotal) * 100;
                Float itemdiscamnt = (mDiscount * itemdiscper) / 100;
                Float itx = ((subtot-itemdiscamnt) / 100) * rate;
                extax += itx;
            }
        }
        Float roundedtax = Float.parseFloat(df.format(extax));
        return roundedtax;
    }

    public  Float getDiscountAmount() {
        return mDiscount;
    }

    public String getCurrentVendor()
    {
        return sCurStore;
    }

    public String getCurrentStore()
    {
        return sCurStore;
    }

    public void setCurrentVendor(String vendor)
    {
        sCurStore=vendor;
    }

    public void setCurrentStore(String vendor)
    {
        sCurStore=vendor;
    }

    public void setStockCheckStatus(Integer sts) {mStockCheckStatus=sts;}

    public Integer getStockCheckStatus() {return mStockCheckStatus;}

    public boolean isStockCheckComplete(){
        if (mStockCheckStatus==CART_STOCK_CHECK_COMPLETE) return true;
        else return false;
    }

    public boolean isStockCheckError(){
        if (mStockCheckStatus==CART_STOCK_CHECK_ERROR) return true;
        else return false;
    }

    public boolean isCartEmpty() {
        if (gOrderList.size() > 0)
            return false;
        else
            return true;
    }


    public synchronized boolean clearCart() {
        gOrderList.clear();
        mSubTotal = new Float(0);
        mActualSubTotal = new Float(0);
        mDiscount = new Float(0);
        mTax = new Float(0);
        mDeliveryCharge = new Float(-1);
        mOrderType=0;
        sCurStore = "";
        mDeliveryAddrName="";
        mDeliveryLocation="";
        mReqOrderInfo=null;
        nCurRequestId=0;
        selCoupon = null;
        nOrderRequestState = ORDER_CART_STATUS_NONE;
        mStockCheckStatus=CART_STOCK_CHECK_NONE;
        ///database clean cleans cart, current vendor , delivery location
        cartDB.clearCart();
        return true;
    }

    public CartItem getOrderPackItemById(int prodid, int skuid) {
        int listsz = gOrderList.size();
        for(int i=0; i< listsz;i++) {
            CartItem ori = gOrderList.get(i);
            if ((ori.getId() == prodid) && (ori.getSKUId() == skuid)) {
                return  ori;
            }
        }
        return null;
    }

    public boolean updateSKUStockVal(int skuid, Float val) {
        int listsz = gOrderList.size();
        for(int i=0; i< listsz;i++) {
            CartItem ori = gOrderList.get(i);
            if (ori.getSKUId() == skuid) {
                ori.curStock = val;
                return true;
            }
        }
        return false;
    }

    private synchronized Integer getNextRequestId() {
        gOrderRequestId++;
        gAppEnv.gAppSettings.setOrderReqId(gOrderRequestId);
        return gOrderRequestId;
    }

    public boolean isStockNotAvailable(Integer inventoryControl) {
        for(int i=0;i<gOrderList.size();i++) {
            CartItem item = gOrderList.get(i);
            if ((inventoryControl > 0) && (item.getCurStock() >= 0)) {
                Float stkval = item.getCurStock();
                if ((stkval == 0) || (stkval < item.getQntyVal())) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean validateStockAvailability() {
        Or2GoStore storeinfo = gAppEnv.getStoreManager().getStoreById(getCurrentVendor());
        JSONArray packidarr = new JSONArray();
        for(int i=0;i<gOrderList.size();i++) {
            CartItem item = gOrderList.get(i);
            if (storeinfo.getInventoryControl() >0) {
                try {
                    JSONObject sku = new JSONObject();
                    sku.put("skuid", item.getSKUId());
                    packidarr.put(sku);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Stock Check SKU ID List="+packidarr.toString());
        if (packidarr.length() <= 0)
            return false;
        Message msg = new Message();
        msg.what = OR2GO_ITEM_STOCK_VAL;    //fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        StockStatusCallback stockcb = new StockStatusCallback(gAppEnv);//Callback(mContext);
        stockcb.setVendorId(storeinfo.vId);

        Bundle b = new Bundle();
        b.putParcelable("callback", stockcb);
        b.putString("storeid", sCurStore);
        b.putString("skuidlist", packidarr.toString());
        msg.setData(b);

        gAppEnv.getCommMgr().postMessage(msg);
        mStockCheckStatus=CART_STOCK_CHECK_REQUEST;
        return  true;
    }

    public synchronized Integer placeOrder(DeliveryAddrInfo deliinfo, String custreq, String reqtime, Integer itemdetailstype) {
        String ordertime = gAppEnv.getCurTime();
        boolean acptDeliveryPlan=true;

        Or2GoStore storeinfo = gAppEnv.getStoreManager().getStoreById(sCurStore);

        mReqOrderInfo = new Or2goOrderInfo( "", mOrderType, sCurStore, gAppEnv.gAppSettings.getUserId(), ORDER_STATUS_REQUEST,ordertime,
                mSubTotal.toString(),  mDeliveryCharge.toString(), getCartTotal(), mDiscount.toString(), deliinfo.getCompleteAddress(), deliinfo.getPlace(), OR2GO_PAY_MODE_COD, custreq);
        mReqOrderInfo.setVendorName(storeinfo.vName);
        mReqOrderInfo.setTax(mTax.toString());
        mReqOrderInfo.setRequestTime(reqtime);
        mReqOrderInfo.setItemDetailsType(itemdetailstype);
        mReqOrderInfo.setDeliveryAddrInfo(deliinfo);
        mReqOrderInfo.setRequestTime(reqtime);

        if (itemdetailstype==OR2GO_ORDER_ITEM_DETAIL_IMG)
            mReqOrderInfo.setOrderImageStatus(OR2GO_ORDER_IMG_STATUS_PENDING);

        Integer reqid = getNextRequestId();

        JSONObject orderPkt = new JSONObject();
        JSONObject pktHeader = new JSONObject();
        try {
            pktHeader.accumulate("pktType", OR2GO_MSG_CUSTOMER_ORDER_PLACE);
            pktHeader.accumulate("ordCustomerId", gAppEnv.gAppSettings.getUserId());
            pktHeader.accumulate("ordAppId", BuildConfig.OR2GO_APPID);
            pktHeader.accumulate("ordStoreId", storeinfo.vId);
            pktHeader.accumulate("ordReqId", reqid);
            pktHeader.accumulate("ordTime", ordertime);
            pktHeader.accumulate("ordType", mOrderType);
            pktHeader.accumulate("ordSubtotal", mSubTotal.toString());
            pktHeader.accumulate("ordDeliveryCharge", mDeliveryCharge);
            pktHeader.accumulate("ordDiscount", mDiscount);
            pktHeader.accumulate("ordTaxAmount", mTax);
            pktHeader.accumulate("ordGrandTotal", getCartTotal());
            pktHeader.accumulate("ordItemList", getOrderDetailsText());
            pktHeader.accumulate("ordDeliveryPlace", deliinfo.getPlace());
            pktHeader.accumulate("ordDeliveryAddress", deliinfo.getCompleteAddress());
            pktHeader.accumulate("ordRequestTime", reqtime);
            pktHeader.accumulate("ordCustRequest", custreq);
            pktHeader.accumulate("ordInvControl", storeinfo.getInventoryControl());
            orderPkt.put("Header", pktHeader);
            gAppEnv.getGposLogger().d("Order Header Json String: " + pktHeader.toString());
        }
        catch(Exception e) {
            gAppEnv.getGposLogger().d("Exception: "+e.getMessage());
        }

        Message msg = new Message();
        msg.what = OR2GO_ORDER_REQ;	//fixed value for sending sales transaction to server
        msg.arg1 = OR2GO_COMM_ASYNC_API;

        OrderRequestCallback ordercb = new OrderRequestCallback(mContext);//Callback(mContext);
        //productcb.setVendorId(vinfo.vId);
        Bundle b = new Bundle();

        b.putString("orderpkt", orderPkt.toString());
        b.putString("storeid", storeinfo.vId);
        b.putParcelable("callback", ordercb );
        msg.setData(b);
        gAppEnv.getCommMgr().postMessage(msg);

        nOrderRequestState = ORDER_CART_STATUS_REQUEST;
        nCurRequestId = reqid;
        return reqid;
    }

    public String getOrderDetailsText() {
        String itemlistdesc="";
        JSONArray jsonArrOrderDetails = new JSONArray();
        for(int i=0;i<gOrderList.size();i++) {
            CartItem item = gOrderList.get(i);
            JSONObject jsonObject = new JSONObject();
            try {
                gAppEnv.getGposLogger().d("Item Name:"+ item.getName()+ " Brand:"+item.getBrandName()+" SKU Id;"+item.getSKUId());
                jsonObject.accumulate("itemid", item.getId());
                jsonObject.accumulate("itemname", item.getName());
                jsonObject.accumulate("brandname", item.getBrandName());
                jsonObject.accumulate("price", item.getPrice());
                jsonObject.accumulate("discount", "");
                jsonObject.accumulate("quantity", item.getQnty());
                jsonObject.accumulate("unit", item.getOrderUnit());
                jsonObject.accumulate("skuid", item.getSKUId());
                jsonArrOrderDetails.put(jsonObject);
            }
            catch(Exception e) {
                gAppEnv.getGposLogger().d("Exception: "+e.getMessage());
            }
        }
        itemlistdesc =jsonArrOrderDetails.toString();
        gAppEnv.getGposLogger().d("Order Details Json String: " + itemlistdesc);
        return itemlistdesc;
    }

    public void completeCartOrder(Integer reqid, String orderid, Integer ordsts, String ordtime) {
        gAppEnv.getGposLogger().d("Order Placed : " + reqid+ " Order Id:"+orderid+ " ReqId="+nCurRequestId+" Time"+ordtime);
        if (nCurRequestId == reqid) {
            mReqOrderInfo.setOrderId(orderid);
            mReqOrderInfo.setOrderTime(ordtime);
            mReqOrderInfo.setStatus(ordsts);//ORDER_STATUS_PLACED);
            gAppEnv.getOrderManager().addCartOrder(mReqOrderInfo, gOrderList);
            nOrderRequestState = ORDER_CART_STATUS_COMPLETE;
            if (ordsts == ORDER_STATUS_CONFIRMED){
                gAppEnv.getOrderManager().boradcastStatusUpdate();
                Intent moveIntent = new Intent(mContext, OrderDetailsActivity.class);
                moveIntent.putExtra("orderid", orderid);
                String notmsg = "Order ID:" + orderid + " is confirmed !!" ;
                gAppEnv.getNotificationManager().setNotification("Order Update", notmsg, moveIntent);
            }
        }
    }

    public boolean isOrderRequestComplete(Integer reqid) {
        gAppEnv.getGposLogger().d("OrderCartManager checking order status of request id: " + reqid + " CurReqId:"+nCurRequestId+"  status:"+nOrderRequestState);
        if ((nCurRequestId == reqid) && (nOrderRequestState == ORDER_CART_STATUS_COMPLETE))
            return true;
        else
            return false;
    }
}
