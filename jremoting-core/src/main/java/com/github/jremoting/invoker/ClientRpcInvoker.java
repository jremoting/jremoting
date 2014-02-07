package com.github.jremoting.invoker;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.InvokeFilterUtil;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.exception.RemotingException;

public class ClientRpcInvoker {

	private final RpcClient rpcClient;
	private final InvokeFilter head;
	
	public ClientRpcInvoker(RpcClient rpcClient, List<InvokeFilter> invokeFilters) {
		this.rpcClient = rpcClient;
		
		invokeFilters.add(new ClientTailInvokeFilter());
		this.head = InvokeFilterUtil.link(invokeFilters);
	}

	public Object invoke(Invoke invoke) {
		return this.head.invoke(invoke);
	}
	
	private  class ClientTailInvokeFilter implements InvokeFilter {
		
		@Override
		public Object invoke(Invoke invoke) {
			MessageFuture future = rpcClient.send(invoke);
			try {
				return future.get();
			} catch (InterruptedException e) {
				throw new RemotingException(e);
			} catch (ExecutionException e) {
				throw new RemotingException(e);
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
