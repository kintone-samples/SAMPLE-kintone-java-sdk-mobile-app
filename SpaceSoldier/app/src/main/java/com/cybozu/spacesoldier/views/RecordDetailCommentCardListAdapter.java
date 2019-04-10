package com.cybozu.spacesoldier.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybozu.kintone.client.model.comment.CommentContent;
import com.cybozu.kintone.client.model.comment.CommentMention;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.activities.ViewRecordActivity;
import com.cybozu.spacesoldier.entities.CommentCard;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class RecordDetailCommentCardListAdapter extends ArrayAdapter<CommentCard> {

    private List<CommentCard> commentCardList = null;

    public RecordDetailCommentCardListAdapter(Context context, int resourceId, List<CommentCard> commentCardList) {
        super(context, resourceId, commentCardList);
        this.commentCardList = commentCardList;
    }

    @Override
    public int getCount() {
        return commentCardList.size();
    }

    @Override
    public CommentCard getItem(int position) {
        return commentCardList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_card_list, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CommentCard commentCard = (CommentCard) getItem(position);
        if (commentCard != null) {
            viewHolder.getId().setText(commentCard.getId());
            viewHolder.getText().setText(commentCard.getText());
            viewHolder.getCreatorName().setText(commentCard.getCreatorName());
            viewHolder.getCreateDateTime().setText(commentCard.getCreateDateTime());
            boolean isEditable = (commentCard.getId() == null || commentCard.getId().equals(""));
            boolean canDelete = commentCard.getCreatorCode().equals(((ViewRecordActivity) RecordDetailCommentCardListAdapter.this.getContext()).getLoginUserName());
            viewHolder.setEditable(isEditable, commentCard.getDataStatus(), canDelete);

            if (commentCard.getDataStatus().equals("reply")) {
                viewHolder.getCommnetMention().setText(commentCard.getMentions());
            }

            if (isEditable) {
                viewHolder.getCancelButton().setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ViewRecordActivity context = (ViewRecordActivity) RecordDetailCommentCardListAdapter.this.getContext();
                        context.getCommentFragment().cancelEdit();
                    }
                });

                viewHolder.getSaveButton().setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ViewRecordActivity context = (ViewRecordActivity) RecordDetailCommentCardListAdapter.this.getContext();
                        ViewHolder viewHolder = new ViewHolder(context.findViewById(R.id.commentCardView));
                        CommentContent comment = new CommentContent();
                        String commnetMention = viewHolder.getCommnetMention().getText().toString().trim();
                        String commnetText = viewHolder.getCommnetText().getText().toString();
                        comment.setText(commnetText);
                        if (!commnetMention.equals("")) {
                            ArrayList<CommentMention> mentionList = new ArrayList<CommentMention>();
                            CommentMention commentMention = new CommentMention();
                            commentMention.setType("USER");
                            commentMention.setCode(commentCard.getReplyComment().getCreatorCode());
                            mentionList.add(commentMention);
                            comment.setMentions(mentionList);
                        }
                        context.getCommentFragment().saveComment(comment);
                    }
                });
            } else {
                viewHolder.getReplyButton().setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ViewRecordActivity context = (ViewRecordActivity) RecordDetailCommentCardListAdapter.this.getContext();
                        context.getCommentFragment().addReplyComment(commentCard);
                    }
                });

                viewHolder.getDeleteButton().setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ViewRecordActivity context = (ViewRecordActivity) RecordDetailCommentCardListAdapter.this.getContext();
                        context.getCommentFragment().deleteComment(commentCard);
                    }
                });
            }
        }
        return convertView;
    }

    private class ViewHolder {
        // display part
        private TextView text = null;
        private TextView id = null;
        private TextView creatorName = null;
        private TextView creatorCode = null;
        private TextView createDateTime = null;
        private LinearLayout editPart = null;
        private LinearLayout displayPartTitle = null;
        private LinearLayout displayPart = null;
        private Button replyButton = null;
        private Button deleteButton = null;
        // edit part
        private EditText commnetMention = null;
        private EditText commnetText = null;
        private Button saveButton = null;
        private Button cancelButton = null;

        public ViewHolder(View view) {
            // display part
            this.id = view.findViewById(R.id.comment_id);
            this.text = view.findViewById(R.id.comment_text);
            this.creatorName = view.findViewById(R.id.comment_creater_name);
            this.creatorName = view.findViewById(R.id.comment_creater_name);
            this.createDateTime = view.findViewById(R.id.comment_create_time);
            this.editPart = view.findViewById(R.id.comment_edit_part);
            this.displayPartTitle = view.findViewById(R.id.comment_display_part_title);
            this.displayPart = view.findViewById(R.id.comment_display_part);
            this.replyButton = view.findViewById(R.id.reply_comment);
            this.deleteButton = view.findViewById(R.id.delete_comment);
            // edit part
            this.commnetMention = view.findViewById(R.id.commnet_mention);
            this.commnetText = view.findViewById(R.id.commnet_text);
            this.saveButton = view.findViewById(R.id.save_comment);
            this.cancelButton = view.findViewById(R.id.cancel_comment);

            this.commnetMention.setFocusable(false);
            this.commnetMention.setFocusableInTouchMode(false);
        }

        public TextView getId() {

            return this.id;
        }

        public TextView getText() {

            return this.text;
        }

        public TextView getCreatorName() {

            return this.creatorName;
        }

        public TextView getCreateDateTime() {
            return this.createDateTime;
        }

        public void setEditable(boolean editable, String status, boolean canDelete) {
            if (editable) {
                this.editPart.setVisibility(View.VISIBLE);
                this.displayPartTitle.setVisibility(View.GONE);
                this.displayPart.setVisibility(View.GONE);

                switch (status) {
                    case "new":
                        this.commnetMention.setVisibility(View.GONE);
                        break;
                    case "reply":
                        this.commnetMention.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                this.editPart.setVisibility(View.GONE);
                this.displayPartTitle.setVisibility(View.VISIBLE);
                this.displayPart.setVisibility(View.VISIBLE);

                if (canDelete) {
                    this.deleteButton.setVisibility(View.VISIBLE);
                } else {
                    this.deleteButton.setVisibility(View.GONE);
                }
            }
        }

        public Button getSaveButton() {

            return this.saveButton;
        }

        public Button getCancelButton() {

            return this.cancelButton;
        }

        public Button getReplyButton() {

            return this.replyButton;
        }

        public Button getDeleteButton() {

            return this.deleteButton;
        }

        public EditText getCommnetMention() {

            return this.commnetMention;
        }

        public EditText getCommnetText() {

            return this.commnetText;
        }
    }
}