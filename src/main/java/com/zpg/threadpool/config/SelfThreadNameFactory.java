package com.zpg.threadpool.config;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhanpengguo
 * @date 2021-04-15 10:50
 */
public class SelfThreadNameFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    @Setter@Getter
    private String threadNamePrefix;

    public SelfThreadNameFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, this.threadNamePrefix + "-" + threadNumber.getAndIncrement());
    }
}
