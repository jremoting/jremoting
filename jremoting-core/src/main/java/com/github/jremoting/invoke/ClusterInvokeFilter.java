package com.github.jremoting.invoke;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.exception.RemotingException;

public class ClusterInvokeFilter extends AbstractInvokeFilter {
	
	private final ServiceRegistry registry;
	
	private AtomicLong nextIndex = new AtomicLong(0);
	
	public ClusterInvokeFilter(ServiceRegistry registry) {
		this.registry = registry;
	}

	@Override
	public Object invoke(Invoke invoke) {
		List<ServiceParticipantInfo> providers = registry.getProviders(invoke.getServiceName());
		
		if(providers == null || providers.isEmpty()) {
			throw new RemotingException("no provier for service " + invoke.getServiceName());
		}
		ServiceParticipantInfo provider = providers.get((int)(nextIndex.getAndIncrement() % providers.size()));
		
		invoke.setRemoteAddress(provider.getAddress());
		
		return getNext().invoke(invoke);
	}

}
