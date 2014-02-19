package com.github.jremoting.exception;


public class SerializeException extends RemotingException {
	private static final long serialVersionUID = -3029816928481543231L;

	public SerializeException(String msg, Throwable throwable) {
		super(msg, throwable);
	}
	
	public SerializeException(String msg) {
		super(msg);
	}

}
