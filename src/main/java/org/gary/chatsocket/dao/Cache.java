package org.gary.chatsocket.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Cache {

    private static JedisPool jedisPool=new JedisPool("127.0.0.1");

    public static void addCookie(String[] cookie){
        if(cookie!=null){
            Jedis jedis=jedisPool.getResource();
            jedis.set(cookie[0],cookie[1]);
            //可以根据业务需求考虑是否让该string过期
            jedis.expire(cookie[0],1800);
            jedis.close();
        }
    }

    public static void removeCookie(String key){
        Jedis jedis=jedisPool.getResource();
        if(key!=null && jedis.get(key)!=null)
            jedis.del(key);
        jedis.close();
    }

    public static boolean judgeCookie(String[] cookie){
        Jedis jedis=jedisPool.getResource();
        String redisUUID=jedis.get(cookie[0]);
        jedis.close();
        return redisUUID!=null && redisUUID.equals(cookie[1]);
    }

    public static void addAuth(String name,String password){
        Jedis jedis=jedisPool.getResource();
        jedis.set("auth-"+name,password);
        jedis.close();
    }

    //0表示redis中没有对应的缓存，1表示成功匹配，-1表示账号信息错误
    public static int judgeAuth(String name,String uploadPassword){
        Jedis jedis=jedisPool.getResource();
        name="auth-"+name;
        String basePassword=jedis.get(name);
        jedis.close();
        if(basePassword==null)
            return 0;
        else if(basePassword.equals(uploadPassword))
            return 1;
        else
            return -1;
    }

}
