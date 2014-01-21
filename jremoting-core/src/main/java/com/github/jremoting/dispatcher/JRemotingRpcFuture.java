package com.github.jremoting.dispatcher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.core.RpcFutureListener;
import com.github.jremoting.exception.RpcException;

public class JRemotingRpcFuture implements RpcFuture {
	
	private final Invocation invocation;
	private volatile Object result;
	
	private static final Object CANCEL = new Object();
	
	
	private final CountDownLatch latch = new CountDownLatch(1);

	public JRemotingRpcFuture(Invocation invocation) {
		this.invocation = invocation;
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
			throw new RpcException((Throwable)result);
		}
		return result;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		latch.await(timeout, unit);
		if(result instanceof Throwable) {
			throw new RpcException((Throwable)result);
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
	public void addListener(RpcFutureListener listener) {
		
	}

	@Override
	public void removeListener(RpcFutureListener listener) {
		
	}
	
	public void setResult(Object result) {
		this.result = result;
		latch.countDown();
	}

	public Invocation getInvocation() {
		return invocation;
	}
}
