package org.gary.chatsocket;


import org.gary.chatsocket.mvc.Controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerMain {

    public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException, SecurityException, CloneNotSupportedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        ServerSocket serverSocket = new ServerSocket(80);
        try {
            while (true) {
                final Socket client = serverSocket.accept();
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }

    }

}
