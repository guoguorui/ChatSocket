package com.gary.chatsocket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
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
	static String statusLine="HTTP/1.1 200 OK\r\n";
	static String responseHeader="Server:ChatSocket\r\n"
    		//+ "Content-Encoding: gzip\r\n"
    		+ "Content-Type:text/html\r\n"
    		+ "Connection:keep-alive\r\n";
	static String head=statusLine+responseHeader;
	public Response(PrintWriter pw,Socket client) throws IOException {
		this.pw=pw;
		this.client=client;
	}
	
	public void doSent(String path) throws IOException, SQLException {
		
		//僵尸socket处理
		if(path.equals("")) {
			pw.write(head+"\r\nSorry, please refresh.\n");
			pw.close();
			return;
		}
		
		//websocket处理
		if(path.contains("$")) {
			WebSocket ws=new WebSocket(path.split("\\$")[1],pw,client);
			name=path.substring(2, 5);
			nameToSocket.put(name,client);
			ws.connect();
			new ReadThread(client).start();
			//sb才不return
			//pw不能在这里close，这个socket要手动升级为websocket
			return;
		}
		
		//cookie处理
		if(path.contains("*")) {
			//因为是正则，所以需要转义
			name=path.split("\\*")[1];
			path=path.split("\\*")[0];
			cookie=true;
		}		
		
		//路径替换处理
		if(path.equals("/")) {
			path="index";
		}	
		
		else {
			path=path.substring(1);
		}
		
		//图标处理
		if(path.contains("favicon.ico")) {
			path="static/favicon.ico";
		}
		
		//静态资源处理
		if(path.contains("static")) {
			directStatic(path);
			return;
		}
		
		//chat处理
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
		
		//登录处理
		else if(path.contains("processLogin")) {
			doProcessLogin(path);
			System.out.println("");
			return;
		}
		
		//响应ajax
		else if(path.contains("ajax")) {
			doAjax();
			System.out.println("");
			return;
		}
		
		else if(path.contains("jdbc")){
			doJdbc();
			System.out.println("");
			return;
		}
		
		else {
			directView(path);
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
		//temp=GzipContent.compress(temp);
		if(destination!=null && !cookie && enableSession) {
			pw.write(head+ "Set-Cookie: JSESSIONID="+name+"023EE23711E1FEB5F792CFD9752F9F79;path=/;HttpOnly\r\n\r\n"
		    		     +temp+"\n");
		}
		else {
			pw.write(head+"\r\n"+temp+"\n");;
		}
		br.close();
		pw.close();
	}
	
	public void directStatic(String path) throws IOException {
		path=path.replaceAll("/", "\\\\");
		String staticName=filename+path;
		String cache=statusLine
				   +"Server:ChatSocket\r\n"
				   + "Age:520\r\n"
				   + "Cache-Control:max-age=5184000\r\n"
				   + "Last-Modified:Thu, 28 Sep 2017 07:43:37 GMT\r\n"
				   + "Date: Mon, 09 Oct 2017 03:35:31 GMT\r\n"
				   + "Expires: Fri, 08 Dec 2017 03:35:31 GMT\r\n";
		//图片需要用字节数组传输
		if(!path.contains(".jpg") && !path.contains(".ico")) {
		br=new BufferedReader(new FileReader(staticName));
		StringBuilder sb=new StringBuilder();
		String temp;
		while((temp=br.readLine())!=null) {
			sb.append(temp);
		}
		temp=sb.toString();
		//cache no for firefox but work with chrome
		pw.write(cache
				+ "Content-Type=text/css\r\n"
				+ "\r\n");
		//temp=GzipContent.compress(temp);
		pw.write(temp);
		br.close();
		}
		else {
			//似乎在chrome只能cache from memory不能是disk
			OutputStream os=client.getOutputStream();
			String sta=cache
					+ "Content-Type=image/*\r\n"
					+ "\r\n";
			os.write(sta.getBytes());
			os.flush();
			FileInputStream fis=new FileInputStream(new File(staticName));
			int length;
			byte[] img=new byte[1024];
			while((length=fis.read(img, 0, 1024))>0) {
				os.write(img,0,length);
				os.flush();
			}
			fis.close();
		}	
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
			pw.write(head+"\r\nsorry invalid account.\n");
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
