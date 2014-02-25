package com.github.jremoting.tps;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryListener;

public class TpsInvokeFilter extends AbstractInvokeFilter implements RegistryListener {
	
	public TpsInvokeFilter(Registry registry) {
		registry.addListener(this);
	}
	
	@Override
	public Object invoke(Invoke invoke) {
		return getNext().invoke(invoke);
	}

	@Override
	public Object beginInvoke(Invoke invoke) {
		return getNext().beginInvoke(invoke);
	}

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		getPrev().endInvoke(invoke, result);
	}

	@Override
	public void onEvent(RegistryEvent event) {
		
	}

}
