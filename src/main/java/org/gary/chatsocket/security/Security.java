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

    public static void verification(String path, View view) throws Exception {
        String param = path.split("\\?")[1];
        String name = param.split("&")[0].split("=")[1];
        String password = param.split("&")[1].split("=")[1];
        AccountDao od = new AccountDao(cp);
        if (od.authenticate(name, password)) {
            String cookieString="cookie-"+name+"="+UUID.randomUUID();
            view.setPutCookie(true);
            view.setCookie(cookieString);
            Cache.addCookie(CookieUtil.parse(cookieString));
            view.setModel(Model.generateModel(name));
            view.directView("chatwho");
        } else {
            view.directView("error");
        }
    }

    public static void intercept(String path, String rawCookie, View view) throws Exception {
        System.out.println("into intercept");
        String[] cookie= CookieUtil.parse(rawCookie);
        if (cookie[0]!=null && Cache.judgeCookie(cookie)) {
            Cache.updateExpire(cookie[0]);
            view.setModel(Model.generateModel(cookie[0].substring(7)));
            view.directView(path);
        } else {
            view.directView("login");
        }
    }

    public static void logout(String rawCookie) throws Exception{
        System.out.println("into logout");
        String key=CookieUtil.parse(rawCookie)[0];
        Cache.removeCookie(key);
    }

}
