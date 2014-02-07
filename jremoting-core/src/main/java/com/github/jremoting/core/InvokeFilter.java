package com.github.jremoting.core;


public interface InvokeFilter {
	
	InvokeFilter getNext();
	
	void setNext(InvokeFilter next);

	public Object invoke(Invoke invoke);
	
}