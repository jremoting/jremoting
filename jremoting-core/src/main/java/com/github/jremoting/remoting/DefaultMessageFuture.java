package com.github.jremoting.remoting;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Message;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.core.MessageFutureListener;

public class DefaultMessageFuture implements MessageFuture {
	
	private final Message msg;
	private volatile Object result;
	
	private static final Object CANCEL = new Object();
	private static final Object VOID = new Object();
	private final CountDownLatch latch = new CountDownLatch(1);
	private final CopyOnWriteArrayList<MessageFutureListener> listeners = new CopyOnWriteArrayList<MessageFutureListener>();
	private static final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
	private final long startTime = System.currentTimeMillis();
	
	

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
		return result;
	}

	@Override
	public boolean isCancelled() {
		return result != null && result == CANCEL;
	}

	@Override
	public boolean isDone() {
		return result != null;
	}

	@Override
	public void addListener(MessageFutureListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(MessageFutureListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners() {
		if (listeners.size() > 0) {
			listenerExecutor.execute(new Runnable() {
				@Override
				public void run() {
					for (MessageFutureListener listener : listeners) {
						try {
							listener.onMessage(DefaultMessageFuture.this.result);
						}
						catch(Throwable throwable) {
							//ignore
						}
					}
				}
			});
		}
	}
	
	public void setResult(Object result) {
		this.result = (result == null? VOID : result);
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
