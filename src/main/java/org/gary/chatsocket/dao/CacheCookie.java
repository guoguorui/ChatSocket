package org.gary.chatsocket.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CacheCookie {

    private static JedisPool jedisPool=new JedisPool("127.0.0.1");

    public static void addCookie(String cookie){
        String[] strings=split(cookie);
        if(strings!=null){
            Jedis jedis=jedisPool.getResource();
            jedis.set(strings[0],strings[1]);
            //可以根据业务需求考虑是否让该string过期
            jedis.expire(strings[0],1800);
        }
    }

    public static boolean judgeCookie(String cookie){
        String[] strings=split(cookie);
        if(strings!=null){
            Jedis jedis=jedisPool.getResource();
            if(jedis.get(strings[0]).equals(strings[1]))
                return true;
        }
        return false;
    }

    private static String[] split(String cookie){
        String[] strings=new String[2];
        int pos=cookie.indexOf('=');
        if(pos==-1)
            return null;
        strings[0]=cookie.substring(0,pos);
        strings[1]=cookie.substring(pos+1,cookie.length());
        return strings;
    }
}
