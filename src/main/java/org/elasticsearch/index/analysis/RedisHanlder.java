package org.elasticsearch.index.analysis;

import com.google.code.simplelrucache.ConcurrentLruCache;
import com.google.code.simplelrucache.LruCache;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
    private boolean useLruCache = true;
    private int capicity = 100000;
    private long ttl = 5 * 60 * 1000; //5 minutes
    private int initialCapicity = 16;

    //trust me,int is bigger enough,or you are in a wrong way.
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> local_cache;// = new HashMap<String, HashMap<String, Integer>>();
    private static ConcurrentHashMap<String, LruCache<String, Integer>> local_lru_cache;// = new HashMap<String, LruCache<String, Integer>>();

    private RedisHanlder(String redis_server, int redis_port, boolean local_mem_cache, boolean useLruCache) {
        jPool = new JedisPool(new JedisPoolConfig(), redis_server, redis_port);
        this.local_mem_cache = local_mem_cache;
        this.useLruCache = useLruCache;
//TODO
//        this.capicity = capicity;
//        this.ttl = ttl;
//        this.initialCapicity = initialCapicity;
    }


    static RedisHanlder getInstance(String redis_server, int redis_port, boolean local_mem_cache, boolean useLruCache) {

        String key = redis_server + redis_port;
        if (instance.containsKey(key)) {
            return instance.get(key);
        }

        if (useLruCache) {
            local_lru_cache = new ConcurrentHashMap<String, LruCache<String, Integer>>();
        } else {
            local_cache = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
        }

        RedisHanlder hanlder = new RedisHanlder(redis_server, redis_port, local_mem_cache, useLruCache);
        instance.put(key, hanlder);
        return hanlder;
    }

    private boolean CheckingCache(String redis_key) {
        if (useLruCache) {
            return local_lru_cache.containsKey(redis_key);
        }
        return local_cache.containsKey(redis_key);
    }


    private long get_from_cache(String redis_key, String item) {
        if (!CheckingCache(redis_key)) {
            return -1;
        }

        if (useLruCache) {
            if (!local_lru_cache.get(redis_key).contains(item)) {
                return -1;
            }
            return local_lru_cache.get(redis_key).get(item);
        } else {
            if (!local_cache.get(redis_key).containsKey(item)) {
                return -1;
            }
            return local_cache.get(redis_key).get(item);
        }

    }

    private static final int NUM_LOCKS = 32;
    private static ReentrantLock[] locks = new ReentrantLock[NUM_LOCKS];

    static {
        for (int i = 0; i < NUM_LOCKS; i++) {

            locks[i] = new ReentrantLock();

        }
    }

    private void set_local_cache(String redis_key, String item, Long value) {
        if (value == -1) {
            return;
        }
       Integer value2 = value.intValue();
        if (!CheckingCache(redis_key)) {
            ReentrantLock lock = locks[Math.abs(redis_key.hashCode()) % NUM_LOCKS];
            lock.lock();
            try {
                if (useLruCache) {
                    ConcurrentLruCache<String, Integer> lruCache = new ConcurrentLruCache<String, Integer>(capicity, ttl, initialCapicity);
                    lruCache.put(item, value2);
                    local_lru_cache.put(redis_key, lruCache);
                } else {
                    ConcurrentHashMap<String, Integer> localCache = new ConcurrentHashMap<String, Integer>();
                    localCache.put(item, value2);
                    local_cache.put(redis_key, localCache);
                }
            } finally {
                lock.unlock();
            }


        } else {
            if (useLruCache) {
                local_lru_cache.get(redis_key).put(item, value2);
            } else {
                local_cache.get(redis_key).put(item, value2);
            }
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
                    if (logger.isDebugEnabled()) {
                        logger.debug("hit cache:" + redis_key + "/" + item + ":" + from_cache);
                    }
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
                    logger.error("redis", e);
                }
            } else {
                ret = Integer.valueOf(jedis.get(item_key));
            }
            set_local_cache(redis_key, item, ret);
        } finally {
            jPool.returnResource(jedis);
        }

//        System.out.println("set local cache");
        return ret;
    }
}
