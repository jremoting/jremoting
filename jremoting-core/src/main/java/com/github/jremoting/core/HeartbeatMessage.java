package com.github.jremoting.core;

public class HeartbeatMessage extends Message {

	private HeartbeatMessage(boolean isTwoWay) {
		super(isTwoWay,null);
	}
	
	public static final HeartbeatMessage PING = new HeartbeatMessage(true);
	public static final HeartbeatMessage PONG = new HeartbeatMessage(false);

}
