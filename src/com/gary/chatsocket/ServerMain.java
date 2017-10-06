package com.gary.chatsocket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

//实现通信框架
//支持post的multipart
//devtools
//API化
//实现Controller注解支持
//实现orm
//用集合处理post数据
//密码加密
//处理/favicon.ico
//@SuppressWarnings("resource")  
//搞定乱七八糟的空指针
public class ServerMain {

	public static void main(String[] args) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException, SecurityException, CloneNotSupportedException {

		ServerSocket serverSocket =new ServerSocket(80);
		try {
		while(true) {
			Socket client=serverSocket.accept();
			System.out.println("\n"
					+ client.getInetAddress() + ":" + client.getPort());
            BufferedReader br=new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter pw=new PrintWriter(client.getOutputStream());
            //偶尔抽风在这里发生空指针异常
	        String path=parseRequest(br);
			exceService(br,pw,path,client);
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			serverSocket.close();
		}
		
	}
		
	public static void exceService(final BufferedReader br,final PrintWriter pw,final String path,final Socket client) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {          
					Response sent=new Response(pw,client);
			        sent.doSent(path);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		}).start();;
	}
	
	public static String parseRequest(BufferedReader br) throws IOException {
		Request r=new Request(br);
		String path=r.getPath();
		return path;
		
	}
}
