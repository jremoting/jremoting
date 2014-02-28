package com.github.jremoting.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executors {

    private static final long keepAliveTime = 300L;

  
    public static ThreadPoolExecutor newExecutor(int corePoolSize, int maxPoolSize, int queueSize) {
    	return newExecutor(corePoolSize, maxPoolSize, queueSize, "JRemoting", true);
    }
    
    public static ThreadPoolExecutor newExecutor(int corePoolSize, int maxPoolSize, int queueSize, String namePrefix) {
    	return newExecutor(corePoolSize, maxPoolSize, queueSize, namePrefix, true);
    }
    
    public static ThreadPoolExecutor newExecutor(int corePoolSize, int maxPoolSize, int queueSize,String namePrefix ,boolean daemon) {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>(queueSize);
        ThreadFactory threadFactory = new NamedThreadFactory(namePrefix, daemon);
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                workQueue, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

}