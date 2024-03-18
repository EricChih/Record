/**
 * Hualiteq International Corp.
 * Copyright © 2023. All Rights Reserved.
 */
package com.ub.gir.web.configuration.thread;


import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;


/**
 * <p></p>
 *
 * @author ： Seimo_Zhan
 * @version :
 * @Date ： 2023/5/2
 */
@Slf4j
public class ThreadPoolHelper {

    private final ThreadPoolExecutor threadPoolExecutor;

    private final CompletionService completionService;

    private ThreadPoolHelper() {
        this(Runtime.getRuntime().availableProcessors() * 2 + 1);
    }

    private ThreadPoolHelper(int threadNum) {

        log.info("**** ThreadPoolHelper Thread Name : {}", Thread.currentThread().getName());

        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(10);
        this.threadPoolExecutor = new ThreadPoolExecutor(threadNum, threadNum * 10, 0L, TimeUnit.SECONDS, blockingQueue);
        this.completionService = new ExecutorCompletionService<>(threadPoolExecutor);

    }

    public static ThreadPoolHelper getInstance() {
        return ThreadPoolHolder.INSTANCE;
    }

    public static Future<?> execute(final Runnable runnable) {
        return getInstance().threadPoolExecutor.submit(runnable);
    }

    public static <T> Future<T> execute(final Callable<T> callable) {
        return getInstance().threadPoolExecutor.submit(callable);
    }

    public static <V> Future<V> executeCompletion(final Callable<V> callable) {
        return getInstance().completionService.submit(callable);
    }

    public static <T> Future<T> getCompletionPool() {
        return (Future<T>) getInstance().completionService.poll();
    }

    public static <T> Future<T> getCompletionPool(long expire, TimeUnit timeUnit) throws InterruptedException {
        return (Future<T>) getInstance().completionService.poll(expire, timeUnit);
    }

    public static void execShutdown() {
        getInstance().threadPoolExecutor.shutdown();
    }

    private static class ThreadPoolHolder {
        private static final ThreadPoolHelper INSTANCE = new ThreadPoolHelper();
    }

}
