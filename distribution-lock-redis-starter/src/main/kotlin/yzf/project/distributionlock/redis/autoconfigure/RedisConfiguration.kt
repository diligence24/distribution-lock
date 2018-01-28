package yzf.project.distributionlock.redis.autoconfigure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import yzf.project.distributionlock.redis.RedisLock

/**
 * @author created by yzf on 12/01/2018
 */
@Configuration
open class RedisConfiguration {

    @Bean
    open fun redisConnectionFactory(@Value("\${spring.redis.host}") host: String,
                                    @Value("\${spring.redis.port}") port: Int,
                                    @Value("\${spring.redis.password}") password: String,
                                    @Value("\${spring.redis.database}") dbIndex: Int) : JedisConnectionFactory {
        val jedisConnectionFactory = JedisConnectionFactory()
        jedisConnectionFactory.hostName = host
        jedisConnectionFactory.port = port
        jedisConnectionFactory.database = dbIndex
        jedisConnectionFactory.password = password
        return jedisConnectionFactory
    }

    @Bean
    open fun redisSerializer(): RedisSerializer<Any> {
        return Jackson2JsonRedisSerializer(Any::class.java)
    }

    @Bean
    open fun redisTemplate(redisConnectionFactory: JedisConnectionFactory?,
                           redisSerializer: RedisSerializer<Any>): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory
        template.defaultSerializer = redisSerializer
        template.afterPropertiesSet()
        return template
    }

    @Bean
    open fun redisLock(redisTemplate: RedisTemplate<String, Any>) : RedisLock {
        return RedisLock(redisTemplate)
    }
}