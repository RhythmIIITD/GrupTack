package io.github.httpsphoenix30.gruptack;

/**
 * Created by Shourya on 26-03-2018.
 */

public class Members {

    private String name, email, img;

    public Members(){ }

    public Members(String name, String email, String img){
        this.name = name;
        this.email = email;
        this.img = img;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
