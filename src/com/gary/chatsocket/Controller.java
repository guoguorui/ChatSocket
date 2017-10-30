package com.gary.chatsocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

//html不能在脚本间用注释//
//重构真麻烦，还是一开始就要注意解耦好

public class Controller {
	
	Socket client;
	PrintWriter pw;
	View view;
	Ajax ajax;
	
	public Controller(PrintWriter pw,Socket client) throws IOException {
		this.pw=pw;
		this.client=client;
		view=new View(client,pw);
		ajax=new Ajax(pw);
	}
	
	public void doSent(Request request) throws Exception {
		String path=new PreProcess(request,client,view,pw).process();
		HashMap<String,String> requestHeader=request.requestHeader;
		
		//僵尸socket处理
		if(path.equals("")) {
			pw.close();
			return;
		}
		
		//websocket处理
		if(requestHeader.containsKey("Sec-WebSocket-Key")) {
			WebSocket.processRead(requestHeader.get("Sec-WebSocket-Key"), pw, client, view);
			return;
			//pw不能在这里close，这个socket要手动升级为websocket
		}
		
		//静态资源处理
		if(path.contains("static")) {
			view.directStatic(path);
			return;
		}
		
		//chat处理
		else if(path.contains("chatwho")) {
			Security.doSafe(path, view);
			System.out.println("");
			return;
		}
			
		// ‘=’并非特殊符号
		else if(path.contains("wschat")) {
			WebSocket.processRelationship(path,view);
			System.out.println("");
			return;
		}
		
		//登录处理
		else if(path.contains("processLogin")) {
			Security.doProcessLogin(path,view,pw);
			System.out.println("");
			return;
		}
		
		//响应ajax
		else if(path.contains("ajax")) {
			ajax.doAjax();
			System.out.println("");
			return;
		}
		
		else if(path.contains("jdbc")){
			ajax.doJdbc();
			System.out.println("");
			return;
		}
		
		//压缩测试
		//不传输length也没毛病
		else if(path.contains("testgzip")){
			view.gzipTest();
			System.out.println("");
			return;
		}
		
		else {
			view.directView(path);
		}
			
	}
}
