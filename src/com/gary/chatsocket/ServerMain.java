package com.gary.chatsocket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


//支持post的multipart
//devtools
//API化
//实现Controller注解支持
//实现orm
//处理不在线
//性能调优
//搞定缓存
//提供Restful API
//Request和Response的进一步封装
//会话用户cookie层的进一步封装
//加入消息队列
public class ServerMain {

    public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException, SecurityException, CloneNotSupportedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        ServerSocket serverSocket = new ServerSocket(80);
        try {
            while (true) {
                Socket client = serverSocket.accept();
                exceService(executor, client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }

    }

    private static void exceService(ThreadPoolExecutor executor, final Socket client) {
        Thread r = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Controller controller= new Controller(client);
                    controller.dispatch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        executor.execute(r);
    }
}
