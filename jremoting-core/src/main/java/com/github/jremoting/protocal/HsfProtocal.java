package com.github.jremoting.protocal;

import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.exception.RpcProtocalException;

public class HsfProtocal implements Protocal {

	@Override
	public Ping getPing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pong getPong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeRequest(Invocation invocation, ChannelBuffer buffer)
			throws RpcProtocalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InvocationResult readResponse(InvocationHolder holder,
			ChannelBuffer buffer) throws RpcProtocalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Invocation readRequest(ChannelBuffer buffer)
			throws RpcProtocalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeResponse(InvocationResult invocationResult,
			ChannelBuffer buffer) throws RpcProtocalException {
		// TODO Auto-generated method stub
		
	}

}
