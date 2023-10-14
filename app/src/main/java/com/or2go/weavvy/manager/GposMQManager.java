package com.or2go.weavvy.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.or2go.weavvy.AppEnv;
import com.or2go.weavvy.BuildConfig;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

public class GposMQManager {

    Context mContext;
    AppEnv gAppEnv;
    String mUserId;
    static final String EXCHANGE_NAME = "ortogo";
    static String QUEUE_NAME;// = "genipos.8895269273.queue";
    Connection gposConn;
    Channel		gposMsgChannel=null;
    String 		gposQueueName = "";
    Integer   mMqMsgNo=0;
    Thread mqStartThread;
    Thread mqStopThread;

    public GposMQManager(Context con) {
        mContext = con;
        gAppEnv = (AppEnv)mContext;
        mMqMsgNo=0;
        mUserId = gAppEnv.gAppSettings.getUserId();
        QUEUE_NAME = BuildConfig.OR2GO_APPID+"."+mUserId+".queue";
        mqStartThread = new Thread() {
            @Override
            public void run() {
                try {
                    initBrokerConnection();
                    while (true) {
                        Thread.sleep(3000);
                        if (gposMsgChannel!= null) {
                            registerConsumer(1000);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mqStartThread.start();
    }

    void initBrokerConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("139.144.15.150");
        factory.setUsername("otgadmin");
        factory.setPassword("Ing3ni*Ortogo");
        factory.setRequestedHeartbeat(10);
        factory.setConnectionTimeout(5000);
        factory.setNetworkRecoveryInterval(5000);
        factory.setAutomaticRecoveryEnabled(true);

        gposConn = factory.newConnection();
        gposMsgChannel = gposConn.createChannel();

        gposMsgChannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC,true);
        gposMsgChannel.queueDeclare(QUEUE_NAME, true, false, false, null);
        gposMsgChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "or2go.global");
        gposMsgChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, BuildConfig.OR2GO_APPID+"."+mUserId);
        gposMsgChannel.queueBind(QUEUE_NAME, EXCHANGE_NAME, BuildConfig.OR2GO_APPID+".global");
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
                gAppEnv.getGposLogger().d(" [x] Rabbitmq Msg Received '" + envelope.getRoutingKey() + "':'" + message + "'");
                Handler mHandler = gAppEnv.getGposMsgHandler();
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("GPOS", message);
                    msg.setData(bundle);
                    msg.what = getMqmMsgNo();
                    mHandler.sendMessage(msg);
                }
            }
        };
        gposMsgChannel.basicConsume(gposQueueName, true /* auto-ack */, consumer);
    }

    int getMqmMsgNo() {
        int newno = mMqMsgNo;
        mMqMsgNo++;
        return newno;
    }

    public void shutdownMQ() {
        mqStopThread = new Thread() {
            @Override
            public void run() {
                try {
                    if (gposConn != null)
                        gposConn.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (AlreadyClosedException c) {
                    c.printStackTrace();
                }
                catch (ShutdownSignalException s) {
                    s.printStackTrace();
                }
            }
        };

        mqStopThread.start();

    }
}
