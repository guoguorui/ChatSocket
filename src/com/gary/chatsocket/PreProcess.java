package com.gary.chatsocket;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class PreProcess {
    String path;
    View view;
    HashMap<String, String> requestHeader;

    public PreProcess(Request request, Socket client, View view) {
        try {
            this.path = request.getPath(client);
            this.view = view;
            requestHeader = request.requestHeader;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String process() {
        if (path.equals("/")) {
            path = "index";
        } else if (!path.equals("")) {
            path = path.substring(1);
        }

        //图标处理
        if (path.contains("favicon.ico")) {
            path = "static/favicon.ico";
        }

        //cookie处理
        if (requestHeader.containsKey("Cookie")) {
            view.setName(requestHeader.get("Cookie").split("=")[1].substring(0, 3));
            view.setCookie(true);
        }

        return path;
    }
}
