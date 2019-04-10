package com.cybozu.spacesoldier.entities;

import com.cybozu.kintone.client.model.file.FileModel;
import com.cybozu.kintone.client.model.member.Member;
import com.cybozu.kintone.client.model.record.field.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordCard {

    private Integer id;
    private String summary;
    private String description;
    private String fileKey;
    private String creator;
    private String createDateTime;

    private enum FieldKeys {
        ID("$id"),
        SUMMARY("Summary"),
        NOTES("Notes"),
        FILE_KEY("Photo"),
        CREATOR("Creator"),
        CREATE_DATE_TIME("CreateDateTime");

        private String fieldKey;

        FieldKeys(String fieldKey) {
            this.fieldKey = fieldKey;
        }

        private String getValue() {
            return fieldKey;
        }
    }

    public RecordCard(String summary, String description, String fileKey) {
        this.description = description;
        this.summary = summary;
        this.fileKey = fileKey;
    }

    public RecordCard(HashMap<String, FieldValue> record) {
        String key = FieldKeys.ID.getValue();
        this.id = Integer.parseInt(record.get(FieldKeys.ID.getValue()).getValue().toString());
        if (record.containsKey(FieldKeys.SUMMARY.getValue()) && record.get(FieldKeys.SUMMARY.getValue()) != null) {
            this.summary = record.get(FieldKeys.SUMMARY.getValue()).getValue().toString();
        }
        if (record.containsKey(FieldKeys.NOTES.getValue()) && record.get(FieldKeys.NOTES.getValue()) != null) {
            this.description = record.get(FieldKeys.NOTES.getValue()).getValue().toString();
        }
        if (record.containsKey(FieldKeys.FILE_KEY.getValue()) && ((ArrayList) record.get(FieldKeys.FILE_KEY.getValue()).getValue()).size() > 0) {
            ArrayList<FileModel> fileList = (ArrayList) record.get(FieldKeys.FILE_KEY.getValue()).getValue();
            String contentType = "";
            Integer limit = ((ArrayList) record.get(FieldKeys.FILE_KEY.getValue()).getValue()).size();
            for (int i = 0; i < limit && !contentType.contains("image"); i++) {
                FileModel file = fileList.get(i);
                contentType = file.getContentType();
                if (contentType.contains("image")) {
                    this.fileKey = file.getFileKey();
                }
            }
        }
        if (record.containsKey(FieldKeys.CREATOR.getValue()) && record.get(FieldKeys.CREATOR.getValue()) != null) {
            this.creator = ((Member) record.get(FieldKeys.CREATOR.getValue()).getValue()).getName();
        }
        if (record.containsKey(FieldKeys.CREATE_DATE_TIME.getValue()) && record.get(FieldKeys.CREATE_DATE_TIME.getValue()) != null) {
            this.createDateTime = record.get(FieldKeys.CREATE_DATE_TIME.getValue()).getValue().toString();
        }
    }

    public void setRecord(HashMap<String, FieldValue> record) {
        this.id = Integer.parseInt(record.get(FieldKeys.ID.name()).getValue().toString());
        if (record.containsKey(FieldKeys.SUMMARY.name())) {
            this.summary = record.get(FieldKeys.SUMMARY.name()).getValue().toString();
        }
        if (record.containsKey(FieldKeys.NOTES.name())) {
            this.description = record.get(FieldKeys.NOTES.name()).getValue().toString();
        }
        if (record.containsKey(FieldKeys.FILE_KEY.name())) {
            this.fileKey = record.get(FieldKeys.FILE_KEY.name()).getValue().toString();
        }
        if (record.containsKey(FieldKeys.CREATOR.name())) {
            this.creator = ((Member) record.get(FieldKeys.CREATOR.name()).getValue()).getName();
        }
        if (record.containsKey(FieldKeys.CREATE_DATE_TIME.name())) {
            this.createDateTime = record.get(FieldKeys.CREATE_DATE_TIME.name()).getValue().toString();
        }
    }

    public Integer getId() {
        return this.id;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public String getFileKey() {
        return fileKey;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }
}