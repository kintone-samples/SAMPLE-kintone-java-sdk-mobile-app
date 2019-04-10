package com.cybozu.spacesoldier.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.model.comment.Comment;
import com.cybozu.kintone.client.model.comment.GetCommentsResponse;
import com.cybozu.kintone.client.model.file.DownloadRequest;
import com.cybozu.kintone.client.module.parser.FileParser;
import com.cybozu.kintone.client.module.record.Record;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.activities.ViewRecordActivity;
import com.cybozu.spacesoldier.constants.IntentKeys;
import com.cybozu.spacesoldier.entities.RecordCard;
import com.cybozu.spacesoldier.logic.BitmapLogic;
import com.cybozu.spacesoldier.logic.ShowErrorDialog;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RecordCardListAdapter extends ArrayAdapter<RecordCard> {

    private List<RecordCard> recordCardList = null;
    private static final int DISPLAY_COMMENT_MAX_NUMBER = 9;

    private HashMap<Object, AsyncTask> getAttachmentsTaskHash = new HashMap<Object, AsyncTask>();
    private HashMap<Object, AsyncTask> getCommentsTaskHash = new HashMap<Object, AsyncTask>();

    public RecordCardListAdapter(Context context, int resourceId, List<RecordCard> recordCardList) {
        super(context, resourceId, recordCardList);

        this.recordCardList = recordCardList;
    }

    @Override
    public int getCount() {
        return recordCardList.size();
    }

    @Override
    public RecordCard getItem(int position) {
        return recordCardList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Activity myActivity = (Activity) getContext();
        final AppCommon myApp = (AppCommon) myActivity.getApplication();

        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.record_card_list, null);
                viewHolder = new ViewHolder(convertView);
                // デフォルト画像をImageViewにセット
                Resources res = myActivity.getResources();
                Bitmap photo = BitmapFactory.decodeResource(res, R.drawable.icon_default);
                viewHolder.getPhoto().setImageBitmap(photo);
                Bitmap commentImg = BitmapFactory.decodeResource(res, R.mipmap.no_comment_notation_foreground);
                viewHolder.getCommentFlg().setImageBitmap(commentImg);
                viewHolder.getCommentNum().setText("");
                convertView.setTag(viewHolder);
            } else {
                if (getAttachmentsTaskHash.get(convertView.getTag()) != null) {
                    getAttachmentsTaskHash.get(convertView.getTag()).cancel(true);
                    getCommentsTaskHash.get(convertView.getTag()).cancel(true);
                }
                viewHolder = (ViewHolder) convertView.getTag();
                // デフォルト画像でImageViewの中身を初期化
                Resources res = myActivity.getResources();
                Bitmap photo = BitmapFactory.decodeResource(res, R.drawable.icon_default);
                viewHolder.getPhoto().setImageBitmap(photo);
                Bitmap commentImg = BitmapFactory.decodeResource(res, R.mipmap.no_comment_notation_foreground);
                viewHolder.getCommentFlg().setImageBitmap(commentImg);
                viewHolder.getCommentNum().setText("");
            }

            final RecordCard recordCard = getItem(position);
            if (recordCard != null) {
                viewHolder.getSummary().setText(recordCard.getSummary());
                viewHolder.getDescription().setText(recordCard.getDescription());
                viewHolder.getCreator().setText(recordCard.getCreator());

                java.text.DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US);
                df.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date d = df.parse(recordCard.getCreateDateTime());
                String str2 = sdf1.format(d);
                viewHolder.getCreateDateTime().setText(str2);

                final String fileKey = recordCard.getFileKey();
                final Integer recordID = recordCard.getId();
                final Connection connection = ((AppCommon) ((Activity) getContext()).getApplication()).getCONNECTION();
                AsyncTask getAttachmentsTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] object) {
                        if (isCancelled()){
                            return null;
                        }

                        if (fileKey == null) {
                            return null;
                        }
                        FileParser parser = new FileParser();
                        DownloadRequest request = new DownloadRequest(fileKey);
                        try {
                            String requestBody = parser.parseObject(request);
                            InputStream is = connection.downloadFile(requestBody);
                            Bitmap photo = BitmapFactory.decodeStream(is);
                            photo = BitmapLogic.resize(photo, 80, 80);
                            return photo;
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        Bitmap photo;
                        if (result == null) {
                            Resources res = myActivity.getResources();
                            photo = BitmapFactory.decodeResource(res, R.drawable.icon_default);
                            viewHolder.getPhoto().setImageBitmap(photo);
                            return;
                        }
                        photo = (Bitmap) result;
                        viewHolder.getPhoto().setImageBitmap(BitmapLogic.getCroppedBitmap(photo));
                    }
                };
                AsyncTask getCommentsTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] object) {
                        if (recordID == null) {
                            return null;
                        }
                        Record recordManagement = new Record(connection);
                        try {
                            GetCommentsResponse response = recordManagement.getComments(myApp.getAppID(), recordID, null, null, null);
                            return response.getComments();
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        if (result != null) {
                            ArrayList<Comment> comments = (ArrayList<Comment>) result;
                            if (comments.size() == 0) {
                                Resources res = myActivity.getResources();
                                Bitmap commentImg = BitmapFactory.decodeResource(res, R.mipmap.no_comment_notation_foreground);
                                viewHolder.getCommentFlg().setImageBitmap(commentImg);
                                viewHolder.getCommentNum().setText("");
                                return;
                            }
                            Resources res = myActivity.getResources();
                            Bitmap commentImg = BitmapFactory.decodeResource(res, R.drawable.icon_message_100);
                            viewHolder.getCommentFlg().setImageBitmap(commentImg);

                            String commentNum = null;
                            if (comments.size() > DISPLAY_COMMENT_MAX_NUMBER) {
                                commentNum = String.valueOf(DISPLAY_COMMENT_MAX_NUMBER).concat("+");
                            } else {
                                commentNum = String.valueOf(comments.size());
                            }
                            viewHolder.getCommentNum().setText(commentNum);
                        }
                    }
                };
                getAttachmentsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                getAttachmentsTaskHash.put(convertView.getTag(), getAttachmentsTask);
                getCommentsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                getCommentsTaskHash.put(convertView.getTag(), getCommentsTask);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(myActivity, ViewRecordActivity.class);
                        intent.putExtra(IntentKeys.RECORD_ID.name(), recordCard.getId());
                        intent.putExtra(IntentKeys.RECORD_NAME.name(), recordCard.getSummary());
                        myActivity.startActivity(intent);
                    }
                });
            }
        } catch (Exception e) {
            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(myActivity);
            ShowErrorDialog.show(myActivity.getString(R.string.exception_dialog_title), e.toString());
        }
        return convertView;
    }

    private class ViewHolder {
        private ImageView photo = null;
        private TextView summary = null;
        private TextView description = null;
        private TextView creator = null;
        private TextView createDateTime = null;
        private ImageView commentFlg = null;
        private TextView commentNum = null;

        public ViewHolder(View view) {
            this.photo = view.findViewById(R.id.record_card_image);
            this.summary = view.findViewById(R.id.record_card_summary);
            this.description = view.findViewById(R.id.record_card_notes);
            this.creator = view.findViewById(R.id.record_card_creator);
            this.createDateTime = view.findViewById(R.id.record_card_createDT);
            this.commentFlg = view.findViewById(R.id.record_card_comment_flg);
            this.commentNum = view.findViewById(R.id.record_card_comment_num);
        }

        public ImageView getPhoto() {
            return this.photo;
        }

        public TextView getSummary() {
            return this.summary;
        }

        public TextView getDescription() {
            return description;
        }

        public TextView getCreator() {
            return creator;
        }

        public TextView getCreateDateTime() {
            return createDateTime;
        }

        public ImageView getCommentFlg() {
            return commentFlg;
        }

        public TextView getCommentNum() {
            return commentNum;
        }
    }
}
