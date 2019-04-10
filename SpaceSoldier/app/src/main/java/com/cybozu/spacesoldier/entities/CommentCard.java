package com.cybozu.spacesoldier.entities;

import com.cybozu.kintone.client.model.comment.Comment;

public class CommentCard {

    private String id = "";
    private String text = "";
    private String creatorCode = "";
    private String creatorName = "";
    private String createDateTime = "";
    private String mentions = "";
    private String status = "";
    private CommentCard replyComment = null;

    public CommentCard(String status) {
        this.status = status;
    }

    public CommentCard(Comment comment) {
        this.id = comment.getId().toString();
        this.text = comment.getText();
        this.creatorCode = comment.getCreator().getCode();
        this.creatorName = comment.getCreator().getName();
        this.createDateTime = comment.getCreatedAt().toString();
    }

    public String getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public String getCreatorCode() {
        return this.creatorCode;
    }

    public String getCreatorName() {
        return this.creatorName;
    }

    public String getMentions() {
        return this.mentions;
    }

    public void setMentions(String mentions) {
        this.mentions = mentions;
    }

    public String getCreateDateTime() {
        return this.createDateTime;
    }

    public String getDataStatus() {
        return this.status;
    }

    public void setReplyComment(CommentCard replyComment) {
        this.replyComment = replyComment;
    }

    public CommentCard getReplyComment() {
        return this.replyComment;
    }
}