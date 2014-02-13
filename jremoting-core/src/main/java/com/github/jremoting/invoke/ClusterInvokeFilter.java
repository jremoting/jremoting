package com.github.jremoting.invoke;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.exception.RemotingException;

public class ClusterInvokeFilter extends AbstractInvokeFilter {
	

	
	private AtomicLong nextIndex = new AtomicLong(0);

	@Override
	public Object invoke(Invoke invoke) {
		if(invoke.getRemoteAddress() != null) {
			return getNext().invoke(invoke);
		}
		
		List<ServiceParticipantInfo> providers = invoke.getRegistry().getProviders(invoke.getServiceName());
		
		if(providers == null || providers.isEmpty()) {
			throw new RemotingException("no provier for service " + invoke.getServiceName());
		}
		ServiceParticipantInfo provider = providers.get((int)(nextIndex.getAndIncrement() % providers.size()));
		
		invoke.setRemoteAddress(provider.getAddress());
		
		return getNext().invoke(invoke);
	}
}
