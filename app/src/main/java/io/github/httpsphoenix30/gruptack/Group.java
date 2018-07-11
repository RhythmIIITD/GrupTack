package io.github.httpsphoenix30.gruptack;

import android.net.Uri;

import java.net.URI;
import java.util.HashMap;

class Group {

    private String name;    //Title
    private String uri;     //ImageLink
    private String key;     //Unique ID

    private HashMap<String,Integer> deadline = new HashMap<>(3);

    public Group(){

    }

    public Group(String name, int date, int month, int year, String uri){
        this.name = name;
        this.deadline.put("Date",date);
        this.deadline.put("Month",month);
        this.deadline.put("Year",year);
        this.uri = uri;

    }


    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Integer> getDeadline() {
        return deadline;
    }

    public void setDeadline(HashMap<String, Integer> deadline) {
        this.deadline = deadline;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
