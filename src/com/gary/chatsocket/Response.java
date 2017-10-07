package com.gary.chatsocket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;


//html不能在脚本间用注释//

public class Response {
	Socket client;
	PrintWriter pw;
	BufferedReader br;
	boolean cookie=false;
	boolean enableSession=false;
	String name="BALALA";
	String friend="CHALABLA";
	static HashMap<String,Socket> nameToSocket=new HashMap<String,Socket>();
	static HashMap<String,String> nameToFriend=new HashMap<String,String>();
	//String filename="E:\\EclipseProject\\ChatSocket\\resource\\";
	String filename=System.getProperty("user.dir")+"\\resource\\";
	static String destination;
	static String head="HTTP/1.1 200 OK\r\n"
    		+ "Server:ChatSocket\r\n"
    		+ "Content-Type:text/html\r\n"
    		+ "Connection:keep-alive\r\n\r\n";
	public Response(PrintWriter pw,Socket client) throws IOException {
		this.pw=pw;
		this.client=client;
	}
	
	public void doSent(String path) throws IOException, SQLException {
		if(path.contains("$")) {
			WebSocket ws=new WebSocket(path.split("\\$")[1],pw,client);
			name=path.substring(2, 5);
			nameToSocket.put(name,client);
			System.out.println(name);
			System.out.println(nameToSocket.get(name));
			ws.connect();
			new ReadThread(client).start();
			//sb才不return
			//pw不能在这里close，这个socket要手动升级为websocket
			return;
		}
		if(path.contains("*")) {
			//因为是正则，所以需要转义
			name=path.split("\\*")[1];
			path=path.split("\\*")[0];
			cookie=true;
		}
		
		if(path.contains("favicon.ico")) {
			pw.write(" ");
			pw.close();
			return;
		}
		
			
		if(path.equals("/")) {
			path="index";
		}	
		
		else {
			path=path.substring(1);
		}
		
		if(path.contains("ajax")) {
			doAjax();
			System.out.println("");
			return;
		}
		
		else if(path.contains("chatwho")) {
			doSafe(path,"chatwho");
			System.out.println("");
			return;
		}
			
		// ‘=’并非特殊符号
		else if(path.contains("wschat")) {
			friend=path.split("=")[1];
			nameToFriend.put(name, friend);
			directView("wschat");
			System.out.println("");
			return;
		}
		
		else if(path.contains("processLogin")) {
			doProcessLogin(path);
			System.out.println("");
			return;
		}
		
		else if(path.contains("jdbc")){
			doJdbc();
			System.out.println("");
			return;
		}
				
		else if(path.contains("static")) {
			directStatic(path);
			return;
		}
		
		else {
			directView(path);
			//System.out.println("");
		}
			
	}
	
	public void directView(String path) throws IOException {
		filename+=path+".html";
		br=new BufferedReader(new FileReader(filename));
		StringBuilder sb=new StringBuilder();
		
		String temp;
		//省略了数据库查询
		if(path.contains("chatwho") || path.contains("wschat")) {
			if(name.equals("GGR"))
				friend="abc";
			else if(name.equals("abc"))
				friend="GGR";
		}
		while((temp=br.readLine())!=null) {
			temp=temp.replaceAll("\\{name\\}", name);
			temp=temp.replaceAll("\\{friend\\}", friend);
			sb.append(temp);
		}
		temp=sb.toString();
		if(destination!=null && !cookie && enableSession) {
			System.out.println("发送cookie前的name："+name);
		pw.write("HTTP/1.1 200 OK\r\n"
	    		+ "Server:ChatSocket\r\n"
	    		+ "Content-Type:text/html\r\n"
	    		+ "Connection:keep-alive\r\n"
	    		+ "Set-Cookie: JSESSIONID="+name+"023EE23711E1FEB5F792CFD9752F9F79;path=/;HttpOnly\r\n"
	    		+ "\r\n"+temp+"\n");
		}
		else {
			pw.write(head+temp+"\n");;
		}
		br.close();
		pw.close();
	}
	
	public void directStatic(String path) throws IOException {
		path=path.replaceAll("/", "\\\\");
		String staticName=filename+path;
		br=new BufferedReader(new FileReader(staticName));
		StringBuilder sb=new StringBuilder();
		String temp;
		while((temp=br.readLine())!=null) {
			sb.append(temp);
		}
		temp=sb.toString();
		pw.write(temp+"\n");
		br.close();
		pw.close();
	}
	
	public void doAjax() {
		System.out.println("into doAjax()");
		pw.write("Hello,I come from ajax\n");
		pw.close();
	}
	
	public void doJdbc() throws SQLException {
		System.out.println("into doJdbc()");
		OperData od=new OperData();
		String username=od.findName();
		pw.write(username+"\n");
		pw.close();
	}
	
	public synchronized void doSafe(String path,String dest) throws IOException {
		System.out.println("into doSafe");
		if(cookie) {
			directView(path);
		}
		else {
		directView("login");
		destination=dest;
		}
		return;
	}

	public synchronized void doProcessLogin(String path) throws IOException {
		String param=path.split("\\?")[1];
		String name=param.split("&")[0].split("=")[1];
		this.name=name;
		//登录路径异常
		String password=param.split("&")[1].split("=")[1];
		OperData od=new OperData();
		if(od.authenticate(name, password)) {
			enableSession=true;
			if(destination!=null) {
				directView(destination);
				destination=null;
			}
			else
				directView("index");
		}
		else {
			pw.write(head+"sorry invalid account.\n");
			pw.close();
		}
	}
	
	public static void mass(String message) {
		for(String name:nameToSocket.keySet()) {
			Socket client=nameToSocket.get(name);
			if(client.isClosed())
				nameToSocket.remove(name);
			else
				new WriteThread(client,message).start();
		}
	}
	
	public static void chatToOne(String name,String message) {
		String friend=nameToFriend.get(name);
		Socket friendClient=nameToSocket.get(friend);
		Socket localClient=nameToSocket.get(name);
		if(friendClient.isClosed())
			nameToSocket.remove(name);
		else {
			new WriteThread(localClient,message).start();
			new WriteThread(friendClient,message).start();
			
		}
			
	}
	
}
