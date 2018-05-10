package org.gary.chatsocket.chat;

import org.gary.chatsocket.mvc.Model;
import org.gary.chatsocket.mvc.View;
import org.gary.chatsocket.util.CookieUtil;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocket {

    static HashMap<String, Socket> nameToSocket = new HashMap<>();
    private static HashMap<String, String> nameToFriend = new HashMap<>();
    private static HashMap<String, String> nameToMessage =new HashMap<>();
    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    static void connect(String key,PrintWriter pw) throws Exception{
        key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(key.getBytes("utf-8"), 0, key.length());
        byte[] sha1Hash = md.digest();
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        key = encoder.encode(sha1Hash);
        pw.println("HTTP/1.1 101 Switching Protocols");
        pw.println("Upgrade: websocket");
        pw.println("Connection: Upgrade");
        pw.println("Sec-WebSocket-Accept: " + key);
        pw.println();
        pw.flush();
    }

    static void writeToClient(String name, String message) {
        String friend = nameToFriend.get(name);
        Socket friendClient = nameToSocket.get(friend);
        Socket localClient = nameToSocket.get(name);
        if (friendClient==null || friendClient.isClosed()) {
            nameToSocket.remove(friend);
            executor.execute(new WriteTask(localClient,message+"\r\n该好友不在线...其上线后会收到信息"));
            String unRead=nameToMessage.get(friend);
            if(unRead==null)
                unRead=message;
            else
                unRead+="\r\n"+message;
            nameToMessage.put(friend,unRead);
        } else {
            executor.execute(new WriteTask(localClient,message));
            executor.execute(new WriteTask(friendClient,message));
        }
    }

    public static void chooseFriend(String path,String rawCookie,View view) throws Exception{
        String name= CookieUtil.getName(rawCookie);
        String friend = path.split("=")[1];
        nameToFriend.put(name, friend);
        view.setModel(new Model(name,friend));
        view.directView("wschat");
    }

    public static void connectAndListen(String key, String rawCookie,Socket client) throws Exception{
        String name=CookieUtil.getName(rawCookie);
        PrintWriter pw=new PrintWriter(client.getOutputStream());
        WebSocket.connect(key,pw);
        nameToSocket.put(name, client);
        String unRead=nameToMessage.get(name);
        if(unRead!=null){
            executor.execute(new WriteTask(client,unRead));
            nameToMessage.remove(name);
        }
        executor.execute(new ReadTask(client));
    }

}

