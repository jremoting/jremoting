package com.github.jremoting.core;

public class DefaultInvocation implements Invocation {

	private final Object[] args;
	
	public DefaultInvocation(Object[] args) {
		this.args = args;
	}

	@Override
	public Object[] getArgs() {
		return args;
	}
}
