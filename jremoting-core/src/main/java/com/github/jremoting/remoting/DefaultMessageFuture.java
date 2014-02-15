package com.github.jremoting.remoting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Message;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.exception.RemotingException;

public class DefaultMessageFuture implements MessageFuture {
	
	private final Message msg;
	private volatile Object result;
	
	private static final Object CANCEL = new Object();
	private static final Object NULL = new Object();
	private final CountDownLatch latch = new CountDownLatch(1);
	private final Map<Runnable,Executor> listeners = new ConcurrentHashMap<Runnable, Executor>();
	private final long startTime = System.currentTimeMillis();
	private final Executor defaultExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.setName("JRemoting-Default-Callback-Runner");
			return thread;
		}
	});
	
	

	public DefaultMessageFuture(Message msg) {
		this.msg = msg;
	}
	
	public boolean isTimeout() {
		long now = System.currentTimeMillis();
		if(now - startTime > msg.getTimeout()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		result = CANCEL;
		latch.countDown();
		return true;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		
		latch.await();
	
		if(result instanceof RuntimeException) {
			throw (RuntimeException)result;
		}
		else if(result instanceof Throwable) {
			throw new RemotingException((Throwable)result);
		}
		return result;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		boolean success = latch.await(timeout, unit);
		if(!success) {
			throw new TimeoutException();
		}
		
		if(result instanceof RuntimeException) {
			throw (RuntimeException)result;
		}
		else if(result instanceof Throwable) {
			throw new RemotingException((Throwable)result);
		}
		
		return result;
	}

	@Override
	public boolean isCancelled() {
		return result == CANCEL;
	}

	@Override
	public boolean isDone() {
		return result != null;
	}

	@Override
	public void addListener(Runnable listener, Executor executor) {
		if(executor == null) {
			listeners.put(listener, defaultExecutor);
		}
		else {
			listeners.put(listener, executor);
		}
	}
	
	private void notifyListeners() {
		if (listeners.size() > 0) {
			for(Runnable listener: listeners.keySet()) {
				Executor executor = listeners.get(listener);
				executor.execute(listener);
			}
		}
	}
	
	public void setResult(Object result) {
		this.result = (result == null? NULL : result);
		latch.countDown();
		notifyListeners();
	}

	public Message getMessage() {
		return msg;
	}

	public long getStartTime() {
		return startTime;
	}


}
