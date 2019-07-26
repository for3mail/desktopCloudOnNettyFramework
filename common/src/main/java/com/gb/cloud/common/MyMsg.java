package com.gb.cloud.common;

import java.io.Serializable;
import java.util.ArrayList;

public class MyMsg implements Serializable {
    public String login;
    public String password;
    public ArrayList<String> stringListView;
    public String fileName;


    public enum Type {FILE_REQUEST, REFRESH_REQUEST, AUTH_REQUEST};
    public Type type;



    public MyMsg(Type type) {
        stringListView = new ArrayList<>();
        this.type = type;
    }
}
