package com.github.jremoting.core;

public abstract class Message {
	private final boolean isTwoWay;
	private  Serializer serializer;
	private String remoteAddress;
	private long id;
	private long timeout;
	
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
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}


}
