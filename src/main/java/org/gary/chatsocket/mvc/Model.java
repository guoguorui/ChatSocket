package org.gary.chatsocket.mvc;

import java.lang.reflect.Field;

public class Model {

    private String name;

    private String friend;

    public Model() {
    }

    public Model(String name, String friend) {
        this.name = name;
        this.friend = friend;
    }

    public void setFields(String ...strings) throws Exception{
        int len=strings.length;
        if(len%2!=0)
            throw new Exception("parameter num error");
        Field[] fields=this.getClass().getDeclaredFields();
        for(int i=0;i<len;i+=2){
            for(Field field:fields){
                if(field.getName().equals(strings[i]))
                    field.set(this,strings[i+1]);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }
}
