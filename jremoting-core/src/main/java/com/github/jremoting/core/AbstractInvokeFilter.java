package com.github.jremoting.core;

public abstract class AbstractInvokeFilter implements InvokeFilter {
	
	private  InvokeFilter next;
	
	public InvokeFilter getNext() {
		return next;
	}
	
	public void setNext(InvokeFilter next) {
		this.next = next;
	}
}
