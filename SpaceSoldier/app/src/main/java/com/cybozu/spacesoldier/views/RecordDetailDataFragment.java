package com.cybozu.spacesoldier.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.model.file.DownloadRequest;
import com.cybozu.kintone.client.module.parser.FileParser;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.activities.ViewRecordActivity;
import com.cybozu.spacesoldier.entities.RecordData;
import com.cybozu.spacesoldier.logic.BitmapLogic;
import com.cybozu.spacesoldier.logic.ViewRecordDetailLogic;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordDetailDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class RecordDetailDataFragment extends Fragment {

    private Integer recordId = null;
    private AppCommon myApp;
    private ViewRecordDetailLogic myLogic;
    private OnFragmentInteractionListener mListener;

    private ViewHolder myViewHolder = null;
    private RecordData detailData = null;

    public void setRecordId(Integer id) {
        this.recordId = id;
    }

    public void setApp(AppCommon app) {
        this.myApp = app;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dataFragmentView = inflater.inflate(R.layout.fragment_data, container, false);
        this.myViewHolder = new ViewHolder(dataFragmentView);
        this.showRecordDetail();
        return dataFragmentView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void showRecordDetail() {
        myLogic = new ViewRecordDetailLogic();
        final Connection connection = myApp.getCONNECTION();
        final Integer appID = myApp.getAppID();
        this.myViewHolder.hidePhotos();
        final ArrayList<Bitmap> photoBits = new ArrayList<Bitmap>();

        ((ViewRecordActivity) mListener).setViewEnabled(false);
        AsyncTask displayRecordsTask = new AsyncTask() {
            @Override
            // 非同期実行したい処理を記載
            protected Object doInBackground(Object[] object) {
                try {
                    return myLogic.createRecordDetail(connection, appID, recordId);
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            // 非同期処理が完了した後に実行したい処理を記載
            protected void onPostExecute(Object result) {
                if (result instanceof Exception) {
                    ((ViewRecordActivity) mListener).showErrorMessage(((Exception) result).getMessage());
                } else if (result != null) {
                    detailData = (RecordData) result;
                    myViewHolder.getRecordTitle().setText(detailData.getTitle());
                    myViewHolder.getRecordNotes().setText(detailData.getDescription());
                    myViewHolder.getRecordCreaterName().setText(detailData.getCreatorName());
                    myViewHolder.getRecordCreateTime().setText(detailData.getCreatorTime());
                    ((ViewRecordActivity) mListener).setToolbarTitle(detailData.getTitle());
                    myViewHolder.getRecordStatus().setText(detailData.getStatus());

                    if (detailData != null && detailData.getFileKey() != null) {
                        for (int i = 0; i < detailData.getFileKey().size(); i++) {
                            final String fileKey = detailData.getFileKey().get(i);
                            final int retIdx = i;
                            AsyncTask displayImgTask = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] object) {
                                    FileParser parser = new FileParser();
                                    DownloadRequest request = new DownloadRequest(fileKey);
                                    try {
                                        String requestBody = parser.parseObject(request);
                                        InputStream is = connection.downloadFile(requestBody);
                                        Bitmap photo = BitmapFactory.decodeStream(is);
                                        photo = BitmapLogic.resize(photo, 110, 110);
                                        photoBits.add(photo);
                                        return retIdx;
                                    } catch (Exception e) {
                                        return null;
                                    }
                                }

                                @Override
                                protected void onPostExecute(Object result) {
                                    if (result == null) {
                                        return;
                                    }
                                    final int index = (int) result;
                                    myViewHolder.getRecordPhotoList().get(index).setImageBitmap(BitmapLogic.getCroppedBitmap(photoBits.get(index)));
                                    myViewHolder.getRecordPhotoList().get(index).setVisibility(View.VISIBLE);
                                }
                            };
                            displayImgTask.execute();
                        }
                    }
                }
                ((ViewRecordActivity) mListener).setViewEnabled(true);
            }

        };
        displayRecordsTask.execute();
    }

    private class ViewHolder {
        private TextView recordTitle = null;
        private TextView recordNotes = null;
        private TextView recordCreaterName = null;
        private TextView recordCreateTime = null;
        ArrayList<ImageView> recordPhotoList = new ArrayList<ImageView>();
        private TextView recordStatus = null;

        public ViewHolder(View view) {
            this.recordTitle = (TextView) view.findViewById(R.id.reord_title);
            this.recordNotes = (TextView) view.findViewById(R.id.reord_notes);
            this.recordCreaterName = (TextView) view.findViewById(R.id.reord_creater);
            this.recordCreateTime = (TextView) view.findViewById(R.id.reord_create_time);
            recordPhotoList.add((ImageView) view.findViewById(R.id.reord_image1));
            recordPhotoList.add((ImageView) view.findViewById(R.id.reord_image2));
            recordPhotoList.add((ImageView) view.findViewById(R.id.reord_image3));
            this.recordStatus = (TextView) view.findViewById(R.id.reord_status);
        }

        public TextView getRecordTitle() {

            return this.recordTitle;
        }

        public TextView getRecordNotes() {

            return this.recordNotes;
        }

        public TextView getRecordCreaterName() {

            return this.recordCreaterName;
        }

        public TextView getRecordCreateTime() {
            return this.recordCreateTime;
        }

        public ArrayList<ImageView> getRecordPhotoList() {
            return this.recordPhotoList;
        }

        public void hidePhotos() {
            for (ImageView photo : this.recordPhotoList) {
                photo.setVisibility(View.INVISIBLE);
            }
        }

        public TextView getRecordStatus() {

            return this.recordStatus;
        }
    }
}
