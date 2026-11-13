package net.cocotea.cyreneadmin.service.impl;

import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.service.RedisService;
import org.noear.redisx.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.Collections;
import java.util.Map;

/**
 * @author CoCoTea
 * @since v1
 */
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final RedisClient redisClient;

    @Override
    public void save(String key, String value, Long seconds){
        redisClient.open(session -> session.key(key).expire(Math.toIntExact(seconds)).set(value));
    }

    @Override
    public void saveByHours(String key, String value, int hours){
        save(key, value, hours * 3600L);
    }

    @Override
    public void saveByMinutes(String key, String value, int minutes){
        save(key, value, minutes * 60L);
    }

    @Override
    public void save(String key, String value) {
        redisClient.open(session -> session.key(key).set(value));
    }

    @Override
    public void delete(String key) {
        redisClient.open(session -> session.deleteKeys(Collections.singleton(key)));
    }

    @Override
    public String get(String key) {
        return redisClient.openAndGet(session -> session.key(key).get());
    }

    @Override
    public void hashPut(String key, String hashKey, String value) {
        redisClient.open(session -> session.key(key).hashSet(hashKey, value));
    }

    @Override
    public void hashPut(String key, String hashKey, Object value) {

    }

    @Override
    public Object hashGet(String key, String hashKey) {
        return null;
    }

    @Override
    public Map<Object, Object> hashGetEntries(String key) {
        return Map.of();
    }

    @Override
    public void hashRemove(String key, Object... hashKeys) {

    }

    @Override
    public long countKeys(String pattern) {
        return redisClient.openAndGet(session -> {
            ScanParams params = new ScanParams().match(pattern).count(200);
            String cursor = ScanParams.SCAN_POINTER_START;
            long count = 0;
            do {
                ScanResult<String> result = session.jedis().scan(cursor, params);
                count += result.getResult().size();
                cursor = result.getCursor();
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
            return count;
        });
    }

    @Override
    public void set(String key, String value) {
        redisClient.open(session -> session.key(key).expire(0).set(value));
    }
}
