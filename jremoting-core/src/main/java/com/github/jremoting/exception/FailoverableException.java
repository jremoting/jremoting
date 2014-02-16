package com.github.jremoting.exception;

public abstract class FailoverableException extends RemotingException {
	private static final long serialVersionUID = 8265613957911302883L;

	public FailoverableException(){}
	public FailoverableException(String msg) {
		super(msg);
	}
	public FailoverableException(String msg, Throwable throwable) {
		super(msg,throwable);
	}
	public FailoverableException(Throwable throwable) {
		super(throwable);
	}
}
