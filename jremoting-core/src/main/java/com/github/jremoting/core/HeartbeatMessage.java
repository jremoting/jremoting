package com.github.jremoting.core;

public class HeartbeatMessage extends Message {

	public HeartbeatMessage(boolean isTwoWay,  Serializer serializer) {
		super(isTwoWay, serializer);
	}

}
