package com.github.jremoting.registry;

import java.util.List;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceParticipantInfo;

public interface ProviderSelector {
	
	public List<ServiceParticipantInfo> select(List<ServiceParticipantInfo> providers, Invoke invoke);
	
}
