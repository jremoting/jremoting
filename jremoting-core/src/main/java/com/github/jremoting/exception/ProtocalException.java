package com.github.jremoting.exception;

public class ProtocalException extends ServerErrorException {
	private static final long serialVersionUID = -3818545760960210866L;

	private final long msgId;
	public ProtocalException(String msg,Throwable throwable, long msgId) {
		super(msg);
		this.msgId = msgId;
	}
	public long getMsgId() {
		return msgId;
	}
}
