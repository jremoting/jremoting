package com.github.jremoting.exception;

public class RemotingException extends RuntimeException {
	private static final long serialVersionUID = 6362765992124030855L;
	
	public RemotingException(){}
	public RemotingException(String msg) {
		super(msg);
	}
	public RemotingException(String msg, Throwable throwable) {
		super(msg,throwable);
	}
	public RemotingException(Throwable throwable) {
		super(throwable);
	}
	
	

}
