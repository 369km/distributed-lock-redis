package com.fudo.lock.service;

import com.fudo.lock.model.Lock;

public interface LockService {
    boolean lock(Lock lock);

    boolean lock(Lock lock, long timeOut);

    boolean lock(Lock lock, long timeOut, long tryInterval);

    boolean lock(Lock lock, long timeOut, long tryInterval, long lockExpireTime);

    void release(Lock lock);
}
