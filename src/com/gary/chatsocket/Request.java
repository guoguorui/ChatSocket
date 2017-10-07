package com.gary.chatsocket;

import java.io.BufferedReader;
import java.io.IOException;



public class Request {
	BufferedReader br;
	public Request(BufferedReader br) throws IOException {
		this.br=br;
	}
	//不同浏览器，提交的header不完全相同
	public String getPath() throws IOException {
		boolean one=false;
		boolean two=false;
		boolean cookie=false;
		boolean websocket=false;
		String line = br.readLine();   
		String path="";
		String cookieName="";
		String keyLine="";
		int ch=0;
		StringBuffer sb = new StringBuffer();	
		//避免读到的是空行
		while(true) {
			if(line!=null)
				break;
			else
				line = br.readLine();   
		}
		if(line.contains("GET")) {  
            	path=line.split(" ")[1];         	
	            while((line=br.readLine())!=null) {
	            	if(line.equals(""))
	            		break;
	            	else {
	            		//System.out.println(line);
	            		if(line.contains("Cookie")) {
	             			cookieName=line.split("=")[1].substring(0, 3);
	     	            	cookie=true;
	     	            	//System.out.print("cookie yes from ");
	             		}
	            		if(line.contains("Sec-WebSocket-Key")) {
	            			keyLine=line.substring(line.indexOf("Key") + 4, line.length()).trim();
	            			websocket=true;
	            			//System.out.println("姑娘们，出来接客啦,这个老板是："+keyLine);
	            		}
	            		
	            	}
	            }
			//System.out.println("out of while");
			if(cookie)
				path+="*"+cookieName;
			if(websocket) 
				path+="$"+keyLine;
			System.out.println(path);
			//response之前无法关闭输入流
			return path;
		}
		
		//这里是post的处理步骤
		sb.setLength(0);
		path=line.split(" ")[1];
		System.out.println(path);
		String temp="";
		//在head和data间有一个空行，需要根据长度逐个字符读取
        while(line!=null) {
        	if(line.contains("Content-Length")) {
        		temp=line.substring(16);		
        	}
        	if(line.contains("Upgrade-Insecure-Requests"))
        		one=true;
        	if(line.contains("Accept-Language"))
        		two=true;
        	if(one && two)
        		break;
        	//System.out.println(line);
        	line=br.readLine();
        }
        int num=Integer.parseInt(temp);
        ch=br.read();
        ch=br.read();
        
        
	    while(num!=0){
		   ch=br.read();
		   sb.append((char)ch);
		   num--;
	    }
	    System.out.println("out of post");
	    //System.out.println(path+"?messages"+sb.toString());
	    System.out.println(path+"?"+sb.toString());
        return path+"?"+sb.toString();
	}
	
}
