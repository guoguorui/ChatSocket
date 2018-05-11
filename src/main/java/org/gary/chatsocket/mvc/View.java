package org.gary.chatsocket.mvc;

import org.gary.chatsocket.util.DateUtil;
import org.gary.chatsocket.util.GZipUtil;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

public class View {

    private OutputStream os;
    private HashMap<String, String> responseHeader = new HashMap<>();
    private String filename = System.getProperty("user.dir") + "\\resource\\";
    private Object model;
    private boolean putCookie;
    private String cookie;

    View(OutputStream os) {
        this.os = os;
        responseHeader.put("Server", "ChatSocket");
        responseHeader.put("Content-Type", "text/html");
        responseHeader.put("Connection", "keep-alive");
        responseHeader.put("Content-Encoding", "gzip");
    }

    public void directView(String path) throws Exception {
        if(putCookie){
            responseHeader.put("Set-Cookie",cookie);
            putCookie=false;
        }
        filename += path + ".html";
        String content=readFile(filename);
        //先支持简单String的映射
        if(model!=null){
            Class<?> clazz=model.getClass();
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields){
                field.setAccessible(true);
                String value=(String) field.get(model);
                if(value!=null)
                    content = content.replaceAll("\\{"+field.getName()+"\\}", value);
            }
        }
        output(content);
    }


    void directStatic(String path,String lastModified) throws Exception {
        path = path.replaceAll("/", "\\\\");
        String staticName = filename + path;
        if(lastModified!=null){
            if(!DateUtil.judgeExpire(staticName,lastModified)){
                System.out.println("the static file doesn't expire");
                os.write("HTTP/1.1 304 Not Modified\r\n".getBytes());
                return;
            }
        }
        responseHeader.put("Cache-Control", "max-age=5184000");
        responseHeader.put("Last-Modified", DateUtil.getLastModified(staticName));
        //图片需要用字节数组传输
        if (!path.contains(".jpg") && !path.contains(".ico")) {
            responseHeader.put("Content-Type", "text/css");
            String content=readFile(staticName);
            output(content);
        } else {
            ByteOutputStream bos=new ByteOutputStream();
            responseHeader.put("Content-Type", "image/*");
            responseHeader.remove("Content-Encoding");
            FileInputStream fis = new FileInputStream(new File(staticName));
            int length;
            byte[] img = new byte[1024];
            while ((length = fis.read(img, 0, 1024)) > 0)
                bos.write(img,0,length);
            fis.close();
            output(bos.getBytes());
        }
    }

    private String readFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String temp;
        while ((temp = br.readLine()) != null){
            sb.append(temp);
            sb.append("\r\n");
        }
        temp = sb.toString();
        return temp;
    }

    private String getHeader(){
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        Set<String> keys = responseHeader.keySet();
        for (String key : keys) {
            sb.append(key);
            sb.append(": ");
            sb.append(responseHeader.get(key));
            sb.append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    private void output(String content) throws IOException{
        String header= getHeader();
        byte[] csb = GZipUtil.compressString(content);
        os.write(header.getBytes("UTF-8"));
        os.write(csb);
    }

    private void output(byte[] content) throws IOException{
        String header= getHeader();
        os.write(header.getBytes("UTF-8"));
        os.write(content);
    }

    public void setPutCookie(boolean putCookie) {
        this.putCookie = putCookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setModel(Object model) {
        this.model = model;
    }
}
