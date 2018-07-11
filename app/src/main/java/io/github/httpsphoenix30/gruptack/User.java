package io.github.httpsphoenix30.gruptack;

import java.util.HashMap;

public class User {
    private String uuid,name,image,email;
    private HashMap<String,Boolean> MyGroups = new HashMap<>(20);

    public User(){

    }

    public HashMap<String, Boolean> getMyGroups() {
        return MyGroups;
    }

    public void setMyGroups(HashMap<String, Boolean> MyGroups) {
        MyGroups = MyGroups;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void addProject(String id){
        MyGroups.put(id,true);
    }

    public void removeProject(String id){
        MyGroups.remove(id);
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
