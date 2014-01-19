package com.github.jremoting.dispatcher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.core.RpcFutureListener;

public class JRemotingRpcFuture implements RpcFuture {
	
	private final Invocation invocation;
	private Object result;

	public JRemotingRpcFuture(Invocation invocation) {
		this.invocation = invocation;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return result;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addListener(RpcFutureListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeListener(RpcFutureListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	public void setResult(Object result) {
		this.result = result;
	}

	public Invocation getInvocation() {
		return invocation;
	}
}
