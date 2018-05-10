package org.gary.chatsocket.chat;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

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

