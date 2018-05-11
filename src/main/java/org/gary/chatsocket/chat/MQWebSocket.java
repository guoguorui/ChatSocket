package org.gary.chatsocket.chat;

import org.gary.chatsocket.mvc.Model;
import org.gary.chatsocket.mvc.View;
import org.gary.chatsocket.util.CookieUtil;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//每一个用户监听的queue是chat-"name"
//其他用户通过ReadTask监听浏览器的输入，再往这个queue发送消息，同时回显给浏览器
//当连接断开时，结束所有监听，并回收mq的连接资源
public class MQWebSocket {

    //local的socket到remote的friend
    static HashMap<Socket,String> socketToFriend=new HashMap<>();

    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());

    //只是渲染视图
    public static void chooseFriend(String path,String rawCookie,View view) throws Exception{
        String name= CookieUtil.getName(rawCookie);
        String friend = path.split("=")[1];
        view.setModel(new Model(name,friend));
        view.directView("wschat");
    }

    //使用MQ监听自己的queue，响应方法是MyMessageListener.onMessage，这里使用了类回调技术
    //使用ReadTask监听浏览器，响应方法是writeToClient
    public static void connectAndListen(String path,String rawCookie,String key,Socket client) throws Exception{
        String name= CookieUtil.getName(rawCookie);
        String friend = path.split("=")[1];
        socketToFriend.put(client,friend);
        PrintWriter pw=new PrintWriter(client.getOutputStream());
        WebSocket.connect(key,pw);
        ResourceReclaim resourceReclaim =ActiveMQ.ConsumerFromQueue("chat-"+name,new MyMessageListener(client));
        executor.execute(new ReadTask(client, name,resourceReclaim));
    }

    //浏览器发送来的数据分为两份
    //一份echo
    //一份写入MQ
    static void writeToClient(Socket client, String message) throws Exception{
        String friend=socketToFriend.get(client);
        ActiveMQ.produceToQueue("chat-"+friend,message);
        executor.execute(new WriteTask(client,message));
    }
}


