package com.gary.chatsocket;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

// 将一个字符串按照zip方式压缩和解压缩   
public class GZip {

    public static byte[] compress(byte[] data) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 压缩
        GZIPOutputStream gos = new GZIPOutputStream(baos);

        gos.write(data, 0, data.length);

        gos.finish();

        byte[] output = baos.toByteArray();

        baos.flush();
        baos.close();

        return output;
    }

    static byte[] compressString(String s) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 压缩
        GZIPOutputStream gos = new GZIPOutputStream(baos);

        byte[] data = s.getBytes("UTF-8");

        gos.write(data, 0, data.length);

        gos.finish();

        byte[] output = baos.toByteArray();

        baos.flush();
        baos.close();

        return output;
    }


}  