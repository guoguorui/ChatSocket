package com.gary.chatsocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

//html不能在脚本间用注释//
//重构真麻烦，还是一开始就要注意解耦好

public class Controller {
	
	Socket client;
	PrintWriter pw;
	View view;
	
	public Controller(PrintWriter pw,Socket client) throws IOException {
		this.pw=pw;
		this.client=client;
		view=new View(client,pw);
	}
	
	public void doSent(Request request) throws Exception {
		String path=new PreProcess(request,client,view,pw).process();
		
		//僵尸socket处理
		if(path.equals("")) {
			pw.close();
			return;
		}
		
		//静态资源处理
		if(path.contains("static")) {
			view.directStatic(path);
			return;
		}
		
		//chat处理
		else if(path.contains("chatwho")) {
			view.doSafe(path);
			System.out.println("");
			return;
		}
			
		// ‘=’并非特殊符号
		else if(path.contains("wschat")) {
			String friend=path.split("=")[1];
			WebSocket.nameToFriend.put(view.name, friend);
			view.setFriend(friend);
			view.directView("wschat");
			System.out.println("");
			return;
		}
		
		//登录处理
		else if(path.contains("processLogin")) {
			if(!Security.doProcessLogin(path, view)) {
				pw.write("sorry invalid account.\n");
				pw.close();
			}
			System.out.println("");
			return;
		}
		
		//响应ajax
		else if(path.contains("ajax")) {
			Ajax ajax=new Ajax(pw);
			ajax.doAjax();
			System.out.println("");
			return;
		}
		
		else if(path.contains("jdbc")){
			Ajax ajax=new Ajax(pw);
			ajax.doJdbc();
			System.out.println("");
			return;
		}
		
		//压缩测试
		//不传输length也没毛病
		else if(path.contains("testgzip")){
			view.gzipTest();
			return;
		}
		
		else {
			view.directView(path);
		}
			
	}
}
