package com.gary.chatsocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

//html不能在脚本间用注释//
//使用多路复用
//架构变成switch case
//对cookie内容进行编码，使其含有用户信息

class Response {

    private Socket client;
    private PrintWriter pw;
    private View view;
    private Ajax ajax;

    Response(Socket client) throws IOException {
        this.client = client;
        pw = new PrintWriter(client.getOutputStream());
        view = new View(client, pw);
        ajax = new Ajax(pw);
    }

    void response(Request request) throws Exception {
        
        System.out.println("\n" + client.getInetAddress() + ":" + client.getPort());

        String path=request.parse(view);
        HashMap<String, String> requestHeader=request.getRequestHeader();


        //僵尸socket处理
        if (path.equals("")) {
            pw.close();
            return;
        }

        //websocket处理
        if (requestHeader.containsKey("Sec-WebSocket-Key")) {
            WebSocket.processRead(requestHeader.get("Sec-WebSocket-Key"), pw, client, view);
            return;
            //pw不能在这里close，这个socket要手动升级为websocket
        }

        //静态资源处理
        if (path.contains("static")) {
            view.directStatic(path);
        }

        //chat处理
        else if (path.contains("chatwho")) {
            Security.doSafe(path, view);
            System.out.println("");
        }

        // ‘=’并非特殊符号
        else if (path.contains("wschat")) {
            WebSocket.processRelationship(path, view);
            System.out.println("");
        }

        //登录处理
        else if (path.contains("processLogin")) {
            Security.doProcessLogin(path, view, pw);
            System.out.println("");
        }

        //响应ajax
        else if (path.contains("ajax")) {
            ajax.doAjax();
            System.out.println("");
        } else if (path.contains("jdbc")) {
            ajax.doJdbc();
            System.out.println("");
        }

        else {
            view.directView(path);
        }

    }
}
