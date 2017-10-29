package com.gary.chatsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;



public class Request {
	BufferedReader br;
	HashMap<String,String> requestHeader=new HashMap<String,String>();
	public HashMap<String, String> getRequestHeader() {
		return requestHeader;
	}
	public Request(BufferedReader br) throws IOException {
		this.br=br;
	}
	//不同浏览器，提交的header不完全相同
	public String getPath(Socket client) throws IOException {
		String line = br.readLine();   
		String path="";
		int ch=0;
		StringBuffer sb = new StringBuffer();	
		//可能是由缓存机制引起的空socket
		if(line==null || line.contains("router") || line.contains("webnoauth")) {
			//System.out.println("哥，这里有人是null");
			return "";
		}	
				
		System.out.println("\n"+ client.getInetAddress() + ":" + client.getPort());
		
		if(line.contains("GET")) {  
            	path=line.split(" ")[1];         	
	            while((line=br.readLine())!=null) {
	            	if(!line.equals("")) {
	            		String[] header=line.split(": ");
	            		requestHeader.put(header[0], header[1]);
	            		//System.out.println(line);      		
	            	}            	
	            	else {
	            		break;    		
	            	}
	            }
			//System.out.println("out of while");
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
		 while((line=br.readLine())!=null) {
         	if(!line.equals("")) {
         		if(line.contains("Content-Length")) {
            		temp=line.substring(16);
            	}
         	}            	
         	else {
         		break;    		
         	}
         }
        int num=Integer.parseInt(temp);
        ch=br.read();
        ch=br.read();
        num=num-2;
	    while(num!=0){
		   ch=br.read();
		   sb.append((char)ch);
		   num--;
	    }
	    //System.out.println("out of post");
	    System.out.println(path+"?na"+sb.toString());
        return path+"?"+sb.toString();
	}
	
}
