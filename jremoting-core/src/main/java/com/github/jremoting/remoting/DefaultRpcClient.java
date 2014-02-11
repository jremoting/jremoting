package com.github.jremoting.remoting;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.invoke.ClientInvokeFilterChain;

public class DefaultRpcClient implements RpcClient,  ApplicationListener<ApplicationEvent> {
	
	private final Protocal protocal;
	private final Serializer serializer;
	private final ClientInvokeFilterChain invokeFilterChain;
	private final ServiceRegistry registry;
	
	public DefaultRpcClient(Protocal protocal, Serializer serializer,
			ClientInvokeFilterChain invokeFilterChain,
			ServiceRegistry registry) {
		this.protocal = protocal;
		this.serializer = serializer;
		this.invokeFilterChain = invokeFilterChain;
		this.registry = registry;
	}
	
	@Override
	public Object invoke(Invoke invoke) {
		if(invoke.getProtocal() == null) {
			invoke.setProtocal(protocal);
		}
		if(invoke.getSerializer() == null) {
			invoke.setSerializer(serializer);
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
			this.invokeFilterChain.getMessageChannel().close();
		}
	}

}
