package org.gary.chatsocket.chat;

import jdk.internal.util.xml.impl.Input;
import org.gary.chatsocket.mvc.Model;
import org.gary.chatsocket.mvc.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocket {

    static HashMap<String, Socket> nameToSocket = new HashMap<>();
    private static HashMap<String, String> nameToFriend = new HashMap<>();
    private static HashMap<String, String> nameToMessage =new HashMap<>();
    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private static void connect(String key,PrintWriter pw){
        try {
            key += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void writeToClient(String name, String message) {
        String friend = nameToFriend.get(name);
        Socket friendClient = nameToSocket.get(friend);
        Socket localClient = nameToSocket.get(name);
        if (friendClient==null || friendClient.isClosed()) {
            executor.execute(new WriteTask(localClient,message+"\r\n该好友不在线...其上线后会收到信息"));
            String unRead=nameToMessage.get(friend);
            if(unRead==null)
                unRead=message;
            else
                unRead+="\r\n"+message;
            nameToMessage.put(friend,unRead);
        } else {
            executor.execute(new WriteTask(localClient,message));
            executor.execute(new WriteTask(friendClient,message));
        }
    }

    public static void chooseFriend(String path, String name, View view) throws IOException{
        String friend = path.split("=")[1];
        nameToFriend.put(name, friend);
        view.setModel(new Model(name,friend));
        view.directView("wschat");
    }

    public static void connectAndListen(String key, String name,Socket client) throws IOException{
        PrintWriter pw=new PrintWriter(client.getOutputStream());
        WebSocket.connect(key,pw);
        nameToSocket.put(name, client);
        String unRead=nameToMessage.get(name);
        if(unRead!=null){
            executor.execute(new WriteTask(client,unRead));
            nameToMessage.remove(name);
        }
        executor.execute(new ReadTask(client));
    }

}

//要extends不要runnable，贼傻逼，搞得一个read()阻塞了
class WriteTask extends Thread {
    //client是要输出的对象
    private Socket client;
    private ByteBuffer byteBuf;

    WriteTask(Socket client, String message) {
        byteBuf = ByteBuffer.wrap(message.getBytes());
        this.client = client;
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
            out.write(byteBuf.array(), 0, byteBuf.limit());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ReadTask extends Thread {

    private Socket client;

    ReadTask(Socket client) {
        this.client=client;
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
            InputStream in = client.getInputStream();
            //该读线程会循环读取
            //这里是阻塞的要害,单纯的while不会阻塞，是read()
            while ((in.read(first, 0, 1)) > 0) {
                //清除高位
                int b = first[0] & 0xFF;
                //1为字符数据，8为关闭socket
                //第1字节的后4位即是opCode
                byte opCode = (byte) (first[0] & 0x0F);
                if (opCode == 8) {
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
                //取出发送端client的name
                String name = "";
                for (String getKey : WebSocket.nameToSocket.keySet()) {
                    if (WebSocket.nameToSocket.get(getKey).equals(client)) {
                        name = getKey;
                    }
                }
                String address = name + ": ";
                byteBuf.put(address.getBytes("UTF-8"));
                while (payloadLength > 0) {
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
                String message = new String(byteBuf.array());
                //将从浏览器input读取到的信息发送给浏览器textArea
                WebSocket.writeToClient(name, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
