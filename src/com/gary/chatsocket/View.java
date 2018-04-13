package com.gary.chatsocket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class View {
    private PrintWriter pw;
    private BufferedReader br;
    private Socket client;
    private HashMap<String, String> responseHeader = new HashMap<String, String>();
    private static String date = getDate();
    private String filename = System.getProperty("user.dir") + "\\resource\\";
    String name = "BALALA";
    private String friend = "CHALABLA";


    private boolean enableSession = false;
    boolean cookie = false;

    View(Socket client, PrintWriter pw) {
        this.pw = pw;
        this.client = client;
        responseHeader.put("Server", "ChatSocket");
        responseHeader.put("Content-Type", "text/html");
        responseHeader.put("Connection", "keep-alive");
        responseHeader.put("Date", date);
    }

    private static String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat greenwichDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        return greenwichDate.format(cal.getTime());
    }

    //根据name设置friend并在页面进行替换，如果刚登陆，还要给出cookie
    void directView(String path) throws IOException {
        filename += path + ".html";
        br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String temp;
        //省略了数据库查询
        if (path.contains("chatwho") || path.contains("wschat")) {
            if (name.equals("GGR"))
                friend = "abc";
            else if (name.equals("abc"))
                friend = "GGR";
        }
        while ((temp = br.readLine()) != null) {
            temp = temp.replaceAll("\\{name\\}", name);
            temp = temp.replaceAll("\\{friend\\}", friend);
            sb.append(temp);
        }
        temp = sb.toString();
        if (!cookie && enableSession) {
            responseHeader.put("Content-Encoding", "gzip");
            responseHeader.put("Set-Cookie", "JSESSIONID=" + name + "023EE23711E1FEB5F792CFD9752F9F79;path=/;HttpOnly");
            compress(assembleHeader(), temp);
        } else {
            responseHeader.put("Content-Encoding", "gzip");
            compress(assembleHeader(), temp);
        }
    }


    void directStatic(String path) throws IOException {
        path = path.replaceAll("/", "\\\\");
        String staticName = filename + path;
        responseHeader.put("Cache-Control", "max-age=5184000");
        responseHeader.put("Last-Modified", "Thu, 28 Sep 2017 07:43:37 GMT");
        //图片需要用字节数组传输
        if (!path.contains(".jpg") && !path.contains(".ico")) {
            br = new BufferedReader(new FileReader(staticName));
            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            temp = sb.toString();
            responseHeader.put("Content-Encoding", "gzip");
            responseHeader.put("Content-Type", "text/css");
            compress(assembleHeader(), temp);
        } else {
            //似乎在chrome只能cache from memory不能是disk
            OutputStream os = client.getOutputStream();
            responseHeader.put("Content-Type", "image/*");
            os.write(assembleHeader().getBytes());
            os.flush();
            FileInputStream fis = new FileInputStream(new File(staticName));
            int length;
            byte[] img = new byte[1024];
            while ((length = fis.read(img, 0, 1024)) > 0) {
                os.write(img, 0, length);
                os.flush();
            }
            fis.close();
            pw.close();
        }
    }

    void gzipTest() {
        try {
            OutputStream os = client.getOutputStream();
            byte[] hb = GZip.compressString("hello");
            responseHeader.put("Content-Encoding", "gzip");
            os.write(assembleHeader().getBytes());
            os.write(hb);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setEnableSession(boolean enableSession) {
        this.enableSession = enableSession;
    }

    void setName(String name) {
        this.name = name;
    }

    void setFriend(String friend) {
        this.friend = friend;
    }

    void setCookie(boolean cookie) {
        this.cookie = cookie;
    }

    public boolean getCookie() {
        return this.cookie;
    }

    private String assembleHeader() {
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

    private void compress(String header, String content) {
        try {
            OutputStream os = client.getOutputStream();
            byte[] csb = GZip.compressString(content);
            os.write(header.getBytes("UTF-8"));
            os.write(csb);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
