package org.elasticsearch.index.analysis;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * User: Medcl
 * Date: 12-11-2
 * Time: 上午9:12
 */
public class RedisHanlder {
    private static ESLogger logger = Loggers.getLogger("sting2int");
    private JedisPool jPool;
    private static HashMap<String, RedisHanlder> instance = new HashMap<String, RedisHanlder>();
    private boolean local_mem_cache = true;

    private static HashMap<String, HashMap<String, Long>> local_cache = new HashMap<String, HashMap<String, Long>>();

    private RedisHanlder(String redis_server, int redis_port, boolean local_mem_cache) {
        jPool = new JedisPool(new JedisPoolConfig(), redis_server, redis_port);
        this.local_mem_cache = local_mem_cache;
    }


    static RedisHanlder getInstance(String redis_server, int redis_port, boolean local_mem_cache) {


        String key = redis_server + redis_port;
        if (instance.containsKey(key)) {
            return instance.get(key);
        }
        RedisHanlder hanlder = new RedisHanlder(redis_server, redis_port, local_mem_cache);
        instance.put(key, hanlder);
        return hanlder;
    }

    private static HashMap<String, HashMap<String, Long>> getLocalCache() {
        return local_cache;
    }

    private long get_from_cache(String redis_key, String item) {
        if (!getLocalCache().containsKey(redis_key)) {
            return -1;
        }
        if (!getLocalCache().get(redis_key).containsKey(item)) {
            return -1;
        }

        return getLocalCache().get(redis_key).get(item);
    }

    private void set_local_cache(String redis_key, String item, long value) {
        if (value == -1) {
            return;
        }
        if (!getLocalCache().containsKey(redis_key)) {
            getLocalCache().put(redis_key, new HashMap<String, Long>());
        }
        if (!getLocalCache().get(redis_key).containsKey(item)) {
            getLocalCache().get(redis_key).put(item, value);
        }
    }

    public long convert(String redis_key, String item) {

        if (item == null) return -1;

        String redis_key_count = redis_key + "_count";
        String redis_key_prefix = redis_key + "_key_";
        String redis_rev_key_prefix = redis_key + "_rkey_";

        item = item.trim();

        long ret = -1;

        Jedis jedis = jPool.getResource();
        try {
            try {
                item = URLEncoder.encode(item, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                logger.error(e.toString());
            }

            if (local_mem_cache) {
                long from_cache = get_from_cache(redis_key, item);
//        System.out.println("fromcache"+from_cache);
                if (from_cache > -1) {
                    logger.info("hit cache:" + redis_key + "/" + item + ":" + from_cache);
                    return from_cache;
                }
            }

            String item_key = redis_key_prefix + item;
            if (!jedis.exists(item_key)) {
                try {
                    long count = jedis.incr(redis_key_count);
//                System.out.println(redis_key_count);
                    jedis.set(item_key, String.valueOf(count));
                    jedis.set(redis_rev_key_prefix + String.valueOf(count), item);
//                System.out.println(item_key);
                    ret = count;
                } catch (Exception e) {
                    logger.equals(e);
                }
            } else {
                ret = Long.valueOf(jedis.get(item_key));
            }
            set_local_cache(redis_key, item, ret);
        } finally {
            jPool.returnResource(jedis);
        }

//        System.out.println("set local cache");
        return ret;
    }
}
