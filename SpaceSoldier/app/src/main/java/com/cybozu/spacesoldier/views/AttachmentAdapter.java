package com.cybozu.spacesoldier.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybozu.kintone.client.model.file.FileModel;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.entities.BitmapScaleInfo;
import com.cybozu.spacesoldier.logic.BitmapLogic;
import com.cybozu.spacesoldier.logic.ShowErrorDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.CustomViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    private ArrayList<HashMap<String, Object>> attachmentFileList = null;

    public AttachmentAdapter(Context context, ArrayList<HashMap<String, Object>> attachmentFileList) {
        mContext = context;
        this.attachmentFileList = attachmentFileList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        mInflater = LayoutInflater.from(viewGroup.getContext());
        return new CustomViewHolder(mInflater.inflate(R.layout.activity_attachment, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder viewHolder, final int i) {

        viewHolder.attachmentDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view, i);
            }
        });

        if (this.attachmentFileList != null && this.attachmentFileList.size() > i && this.attachmentFileList.get(i) != null) {

            String name = null;
            long size = 0;
            Bitmap bitmap = null;
            BitmapScaleInfo resizeScale = null;

            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            if (attachmentFileList.get(i).containsKey("URI")) {
                Uri FileUri = (Uri) attachmentFileList.get(i).get("URI");

                long availableSize = 0;
                boolean hasError = false;
                try {
                    InputStream stream = mContext.getContentResolver().openInputStream(FileUri);
                    availableSize = stream.available();
                    bitmap = BitmapFactory.decodeStream(new BufferedInputStream(stream));
                    bitmap = BitmapLogic.resize(bitmap, 110, 110);
                    resizeScale = BitmapLogic.getScaleSize(bitmap, 110, 110);

                } catch (Exception e) {
                    hasError = true;
                    viewHolder.visibileMe(false);
                    listener.onClick(viewHolder.getAttachmentDeleteButton(), i);
                    return ;
                }

                if (hasError == false)
                {
                    Cursor cursor = mContext.getContentResolver().query(FileUri, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            name = cursor.getString(0);
                        }
                        cursor.close();
                    }

                    String[] projection_size = {MediaStore.MediaColumns.SIZE};
                    Cursor cursor_size = mContext.getContentResolver().query(FileUri, projection_size, null, null, null);
                    if (cursor_size != null) {
                        if (cursor_size.moveToFirst()) {
                            size = cursor_size.getLong(0);
                        }
                        cursor_size.close();
                    }

                    if (size == 0) {
                        String[] projection_size2 = {MediaStore.MediaColumns.DATA};
                        Cursor cursor_size2 = mContext.getContentResolver().query(FileUri, projection_size2, null, null, null);
                        if (cursor_size2 != null) {
                            String path = null;
                            if (cursor_size2.moveToFirst()) {
                                path = cursor_size2.getString(0);
                            }
                            cursor_size2.close();
                            if (path != null) {
                                File file = new File(path);
                                size = file.length();
                            }
                        }
                    }

                    if (size == 0) {
                        size = availableSize;
                    }

                }

            } else if (attachmentFileList.get(i).containsKey("FileModel")) {
                FileModel fm = (FileModel) attachmentFileList.get(i).get("FileModel");
                name = fm.getName();
                size = Long.valueOf(fm.getSize());

                String key = (String) attachmentFileList.get(i).get("Bitmap");
                SharedPreferences data = mContext.getSharedPreferences("tmpImage", Context.MODE_PRIVATE);
                String s = data.getString(key, "");
                if (!s.equals("")) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    byte[] b = Base64.decode(s, Base64.DEFAULT);
                    bitmap = BitmapFactory.decodeByteArray(b, 0, b.length).copy(Bitmap.Config.ARGB_8888, true);
                    resizeScale = BitmapLogic.getScaleSize(bitmap, 110, 110);
                }
            }

            double unit_size = size;
            if (unit_size > 0) {
                unit_size = unit_size / 1024;
            }
            if (unit_size >= 1000) {
                unit_size = unit_size / 1024;
                viewHolder.attachmentFileSize.setText(String.format("%.2f", unit_size) + " MB");
            } else {
                viewHolder.attachmentFileSize.setText(String.format("%.2f", unit_size) + " KB");
            }
            viewHolder.attachmentFileName.setText(name);
            viewHolder.attachmentImage.setImageBitmap(bitmap);

            int width = 0;
            int height = 0;
            if (resizeScale != null)
            {
                width =  resizeScale.getScaleWidth();
                height = resizeScale.getScaleHeight();
            }

            ViewGroup.LayoutParams lp = viewHolder.attachmentDeleteButton.getLayoutParams();
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

            float d = mContext.getResources().getDisplayMetrics().density;

            mlp.setMargins(mlp.leftMargin, (int) ((((110 - height) / 2) + 2) * d), (int) ((((110 - width) / 2) + 2) * d), mlp.bottomMargin);
            if (Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT) {
                mlp.setMarginEnd((int) ((((110 - width) / 2) + 2) * d));
            }

            viewHolder.attachmentDeleteButton.setLayoutParams(mlp);
        }
    }

    @Override
    public int getItemCount() {
        if (this.attachmentFileList != null) {
            return this.attachmentFileList.size();
        } else {
            return 0;
        }
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        private ImageView attachmentImage = null;
        private ImageButton attachmentDeleteButton = null;
        private TextView attachmentFileName = null;
        private TextView attachmentFileSize = null;

        public ImageButton getAttachmentDeleteButton(){
            return  this.attachmentDeleteButton;
        }

        public CustomViewHolder(View view) {
            super(view);

            this.attachmentImage = (ImageView) view.findViewById(R.id.attachment_image);
            this.attachmentDeleteButton = (ImageButton) view.findViewById(R.id.attachment_delete_button);
            this.attachmentFileName = (TextView) view.findViewById(R.id.attachment_filename);
            this.attachmentFileSize = (TextView) view.findViewById(R.id.attachment_filesize);
        }

        public void visibileMe(boolean visibile) {
            if (visibile == false) {
                this.attachmentDeleteButton .setVisibility(View.GONE);
            }
        }
    }
}
