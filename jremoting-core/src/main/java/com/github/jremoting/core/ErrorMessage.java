package com.github.jremoting.core;

public class ErrorMessage extends InvokeResult {

	public ErrorMessage(String errorMsg,long msgId ,Protocal protocal, int serializerId) {
		super(errorMsg, msgId,protocal, serializerId);
	}

	public String getErrorMsg() {
		return (String) this.getResult();
	}
	
	public static final ErrorMessage NEED_MORE_INPUT_MESSAGE = new ErrorMessage(null, 0, null, 0);
}
