package org.gary.chatsocket.security;

import org.gary.chatsocket.dao.CacheCookie;
import org.gary.chatsocket.dao.ConnectPool;
import org.gary.chatsocket.dao.DAO;
import org.gary.chatsocket.mvc.Ajax;
import org.gary.chatsocket.mvc.Model;
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
            view.setEnableSession(true);
            CacheCookie.addCookie(token);
            view.directView("chatwho");
        } else {
            os.write("sorry invalid account.\n".getBytes());
        }
    }

    public static void intercept(String path, String cookie, View view) throws Exception {
        System.out.println("into intercept");
        if (cookie!=null && CacheCookie.judgeCookie(cookie)) {
            //需要查库将friend查出来填入model中的属性
            String name=cookie.substring(0,cookie.indexOf('='));
            String friend="";
            if(name.equals("GGR"))
                friend="abc";
            else if(name.equals("abc"))
                friend="GGR";
            Model model=new Model();
            model.setFields("friend",friend);
            view.setModel(model);
            view.directView(path);
        } else {
            view.directView("login");
        }
    }
}
