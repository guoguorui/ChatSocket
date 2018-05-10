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
        }
    }

    public static boolean judgeCookie(String[] cookie){
        Jedis jedis=jedisPool.getResource();
        String redisUUID=jedis.get(cookie[0]);
        return redisUUID!=null && redisUUID.equals(cookie[1]);
    }

}
