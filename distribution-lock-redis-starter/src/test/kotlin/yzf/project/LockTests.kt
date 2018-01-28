package yzf.project

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import yzf.project.distributionlock.redis.RedisLock
import java.util.concurrent.CountDownLatch

/**
 * @author created by yzf on 16/01/2018
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(LockTestContext::class))
@ActiveProfiles("test")
class LockTests {

    @Autowired
    private lateinit var redisLock: RedisLock

    @Test
    fun testLock() {
        val key = "distribution-lock-test"
        val countDownLatch = CountDownLatch(2)

        val task1 = LockTask(key, 2000L, 0L, redisLock, countDownLatch)
        val task2 = LockTask(key, 2000L, 2500L, redisLock, countDownLatch)

        task1.start()
        task2.start()

        countDownLatch.await()
    }

    @Test
    fun testLock1() {
        val key = "distribution-lock-test"
        val countDownLatch = CountDownLatch(2)

        val task1 = LockTask(key, 2000L, 0L, redisLock, countDownLatch)
        val task2 = LockTask(key, 2000L, 1000L, redisLock, countDownLatch)

        task1.start()
        task2.start()

        countDownLatch.await()
    }

    @Test
    fun testTryLock() {
        val key = "distribution-lock-test"
        val countDownLatch = CountDownLatch(2)

        val task1 = TryLockTask(key, 3000L, 2000L, redisLock, countDownLatch)
        val task2 = TryLockTask(key, 3000L, 2000L, redisLock, countDownLatch)

        task1.start()
        task2.start()

        countDownLatch.await()
    }

    @Test
    fun testTryLock1() {
        val key = "distribution-lock-test"
        val countDownLatch = CountDownLatch(2)

        val task1 = TryLockTask(key, 1000L, 2000L, redisLock, countDownLatch)
        val task2 = TryLockTask(key, 1000L, 2000L, redisLock, countDownLatch)

        task1.start()
        task2.start()

        countDownLatch.await()
    }

    class TryLockTask (
            private var key: String,
            private var retryTime: Long,
            private var timeout: Long,
            private var redisLock: RedisLock,
            private var countDownLatch: CountDownLatch
    ) : Thread() {

        override fun run() {
            val startMs = System.currentTimeMillis()
            val value = redisLock.getRandomValue()
            val res = redisLock.tryLock(key, value, expireMills = timeout, retryMills = retryTime)
            System.out.println("thread name:" + Thread.currentThread().name + " try lock res is:" + res +
            " time escape ms:" + (System.currentTimeMillis() - startMs))

            countDownLatch.countDown()
        }
    }

    class LockTask (
            private var key: String,
            private var timeout: Long,
            private var delay: Long,
            private var redisLock: RedisLock,
            private var countDownLatch: CountDownLatch
    ) : Thread() {

        override fun run() {
            if (delay != 0L) {
                Thread.sleep(delay)
            }
            val value = redisLock.getRandomValue()
            val res = redisLock.lock(key, value, timeout)
            System.out.println("thread name:" + Thread.currentThread().name + " lock result:" + res.toString())
            countDownLatch.countDown()
        }
    }
}