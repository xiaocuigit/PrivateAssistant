package com.assistant.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 作者 : xiaocui
 * <p>
 * 版本 : 1.0
 * <p>
 * 创建日期 : 2016/3/28
 * <p>
 * 功能描述 : 创建一个线程池
 */
public class ThreadPoolUtils {
    private ExecutorService pool;
    private final static int DEFAULT_SIZE = 8;

    public ThreadPoolUtils(int size) {
        pool = Executors.newFixedThreadPool(size);
    }

    public ThreadPoolUtils() {
        this(DEFAULT_SIZE);
    }

    public void shutDown() {
        if (!pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdown();
                }
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("pool did not terminate");
                }
            } catch (InterruptedException e) {
                pool.shutdown();
                Thread.currentThread().interrupt();
            }
        } else {

        }
    }

    public ExecutorService getPool() {
        if (pool != null) {
            return pool;
        }
        return null;
    }

    public void execute(Runnable runnable) {
        // 当线程池还没有关闭，就可以选择一个空闲的线程来运行
        if (!pool.isShutdown()) {
            pool.execute(runnable);
        }
    }
}
