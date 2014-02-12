package com.github.jremoting.invoke;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.InvokeFilterUtil;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.exception.RemotingException;

public class ClientInvokeFilterChain {

	private final MessageChannel messageChannel;
	private final long DEFAULT_TIMEOUT = 60*1000*5; //default timeout 5 mins
	public MessageChannel getMessageChannel() {
		return messageChannel;
	}

	private final InvokeFilter head;
	
	public ClientInvokeFilterChain(MessageChannel messageChannel, List<InvokeFilter> invokeFilters) {
		this.messageChannel = messageChannel;
		
		invokeFilters.add(new ClientTailInvokeFilter());
		this.head = InvokeFilterUtil.link(invokeFilters);
	}

	public Object invoke(Invoke invoke) {
		return this.head.invoke(invoke);
	}
	
	private  class ClientTailInvokeFilter implements InvokeFilter {
		
		@Override
		public Object invoke(Invoke invoke) {
			MessageFuture future = messageChannel.send(invoke);
			try {
				if(invoke.getTimeout() <= 0) {
					invoke.setTimeout(DEFAULT_TIMEOUT);
				}
				return future.get(invoke.getTimeout(), TimeUnit.MILLISECONDS);
				
			} catch (InterruptedException e) {
				throw new RemotingException(e);
			} catch (ExecutionException e) {
				throw new RemotingException(e);
			} catch (TimeoutException e) {
				throw new com.github.jremoting.exception.TimeoutException("invoke time out timeout:" + invoke.getTimeout());
			}
		}
		
		@Override
		public InvokeFilter getNext() {
			return null;
		}
		@Override
		public void setNext(InvokeFilter next) {
		}
		
	}
}
