package com.github.jremoting.exception;

public class ProtocalException extends RemotingException {
	private static final long serialVersionUID = -3818545760960210866L;

	public ProtocalException(String msg,Throwable throwable) {
		super(msg, throwable);
	}
}
