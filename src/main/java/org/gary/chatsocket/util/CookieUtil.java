package org.gary.chatsocket.util;

public class CookieUtil {

    public static String[] parse(String rawCookie){
        String[] res=new String[2];
        String[] ss=rawCookie.split("; ");
        for(String s:ss){
            if(s.startsWith("cookie-")){
                res=s.split("=");
                break;
            }
        }
        return res;
    }

    public static String getName(String rawCookie){
        return parse(rawCookie)[0].substring(7);
    }

}
