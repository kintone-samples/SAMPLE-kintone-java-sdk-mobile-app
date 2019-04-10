package com.cybozu.spacesoldier;

import android.app.Application;
import android.util.Log;

import com.cybozu.kintone.client.connection.Connection;


public class AppCommon extends Application {

    private static final String TAG = "AppCommon";
    private static com.cybozu.kintone.client.connection.Connection CONNECTION;
    private Integer APP_ID;
    private String APP_NAME;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    public Connection getCONNECTION() {
        return CONNECTION;
    }

    public void setCONNECTION(Connection connection) {
        CONNECTION = connection;
    }

    public Integer getAppID() {
        return this.APP_ID;
    }

    public void setAPP_ID(Integer APP_ID) {
        this.APP_ID = APP_ID;
    }

    public String getAPP_NAME() {
        return APP_NAME;
    }

    public void setAPP_NAME(String APP_NAME) {
        this.APP_NAME = APP_NAME;
    }
}
