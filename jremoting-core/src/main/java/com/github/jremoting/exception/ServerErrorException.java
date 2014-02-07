package com.github.jremoting.exception;


public class ServerErrorException extends RemotingException {

	private static final long serialVersionUID = -8028278893390184262L;

	public ServerErrorException(String msg) {
		super(msg);
	}
	public ServerErrorException(Throwable throwable) {
		super(throwable);
	}

}
