/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.thread;


import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;


import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * <p>Thread 非同步定義設定</p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/4/20
 */
@Configuration
@EnableAsync
@Slf4j
public class ThreadPoolConfig implements AsyncConfigurer {

    /**
     * Constructor
     */
    public ThreadPoolConfig() {
        super();
        log.info("**** Start-Up : {}", this.getClass().getName());
    }

    public Executor taskExecutor() {

        /*
          Set the ThreadPoolExecutor's core pool size.
         */
        final int DEFAULT_CORE_POOL_SIZE = 10;

        /*
         * Set the ThreadPoolExecutor's maximum pool size.
         */
        final int DEFAULT_MAX_POOL_SIZE = 200;

        /*
         * Set the capacity for the ThreadPoolExecutor's BlockingQueue.
         */
        final int DEFAULT_QUEUE_CAPACITY = 10;

        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int coreCount = Runtime.getRuntime().availableProcessors() * 2 + 1;

        executor.setThreadNamePrefix("Aasync-Service-");
        executor.setCorePoolSize(coreCount);
        executor.setMaxPoolSize(DEFAULT_MAX_POOL_SIZE);
        executor.setQueueCapacity(DEFAULT_QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(300);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.setRejectedExecutionHandler ( (r,e)->{
            throw new RejectedExecutionException("Task : " + r.toString() + " Rejected from : " + e.toString());
        } ) ;
        executor.initialize();

        return executor;

    }

    /* (non-Javadoc)
     * @see org.springframework.scheduling.annotation.AsyncConfigurer#getAsyncExecutor()
     */
    @Bean("taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    /* (non-Javadoc)
     * @see org.springframework.scheduling.annotation.AsyncConfigurer#getAsyncUncaughtExceptionHandler()
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

}
