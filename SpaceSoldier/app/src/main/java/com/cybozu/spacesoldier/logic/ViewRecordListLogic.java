package com.cybozu.spacesoldier.logic;

import android.app.Activity;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.exception.KintoneAPIException;
import com.cybozu.kintone.client.model.record.GetRecordsResponse;
import com.cybozu.kintone.client.model.record.field.FieldValue;
import com.cybozu.kintone.client.module.record.Record;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.entities.RecordCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewRecordListLogic {

    AppCommon myApp;
    Activity myActivity;

    public ViewRecordListLogic(Activity activity, AppCommon appCommon) {
        this.myActivity = activity;
        this.myApp = appCommon;
    }

    public List<RecordCard> createRecordList(Connection connection, Integer appID, String query) throws KintoneAPIException {
        ArrayList<HashMap<String, FieldValue>> resultRecords;
        // get records
        Record recordManagement = new Record(connection);
        // テストアプリではとりあえず固定で50件のレコードを取得
        if (query == null) {
            query = "order by $id desc limit 100";
        }
        GetRecordsResponse response = recordManagement.getRecords(appID, query, null, true);
        resultRecords = response.getRecords();
        List<RecordCard> recordCardList = new ArrayList<>();
        if (resultRecords != null) {
            for (HashMap<String, FieldValue> hashMap : resultRecords) {
                RecordCard recordCard = new RecordCard(hashMap);
                recordCardList.add(recordCard);
            }
        }
        return recordCardList;
    }

}
