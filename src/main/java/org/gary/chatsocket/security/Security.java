package org.gary.chatsocket.security;

import org.gary.chatsocket.dao.CacheCookie;
import org.gary.chatsocket.dao.ConnectPool;
import org.gary.chatsocket.dao.DAO;
import org.gary.chatsocket.mvc.Ajax;
import org.gary.chatsocket.mvc.View;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class Security {

    private static ConnectPool cp = Ajax.cp;

    public static void verification(String path, View view, OutputStream os) throws IOException {
        String param = path.split("\\?")[1];
        String name = param.split("&")[0].split("=")[1];
        String password = param.split("&")[1].split("=")[1];
        DAO od = new DAO(cp);
        if (od.authenticate(name, password)) {
            String token=name+"="+UUID.randomUUID();
            view.setToken(token);
            view.setName(name);
            view.setEnableSession(true);
            CacheCookie.addCookie(token);
            view.directView("chatwho");
        } else {
            os.write("sorry invalid account.\n".getBytes());
        }
    }

    public static void intercept(String path, String cookie, View view) throws IOException {
        System.out.println("into intercept");
        if (cookie!=null && CacheCookie.judgeCookie(cookie)) {
            view.setName(cookie.substring(0,cookie.indexOf('=')));
            view.directView(path);
        } else {
            view.directView("login");
        }
    }
}
