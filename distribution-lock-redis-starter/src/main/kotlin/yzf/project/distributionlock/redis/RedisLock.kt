package yzf.project.distributionlock.redis

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
import java.util.*

/**
 * @author created by yzf on 15/01/2018
 */
class RedisLock @Autowired constructor(
        private val redisTemplate: RedisTemplate<String, Any>
) {

    fun lock(key: String, value: String, expireMills: Long) : Boolean {
        val res = redisTemplate.execute({ connection: RedisConnection? ->
            run {
                val nativeConnection = connection?.nativeConnection
                if (nativeConnection is Jedis) {
                    return@run nativeConnection.set(key, value, "NX", "PX", expireMills)
                }
                return@run "fail"
            }
        })
        return "OK" == res
    }

    fun unlock(key: String, expectValue: String) : Boolean {
        val luaScript =
                "if redis.call(\"get\",\"" + key + "\") == \"" + expectValue + "\" then\n" +
                        "    return redis.call(\"del\",\"" + key + "\")\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end"
        val res = redisTemplate.execute{
            connection: RedisConnection? -> run {
            val nativeConnection = connection?.nativeConnection
            if (nativeConnection is Jedis) {
                return@run nativeConnection.eval(luaScript)
            }
            return@run 0L
        }}

        return res == 1L // 等于0表示，这个锁已经被其他线程占有了
    }

    fun tryLock(key: String, value: String, expireMills: Long, retryMills: Long) : Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < retryMills) {
            if (lock(key, value, expireMills)) {
                return true
            }
            Thread.sleep(2)
        }
        return false
    }

    fun getRandomValue(): String {
        return UUID.randomUUID().toString().substring(0, 8) + System.currentTimeMillis()
    }
}