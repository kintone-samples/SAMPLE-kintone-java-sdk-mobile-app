package com.cybozu.spacesoldier.views;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.model.comment.CommentContent;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.activities.ViewRecordActivity;
import com.cybozu.spacesoldier.entities.CommentCard;
import com.cybozu.spacesoldier.logic.ViewRecordDetailLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordDetailCommentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class RecordDetailCommentFragment extends Fragment implements DialogInterface.OnClickListener {

    private Integer recordId = null;
    private AppCommon myApp;
    private ViewRecordDetailLogic myLogic;
    private OnFragmentInteractionListener mListener;

    private ListView listView = null;
    private View noCommentView = null;
    private List<CommentCard> commentCardList = new ArrayList<CommentCard>();
    private RecordDetailCommentCardListAdapter adapter = null;
    private Integer commentCardCount = 0;
    private String deleteCommentId = "";

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
        View commnetFragmentView = inflater.inflate(R.layout.fragment_comment, container, false);
        listView = commnetFragmentView.findViewById(R.id.view_record_comment_list);
        noCommentView = commnetFragmentView.findViewById(R.id.view_record_comment_no_comment);
        return commnetFragmentView;
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

    public void showCommentList() {
        myLogic = new ViewRecordDetailLogic();
        final Connection connection = myApp.getCONNECTION();
        final Integer appID = myApp.getAppID();

        final Context that = (Context) mListener;
        ((ViewRecordActivity) mListener).setViewEnabled(false);
        AsyncTask displayRecordsTask = new AsyncTask() {
            @Override
            // 非同期実行したい処理を記載
            protected Object doInBackground(Object[] object) {
                try {
                    return myLogic.createCommentList(connection, appID, recordId);
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
                    commentCardList = (List<CommentCard>) result;
                    List<CommentCard> list = commentCardList;
                    if (list.size() == 0) {
                        if (adapter != null) {
                            adapter.clear();
                        }
                        showNoCommentView();
                    } else {
                        hideNoCommentView();
                    }
                    commentCardList = list;
                    commentCardCount = list.size();
                    adapter = new RecordDetailCommentCardListAdapter(RecordDetailCommentFragment.this.getActivity(), R.layout.comment_card_list, list);
                    listView.setAdapter(adapter);
                }
                ((ViewRecordActivity) mListener).setViewEnabled(true);
            }
        };
        displayRecordsTask.execute();

    }

    public void showNoCommentView() {
        noCommentView.setVisibility(View.VISIBLE);
    }

    public void hideNoCommentView() {
        noCommentView.setVisibility(View.GONE);
    }

    public void addNewComment() {
        if (!this.isAllowToAddNew()) {
            return;
        }
        hideNoCommentView();
        CommentCard newCommnet = new CommentCard("new");
        commentCardList.add(0, newCommnet);
        adapter = new RecordDetailCommentCardListAdapter(RecordDetailCommentFragment.this.getActivity(), R.layout.comment_card_list, commentCardList);
        listView.setAdapter(adapter);
    }

    public void addReplyComment(CommentCard replyComment) {
        if (!this.isAllowToAddNew()) {
            return;
        }
        CommentCard newCommnet = new CommentCard("reply");
        newCommnet.setMentions("@" + replyComment.getCreatorName());
        newCommnet.setReplyComment(replyComment);

        commentCardList.add(0, newCommnet);
        adapter = new RecordDetailCommentCardListAdapter(RecordDetailCommentFragment.this.getActivity(), R.layout.comment_card_list, commentCardList);
        listView.setAdapter(adapter);
    }

    public void deleteComment(CommentCard deleteComment) {
        this.deleteCommentId = deleteComment.getId();
        ((ViewRecordActivity) mListener).showConfirmDialog(getString(R.string.comment_delete_message), getString(R.string.delete_ok_message), getString(R.string.delete_ng_message), this);
    }

    public void saveComment(CommentContent comment) {
        myLogic = new ViewRecordDetailLogic();
        final CommentContent commentData = comment;
        AsyncTask saveCommentTask = new AsyncTask() {
            @Override
            // 非同期実行したい処理を記載
            protected Object doInBackground(Object[] object) {
                try {
                    final Connection connection = myApp.getCONNECTION();
                    final Integer appID = myApp.getAppID();
                    myLogic.addComment(connection, appID, recordId, commentData);
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
                        ((ViewRecordActivity) mListener).showErrorMessage(((Exception) result).getMessage());
                    } else if ((boolean) result == true) {
                        commentCardCount = 0;
                        showCommentList();
                    }
                }
            }
        };
        saveCommentTask.execute();
    }

    public void cancelEdit() {
        this.deleteAddedCommentCard();
        List<CommentCard> list = commentCardList;
        if (list.size() == 0) {
            if (adapter != null) {
                adapter.clear();
            }
            showNoCommentView();
        }
        adapter = new RecordDetailCommentCardListAdapter(RecordDetailCommentFragment.this.getActivity(), R.layout.comment_card_list, commentCardList);
        listView.setAdapter(adapter);
    }

    private boolean isAllowToAddNew() {
        if (commentCardList.size() > 0) {
            switch (commentCardList.get(0).getDataStatus()) {
                case "new":
                case "reply":
                    return false;
            }
        }
        return true;
    }

    private void deleteAddedCommentCard() {
        if (commentCardList.size() > 0) {
            switch (commentCardList.get(0).getDataStatus()) {
                case "new":
                case "reply":
                    commentCardList.remove(0);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (((AlertDialog) dialog).getButton(which).getText().toString() == getString(R.string.delete_ok_message)) {
            myLogic = new ViewRecordDetailLogic();
            AsyncTask deleteCommentTask = new AsyncTask() {
                @Override
                // 非同期実行したい処理を記載
                protected Object doInBackground(Object[] object) {
                    try {
                        final Connection connection = myApp.getCONNECTION();
                        final Integer appID = myApp.getAppID();
                        myLogic.deleteComment(connection, appID, recordId, Integer.parseInt(deleteCommentId));
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
                            ((ViewRecordActivity) mListener).showErrorMessage(((Exception) result).getMessage());
                        } else if ((boolean) result == true) {
                            commentCardCount = 0;
                            showCommentList();
                        }
                    }
                }
            };
            deleteCommentTask.execute();
        }
    }
}
