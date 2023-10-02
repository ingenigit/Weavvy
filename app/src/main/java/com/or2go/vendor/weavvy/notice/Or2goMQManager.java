package com.or2go.vendor.weavvy.notice;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.or2go.vendor.weavvy.AppEnv;
import com.or2go.vendor.weavvy.storeList.StoreList;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Or2goMQManager {
    Context mContext;

    AppEnv gAppEnv;

    //String mMsgKeyName;

    //static final String EXCHANGE_NAME = "genipos";
    static final String EXCHANGE_NAME = "ortogo";
    //static final String QUEUE_NAME = "genipos.8895269273.queue";
    //static String QUEUE_NAME;// = "genipos.8895269273.queue";
    Connection gposConn;

    Channel gposMsgChannel=null;
    ArrayList<Channel> listMsgChannel;

    String 		gposQueueName = "";


    Integer   mMqMsgNo=0;

    public Or2goMQManager(Context con)
    {
        mContext = con;
        gAppEnv = (AppEnv)mContext;
        mMqMsgNo=0;

        listMsgChannel = new ArrayList<Channel>();

        //mMsgKeyName = gAppEnv.gAppSettings.getStoreId();

        //QUEUE_NAME = "genipos."+mUserId+".queue";
        //QUEUE_NAME = "ortogo."+mMsgKeyName+".queue";
        //Toast.makeText(mContext, "Initializing Messaging Manager: ",Toast.LENGTH_LONG).show();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    initBrokerConnection();
                    ///registerConsumer(1000);
                    while (true) {
                        Thread.sleep(3000);
                        //if (gposMsgChannel!= null)
                        if (listMsgChannel.size()>0) {
                            //registerConsumer(1000);
                            registerConsumerList(1000);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }


    void initBrokerConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("139.144.15.150");
        factory.setUsername("otgadmin");
        factory.setPassword("Ing3ni*Ortogo");
        /*factory.setHost("172.105.54.66");
        factory.setUsername("or2goadmin");
        factory.setPassword("ing3ni*or2go");*/
        factory.setRequestedHeartbeat(10);
        factory.setConnectionTimeout(5000);
        factory.setNetworkRecoveryInterval(5000);
        factory.setAutomaticRecoveryEnabled(true);
        //factory.setTopologyRecoveryEnabled(true);
        gposConn = factory.newConnection();
        ///gposMsgChannel = gposConn.createChannel();
        ///gposMsgChannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC,true);
        /*
        //durable, non-exclusive, non-autodelete queue with a well-known name
        gposMsgChannel.queueDeclare(QUEUE_NAME, true, false, false, null);
        gposMsgChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "ortogo."+mMsgKeyName);
        gposMsgChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "ortogo.global");
        */
        ArrayList<StoreList> storelist = gAppEnv.getStoreManager().getStoreList();
        for(int i=0; i<storelist.size();i++)
        {
            StoreList istore = storelist.get(i);

            //mMsgKeyName = gAppEnv.gAppSettings.getStoreId();
            //QUEUE_NAME = "ortogo."+mMsgKeyName+".queue";

            String storeid = istore.getStringID();

            //Create Channel For Store
            Channel iMQChannel = gposConn.createChannel();
            listMsgChannel.add(iMQChannel);
            iMQChannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC,true);


            //durable, non-exclusive, non-autodelete queue with a well-known name
            iMQChannel.queueDeclare("ortogo."+storeid+".queue", true, false, false, null);
            iMQChannel.queueBind("ortogo."+storeid+".queue", EXCHANGE_NAME, "ortogo."+storeid);

            System.out.println("MQManager: Adding Queue="+"ortogo."+storeid+".queue");

        }

        ////gposMsgChannel.queueBind("ortogo."+gAppEnv.gAppSettings.getVendorId()+".queue", EXCHANGE_NAME, "ortogo."+gAppEnv.gAppSettings.getVendorId());
    }

    void registerConsumer(final int timeout) throws IOException {

        //QueueingConsumer
        Consumer consumer = new DefaultConsumer(gposMsgChannel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {

                String message = new String(body, "UTF-8");
//                gAppEnv.getGposLogger().d(" [x] Rabbitmq Msg Received '" + envelope.getRoutingKey() + "':'" + message + "'");
                //Toast.makeText(mContext, "Rabbitmq Msg Received: ",Toast.LENGTH_LONG).show();
                ///GposAlert alert = new GposAlert();
                ///alert.alertType = "AMQP";
                //alert.alertData1 = "";
                //alert.alertMessage = message;
                ///gAppEnv.getAlertManager().setAlert(alert);
                Handler mHandler = gAppEnv.getOr2goMsgHandler();
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("RoutingKey", envelope.getRoutingKey());
                    bundle.putString("Or2Go", message);
                    msg.setData(bundle);
                    msg.what = getMqmMsgNo();
//                    mHandler.sendMessage(msg);
                }
                		/*
                		try {
                			Thread.sleep(timeout);
                		} catch (Exception e) {
                			//Toast.makeText(mContext, "Rabbitmq Consumer: Exception ",Toast.LENGTH_LONG).show();
                		}*/
            }
        };

        gposMsgChannel.basicConsume(gposQueueName, true /* auto-ack */, consumer);

    }


    void registerConsumerList(final int timeout) throws IOException {

        int chncnt = listMsgChannel.size();
        for(int i=0; i< chncnt ; i++){

            //QueueingConsumer
            Consumer consumer = new DefaultConsumer(listMsgChannel.get(i)) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {

                    String message = new String(body, "UTF-8");
//                    gAppEnv.getGposLogger().d(" [x] Rabbitmq Msg Received '" + envelope.getRoutingKey() + "':'" + message + "'");

                    Handler mHandler = gAppEnv.getOr2goMsgHandler();
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("RoutingKey", envelope.getRoutingKey());
                        bundle.putString("Or2Go", message);
                        msg.setData(bundle);
                        msg.what = getMqmMsgNo();
//                        mHandler.sendMessage(msg);
                    }

                }
            };

            listMsgChannel.get(i).basicConsume(gposQueueName, true /* auto-ack */, consumer);

        }


    }

    int getMqmMsgNo()
    {
        int newno = mMqMsgNo;

        mMqMsgNo++;

        return newno;
    }

    public void shutdownMQ()
    {
        try {

            int chncnt = listMsgChannel.size();
            for(int i=0; i< chncnt ; i++){
                Channel ch = listMsgChannel.get(i);
                ch.close();
            }
            gposConn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (TimeoutException te)
        {
            te.printStackTrace();
        }
    }
}
