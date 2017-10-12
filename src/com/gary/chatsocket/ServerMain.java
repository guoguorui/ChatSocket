package com.gary.chatsocket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;


//支持post的multipart
//devtools
//API化
//实现Controller注解支持
//实现orm
//处理不在线
//性能调优
public class ServerMain {

	public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException, SecurityException, CloneNotSupportedException {

		ServerSocket serverSocket =new ServerSocket(80);
		try {
		while(true) {
			Socket client=serverSocket.accept();
			exceService(client);
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			serverSocket.close();
		}
		
	}
		
	public static void exceService(final Socket client) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//System.out.println("\n"+ client.getInetAddress() + ":" + client.getPort());
					BufferedReader br=new BufferedReader(new InputStreamReader(client.getInputStream()));
			        PrintWriter pw=new PrintWriter(client.getOutputStream());
					Response sent=new Response(pw,client);
			        sent.doSent(new Request(br).getPath(client));
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		}).start();;
	}
}
