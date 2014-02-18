package com.github.jremoting.core;

public abstract class Message {
	
	public static final Message NEED_MORE = new Message(false, null) {};
	private final boolean isTwoWay;
	private  Serializer serializer;
	private String remoteAddress;
	private long id;
	private long timeout;
	private static final long DEFAULT_TIMEOUT = 60*1000*5; //default timeout 5 mins
	
	public Message(boolean isTwoWay , Serializer serializer) {
		this.isTwoWay = isTwoWay;
		this.setSerializer(serializer);
	}

	public boolean isTwoWay() {
		return isTwoWay;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public void setRemoteAddress(String address) {
		this.remoteAddress = address;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public long getTimeout() {
		return  timeout == 0 ? DEFAULT_TIMEOUT : timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}


}
