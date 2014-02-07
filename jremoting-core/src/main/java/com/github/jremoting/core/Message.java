package com.github.jremoting.core;

public abstract class Message {
	private final boolean isTwoWay;
	private  Protocal protocal;
	private  Serializer serializer;
	private String remoteAddress;
	private long id;
	
	public Message(boolean isTwoWay , Protocal protocal, Serializer serializer) {
		this.isTwoWay = isTwoWay;
		this.protocal = protocal;
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

	public Protocal getProtocal() {
		return protocal;
	}
	public void setProtocal(Protocal protocal) {
		this.protocal = protocal;
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


}
