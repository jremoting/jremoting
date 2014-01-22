package com.github.jremoting.exception;

public class RpcConnectFailedException extends RpcException {

	private static final long serialVersionUID = -5593371872289925895L;

	public RpcConnectFailedException(String msg, Throwable th) {
		super(msg, th);
	}
	public RpcConnectFailedException(String msg) {
		super(msg);
	}

}
