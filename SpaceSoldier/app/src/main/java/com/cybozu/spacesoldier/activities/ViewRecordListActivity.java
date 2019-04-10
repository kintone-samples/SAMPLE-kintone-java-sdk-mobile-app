package com.cybozu.spacesoldier.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.constants.IntentKeys;
import com.cybozu.spacesoldier.data.strage.AppRegistrationDAO;
import com.cybozu.spacesoldier.data.strage.DatabaseHelper;
import com.cybozu.spacesoldier.entities.RecordCard;
import com.cybozu.spacesoldier.logic.ShowErrorDialog;
import com.cybozu.spacesoldier.logic.ViewRecordListLogic;
import com.cybozu.spacesoldier.views.RecordCardListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewRecordListActivity extends AppCompatActivity {

    private AppCommon myApp;
    private ViewRecordListLogic myLogic;
    Activity myActivity;

    private ListView recordCardListView = null;
    private RecordCardListAdapter adapter = null;
    private List<RecordCard> recordCardList = new ArrayList<>();
    private Integer totalRecordCount = 0;
    private Integer VIEW_LIMIT = 100;

    private static AsyncTask displayRecordsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record_list);
        myApp = (AppCommon) this.getApplication();
        myLogic = new ViewRecordListLogic(this, myApp);
        this.myActivity = this;
        final Context that = this;

        final Connection connection = myApp.getCONNECTION();
        final Integer appID = myApp.getAppID();
        final String appName = myApp.getAPP_NAME();
        setTitle(appName);

        recordCardListView = findViewById(R.id.view_record_list_card_list);
        recordCardListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount - visibleItemCount) == firstVisibleItem && totalRecordCount > totalItemCount && totalItemCount < VIEW_LIMIT) {
                    Integer count = totalItemCount + 10;
                    if (count >= totalRecordCount) {
                        count = totalRecordCount;
                    }
                    List<RecordCard> list = recordCardList.subList(totalItemCount, count);
                    adapter.addAll(list);
                }
            }
        });

        Spinner recordListSelector = findViewById(R.id.view_record_list_record_list);
        recordListSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recordCardListView.setSelection(0);
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                String query = null;
                if (item == getResources().getString(R.string.allRecords)) {
                    query = null;
                } else if (item == getResources().getString(R.string.confirmed)) {
                    query = " Status in (\"Confirmed\") order by $id desc limit 100";
                } else if (item == getResources().getString(R.string.unconfirmed)) {
                    query = " Status in (\"Unconfirmed\") order by $id desc limit 100";
                }
                try {
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
                            ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), msg.obj.toString());
                        }
                    };

                    final String querySt = query;
                    displayRecordsTask = new AsyncTask() {
                        @Override
                        // 非同期実行したい処理を記載
                        protected Object doInBackground(Object[] object) {
                            try {
                                return myLogic.createRecordList(connection, appID, querySt);
                            } catch (Exception e) {
                                final Message msg = new Message();
                                msg.obj = e;
                                new Thread(new Runnable() {
                                    public void run() {
                                        handler.sendMessage(msg);
                                    }
                                }).start();
                                displayRecordsTask.cancel(true);
                                return false;
                            }
                        }

                        @Override
                        // 非同期処理が完了した後に実行したい処理を記載
                        protected void onPostExecute(Object result) {
                            hideNoRecordView();
                            if (result != null) {
                                recordCardList = (List<RecordCard>) result;
                                List<RecordCard> list = new ArrayList<>();
                                list.addAll(recordCardList);
                                totalRecordCount = recordCardList.size();
                                if (totalRecordCount == 0) {
                                    if (adapter != null && adapter.getCount() > 0) {
                                        adapter.clear();
                                    }
                                    showNoRecordView();
                                    return;
                                }
                                if (totalRecordCount > 10) {
                                    list = list.subList(0, 10);
                                }
                                adapter = new RecordCardListAdapter(that, R.layout.record_card_list, list);
                                recordCardListView.setAdapter(adapter);
                            }
                        }
                    };

                    displayRecordsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                } catch (Exception e) {
                    ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
                    ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), e.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void showNoRecordView() {
        View noRecordView = findViewById(R.id.view_record_list_no_record);
        noRecordView.setVisibility(View.VISIBLE);
    }

    public void hideNoRecordView() {
        View noRecordView = findViewById(R.id.view_record_list_no_record);
        noRecordView.setVisibility(View.GONE);
    }

    public void logOut(View view) {
        if (myApp.getCONNECTION() == null || myApp.getAppID() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            String domain = myApp.getCONNECTION().getDomain();
            Integer appId = myApp.getAppID();
            String userName = myApp.getCONNECTION().getAuth().getPasswordAuth().getUsername();
            try {
                DatabaseHelper helper = new DatabaseHelper(this.getApplicationContext());
                SQLiteDatabase myDB = helper.getReadableDatabase();
                AppRegistrationDAO dao = new AppRegistrationDAO(myDB);
                dao.deleteByUniqueKey(domain, userName, appId);
                myApp.setCONNECTION(null);
                myApp.setAPP_NAME(null);
                myApp.setAPP_ID(null);
                Intent intent = new Intent(this, LoginActivity.class);
                domain = domain.substring(8);
                intent.putExtra(IntentKeys.DOMAIN.name(), domain);
                intent.putExtra(IntentKeys.USER_NAME.name(), userName);
                intent.putExtra(IntentKeys.APP_ID.name(), appId.toString());
                startActivity(intent);
                finish();
            } catch (Exception e) {
                ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
                ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), e.toString());
            }
        }
    }

    public void addRecord(View view) {
        Intent intent = new Intent(this, AddRecordActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // inactivate back button
    }
}
