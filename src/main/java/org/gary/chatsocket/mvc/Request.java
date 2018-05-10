package org.gary.chatsocket.mvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;


class Request {

    private BufferedReader br;
    private HashMap<String, String> requestHeader = new HashMap<String, String>();

    Request(BufferedReader br) throws IOException {
        this.br = br;
    }

    String parse() throws  IOException{
        String path=getPath();
        if (path.equals("/")) {
            path = "index";
        } else if (!path.equals("")) {
            path = path.substring(1);
        }

        //图标处理
        if (path.contains("favicon.ico")) {
            path = "static/favicon.ico";
        }

        return path;
    }

    //不同浏览器，提交的header不完全相同
    private String getPath() throws IOException {
        String line = br.readLine();
        String path = "";
        int ch = 0;
        StringBuilder sb = new StringBuilder();
        //可能是由缓存机制引起的空socket
        if (line == null || line.contains("router") || line.contains("webnoauth")) {
            return "";
        }

        if (line.contains("GET")) {
            path = line.split(" ")[1];
            while ((line = br.readLine()) != null) {
                if (!line.equals("")) {
                    String[] header = line.split(": ");
                    requestHeader.put(header[0], header[1]);
                } else {
                    break;
                }
            }
            //response之前无法关闭输入流
            return path;
        }
        //这里是post的处理步骤
        sb.setLength(0);
        path = line.split(" ")[1];
        System.out.println(path);
        String temp = "";
        //在head和data间有一个空行，需要根据长度逐个字符读取
        while ((line = br.readLine()) != null) {
            if (!line.equals("")) {
                if (line.contains("Content-Length")) {
                    temp = line.substring(16);
                }
            } else {
                break;
            }
        }
        int num = Integer.parseInt(temp);
        ch = br.read();
        ch = br.read();
        num = num - 2;
        while (num != 0) {
            ch = br.read();
            sb.append((char) ch);
            num--;
        }
        System.out.println(path + "?na" + sb.toString());
        return path + "?" + sb.toString();
    }

    HashMap<String, String> getRequestHeader() {
        return requestHeader;
    }

}
