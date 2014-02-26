package com.github.jremoting.transport;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.Registry;
import com.github.jremoting.invoke.ClientInvokeFilterChain;
import com.github.jremoting.util.LifeCycleSupport;
import com.github.jremoting.util.NetUtil;

public class DefaultRpcClient implements RpcClient {
	
	private final Serializer defaultSerializer;
	private final ClientInvokeFilterChain invokeFilterChain;
	private final Registry registry;
	private final MessageChannel messageChannel;
	private final ExecutorService asyncInvokeExecutor;
	private final LifeCycleSupport lifeCycleSupport = new LifeCycleSupport();
	private final static AtomicLong NEXT_MSG_ID = new AtomicLong(0);
	private final String localIp = NetUtil.getLocalIp();
	
	public DefaultRpcClient(Protocal protocal, Serializer defaultSerializer,ExecutorService asyncInvokeExecutor  ,EventExecutor eventExecutor, 
			List<InvokeFilter> invokeFilters) {
		this.defaultSerializer = defaultSerializer;
		this.messageChannel = new DefaultMessageChannel(eventExecutor.getChildGroup(), protocal);
		this.invokeFilterChain = new ClientInvokeFilterChain(this.messageChannel , invokeFilters);
		this.registry = protocal.getRegistry();
		this.asyncInvokeExecutor = asyncInvokeExecutor;
	}
	
	@Override
	public Object invoke(Invoke invoke) {
		
		invoke.setId(NEXT_MSG_ID.incrementAndGet());
		
		if(invoke.getSerializer() == null) {
			invoke.setSerializer(defaultSerializer);
		}
		if(invoke.getRegistry() == null) {
			invoke.setRegistry(registry);
		}
		
		invoke.setAsyncInvokeExecutor(asyncInvokeExecutor);

		DefaultResultFuture future = new DefaultResultFuture(invoke);
		invoke.setResultFuture(future);
		
		if(invoke.isAsync()) {
			this.invokeFilterChain.beginInvoke(invoke);
			return future;
		}
		else {
			return this.invokeFilterChain.invoke(invoke);
		}
	}

	@Override
	public void register(ServiceConsumer consumer) {
	
		this.start();
		if(this.registry != null) {
			consumer.setAddress(localIp);
			this.registry.subscribe(consumer);
		}
		
	}

	@Override
	public void close() {
		lifeCycleSupport.close(new Runnable() {
			@Override
			public void run() {
				if(DefaultRpcClient.this.registry != null) {
					DefaultRpcClient.this.registry.close();
				}
				DefaultRpcClient.this.messageChannel.close();
				DefaultRpcClient.this.asyncInvokeExecutor.shutdown();
			}
		});
	}

	@Override
	public void start() {
		lifeCycleSupport.start(new Runnable() {
			@Override
			public void run() {
				if(DefaultRpcClient.this.registry != null) {
					DefaultRpcClient.this.registry.start();
				}
			}
		});
	}

}
