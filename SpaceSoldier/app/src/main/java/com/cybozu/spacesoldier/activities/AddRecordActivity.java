package com.cybozu.spacesoldier.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.model.app.form.FieldType;
import com.cybozu.kintone.client.model.file.FileModel;
import com.cybozu.kintone.client.model.record.AddRecordResponse;
import com.cybozu.kintone.client.model.record.field.FieldValue;
import com.cybozu.kintone.client.module.parser.FileParser;
import com.cybozu.kintone.client.module.record.Record;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.constants.IntentKeys;
import com.cybozu.spacesoldier.logic.ShowErrorDialog;
import com.cybozu.spacesoldier.views.AttachmentAdapter;
import com.cybozu.spacesoldier.views.WrapContentLinearLayoutManager;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class AddRecordActivity extends AppCompatActivity {

    private AppCommon myApp;
    private static Connection connection;
    private Integer appID;
    Activity myActivity;

    private RecyclerView attachmentRecyclerView = null;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static AsyncTask asyncTask;
    private static AsyncTask asyncTask_child;
    private static AsyncTask asyncTask_add_record;

    private ImageButton btnSave = null;

    private Uri m_uri;
    private static final int PERMISSION_REQUEST_CODE = 1;

    static final int REQUEST_CODE_CAMERA = 1;
    static final int REQUEST_CODE_GALLERY = 2;

    private final int MAX_ATTACHMENT_FILE_NUM = 3;
    ArrayList<FileModel> attachmentFileKeyList = new ArrayList<FileModel>();
    private ArrayList<HashMap<String, Object>> attachmentFileList = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        this.myApp = (AppCommon) this.getApplication();
        connection = myApp.getCONNECTION();
        this.appID = myApp.getAppID();
        setTitle(myApp.getAPP_NAME());

        this.myActivity = this;

        this.btnSave = findViewById(R.id.add_save_button);

        attachmentRecyclerView = findViewById(R.id.add_attachment_recycler_view);
        mAdapter = new AttachmentAdapter(myActivity, attachmentFileList);
        ((AttachmentAdapter) mAdapter).setOnItemClickListener(new AttachmentAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int i) {
                attachmentFileList.remove(i);
                creteAttachList();
            }
        });
        attachmentRecyclerView.setAdapter(mAdapter);
        attachmentRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public void onBackPressed() {
        this.cancel(findViewById(R.id.add_cancel_button));
    }

    public void cancel(View view) {
        Intent intent = new Intent(this, ViewRecordListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void save(View view) {
        try {
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
                    ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), msg.obj.toString());
                }
            };

            final AddRecordActivity that  = this;
            that.setViewEnabled(false);
            asyncTask = new AsyncTask() {
                final CountDownLatch latch = new CountDownLatch(attachmentFileList.size());

                @Override
                protected Object doInBackground(Object[] object) {
                    try {
                        for (int i = 0; i < attachmentFileList.size(); i++) {
                            final Uri target_uri = (Uri) attachmentFileList.get(i).get("URI");

                            asyncTask_child = new AsyncTask() {

                                @Override
                                protected Object doInBackground(Object[] object) {
                                    try {
                                        InputStream stream = getContentResolver().openInputStream(target_uri);

                                        String name = "";
                                        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
                                        Cursor cursor = getContentResolver().query(target_uri, projection, null, null, null);
                                        if (cursor != null) {
                                            if (cursor.moveToFirst()) {
                                                name = cursor.getString(0);
                                            }
                                            cursor.close();
                                        }
                                        return connection.uploadFile(name, stream);
                                    } catch (Exception e) {
                                        final Message msg = new Message();
                                        msg.obj = e;
                                        new Thread(new Runnable() {
                                            public void run() {
                                                handler.sendMessage(msg);
                                            }
                                        }).start();
                                        asyncTask_child.cancel(true);
                                        return false;
                                    }
                                }

                                @Override
                                protected void onPostExecute(Object result) {
                                    try {
                                        if (result != null) {
                                            JsonElement upload_response = (JsonElement) result;
                                            FileParser parser = new FileParser();
                                            FileModel fm = (FileModel) parser.parseJson(upload_response, FileModel.class);
                                            attachmentFileKeyList.add(fm);
                                        }
                                    } catch (Exception e) {
                                        final Message msg = new Message();
                                        msg.obj = e;
                                        new Thread(new Runnable() {
                                            public void run() {
                                                handler.sendMessage(msg);
                                            }
                                        }).start();
                                    } finally {
                                        latch.countDown();
                                    }
                                }
                            };
                            asyncTask_child.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        latch.await();
                        return 0;
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
                            final HashMap<String, FieldValue> resultRecord = new HashMap<String, FieldValue>();
                            FieldValue summary_fv = new FieldValue();
                            FieldValue notes_fv = new FieldValue();
                            FieldValue photo_fv = new FieldValue();

                            final Record recordManagement = new Record(connection);

                            // Summary
                            EditText summary = (EditText) findViewById(R.id.add_summary_edit);
                            summary_fv.setType(FieldType.SINGLE_LINE_TEXT);
                            summary_fv.setValue(summary.getText().toString());
                            resultRecord.put("Summary", summary_fv);

                            // Notes
                            EditText notes = (EditText) findViewById(R.id.add_notes_edit);
                            notes_fv.setType(FieldType.MULTI_LINE_TEXT);
                            notes_fv.setValue(notes.getText().toString());
                            resultRecord.put("Notes", notes_fv);

                            // photo
                            photo_fv.setType(FieldType.FILE);
                            photo_fv.setValue(attachmentFileKeyList);
                            resultRecord.put("Photo", photo_fv);

                            asyncTask_add_record = new AsyncTask() {

                                @Override
                                protected Object doInBackground(Object[] object) {
                                    try {
                                        return recordManagement.addRecord(appID, resultRecord);
                                    } catch (Exception e) {
                                        final Message msg = new Message();
                                        msg.obj = e;
                                        new Thread(new Runnable() {
                                            public void run() {
                                                handler.sendMessage(msg);
                                            }
                                        }).start();
                                        asyncTask_add_record.cancel(true);
                                        return false;
                                    }
                                }

                                @Override
                                protected void onPostExecute(Object result) {
                                    if (result != null) {
                                        AddRecordResponse response = (AddRecordResponse) result;
                                        Integer record_id = response.getID();

                                        Intent intent = new Intent(getApplicationContext(), ViewRecordActivity.class);
                                        intent.putExtra(IntentKeys.RECORD_ID.name(), record_id);
                                        startActivity(intent);
                                    }
                                }
                            };
                            asyncTask_add_record.execute();
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

                    that.setViewEnabled(true);
                }
            };
            asyncTask.execute();
        } catch (Exception e) {
            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
            ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), e.toString());
        }
    }

    public void attachment(View view) {
        ArrayList<String> requestPermissions = new ArrayList<>();
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.CAMERA);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (requestPermissions.isEmpty()) {
            handlePermissionsSuccess();
        } else {
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                requestPermissions(requestPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void setViewEnabled(boolean enabled) {
        this.btnSave.setEnabled(enabled);
    }

    private void handlePermissionsSuccess() {
        String[] str_items = {getString(R.string.attachment_camera_message), getString(R.string.attachment_gallery_message), getString(R.string.attachment_cancel_message)};
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.attachment_upload_title))
                .setItems(str_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                wakeupCamera();
                                break;
                            case 1:
                                wakeupGallery();
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean allSuccess = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allSuccess = false;
            }
        }
        if (allSuccess) {
            handlePermissionsSuccess();
        }
    }

    protected void wakeupCamera() {
        try {
            String photoName = System.currentTimeMillis() + ".jpg";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE, photoName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            m_uri = getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, m_uri);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } catch (Exception e) {
            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(this);
            ShowErrorDialog.show(getString(R.string.exception_dialog_title), e.toString());
        }
    }

    protected void wakeupGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == REQUEST_CODE_CAMERA) {
            HashMap<String, Object> fileinfo = new HashMap<String, Object>();
            fileinfo.put("URI", m_uri);
            attachmentFileList.add(fileinfo);
        }
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                m_uri = data.getData();

                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                getContentResolver().takePersistableUriPermission(m_uri, takeFlags);
            } else {
                ContentResolver cr = getContentResolver();
                String[] columns = {MediaStore.Images.Media.DATA};
                Cursor c = cr.query(data.getData(), columns, null, null, null);
                c.moveToFirst();
                m_uri = Uri.fromFile(new File(c.getString(0)));
            }
            HashMap<String, Object> fileinfo = new HashMap<String, Object>();
            fileinfo.put("URI", m_uri);
            attachmentFileList.add(fileinfo);
        }

        creteAttachList();
    }

    protected void creteAttachList() {
        ViewGroup vg = (ViewGroup) findViewById(R.id.add_attachment_recycler_view);
        vg.removeAllViews();
        ConstraintLayout attach_fld = (ConstraintLayout) findViewById(R.id.add_attach_field);
        if (attachmentFileList.size() < MAX_ATTACHMENT_FILE_NUM) {
            attach_fld.setVisibility(View.VISIBLE);
        } else {
            attach_fld.setVisibility(View.GONE);
        }
    }
}
