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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;


//html不能在脚本间用注释//

public class Response {
	Socket client;
	PrintWriter pw;
	BufferedReader br;
	boolean cookie=false;
	boolean enableSession=false;
	String name="BALALA";
	String friend="CHALABLA";
	static ConnectPool cp=new ConnectPool();
	static HashMap<String,Socket> nameToSocket=new HashMap<String,Socket>();
	static HashMap<String,String> nameToFriend=new HashMap<String,String>();
	//String filename="E:\\EclipseProject\\ChatSocket\\resource\\";
	String filename=System.getProperty("user.dir")+"\\resource\\";
	static String destination;
	static String date=getDate();
	HashMap<String,String> responseHeader=new HashMap<String,String>();
	public Response(PrintWriter pw,Socket client) throws IOException {
		this.pw=pw;
		this.client=client;
		responseHeader.put("Server", "ChatSocket");
		responseHeader.put("Content-Type", "text/html");
		responseHeader.put("Connection", "keep-alive");
		responseHeader.put("Date", date);
	}
	
	public void doSent(Request request) throws Exception {
		String path=request.getPath(client);
		HashMap<String,String> requestHeader=request.getRequestHeader();
		
		//僵尸socket处理
		if(path.equals("")) {
			pw.write(assembleHeader());
			pw.write("Sorry, please refresh.\n");
			pw.close();
			return;
		}
			
		//cookie处理
		if(requestHeader.containsKey("Cookie")) {
			name=requestHeader.get("Cookie").split("=")[1].substring(0, 3);
         	cookie=true;
		}
		
		//websocket处理
		if(requestHeader.containsKey("Sec-WebSocket-Key")) {
			WebSocket ws=new WebSocket(requestHeader.get("Sec-WebSocket-Key"),pw,client);
			nameToSocket.put(name,client);
			ws.connect();
			new ReadThread(client).start();
			//sb才不return
			//pw不能在这里close，这个socket要手动升级为websocket
			return;
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
		
		//压缩测试
		//不传输length也没毛病
		if(path.contains("testgzip")){
			OutputStream os=client.getOutputStream();
			byte[]  hb=GZip.compressString("hello");
			responseHeader.put("Content-Encoding", "gzip");
			os.write(assembleHeader().getBytes());
			os.write(hb);
			os.flush();
			os.close();
			return;
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
		if(destination!=null && !cookie && enableSession) {
			responseHeader.put("Content-Encoding", "gzip");
			responseHeader.put("Set-Cookie","JSESSIONID="+name+"023EE23711E1FEB5F792CFD9752F9F79;path=/;HttpOnly");
			compress(assembleHeader(),temp);
		}
		else {
			responseHeader.put("Content-Encoding", "gzip");
			compress(assembleHeader(),temp);
		}
	}
	
	public void directStatic(String path) throws IOException {
		path=path.replaceAll("/", "\\\\");
		String staticName=filename+path;
		responseHeader.put("Cache-Control", "max-age=5184000");
		responseHeader.put("Last-Modified", "Thu, 28 Sep 2017 07:43:37 GMT");
		responseHeader.put("Expires", "Fri, 08 Dec 2017 03:35:31 GMT");
		//图片需要用字节数组传输
		if(!path.contains(".jpg") && !path.contains(".ico")) {
		br=new BufferedReader(new FileReader(staticName));
		StringBuilder sb=new StringBuilder();
		String temp;
		while((temp=br.readLine())!=null) {
			sb.append(temp);
		}
		temp=sb.toString();
		responseHeader.put("Content-Encoding", "gzip");
		//难道以前是这个鬼玩意输错了？
		responseHeader.put("Content-Type", "text/css");
		compress(assembleHeader(),temp);
		}
		
		else {
			//似乎在chrome只能cache from memory不能是disk
			OutputStream os=client.getOutputStream();
			responseHeader.put("Content-Type", "image/*");
			os.write(assembleHeader().getBytes());
			os.flush();
			FileInputStream fis=new FileInputStream(new File(staticName));
			int length;
			byte[] img=new byte[1024];
			while((length=fis.read(img, 0, 1024))>0) {
				os.write(img,0,length);
				os.flush();
			}
			fis.close();
			pw.close();
		}	
	}
	
	public void doAjax() {
		System.out.println("into doAjax()");
		pw.write("Hello,I come from ajax\n");
		pw.close();
	}
	
	public void doJdbc() throws SQLException {
		System.out.println("into doJdbc()");
		OperData od=new OperData(cp);
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
		OperData od=new OperData(cp);
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
			pw.write(assembleHeader()+"sorry invalid account.\n");
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
	
	public void compress(String header,String content){
		try {
		OutputStream os=client.getOutputStream();
		byte[] csb=GZip.compressString(content);
		os.write(header.getBytes("UTF-8"));
		os.write(csb);
		os.flush();
		os.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getDate() {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat greenwichDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        return greenwichDate.format(cal.getTime());        
	}
	
	public String assembleHeader() {
		StringBuilder sb=new StringBuilder();
		sb.append("HTTP/1.1 200 OK\r\n");
		Set<String> keys=responseHeader.keySet();
		for(String key:keys) {
			sb.append(key+": "+responseHeader.get(key)+"\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}
	
}
