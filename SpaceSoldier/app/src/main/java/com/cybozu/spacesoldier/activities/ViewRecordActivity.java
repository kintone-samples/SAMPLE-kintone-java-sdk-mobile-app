package com.cybozu.spacesoldier.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.constants.IntentKeys;
import com.cybozu.spacesoldier.logic.ViewRecordDetailLogic;
import com.cybozu.spacesoldier.views.RecordDetailCommentFragment;
import com.cybozu.spacesoldier.views.RecordDetailDataFragment;
import com.cybozu.spacesoldier.views.RecordDetailPageAdapter;

public class ViewRecordActivity extends AppCompatActivity
        implements RecordDetailDataFragment.OnFragmentInteractionListener, RecordDetailCommentFragment.OnFragmentInteractionListener, DialogInterface.OnClickListener {

    private AppCommon myApp;
    private ViewRecordDetailLogic myLogic;
    private String loginUserName = "";

    private TabLayout tabLayout = null;
    private TabItem tabDataItem = null;
    private TabItem tabCommentItem = null;

    private ViewPager viewPager = null;
    private RecordDetailPageAdapter pageAdapter = null;

    private TextView toolbarTitle = null;
    private ImageButton btnBack = null;
    private ImageButton btnEdit = null;
    private ImageButton btnDelete = null;
    private Button btnAddComment = null;
    private Integer currentRecordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record);
        this.myApp = (AppCommon) this.getApplication();
        this.loginUserName = myApp.getCONNECTION().getAuth().getPasswordAuth().getUsername();
        final String appName = myApp.getAPP_NAME();
        setTitle(appName);

        Intent intent = getIntent();
        this.currentRecordId = intent.getIntExtra(IntentKeys.RECORD_ID.name(), 0);

        this.initActivity();
        this.setViewVisibility();
        this.initTabSelectedAction();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initActivity() {
        tabLayout = findViewById(R.id.tablayout);
        tabDataItem = findViewById(R.id.tabDataItem);
        tabCommentItem = findViewById(R.id.tabCommentItem);

        viewPager = findViewById(R.id.viewPager);
        pageAdapter = new RecordDetailPageAdapter(getSupportFragmentManager(), this.myApp, this.currentRecordId);
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        toolbarTitle = findViewById(R.id.header_title);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnAddComment = findViewById(R.id.btnAddComment);

        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doBackAction(v);
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doEditAction(v);
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDeleteAction(v);
            }
        });
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doAddCommentAction(v);
            }
        });
    }

    private void setViewVisibility() {
        switch (viewPager.getCurrentItem()) {
            case 0: // DataFragment
                btnBack.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnAddComment.setVisibility(View.GONE);
                break;
            case 1: // CommentFragment
                btnBack.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnAddComment.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setViewEnabled(boolean enabled) {
        switch (viewPager.getCurrentItem()) {
            case 0: // DataFragment
                btnBack.setEnabled(enabled);
                btnEdit.setEnabled(enabled);
                btnDelete.setEnabled(enabled);
                break;
            case 1: // CommentFragment
                btnBack.setEnabled(enabled);
                btnAddComment.setEnabled(enabled);
                break;
        }
    }

    private void initTabSelectedAction() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                setViewVisibility();
                switch (tab.getPosition()) {
                    case 0: // DataFragment
                        getDataFragment().showRecordDetail();
                        break;
                    case 1: // CommentFragment
                        getCommentFragment().showCommentList();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public RecordDetailDataFragment getDataFragment() {
        return ((RecordDetailDataFragment) pageAdapter.getItem(0));
    }

    public RecordDetailCommentFragment getCommentFragment() {
        return ((RecordDetailCommentFragment) pageAdapter.getItem(1));
    }

    public void showErrorMessage(String msg) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(ViewRecordActivity.this);
        normalDialog.setTitle("エラー");
        normalDialog.setMessage(msg);
        normalDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        normalDialog.show();
    }

    public void setToolbarTitle(String title) {
        this.toolbarTitle.setText(title);
    }

    public String getLoginUserName() {
        return this.loginUserName;
    }

    private void doBackAction(View v) {
        Intent intent = new Intent(this, ViewRecordListActivity.class);
        startActivity(intent);
    }

    private void doEditAction(View v) {
        Intent intent = new Intent(this, EditRecordActivity.class);
        intent.putExtra(IntentKeys.RECORD_ID.name(), this.currentRecordId);
        startActivity(intent);
    }

    private void doDeleteAction(View v) {
        this.showConfirmDialog(getString(R.string.delete_message), getString(R.string.delete_ok_message), getString(R.string.delete_ng_message), this);
    }

    private void doAddCommentAction(View v) {
        this.getCommentFragment().addNewComment();
    }

    public void showConfirmDialog(String msg, String okButton, String ngButton, final DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(ViewRecordActivity.this);
        normalDialog.setMessage(msg);
        normalDialog.setPositiveButton(okButton, listener);
        normalDialog.setNegativeButton(ngButton, listener);
        normalDialog.show();
    }

    private void deleteKintoneRecord() {
        myLogic = new ViewRecordDetailLogic();
        final Intent intent = new Intent(this, ViewRecordListActivity.class);
        AsyncTask deleteRecordTask = new AsyncTask() {
            @Override
            // 非同期実行したい処理を記載
            protected Object doInBackground(Object[] object) {
                try {
                    final Connection connection = myApp.getCONNECTION();
                    final Integer appID = myApp.getAppID();
                    myLogic.deleteRecord(connection, appID, currentRecordId);
                    return true;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            // 非同期処理が完了した後に実行したい処理を記載
            protected void onPostExecute(Object result) {
                if (result != null) {
                    if (result instanceof Exception) {
                        showErrorMessage(((Exception) result).getMessage());
                    } else if ((boolean) result == true) {
                        startActivity(intent);
                    }
                }
            }
        };
        deleteRecordTask.execute();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (((AlertDialog) dialog).getButton(which).getText().toString() == getString(R.string.delete_ok_message)) {
            this.deleteKintoneRecord();
        }
    }
}
