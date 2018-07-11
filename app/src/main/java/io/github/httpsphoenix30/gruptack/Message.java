package io.github.httpsphoenix30.gruptack;

public class Message {
    private String text,sender,uri,key,timeStamp;
    private boolean isSelected;
    private String caption;

    public Message() {}

    public Message(String text,String sender,String uri,String key, String timeStamp){
        this.text = text;
        this.sender = sender;
        this.uri = uri;
        this.key = key;
        this.timeStamp = timeStamp;
        this.isSelected=false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getUri() {
        return uri;
    }

    public String getKey() {
        return key;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
