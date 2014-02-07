package com.github.jremoting.exception;


public class ConnectFailedException extends RemotingException {

	private static final long serialVersionUID = -5593371872289925895L;

	public ConnectFailedException(String msg, Throwable th) {
		super(msg, th);
	}
	public ConnectFailedException(String msg) {
		super(msg);
	}

}
