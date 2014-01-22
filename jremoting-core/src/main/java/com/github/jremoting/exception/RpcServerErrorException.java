package com.github.jremoting.exception;

public class RpcServerErrorException extends RpcException {

	private static final long serialVersionUID = -8028278893390184262L;

	public RpcServerErrorException(String msg) {
		super(msg);
	}
	public RpcServerErrorException(Throwable throwable) {
		super(throwable);
	}

}
