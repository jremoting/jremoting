package com.github.jremoting.core;

public class HeartbeatMessage extends Message {

	public HeartbeatMessage(boolean isTwoWay, Protocal protocal, Serializer serializer) {
		super(isTwoWay, protocal, serializer);
	}

}
