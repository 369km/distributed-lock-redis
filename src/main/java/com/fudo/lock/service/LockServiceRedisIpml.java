package com.fudo.lock.service;

import com.fudo.lock.model.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class LockServiceRedisIpml implements LockService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceRedisIpml.class);
    private final static long LOCK_EXPIRE = 30 * 1000L;//单个业务持有锁的时间30s，防止死锁
    private final static long LOCK_TRY_INTERVAL = 30L;//默认30ms尝试一次
    private final static long LOCK_TRY_TIMEOUT = 20 * 1000L;//默认尝试20s

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean lock(Lock lock) {
        return setLock(lock, LOCK_TRY_TIMEOUT, LOCK_TRY_INTERVAL, LOCK_EXPIRE);
    }

    @Override
    public boolean lock(Lock lock, long timeOut) {
        return setLock(lock, timeOut, LOCK_TRY_INTERVAL, LOCK_EXPIRE);
    }

    @Override
    public boolean lock(Lock lock, long timeOut, long tryInterval) {
        return setLock(lock, timeOut, tryInterval, LOCK_EXPIRE);
    }

    @Override
    public boolean lock(Lock lock, long timeOut, long tryInterval, long lockExpireTime) {
        return setLock(lock, timeOut, tryInterval, lockExpireTime);
    }

    private boolean setLock(Lock lock, long timeout, long tryInterval, long lockExpireTime) {
        //校验lock不为空
        if (Objects.isNull(lock)) {
            LOGGER.error("lock is null");
            return false;
        }
        //校验lock
        if (StringUtils.isEmpty(lock.getName()) || StringUtils.isEmpty(lock.getValue())) {
            return false;
        }
        //获取锁开始时间
        long startTime = System.currentTimeMillis();
        //循环执行向redis中添加lock
        do {
            //获取锁成功
            if (!stringRedisTemplate.hasKey(lock.getName())) {
                stringRedisTemplate.opsForValue().set(lock.getName(), lock.getValue(), lockExpireTime, TimeUnit.MILLISECONDS);
                return true;
            }
            //获取锁超时
            if (System.currentTimeMillis() - startTime > timeout) {
                return false;
            }
            //设置获取锁间隔时间
            try {
                Thread.sleep(tryInterval);
            } catch (InterruptedException e) {
                LOGGER.error("InterruptedException", e);
                return false;
            }
        } while (stringRedisTemplate.hasKey(lock.getName()));

        return false;
    }

    @Override
    public void release(Lock lock) {
        if (!StringUtils.isEmpty(lock.getName())) {
            stringRedisTemplate.delete(lock.getName());
        }
    }
}
