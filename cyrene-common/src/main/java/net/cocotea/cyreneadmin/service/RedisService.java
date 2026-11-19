package net.cocotea.cyreneadmin.service;

/**
 * redis包装服务
 *
 * @author CoCoTea
 * @version 2.0
 */
public interface RedisService {
    /**
     * 保存方法，时间为秒
     *
     * @param key     k
     * @param value   v
     * @param seconds 单位秒
     */
    void save(String key, String value, Long seconds);

    /**
     * 保存方法，时间为小时
     *
     * @param key   k
     * @param value v
     * @param hours 单位小时
     */
    void saveByHours(String key, String value, int hours);

    /**
     * 保存方法，时间为分钟
     *
     * @param key     k
     * @param value   v
     * @param minutes 单位分钟
     */
    void saveByMinutes(String key, String value, int minutes);

    /**
     * 保存方法，时效为永久
     *
     * @param key   k
     * @param value v
     */
    void save(String key, String value);

    /**
     * 删除
     *
     * @param key k
     */
    void delete(String key);

    /**
     * 获取值
     *
     * @param key k
     * @return 值
     */
    String get(String key);

    /**
     * 统计匹配键的数量，使用 SCAN 增量遍历，避免 KEYS 阻塞 Redis
     *
     * @param pattern 匹配表达式
     * @return 键数量
     */
    long countKeys(String pattern);

    /**
     * 按匹配表达式批量删除键，使用 SCAN 增量遍历，避免 KEYS 阻塞 Redis
     *
     * @param pattern 匹配表达式
     * @return 删除的键数量
     */
    long deleteByPattern(String pattern);

    void set(String redisKey, String s);

}
