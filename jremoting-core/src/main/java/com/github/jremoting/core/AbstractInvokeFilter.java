package com.github.jremoting.core;

public abstract  class AbstractInvokeFilter implements InvokeFilter {
	
	private  InvokeFilter next;
	
	private InvokeFilter prev;
	
	public InvokeFilter getNext() {
		return next;
	}
	
	public void setNext(InvokeFilter next) {
		this.next = next;
	}

	@Override
	public Object invoke(Invoke invoke) {
		return getNext().invoke(invoke);
	}

	@Override
	public void beginInvoke(Invoke invoke) {
		 getNext().beginInvoke(invoke);
	}

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		getPrev().endInvoke(invoke, result);
	}

	@Override
	public void setPrev(InvokeFilter prev) {
		this.prev = prev;
	}

	@Override
	public InvokeFilter getPrev() {
		return this.prev;
	}
}
