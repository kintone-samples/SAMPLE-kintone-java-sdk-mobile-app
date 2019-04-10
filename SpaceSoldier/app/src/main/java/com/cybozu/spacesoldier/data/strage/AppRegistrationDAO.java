package com.cybozu.spacesoldier.data.strage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class AppRegistrationDAO {

    private static final String TABLE_NAME = "App_Registration_Table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DOMAIN = "Domain";
    private static final String COLUMN_USER_NAME = "UserName";
    private static final String COLUMN_PASS = "Password";
    private static final String COLUMN_APP_ID = "AppID";
    private static final String COLUMN_CLIENT_CERT = "ClientCert";
    private static final String COLUMN_CERT_PASS = "CertPass";


    private SQLiteDatabase db;

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static String getColumnDomain() {
        return COLUMN_DOMAIN;
    }

    public static String getColumnUserName() {
        return COLUMN_USER_NAME;
    }

    public static String getColumnPass() {
        return COLUMN_PASS;
    }

    public static String getColumnAppId() {
        return COLUMN_APP_ID;
    }

    public static String getColumnClientCert() { return COLUMN_CLIENT_CERT; }

    public static String getColumnCertPass() { return COLUMN_CERT_PASS; }

    public AppRegistrationDAO(SQLiteDatabase db) {
        this.db = db;
    }

    public void createTable() {
        // create App_Registration_Table
        String sql = "";
        // set table name
        sql += "create table " + TABLE_NAME + " (";
        // set columns
        sql += COLUMN_ID + " integer primary key autoincrement";
        sql += "," + COLUMN_DOMAIN + " text not null";
        sql += "," + COLUMN_USER_NAME + " text not null";
        sql += "," + COLUMN_PASS + " text not null";
        sql += "," + COLUMN_APP_ID + " integer not null";
        sql += "," + COLUMN_CLIENT_CERT + " blob";
        sql += "," + COLUMN_CERT_PASS + " text";
        sql += ", unique(" + COLUMN_DOMAIN + ", " + COLUMN_USER_NAME + ", " + COLUMN_APP_ID + "))";
        this.db.execSQL(sql);
    }

    public ArrayList<HashMap> getData(String sql) {
        ArrayList<HashMap> data = new ArrayList<>();
        try {
            Cursor result = this.db.rawQuery(sql, null);
            if (result.moveToFirst()) {
                do {
                    HashMap resultRow = new HashMap();
                    resultRow.put(COLUMN_ID, result.getInt(result.getColumnIndex(COLUMN_ID)));
                    resultRow.put(COLUMN_DOMAIN, result.getString(result.getColumnIndex(COLUMN_DOMAIN)));
                    resultRow.put(COLUMN_USER_NAME, result.getString(result.getColumnIndex(COLUMN_USER_NAME)));
                    resultRow.put(COLUMN_PASS, result.getString(result.getColumnIndex(COLUMN_PASS)));
                    resultRow.put(COLUMN_APP_ID, result.getInt(result.getColumnIndex(COLUMN_APP_ID)));
                    resultRow.put(COLUMN_CLIENT_CERT, result.getBlob(result.getColumnIndex(COLUMN_CLIENT_CERT)));
                    resultRow.put(COLUMN_CERT_PASS, result.getString(result.getColumnIndex(COLUMN_CERT_PASS)));
                    data.add(resultRow);
                } while (result.moveToNext());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            this.db.close();
            return data;
        }
    }

    public void insertData(String domain, String userName, String pass, int appId, byte[] certBuffer, String certPass) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DOMAIN, domain);
            values.put(COLUMN_USER_NAME, userName);
            values.put(COLUMN_PASS, pass);
            values.put(COLUMN_APP_ID, appId);
            if (certBuffer != null && certPass != null) {
                values.put(COLUMN_CLIENT_CERT, certBuffer);
                values.put(COLUMN_CERT_PASS, certPass);
            }
            this.db.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            throw e;
        } finally {
            this.db.close();
        }
    }

    public void deleteByUniqueKey(String domain, String useName, int appId) {
        try {
            String sql = "";
            sql += "delete from " + TABLE_NAME + " where ";
            sql += COLUMN_DOMAIN + " = '" + domain + "'";
            sql += " and " + COLUMN_USER_NAME + " = '" + useName + "'";
            sql += " and " + COLUMN_APP_ID + " = " + appId;
            this.db.execSQL(sql);
        } catch (Exception e) {
            throw e;
        } finally {
            this.db.close();
        }
    }

    public void deleteAll() {
        try {
            String sql = "";
            sql += "delete from " + TABLE_NAME;
            this.db.execSQL(sql);
        } catch (Exception e) {
            throw e;
        } finally {
            this.db.close();
        }
    }
}
