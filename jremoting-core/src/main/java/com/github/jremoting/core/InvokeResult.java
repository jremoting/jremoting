package com.github.jremoting.core;

public class InvokeResult extends Message {

	public InvokeResult(Object result,long msgId ,Protocal protocal, int serializerId) {
		super(false, protocal, serializerId);
		this.result = result;
		this.setId(msgId);
	}

	private final Object result;
	
	public Object getResult() {
		return result;
	}
}
