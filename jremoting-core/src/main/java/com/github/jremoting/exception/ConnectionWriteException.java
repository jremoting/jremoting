package com.github.jremoting.exception;

public class ConnectionWriteException extends FailoverableException {
	private static final long serialVersionUID = -279183785586983218L;
	
	public ConnectionWriteException(String msg, Throwable th) {
		super(msg, th);
	}
	public ConnectionWriteException(String msg) {
		super(msg);
	}

}
