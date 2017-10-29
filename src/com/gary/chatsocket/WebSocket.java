package com.gary.chatsocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;

public class WebSocket {
	
	String key;
	PrintWriter pw;
	Socket client;
	static HashMap<String,Socket> nameToSocket=new HashMap<String,Socket>();
	static HashMap<String,String> nameToFriend=new HashMap<String,String>();
	
	public WebSocket(String key,PrintWriter pw,Socket client) throws IOException {
		this.key=key;
		this.pw=pw;
		this.client=client;
	}
	
	public void connect() {
		try {
				key+= "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
				MessageDigest md = MessageDigest.getInstance("SHA-1");  
				md.update(key.getBytes("utf-8"), 0, key.length());
				byte[] sha1Hash = md.digest();  
			    sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();  
				key = encoder.encode(sha1Hash);  
				pw.println("HTTP/1.1 101 Switching Protocols");
				pw.println("Upgrade: websocket");
				pw.println("Connection: Upgrade");
				pw.println("Sec-WebSocket-Accept: " + key);
				pw.println();
				pw.flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	

	//控制台输出
	public static void printRes(byte[] array) {
		Charset charset = Charset.forName("UTF-8");  
		ByteArrayInputStream  byteIn = new ByteArrayInputStream(array);
		InputStreamReader reader = new InputStreamReader(byteIn, charset.newDecoder());
		int b = 0;
		String res = "";
		try {
			while((b = reader.read()) > 0){
				res += (char)b;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(res);
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

//要extends不要runnable，贼傻逼，搞得一个read()阻塞了
class WriteThread extends Thread{
	Socket client;
	ByteBuffer byteBuf;
	
	public WriteThread(Socket client,String message) {
		byteBuf=ByteBuffer.wrap(message.getBytes());
		this.client=client;
	}
	
	@Override
	public void run() {
		try {
			//同读的frame一样，每write一个都是一个字节，int的高位被忽略
		    OutputStream out = client.getOutputStream();
		    int first = 0x00;
			//是否是输出最后的WebSocket响应片段,01110001
	            
	                first = first + 0x80;
	                first = first + 0x1;
	            
	            out.write(first);
	        
	            //2^7,payload只能显示7位，此处的limit类似于length
	            if (byteBuf.limit() < 126) {
	                out.write(byteBuf.limit());
	            //2^16
	            } else if (byteBuf.limit() < 65536) {
	            //填充负载字节
	        	out.write(126);
	        	out.write(byteBuf.limit() >>> 8);
	        	out.write(byteBuf.limit() & 0xFF);
	            } else {
	            // Will never be more than 2^31-1，所以扩展的8个字节只需要用到后4个
	        	out.write(127);
	        	out.write(0);
	        	out.write(0);
	        	out.write(0);
	        	out.write(0);
	        	out.write(byteBuf.limit() >>> 24);
	        	out.write(byteBuf.limit() >>> 16);
	        	out.write(byteBuf.limit() >>> 8);
	        	out.write(byteBuf.limit() & 0xFF);	

	            }
	            //返回客户端不需要maskKey
	            // Write the content
	            out.write(byteBuf.array(), 0, byteBuf.limit());
	            out.flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

class ReadThread extends Thread{
	InputStream in;
	Socket client;
	public ReadThread(Socket client) {
		try {
		this.client=client;
		in=client.getInputStream();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		//解析frame结构
		/*
      0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-------+-+-------------+-------------------------------+
     |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
     |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
     |N|V|V|V|       |S|             |   (if payload len==126/127)   |
     | |1|2|3|       |K|             |                               |
     +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
     |     Extended payload length continued, if payload len == 127  |
     + - - - - - - - - - - - - - - - +-------------------------------+
     |                               |Masking-key, if MASK set to 1  |
     +-------------------------------+-------------------------------+
     | Masking-key (continued)       |          Payload Data         |
     +-------------------------------- - - - - - - - - - - - - - - - +
     :                     Payload Data continued ...                :
     + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     |                     Payload Data continued ...                |
     +---------------------------------------------------------------+
		 */
		byte[] first = new byte[1];
		try {
	       //这里是阻塞的要害,单纯的while不会阻塞，是read()
			int read;
	        while((read = in.read(first, 0, 1))>0){
	        	//int read = in.read(first, 0, 1);
	        	if(read<0)
	        		return;
	        	//清除高位
	    	    int b = first[0] & 0xFF;
	    	    //1为字符数据，8为关闭socket
	    	    //第1字节的后4位即是opCode
	            byte opCode = (byte) (first[0] & 0x0F);
	        
	            if(opCode == 8){
	        	client.getOutputStream().close();
	        	break;
	            }
	            b = in.read();
	            //第2字节的后7位是有效荷载长度，<=125即为真长度，126则计算extends的长度，需要读2个字节
	            int payloadLength = b & 0x7F;
	            if (payloadLength == 126) {
	                byte[] extended = new byte[2];
	                in.read(extended, 0, 2);
	                int shift = 0;
	                payloadLength = 0;
	                for (int i = extended.length - 1; i >= 0; i--) {
	                	//高位需要左移
	                	payloadLength = payloadLength + ((extended[i] & 0xFF) << shift);
	                    shift += 8;
	                }
	            //是126的进阶版，需要8个字节
	            } else if (payloadLength == 127) {
	                byte[] extended = new byte[8];
	                in.read(extended, 0, 8);
	                int shift = 0;
	                payloadLength = 0;
	                for (int i = extended.length - 1; i >= 0; i--) {
	                	payloadLength = payloadLength + ((extended[i] & 0xFF) << shift);
	                    shift += 8;
	                }
	            }
	        
	        //掩码，有4个字节
	        byte[] mask = new byte[4];
	        in.read(mask, 0, 4);
	        int readThisFragment = 1;
	        ByteBuffer byteBuf = ByteBuffer.allocate(payloadLength + 25);
	        String name="";
	        for(String getKey: WebSocket.nameToSocket.keySet()){  
	        	   if(WebSocket.nameToSocket.get(getKey).equals(client)){  
	        	             name = getKey;  
	               }
	        }
	        String address=name+ ": ";
	        byteBuf.put(address.getBytes("UTF-8"));
	        while(payloadLength > 0){
	        	//客户端发送的数据，根据掩码进行异或运行解码
	        	/*官方伪代码
	        	 var DECODED = "";
					for (var i = 0; i < ENCODED.length; i++) {
    					DECODED[i] = ENCODED[i] ^ MASK[i % 4];
					}
	        	 */
	        	 int masked = in.read();
	             masked = masked ^ (mask[(int) ((readThisFragment - 1) % 4)] & 0xFF);
	             byteBuf.put((byte) masked);
	             payloadLength--;
	             readThisFragment++;
	        }
	        byteBuf.flip();
	        //responseClient(byteBuf, true);
	        WebSocket.printRes(byteBuf.array());
	        String message=new String(byteBuf.array());
	        //Response.mass(message);
	        WebSocket.chatToOne(name,message);
	    }
	}
	catch(Exception e) {
		e.printStackTrace();
	}
}
}
