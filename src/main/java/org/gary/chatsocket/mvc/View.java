package org.gary.chatsocket.mvc;

import org.gary.chatsocket.util.GZipUtil;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.*;
import java.util.HashMap;
import java.util.Set;

public class View {
    private OutputStream os;
    private HashMap<String, String> responseHeader = new HashMap<>();
    private String filename = System.getProperty("user.dir") + "\\resource\\";
    private String name = "BALALA";
    private String friend = "CHALABLA";
    private boolean enableSession = false;
    private boolean cookie = false;
    private String token;

    View(OutputStream os) {
        this.os = os;
        responseHeader.put("Server", "ChatSocket");
        responseHeader.put("Content-Type", "text/html");
        responseHeader.put("Connection", "keep-alive");
        responseHeader.put("Content-Encoding", "gzip");
    }

    //根据name设置friend并在页面进行替换，如果刚登陆，还要给出cookie
    public void directView(String path) throws IOException {
        if (!cookie && enableSession){
            //responseHeader.put("Set-Cookie", "JSESSIONID=" + name + "023EE23711E1FEB5F792CFD9752F9F79;path=/;HttpOnly");
            responseHeader.put("Set-Cookie",token);
            cookie=true;
        }
        filename += path + ".html";
        String content=readFile(filename);
        //省略了数据库查询
        if (path.contains("chatwho") || path.contains("wschat")) {
            if (name.equals("GGR"))
                friend = "abc";
            else if (name.equals("abc"))
                friend = "GGR";
        }
        content = content.replaceAll("\\{name\\}", name);
        content = content.replaceAll("\\{friend\\}", friend);
        output(content);
    }


    public void directStatic(String path) throws IOException {
        path = path.replaceAll("/", "\\\\");
        String staticName = filename + path;
        responseHeader.put("Cache-Control", "max-age=5184000");
        responseHeader.put("Last-Modified", "Feb, 28 Sep 2018 07:43:37 GMT");
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
        while ((temp = br.readLine()) != null)
            sb.append(temp);
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

    private void output(String content){
        String header= getHeader();
        try {
            byte[] csb = GZipUtil.compressString(content);
            os.write(header.getBytes("UTF-8"));
            os.write(csb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void output(byte[] content){
        String header= getHeader();
        try {
            os.write(header.getBytes("UTF-8"));
            os.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEnableSession(boolean enableSession) {
        this.enableSession = enableSession;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public void setCookie(boolean cookie) {
        this.cookie = cookie;
    }

    public boolean getCookie() {
        return this.cookie;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
