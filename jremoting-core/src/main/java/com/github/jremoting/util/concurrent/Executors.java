package com.github.jremoting.util.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    
    public static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;
        private final boolean isDaemon;

        public NamedThreadFactory() {
            this("pool");
        }

        public NamedThreadFactory(String name) {
            this(name, false);
        }

        public NamedThreadFactory(String preffix, boolean daemon) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
            isDaemon = daemon;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(isDaemon);
            return t;
        }
    }
    
    
}