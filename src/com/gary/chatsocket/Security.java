package com.gary.chatsocket;

import java.io.IOException;

public class Security {
	boolean cookie=false;
	boolean enableSession=false;
	//String name="BALALA";
	static ConnectPool cp=Ajax.cp;

	public static boolean doProcessLogin(String path,View view) throws IOException {
		String param=path.split("\\?")[1];
		String name=param.split("&")[0].split("=")[1];
		String password=param.split("&")[1].split("=")[1];
		OperData od=new OperData(cp);
		if(od.authenticate(name, password)) {
			view.setName(name);
			view.setEnableSession(true);
			view.directView("chatwho");
			return true;
		}
		return false;
	}
}
