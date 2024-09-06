package com.example.glaucoscan;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FileModel {
    @SerializedName("image")
    public String image;

    @SerializedName("statusCode")
    public int statusCode;

    @SerializedName("body")
    public String body;

    public FileModel(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
