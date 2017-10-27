package com.example.android.firebasegps1;

/**
 * Created by nick on 10/24/2017.
 */

public class ChatMessageObject {

    public String username;
    public String message;
    public String userImageUrl = null;
    public ChatMessageObject(){}

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public ChatMessageObject(String name, String msg, String url ){
        username = name;
        message = msg;
        userImageUrl = url;
    }

    @Override
    public String toString() {
        return "Username: " + username + ", message: " + message + ", ImgUrl: " + userImageUrl;
    }
}
