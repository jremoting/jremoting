package com.github.jremoting.core;

public interface Protocal {
	
	public static final Ping PING = new Ping();
	public static final Pong PONG = new Pong();

	boolean writeRequest(Invocation invocation, ChannelBuffer buffer);
	InvocationResult readResponse(InvocationHolder holder, ChannelBuffer buffer);
	
	Invocation readRequest(ChannelBuffer buffer);
	boolean writeResponse(Invocation invocation, InvocationResult invocationResult, ChannelBuffer buffer);
	
	public static class Pong extends InvocationResult {
		private Pong(){
			super(0, null);
		}
	}
	
	public static class Ping extends DefaultInvocation {
		private Ping() {
			super(null, null, null, null, null,null,0);
			
		}
	}
	
}
