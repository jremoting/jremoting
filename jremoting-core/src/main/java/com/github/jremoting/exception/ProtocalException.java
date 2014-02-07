package com.github.jremoting.exception;

import com.github.jremoting.core.Invoke;

public class ProtocalException extends ServerErrorException {
	private static final long serialVersionUID = -3818545760960210866L;
	private  Invoke badInvoke = null;

	public ProtocalException(String msg,Invoke badInvoke ,Throwable throwable) {
		super(msg);
		this.badInvoke= badInvoke;
	}

	public Invoke getBadInvoke() {
		return badInvoke;
	}
	
}
