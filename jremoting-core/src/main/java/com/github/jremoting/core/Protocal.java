package com.github.jremoting.core;

import com.github.jremoting.exception.RpcProtocalException;

public interface Protocal {
	
	Ping getPing();
	
	Pong getPong();
	
	void writeRequest(Invocation invocation, ChannelBuffer buffer) throws RpcProtocalException;
	InvocationResult readResponse(InvocationHolder holder, ChannelBuffer buffer) throws RpcProtocalException;
	
	Invocation readRequest(ChannelBuffer buffer) throws RpcProtocalException;
	void writeResponse(InvocationResult invocationResult, ChannelBuffer buffer) throws RpcProtocalException;
	
	public static class Pong extends InvocationResult {
		public Pong(Ping ping){
			super(null, ping);
		}
	}
	
	public static class Ping extends DefaultInvocation {
		public Ping(Protocal protocal) {
			super(null, null, null, null,null, null,protocal,0);
		}
	}
	
}
