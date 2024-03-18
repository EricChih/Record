/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.util;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.lang.NonNull;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/5/5
 */
public class ThreadPoolUtil {

    private static volatile ThreadPoolUtil instance;

    private int corePoolSize;

    private int maxPoolSize;

    private long keepAliveTime = 1;

    private TimeUnit unit = TimeUnit.HOURS;

    private ThreadPoolExecutor executor;

    private ThreadPoolUtil() {

        corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;

        maxPoolSize = corePoolSize;

        executor = new ThreadPoolExecutor(

                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                unit,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory(Thread.NORM_PRIORITY, "thread-pool-"),
                new ThreadPoolExecutor.AbortPolicy()

        );

    }

    public static ThreadPoolUtil getInstance() {

        if (instance==null) {

            synchronized (ThreadPoolUtil.class) {

                if (instance==null) {
                    instance = new ThreadPoolUtil();
                }

            }

        }

        return instance;

    }

    public void execute(Runnable runnable) {

        if (executor==null) {

            executor = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    new DefaultThreadFactory(Thread.NORM_PRIORITY, "thread-pool-"),
                    new ThreadPoolExecutor.AbortPolicy());

        }

        if (runnable!=null) {
            executor.execute(runnable);
        }

    }

    public void remove(Runnable runnable) {

        if (runnable!=null) {
            executor.remove(runnable);
        }

    }

    private static class DefaultThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final ThreadGroup group;

        private final String namePrefix;

        private final int threadPriority;

        DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
            this.threadPriority = threadPriority;
            this.group = Thread.currentThread().getThreadGroup();
            this.namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {

            Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }

            thread.setPriority(threadPriority);

            return thread;

        }

    }

}
