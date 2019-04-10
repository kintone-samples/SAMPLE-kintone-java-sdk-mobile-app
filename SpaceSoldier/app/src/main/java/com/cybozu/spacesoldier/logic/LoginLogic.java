package com.cybozu.spacesoldier.logic;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.cybozu.kintone.client.authentication.Auth;
import com.cybozu.kintone.client.authentication.AuthenticationConstants;
import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.exception.KintoneAPIException;
import com.cybozu.kintone.client.model.app.AppModel;
import com.cybozu.kintone.client.model.authentication.Credential;
import com.cybozu.kintone.client.model.http.HTTPHeader;
import com.cybozu.kintone.client.module.app.App;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.activities.ViewRecordListActivity;
import com.cybozu.spacesoldier.data.strage.AppRegistrationDAO;
import com.cybozu.spacesoldier.data.strage.DatabaseHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class LoginLogic {

    AppCommon myApp;
    Activity myActivity;
    Auth auth = new Auth() {
        // SDKで利用しているbase64変換関数が利用できないため、上書きを行う
        @Override
        public ArrayList<HTTPHeader> createHeaderCredentials() {
            ArrayList<HTTPHeader> headers = new ArrayList<>();

            if (getPasswordAuth() != null) {
                Credential auth = getPasswordAuth();
                String passwordAuthString = auth.getUsername() + ":" + auth.getPassword();
                headers.add(new HTTPHeader(AuthenticationConstants.HEADER_KEY_AUTH_PASSWORD, Base64.encodeToString(passwordAuthString.getBytes(), Base64.DEFAULT)));
            }

            if (getApiToken() != null) {
                headers.add(new HTTPHeader(AuthenticationConstants.HEADER_KEY_AUTH_APITOKEN, getApiToken()));
            }

            if (getBasicAuth() != null) {
                Credential auth = getBasicAuth();
                String basicAuthString = auth.getUsername() + ":" + auth.getPassword();
                headers.add(new HTTPHeader(AuthenticationConstants.HEADER_KEY_AUTH_BASIC, AuthenticationConstants.AUTH_BASIC_PREFIX + Base64.encodeToString(basicAuthString.getBytes(), Base64.DEFAULT)));
            }
            return headers;
        }
    };

    private static AsyncTask asyncTask;

    public LoginLogic(Activity activity, AppCommon appCommon) {
        this.myActivity = activity;
        this.myApp = appCommon;
    }

    public byte[] convertStreamToBuffer(InputStream is) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int len = is.read(buffer);
        while (len != -1) {
            bout.write(buffer, 0, len);
            len = is.read(buffer);
        }
        return bout.toByteArray();
    }

    public void checkLoginStatus() throws KintoneAPIException {
        DatabaseHelper helper = new DatabaseHelper(myActivity.getApplicationContext());
        SQLiteDatabase myDB = helper.getReadableDatabase();
        AppRegistrationDAO dao = new AppRegistrationDAO(myDB);
        String sql = "select * from " + AppRegistrationDAO.getTableName();
        ArrayList<HashMap> result = dao.getData(sql);
        if (result.size() > 0) {
            HashMap registInfo = result.get(0);
            final Integer appId = Integer.parseInt(registInfo.get(AppRegistrationDAO.getColumnAppId()).toString());
            final String userName = registInfo.get(AppRegistrationDAO.getColumnUserName()).toString();
            final String password = registInfo.get(AppRegistrationDAO.getColumnPass()).toString();
            final String domain = registInfo.get(AppRegistrationDAO.getColumnDomain()).toString();

            InputStream is = null;
            final Object clientCertByteArr =  result.get(0).get(AppRegistrationDAO.getColumnClientCert());
            if (clientCertByteArr != null) {
                is = new ByteArrayInputStream((byte[]) clientCertByteArr);
            }
            final Object daoCertPass = result.get(0).get(AppRegistrationDAO.getColumnCertPass());

            final AppCommon appCommon = this.myApp;
            auth.setPasswordAuth(userName, password);
            if (is != null && daoCertPass != null) {
                auth.setClientCert(is, daoCertPass.toString());
            }
            final Connection connection = new Connection(domain, auth);
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
                    ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), msg.obj.toString());
                }
            };
            asyncTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] object) {
                    try {
                        App appManagement = new App(connection);
                        return appManagement.getApp(appId);
                    } catch (Exception e) {
                        final Message msg = new Message();
                        msg.obj = e;
                        new Thread(new Runnable() {
                            public void run() {
                                handler.sendMessage(msg);
                            }
                        }).start();
                        asyncTask.cancel(true);
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Object result) {
                    try {
                        if (result != null) {
                            AppModel app = (AppModel) result;
                            appCommon.setAPP_ID(appId);
                            appCommon.setCONNECTION(connection);
                            appCommon.setAPP_NAME(app.getName());
                            Intent intent = new Intent(myActivity, ViewRecordListActivity.class);
                            myActivity.startActivity(intent);
                        }
                    } catch (Exception e) {
                        final Message msg = new Message();
                        msg.obj = e;
                        new Thread(new Runnable() {
                            public void run() {
                                handler.sendMessage(msg);
                            }
                        }).start();
                    }
                }
            };
            asyncTask.execute();
        }
    }


    public void checkConnection(final Integer appId, final String domain, final String userName, final String password, final InputStream cert, final String certPass, final byte[] certBuffer) {
        DatabaseHelper helper = new DatabaseHelper(myActivity.getApplicationContext());
        SQLiteDatabase myDB = helper.getReadableDatabase();
        AppRegistrationDAO dao = new AppRegistrationDAO(myDB);
        dao.deleteAll();
        final AppCommon appCommon = this.myApp;
        try {
            auth.setPasswordAuth(userName, password);
            if (cert != null && certPass != null) {
                auth.setClientCert(cert, certPass);
            }
            final Connection connection = new Connection(domain, auth);
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
                    ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), msg.obj.toString());
                }
            };

            asyncTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] object) {
                    try {
                        App appManagement = new App(connection);
                        return appManagement.getApp(appId);
                    } catch (Exception e) {
                        final Message msg = new Message();
                        msg.obj = e;
                        new Thread(new Runnable() {
                            public void run() {
                                handler.sendMessage(msg);
                            }
                        }).start();
                        asyncTask.cancel(true);
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Object result) {
                    try {
                        if (result != null) {
                            AppModel app = (AppModel) result;
                            appCommon.setAPP_ID(appId);
                            appCommon.setCONNECTION(connection);
                            appCommon.setAPP_NAME(app.getName());
                            DatabaseHelper helper = new DatabaseHelper(myActivity.getApplicationContext());
                            SQLiteDatabase myDB = helper.getReadableDatabase();
                            AppRegistrationDAO dao = new AppRegistrationDAO(myDB);
                            dao.insertData(domain, userName, password, appId, certBuffer, certPass);
                            Intent intent = new Intent(myActivity, ViewRecordListActivity.class);
                            myActivity.startActivity(intent);
                            myActivity.finish();
                        }
                    } catch (Exception e) {
                        final Message msg = new Message();
                        msg.obj = e;
                        new Thread(new Runnable() {
                            public void run() {
                                handler.sendMessage(msg);
                            }
                        }).start();
                    }
                }
            };
            asyncTask.execute();
        } catch (Exception e) {
            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
            ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), e.toString());
        }
    }
}
