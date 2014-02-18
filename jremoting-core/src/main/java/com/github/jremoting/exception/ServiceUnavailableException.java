package com.github.jremoting.exception;

public class ServiceUnavailableException extends FailoverableException {
	private static final long serialVersionUID = 7864138837953515051L;
	
	public ServiceUnavailableException(String msg) {
		super(msg);
	}
}
