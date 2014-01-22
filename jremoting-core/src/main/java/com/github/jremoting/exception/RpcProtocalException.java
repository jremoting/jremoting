package com.github.jremoting.exception;

import com.github.jremoting.core.Invocation;

public class RpcProtocalException extends RpcServerErrorException {
	private static final long serialVersionUID = -3818545760960210866L;
	private  Invocation badInvocation = null;

	public RpcProtocalException(String msg,Invocation badInvocation ,Throwable throwable) {
		super(msg);
		this.badInvocation = badInvocation;
	}

	public Invocation getBadInvocation() {
		return badInvocation;
	}
	
}
