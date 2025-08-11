package com.zhentao.service.impl;

public interface ILock {
    boolean tryLock(long timeoutSec);
    void unlock();
}
