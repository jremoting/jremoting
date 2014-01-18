package com.github.jremoting.core;

import java.util.LinkedList;

public class InvokePipeLineBuilder {
	
	private LinkedList<InvokeFilter> filters = new LinkedList<InvokeFilter>();
	
	public void addFirst(InvokeFilter filter){
		filters.addFirst(filter);
	}
	
	public void addLast(InvokeFilter filter){
		filters.addLast(filter);
	}
	
	public InvokePipeline build() {
		
		InvokeFilter[] arrayFilters = (InvokeFilter[]) filters.toArray();
		
		if(arrayFilters.length == 0){
			throw new RpcException("invoke pipeline filters can not be empty!");
		}
		
		if(!(arrayFilters[arrayFilters.length-1] instanceof FinalFilter)) {
			throw new RpcException("last filter in  pipeline must be one of RpcInvoker!");
		}
		
		return new DefaultInvokePipeLine(arrayFilters);
	}
}
