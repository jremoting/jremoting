package com.github.jremoting.remoting;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.invoke.ClientInvokeFilterChain;
import com.github.jremoting.util.LifeCycleSupport;
import com.github.jremoting.util.concurrent.EventExecutor;

public class DefaultRpcClient implements RpcClient {
	
	private final Serializer defaultSerializer;
	private final ClientInvokeFilterChain invokeFilterChain;
	private final ServiceRegistry registry;
	private final MessageChannel messageChannel;
	private final ExecutorService callbackExecutor;
	private final LifeCycleSupport lifeCycleSupport = new LifeCycleSupport();
	private final static AtomicLong NEXT_MSG_ID = new AtomicLong(0);
	
	public DefaultRpcClient(Protocal protocal, Serializer defaultSerializer,ExecutorService callbackExecutor  ,EventExecutor eventExecutor, 
			List<InvokeFilter> invokeFilters) {
		this.defaultSerializer = defaultSerializer;
		this.messageChannel = new DefaultMessageChannel(eventExecutor.getChildGroup(), protocal);
		this.invokeFilterChain = new ClientInvokeFilterChain(this.messageChannel , invokeFilters);
		this.registry = protocal.getRegistry();
		this.callbackExecutor = callbackExecutor;
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
		if(invoke.getCallbackExecutor() == null) {
			invoke.setCallbackExecutor(callbackExecutor);
		}
		
		if(invoke.isAsync()) {
			return this.invokeFilterChain.beginInvoke(invoke);
		}
		else {
			return this.invokeFilterChain.invoke(invoke);
		}
		
	}

	@Override
	public void register(ServiceParticipantInfo consumerInfo) {
		if(consumerInfo.getType() != ParticipantType.CONSUMER) {
			throw new IllegalArgumentException("can only register consumer info");
		}	
		this.start();
		this.registry.registerParticipant(consumerInfo);
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
				DefaultRpcClient.this.callbackExecutor.shutdown();
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
