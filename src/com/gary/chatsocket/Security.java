package com.gary.chatsocket;

import java.io.IOException;
import java.io.PrintWriter;

class Security {

    private static ConnectPool cp = Ajax.cp;

    static void doProcessLogin(String path, View view, PrintWriter pw) throws IOException {
        String param = path.split("\\?")[1];
        String name = param.split("&")[0].split("=")[1];
        String password = param.split("&")[1].split("=")[1];
        DAO od = new DAO(cp);
        if (od.authenticate(name, password)) {
            view.setName(name);
            view.setEnableSession(true);
            view.directView("chatwho");
        } else {
            pw.write("sorry invalid account.\n");
            pw.close();
        }
    }

    static void doSafe(String path, View view) throws IOException {
        System.out.println("into doSafe");
        if (view.cookie) {
            view.directView(path);
        } else {
            view.directView("login");
        }
    }
}
