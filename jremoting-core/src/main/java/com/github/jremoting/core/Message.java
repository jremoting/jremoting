package com.github.jremoting.core;

public abstract class Message {
	private final boolean isTwoWay;
	private final Protocal protocal;
	private final int serializerId;
	
	private String remoteAddress;
	private long id;
	
	public Message(boolean isTwoWay , Protocal protocal, int serializerId) {
		this.isTwoWay = isTwoWay;
		this.protocal = protocal;
		this.serializerId = serializerId;
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

	public Protocal getProtocal() {
		return protocal;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSerializerId() {
		return serializerId;
	}
}
