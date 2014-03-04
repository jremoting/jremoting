package com.github.jremoting.tps;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.SystemProperties;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryEvent.EventType;
import com.github.jremoting.core.RegistryListener;
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
		
		TpsRule tpsRule = beginTpsInvoke(invoke);
		
		Object invokeResult = getNext().invoke(invoke);
		
		tpsRule.endInvoke(invoke);
		
		return invokeResult;
		
	}

	private TpsRule beginTpsInvoke(Invoke invoke) {
		if(cachedTpsRule == null) {
			String config = this.registry.getAppConfig(SystemProperties.APP_NAME, configFileName);
			cachedTpsRule = TpsRules.parseJsonRule(config);
		}
		
		TpsRule tpsRule = tpsRules.get(invoke);
		
		if(tpsRule == null) {
			tpsRule = cachedTpsRule.getTpsRule(invoke);
			tpsRules.put(invoke.getServiceName(), tpsRule);
		}
		
		tpsRule.beginInvoke(invoke);
		
		return tpsRule;
	}



	@Override
	public Object beginInvoke(Invoke invoke) {
		
		TpsRule tpsRule = beginTpsInvoke(invoke);

		invoke.setAsyncContext(CONTEXT_KEY, tpsRule);
		return getNext().beginInvoke(invoke);
	}
	
	private static final String CONTEXT_KEY = TpsInvokeFilter.class.getName();

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		TpsRule tpsRule = (TpsRule) invoke.getAsyncContext(CONTEXT_KEY);
		
		if(tpsRule !=null) {
			tpsRule.endInvoke(invoke);
		}
		
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
