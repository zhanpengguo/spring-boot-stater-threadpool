package com.zpg.threadpool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhanpengguo
 * @date 2021-04-15 10:46
 */
@Data
@ConfigurationProperties(prefix = "zpg.thread")
public class SelfThreadProperties {

    private boolean enable;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;
    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;
    /**
     * 当线程数大于核心线程数时，空余线程等待新任务的最长时间
     */
    private Integer keepAliveTime;
    /**
     * 线程名前缀【业务名】
     */
    private String threadNamePrefix;
}
