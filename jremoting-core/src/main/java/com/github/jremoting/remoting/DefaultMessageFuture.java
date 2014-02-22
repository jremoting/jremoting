package com.github.jremoting.remoting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.concurrent.FutureListener;

public class DefaultMessageFuture implements MessageFuture {
	
	private final Invoke invoke;
	private volatile Object result;
	
	private static final Object CANCEL = new Object();
	private static final Object NULL = new Object();
	private final CountDownLatch latch = new CountDownLatch(1);

	private final long startTime = System.currentTimeMillis();
	
	private final Map<FutureListener<Object>, Executor> listeners = new HashMap<FutureListener<Object>, Executor>();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageFuture.class);
	
	

	public DefaultMessageFuture(Invoke invoke) {
		this.invoke = invoke;
	}
	
	public boolean isTimeout() {
		long now = System.currentTimeMillis();
		if(now - startTime > invoke.getTimeout()) {
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



	public void setResult(Object result) {
		this.result = (result == null? NULL : result);
		latch.countDown();
	}
	
	public void onResult(final Object result) {
		
		if(!invoke.isAsync()) {
			this.setResult(result);
			return;
		}
		
		//this method is called from netty io thread , callback will use other thread

		invoke.getAsyncInvokeExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					//run invoke filters's enInvoke first then run user callback 
					DefaultMessageFuture.this.invoke.getInvokeChain().endInvoke(invoke, result);
				} catch (Throwable th) {
					LOGGER.error("error happens when run client filter's endInvoke chain , msg->" + th.getMessage(), th);
				}

				if(isDone()) {
					notifyListeners();
				}
			}

		});
	}
	

	private void notifyListeners() {
		//if  result was set then notify listeners
		for (final FutureListener<Object> listener : listeners.keySet()) {
			Executor executor = listeners.get(listener);
			
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						listener.operationComplete(DefaultMessageFuture.this);
					} catch (Throwable th) {
						LOGGER.error(
								"error happens when run user's callback , msg->"
										+ th.getMessage(), th);
					}
				}
			});
			
		}
	}

	public Invoke getInvoke() {
		return invoke;
	}
	
	@Override
	public void addListener(final FutureListener<Object> listener, Executor executor) {
		if(listener == null) {
			throw new NullPointerException("listener can not be null.");
		}
		if(executor == null) {
			throw new NullPointerException("executor can not be null.");
		}
		
		if(isDone()) {
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					listener.operationComplete(DefaultMessageFuture.this);
				}
			});
		}
		else {
			this.listeners.put(listener, executor);
		}
		
	}

	@Override
	public void addListener(final FutureListener<Object> listener) {
		
		if(listener == null) {
			throw new NullPointerException("listener can not be null.");
		}
		
		if(isDone()) {
			invoke.getAsyncInvokeExecutor().execute(new Runnable() {
				@Override
				public void run() {
					listener.operationComplete(DefaultMessageFuture.this);
				}
			});
		}
		else {
			this.listeners.put(listener, invoke.getAsyncInvokeExecutor());
		}
	
	}

	@Override
	public boolean isSuccess() {
		return this.isDone() && !(this.result instanceof Throwable);
	}

	@Override
	public Throwable cause() {
		return isSuccess() ? null : (Throwable)result;
	}

	@Override
	public Object result() {
		return result;
	}

}
