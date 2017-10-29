package com.gary.chatsocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class PreProcess {
	String path;
	View view;
	HashMap<String,String> requestHeader;
	PrintWriter pw;
	Socket client;
	
	public PreProcess(Request request,Socket client,View view,PrintWriter pw) {
		try {
			this.path=request.getPath(client);
			this.view=view;
			this.pw=pw;
			this.client=client;
			requestHeader=request.requestHeader;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String process() {
		if(path.equals("/")) {
			path="index";
		}	
		
		else if(!path.equals("")){
			path=path.substring(1);
		}
		
		//图标处理
		if(path.contains("favicon.ico")) {
			path="static/favicon.ico";
		}
		
		//cookie处理
		if(requestHeader.containsKey("Cookie")) {
			view.setName(requestHeader.get("Cookie").split("=")[1].substring(0, 3));
         	view.setCookie(true);
		}
		
		
		//websocket处理
		if(requestHeader.containsKey("Sec-WebSocket-Key")) {
			WebSocket ws=null;
			try {
				ws = new WebSocket(requestHeader.get("Sec-WebSocket-Key"),pw,client);
			} catch (IOException e) {
				e.printStackTrace();
			}
			WebSocket.nameToSocket.put(view.name,client);
			ws.connect();
			new ReadThread(client).start();
			//sb才不return
			//pw不能在这里close，这个socket要手动升级为websocket
		}
		
		return path;
	}
}
