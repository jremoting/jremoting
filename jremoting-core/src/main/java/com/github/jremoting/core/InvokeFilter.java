package com.github.jremoting.core;


public interface InvokeFilter {
	
	InvokeFilter getNext();
	
	void setNext(InvokeFilter next);
	
	void setPrev(InvokeFilter prev);
	
	InvokeFilter getPrev();

	Object invoke(Invoke invoke);
	
	void beginInvoke(Invoke invoke);
	
	void endInvoke(Invoke invoke, Object result);
	
}