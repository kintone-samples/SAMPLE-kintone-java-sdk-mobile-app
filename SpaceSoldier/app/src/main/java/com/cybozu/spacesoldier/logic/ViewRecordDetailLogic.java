package com.cybozu.spacesoldier.logic;

import com.cybozu.kintone.client.connection.Connection;
import com.cybozu.kintone.client.exception.KintoneAPIException;
import com.cybozu.kintone.client.model.comment.Comment;
import com.cybozu.kintone.client.model.comment.CommentContent;
import com.cybozu.kintone.client.model.comment.GetCommentsResponse;
import com.cybozu.kintone.client.model.file.FileModel;
import com.cybozu.kintone.client.model.member.Member;
import com.cybozu.kintone.client.model.record.GetRecordResponse;
import com.cybozu.kintone.client.model.record.field.FieldValue;
import com.cybozu.kintone.client.module.record.Record;
import com.cybozu.spacesoldier.entities.CommentCard;
import com.cybozu.spacesoldier.entities.RecordData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewRecordDetailLogic {

    public RecordData createRecordDetail(Connection connection, Integer appID, Integer recordId) throws KintoneAPIException {
        RecordData recordData = new RecordData();
        HashMap<String, FieldValue> record;
        Record recordManagement = new Record(connection);
        GetRecordResponse response = recordManagement.getRecord(appID, recordId);
        record = response.getRecord();
        if (record.containsKey("$id")) {
            recordData.setId((String) record.get("$id").getValue());
        }

        if (record.containsKey("Summary")) {
            recordData.setTitle((String) record.get("Summary").getValue());
        }
        if (record.containsKey("Notes")) {
            recordData.setDescription((String) record.get("Notes").getValue());
        }
        if (record.containsKey("Creator")) {
            recordData.setCreatorName(((Member) record.get("Creator").getValue()).getName());
        }

        if (record.containsKey("CreateDateTime")) {
            recordData.setCreatorTime((String) record.get("CreateDateTime").getValue());
        }

        if (record.containsKey("Status")) {
            recordData.setStatus((String) record.get("Status").getValue());
        }

        ArrayList<String> fileKeyList = new ArrayList<String>();
        if (record.containsKey("Photo")) {
            ArrayList<FileModel> fileList = (ArrayList<FileModel>) record.get("Photo").getValue();
            if (fileList.size() > 0) {
                for (FileModel fileData : fileList) {
                    if (Integer.parseInt(fileData.getSize()) > 0 && fileData.getContentType().contains("image")) {
                        fileKeyList.add((String) fileData.getFileKey());
                        if (fileKeyList.size() == 3) {
                            break;
                        }
                    }
                }
            }
            recordData.setFileKey(fileKeyList);
        }
        return recordData;
    }

    public void deleteRecord(Connection connection, Integer appID, Integer recordId) throws KintoneAPIException {
        ArrayList<Integer> recordIds = new ArrayList<>();
        recordIds.add(recordId);

        // delete records
        Record recordManagement = new Record(connection);
        recordManagement.deleteRecords(appID, recordIds);
    }

    public List<CommentCard> createCommentList(Connection connection, Integer appID, Integer recordId) throws KintoneAPIException {
        ArrayList<Comment> resultComments;
        // get records
        Record recordManagement = new Record(connection);
        Integer offset = 0;
        Integer limit = 10;
        List<CommentCard> commentCardList = new ArrayList<>();

        boolean hasRecord = true;
        while (hasRecord) {
            GetCommentsResponse response = recordManagement.getComments(appID, recordId, null, offset, limit);
            resultComments = response.getComments();
            offset = offset + limit;
            hasRecord = response.getOlder();
            if (resultComments != null) {
                for (Comment comment : resultComments) {
                    CommentCard commentCard = new CommentCard(comment);
                    commentCardList.add(commentCard);
                }
            }
        }
        return commentCardList;
    }

    public void addComment(Connection connection, Integer appID, Integer recordId, CommentContent comment) throws KintoneAPIException {
        Record recordManagement = new Record(connection);
        recordManagement.addComment(appID, recordId, comment);
    }

    public void deleteComment(Connection connection, Integer appID, Integer recordId, Integer commentId) throws KintoneAPIException {
        Record recordManagement = new Record(connection);
        recordManagement.deleteComment(appID, recordId, commentId);
    }

}
