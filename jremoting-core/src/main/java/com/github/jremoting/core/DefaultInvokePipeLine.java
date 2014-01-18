package com.github.jremoting.core;

public class DefaultInvokePipeLine implements InvokePipeline {
	
	private final InvokeFilter[] filters;
	
	public DefaultInvokePipeLine(InvokeFilter[] filters) {
		this.filters = filters;
	}
	
	private class DefaultInvokeChain implements InvokeChain {

		private int currentFilterIndex = 0;
		
		@Override
		public Object invoke(Invocation invocation) {

			int index = currentFilterIndex++;

			return filters[index].invoke(invocation, this);
		}
	}

	@Override
	public Object invoke(Invocation invocation) {
		InvokeChain chain =  new DefaultInvokeChain();
		return chain.invoke(invocation);
	}

}
