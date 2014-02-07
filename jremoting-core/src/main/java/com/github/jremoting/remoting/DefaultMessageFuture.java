package com.github.jremoting.remoting;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Message;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.core.MessageFutureListener;
import com.github.jremoting.exception.RemotingException;

public class DefaultMessageFuture implements MessageFuture {
	
	private final Message msg;
	private volatile Object result;
	
	private static final Object CANCEL = new Object();
	private static final Object VOID = new Object();
	
	
	private final CountDownLatch latch = new CountDownLatch(1);

	public DefaultMessageFuture(Message msg) {
		this.msg = msg;
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
		if(result instanceof Throwable) {
			throw new RemotingException((Throwable)result);
		}
		return result;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		latch.await(timeout, unit);
		if(result instanceof Throwable) {
			throw new RemotingException((Throwable)result);
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
		
	}

	@Override
	public void removeListener(MessageFutureListener listener) {
		
	}
	
	public void setResult(Object result) {
		this.result = (result == null? VOID : result);
		latch.countDown();
	}

	public Message getMessage() {
		return msg;
	}
}
