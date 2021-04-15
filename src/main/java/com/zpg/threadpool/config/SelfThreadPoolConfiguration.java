package com.zpg.threadpool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanpengguo
 * @date 2021-04-15 10:48
 */
@Configuration
@EnableConfigurationProperties(SelfThreadProperties.class)
@ConditionalOnProperty(prefix = "zpg.thread", name = "enable", havingValue = "true", matchIfMissing = true)
public class SelfThreadPoolConfiguration {

    private final SelfThreadProperties selfThreadProperties;

    @Autowired
    public SelfThreadPoolConfiguration(SelfThreadProperties selfThreadProperties) {
        this.selfThreadProperties = selfThreadProperties;
    }

    @Bean
    public SelfThreadPoolExecutor selfThreadPoolExecutor() {
        // 默认核心线程数=2*cpu
        int corePoolSize = selfThreadProperties.getCorePoolSize() == null ?
                Runtime.getRuntime().availableProcessors() * 2 : selfThreadProperties.getCorePoolSize();
        // 默认最大线程数=5*corePoolSize
        int maximumPoolSize = selfThreadProperties.getMaximumPoolSize() == null ?
                corePoolSize * 5 : selfThreadProperties.getMaximumPoolSize();
        // 默认等待时长为60秒
        int keepAliveTime = selfThreadProperties.getKeepAliveTime() == null ?
                60 : selfThreadProperties.getKeepAliveTime();
        // 线程名
        String threadNamePrefix = selfThreadProperties.getThreadNamePrefix() == null ?
                "self-thread" : selfThreadProperties.getThreadNamePrefix();
        return new SelfThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new SelfThreadNameFactory(threadNamePrefix));
    }
}
