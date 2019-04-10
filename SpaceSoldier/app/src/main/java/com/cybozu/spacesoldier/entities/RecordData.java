package com.cybozu.spacesoldier.entities;

import java.util.ArrayList;

public class RecordData {

    private String id = "";
    private String title = "";
    private String description = "";
    private ArrayList<String> fileKey = null;
    private String creatorName = "";
    private String creatorTime = "";
    private String status = "";

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setFileKey(ArrayList<String> fileKey) {
        this.fileKey = fileKey;
    }

    public ArrayList<String> getFileKey() {
        return this.fileKey;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorName() {
        return this.creatorName;
    }

    public void setCreatorTime(String creatorTime) {
        this.creatorTime = creatorTime;
    }

    public String getCreatorTime() {
        return this.creatorTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

}