package com.cybozu.spacesoldier.data.strage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DATABASE_NAME = "SpaceSoldier.db";

    // コンストラクタ
    public DatabaseHelper(Context c) {
        super(c, DATABASE_NAME, null, DB_VERSION);
    }

    // データベースが一番最初に作られたときの処理
    // コンストラクタに渡されたDBファイル名が存在しない場合に呼ばれる
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create App_Auth_Info_Table
        try {
            AppRegistrationDAO appRegistrationDao = new AppRegistrationDAO(db);
            appRegistrationDao.createTable();
        } catch (Exception e) {
            Log.d("DB", e.getMessage());
        }
    }

    //データベースのアップグレード時の処理
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 今回はヴァージョンアップを想定しないため処理は行わない
    }

}
