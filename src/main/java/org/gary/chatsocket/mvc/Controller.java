package org.gary.chatsocket.mvc;

import org.gary.chatsocket.chat.WebSocket;
import org.gary.chatsocket.security.Security;
import org.gary.chatsocket.util.CookieUtil;

import java.io.*;
import java.net.Socket;

//html不能在脚本间用注释//
//使用多路复用
//整理实例和静态
//整理WebSocket
//异常封装

public class Controller {

    private Socket client;
    private OutputStream os;
    private Request request;
    private View view;
    private Ajax ajax;

    public Controller(Socket client) throws IOException {
        this.client = client;
        os=client.getOutputStream();
        request=new Request(new BufferedReader(new InputStreamReader(client.getInputStream())));
        view = new View(os);
        ajax = new Ajax(os);
    }

    public void dispatch() throws Exception {

        //request对path进行解析，包括header的存储
        String path=request.parse();
        String rawCookie=request.getRequestHeader().get("Cookie");

        //僵尸socket处理
        if (path.equals("")) {
            os.close();
            return;
        }

        System.out.println("\n" + client.getInetAddress() + ":" + client.getPort());
        System.out.println(path);

        //用户点击了建立连接升级websocket协议的按钮，开始监听用户输入，控制权交给WebSocket类
        if (request.getRequestHeader().containsKey("Sec-WebSocket-Key")) {
            String key=request.getRequestHeader().get("Sec-WebSocket-Key");
            String name=CookieUtil.getName(rawCookie);
            WebSocket.connectAndListen(key,name,client);
            return;
            //pw不能在这里close，这个socket要手动升级为websocket
        }

        //选择聊天对象前进行登录拦截
        if (path.contains("chatwho")) {
            Security.intercept(path,rawCookie,view);
            System.out.println("");
        }

        //验证用户名和密码
        else if (path.contains("processLogin")) {
            Security.verification(path, view, os);
            System.out.println("");
        }

        //用户选中了要聊天的对象，进行关系映射
        else if (path.contains("wschat")) {
            String name= CookieUtil.getName(rawCookie);
            WebSocket.chooseFriend(path,name,view);
            System.out.println("");
        }

        //清除缓存中的cookie
        else if (path.contains("logout")){
            Security.logout(rawCookie,view);
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

        //静态页面渲染
        else if (path.contains("static")) {
            view.directStatic(path);
        }

        //普通页面渲染
        else {
            view.directView(path);
        }

        os.close();
    }
}
