package com.gary.chatsocket;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public class GZip {

    public static byte[] compress(byte[] data) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
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