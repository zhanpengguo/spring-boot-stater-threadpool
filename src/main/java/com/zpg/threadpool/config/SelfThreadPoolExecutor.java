package com.zpg.threadpool.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zhanpengguo
 * @date 2021-04-15 10:49
 */
public class SelfThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfThreadPoolExecutor.class);

    /**
     * 保存任务开始执行的时间，当任务结束时，用任务结束时间减去开始时间计算任务执行时间
     */
    private final ConcurrentHashMap<String, Date> timeMap;

    public static SelfThreadPoolExecutor instance() {
        return instance("self-thread");
    }

    public static SelfThreadPoolExecutor instance(String threadNamePrefix) {
        // 默认核心线程数=2*cpu
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        // 默认最大线程数=5*corePoolSize
        int maximumPoolSize = corePoolSize * 5;
        // 默认等待时长为60秒
        int keepAliveTime = 60;
        // 线程名
        return instance(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                threadNamePrefix);
    }

    public static SelfThreadPoolExecutor instance(int corePoolSize, int maximumPoolSize, int keepAliveTime, String threadNamePrefix) {
        return new SelfThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new SelfThreadNameFactory(threadNamePrefix));
    }

    public SelfThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, SelfThreadNameFactory threadFactory) {
        // 拒绝策略
        // ThreadPoolExecutor.AbortPolicy();//默认，队列满了丢任务抛出异常
        // ThreadPoolExecutor.DiscardPolicy();//队列满了丢任务不异常
        // ThreadPoolExecutor.DiscardOldestPolicy();//将最早进入队列的任务删，之后再尝试加入队列
        // ThreadPoolExecutor.CallerRunsPolicy();//如果添加到线程池失败，那么主线程会自己去执行该任务
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.timeMap = new ConcurrentHashMap<>();
    }
    /**
     * 线程池延迟关闭时（等待线程池里的任务都执行完毕），统计线程池情况
     */
    @Override
    public void shutdown() {
        LOGGER.info("线程池停止. 已执行任务: {}, 正在执行任务: {}, 未执行任务数量: {}",
                this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        super.shutdown();
    }
    /**
     * 线程池立即关闭时，统计线程池情况
     */
    @Override
    public List<Runnable> shutdownNow() {
        LOGGER.info("线程池立即停止. 已执行任务: {}, 正在执行任务: {}, 未执行任务: {}",
                this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        return super.shutdownNow();
    }

    /**
     * 任务执行之前，记录任务开始时间
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        timeMap.put(String.valueOf(r.hashCode()), new Date());
    }

    /**
     * 任务执行之后，计算任务结束时间
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Date startDate = timeMap.remove(String.valueOf(r.hashCode()));
        Date finishDate = new Date();
        long diff = finishDate.getTime() - startDate.getTime();
        LOGGER.info("任务耗时: {} ms, 初始线程数: {}, 核心线程数: {}, 正在执行的任务数量: {}, " +
                        "已完成任务数: {}, 任务总数: {}, 队列里缓存的任务数: {}, 池中存在的最大线程数: {}, " +
                        "最大允许的线程数: {},  线程空闲时间: {}, 线程池是否关闭: {}, 线程池是否终止: {}",
                diff, this.getPoolSize(), this.getCorePoolSize(), this.getActiveCount(),
                this.getCompletedTaskCount(), this.getTaskCount(), this.getQueue().size(), this.getLargestPoolSize(),
                this.getMaximumPoolSize(), this.getKeepAliveTime(TimeUnit.MILLISECONDS), this.isShutdown(), this.isTerminated());
    }
}
