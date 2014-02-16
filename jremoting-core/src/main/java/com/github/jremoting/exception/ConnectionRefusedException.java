package com.github.jremoting.exception;


public class ConnectionRefusedException extends FailoverableException {

	private static final long serialVersionUID = -5593371872289925895L;

	public ConnectionRefusedException(String msg, Throwable th) {
		super(msg, th);
	}
	public ConnectionRefusedException(String msg) {
		super(msg);
	}

}
