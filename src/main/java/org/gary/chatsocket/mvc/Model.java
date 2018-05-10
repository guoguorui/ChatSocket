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
            throw new RuntimeException("parameter num error");
        Field[] fields=this.getClass().getDeclaredFields();
        for(int i=0;i<len;i+=2){
            for(Field field:fields){
                if(field.getName().equals(strings[i]))
                    field.set(this,strings[i+1]);
            }
        }
    }

    public static Model generateModel(String name) throws Exception{
        //需要查库将friend查出来填入model中的属性
        String friend="";
        if(name.equals("GGR"))
            friend="abc";
        else if(name.equals("abc"))
            friend="GGR";
        Model model=new Model();
        model.setFields("friend",friend);
        return model;
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
