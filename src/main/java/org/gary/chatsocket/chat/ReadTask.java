package org.gary.chatsocket.chat;

import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

class ReadTask extends Thread {

    private Socket client;
    private ResourceReclaim resourceReclaim;
    private String name;

    ReadTask(Socket client,String name) {
        this.client=client;
        this.name=name;
    }

    ReadTask(Socket client,String name,ResourceReclaim resourceReclaim){
        this(client,name);
        this.resourceReclaim = resourceReclaim;
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
                WebSocket.writeToClient(name, message);   //3
                //MQWebSocket.writeToClient(client,message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(resourceReclaim !=null){
                    resourceReclaim.close();
                    System.out.println("reclaim success");
                }
                WebSocket.nameToSocket.remove(name);    //4
                WebSocket.nameToFriend.remove(name);
                //MQWebSocket.socketToFriend.remove(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
