package com.github.jremoting.remoting;

import io.netty.channel.EventLoopGroup;

import java.util.List;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

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

public class DefaultRpcClient implements RpcClient,  ApplicationListener<ApplicationEvent> {
	
	private final Serializer defaultSerializer;
	private final ClientInvokeFilterChain invokeFilterChain;
	private final ServiceRegistry registry;
	private final MessageChannel messageChannel;
	
	public DefaultRpcClient(Protocal protocal, Serializer defaultSerializer,EventLoopGroup eventLoopGroup, 
			List<InvokeFilter> invokeFilters) {
		this.defaultSerializer = defaultSerializer;
		this.messageChannel = new DefaultMessageChannel(eventLoopGroup, protocal);
		this.invokeFilterChain = new ClientInvokeFilterChain(this.messageChannel , invokeFilters);
		this.registry = protocal.getRegistry();
	}
	
	@Override
	public Object invoke(Invoke invoke) {
		if(invoke.getSerializer() == null) {
			invoke.setSerializer(defaultSerializer);
		}
		if(invoke.getRegistry() == null) {
			invoke.setRegistry(registry);
		}
		return this.invokeFilterChain.invoke(invoke);
	}

	@Override
	public void register(ServiceParticipantInfo consumerInfo) {
		if(consumerInfo.getType() != ParticipantType.CONSUMER) {
			throw new IllegalArgumentException("can only register consumer info");
		}	
		this.registry.registerParticipant(consumerInfo);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof ContextRefreshedEvent) {
			this.registry.start();
		}
		else if (event instanceof ContextClosedEvent) {
			this.registry.close();
			this.messageChannel.close();
		}
	}

}
