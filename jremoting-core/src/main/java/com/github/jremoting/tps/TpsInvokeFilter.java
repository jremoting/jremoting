package com.github.jremoting.tps;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.SystemProperties;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryEvent.EventType;
import com.github.jremoting.core.RegistryListener;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.tps.TpsRules.TpsRule;

public class TpsInvokeFilter extends AbstractInvokeFilter implements RegistryListener {
	
	
	private String configFileName = "tps.rule";
	
	private ConcurrentHashMap<String, TpsRule> tpsRules = new ConcurrentHashMap<String, TpsRule>();
	
	private volatile TpsRules cachedTpsRule;

	private final Registry registry;

	
	public TpsInvokeFilter(Registry registry) {
		this.registry = registry;
		registry.addListener(this);
	}
	
	@Override
	public Object invoke(Invoke invoke) {
		
		checkTpsRule(invoke);
	
		return getNext().invoke(invoke);
	}

	private void checkTpsRule(Invoke invoke) {
		if(cachedTpsRule == null) {
			String config = this.registry.getAppConfig(SystemProperties.APP_NAME, configFileName);
			cachedTpsRule = TpsRules.parseJsonRule(config);
		}
		
		TpsRule tpsRule = tpsRules.get(invoke);
		
		if(tpsRule == null) {
			tpsRule = cachedTpsRule.getTpsRule(invoke);
			tpsRules.put(invoke.getServiceName(), tpsRule);
		}
		
		if(!tpsRule.check(invoke)) {
			throw new RemotingException("tps error");
		}
	}

	@Override
	public Object beginInvoke(Invoke invoke) {
		checkTpsRule(invoke);
		return getNext().beginInvoke(invoke);
	}

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		getPrev().endInvoke(invoke, result);
	}

	@Override
	public void onEvent(RegistryEvent event) {
		if(event.getType() == EventType.APP_CONFIG_CHANGED && configFileName.equals(event.getFileName())) {
			TpsRules newRules = TpsRules.parseJsonRule(event.getNewContent());
			cachedTpsRule = newRules;
			tpsRules.clear();
		}
	}

}
