package com.github.jremoting.exception;

public class RpcException extends RuntimeException {
	private static final long serialVersionUID = 6362765992124030855L;
	
	public RpcException(String msg) {
		super(msg);
	}
	public RpcException(String msg, Throwable throwable) {
		super(msg,throwable);
	}
	public RpcException(Throwable throwable) {
		super(throwable);
	}
	
	

}
