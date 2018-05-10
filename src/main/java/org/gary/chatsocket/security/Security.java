package org.gary.chatsocket.security;

import org.gary.chatsocket.dao.Cache;
import org.gary.chatsocket.dao.ConnectPool;
import org.gary.chatsocket.dao.AccountDao;
import org.gary.chatsocket.mvc.Ajax;
import org.gary.chatsocket.mvc.Model;
import org.gary.chatsocket.mvc.View;
import org.gary.chatsocket.util.CookieUtil;

import java.io.OutputStream;
import java.util.UUID;

public class Security {

    private static ConnectPool cp = Ajax.cp;

    public static void verification(String path, View view, OutputStream os) throws Exception {
        String param = path.split("\\?")[1];
        String name = param.split("&")[0].split("=")[1];
        String password = param.split("&")[1].split("=")[1];
        AccountDao od = new AccountDao(cp);
        if (od.authenticate(name, password)) {
            String cookieString="cookie-"+name+"="+UUID.randomUUID();
            view.setPutCookie(true);
            view.setCookie(cookieString);
            Cache.addCookie(CookieUtil.parse(cookieString));
            view.setModel(generateModel(name));
            view.directView("chatwho");
        } else {
            os.write("sorry invalid account.\n".getBytes());
        }
    }

    public static void intercept(String path, String rawCookie, View view) throws Exception {
        System.out.println("into intercept");
        String[] cookie= CookieUtil.parse(rawCookie);
        if (cookie[0]!=null && Cache.judgeCookie(cookie)) {
            view.setModel(generateModel(cookie[0].substring(7)));
            view.directView(path);
        } else {
            view.directView("login");
        }
    }

    public static void logout(String rawCookie,View view) throws Exception{
        System.out.println("into logout");
        String key=CookieUtil.parse(rawCookie)[0];
        Cache.removeCookie(key);
        view.directView("index");
    }

    private static Model generateModel(String name) throws Exception{
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

}
