package com.github.jremoting.invoke;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.exception.RemotingException;

public class RetryInvokeFilter extends AbstractInvokeFilter {

	@Override
	public Object invoke(Invoke invoke) {
		int tryTimes = invoke.getRetry() + 1;
		while (true) {
			try {
				return getNext().invoke(invoke);
			} catch (Exception e) {
				tryTimes--;
				if(tryTimes > 0) {
					continue;
				}
				else {
					throw new RemotingException(e);
				}
			}
		}
	}
	
	private static class RetryAsyncInvokeContext {
		public int tryTimes = 0 ;
		public RetryAsyncInvokeContext(int retry) {
			this.tryTimes = retry + 1;
		}
		
		public static final String CONTEXT_KEY = RetryAsyncInvokeContext.class.getName();
	}
	
	@Override
	public void beginInvoke(Invoke invoke) {
		
		RetryAsyncInvokeContext context = (RetryAsyncInvokeContext) invoke.getAsyncContext(RetryAsyncInvokeContext.CONTEXT_KEY);
		if(context == null) {
			context = new RetryAsyncInvokeContext(invoke.getRetry());
			invoke.setAsyncContext(RetryAsyncInvokeContext.CONTEXT_KEY, context);
		}
		context.tryTimes--;
		
	    getNext().beginInvoke(invoke);
	}

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		RetryAsyncInvokeContext context = (RetryAsyncInvokeContext) invoke.getAsyncContext(RetryAsyncInvokeContext.CONTEXT_KEY);
		if(context.tryTimes > 0) {
			this.beginInvoke(invoke);
			return;
		}
		
		
		getPrev().endInvoke(invoke, result);
	}

}
