package org.gary.chatsocket.security;

import org.gary.chatsocket.dao.ConnectPool;
import org.gary.chatsocket.dao.DAO;
import org.gary.chatsocket.mvc.Ajax;
import org.gary.chatsocket.mvc.View;

import java.io.IOException;
import java.io.OutputStream;

public class Security {

    private static ConnectPool cp = Ajax.cp;

    public static void doProcessLogin(String path, View view, OutputStream os) throws IOException {
        String param = path.split("\\?")[1];
        String name = param.split("&")[0].split("=")[1];
        String password = param.split("&")[1].split("=")[1];
        DAO od = new DAO(cp);
        if (od.authenticate(name, password)) {
            view.setName(name);
            view.setEnableSession(true);
            view.directView("chatwho");
        } else {
            os.write("sorry invalid account.\n".getBytes());
        }
    }

    public static void doSafe(String path, View view) throws IOException {
        System.out.println("into doSafe");
        if (view.getCookie()) {
            view.directView(path);
        } else {
            view.directView("login");
        }
    }
}
