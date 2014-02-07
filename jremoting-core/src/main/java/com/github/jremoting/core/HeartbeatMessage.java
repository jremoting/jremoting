package com.github.jremoting.core;

public class HeartbeatMessage extends Message {

	public HeartbeatMessage(boolean isTwoWay, Protocal protocal, int serializerId) {
		super(isTwoWay, protocal, serializerId);
	}

}
