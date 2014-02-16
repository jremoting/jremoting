package com.github.jremoting.core;

import com.github.jremoting.util.concurrent.ListenableFuture;


public interface InvokeFilter {
	
	InvokeFilter getNext();
	
	void setNext(InvokeFilter next);
	
	void setPrev(InvokeFilter prev);
	
	InvokeFilter getPrev();

	Object invoke(Invoke invoke);
	
	ListenableFuture<Object> beginInvoke(Invoke invoke);
	
	void endInvoke(Invoke invoke, Object result);
	
}