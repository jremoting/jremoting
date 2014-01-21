package com.github.jremoting.protocal;

import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;

public class Protocals  {
	private final Protocal[]  protocals;
	public Protocals(Protocal[]  protocals) {
		this.protocals = protocals;
	}

	public InvocationResult readResponse(InvocationHolder holder,
			ChannelBuffer buffer) {
		for (Protocal protocal : protocals) {
			InvocationResult result = protocal.readResponse(holder, buffer);
			if(result != null) {
				return result;
			}
		}
		return null;
	}

	public Invocation readRequest(ChannelBuffer buffer) {
		for (Protocal protocal : protocals) {
			Invocation invocation = protocal.readRequest(buffer);
			if(invocation != null) {
				return invocation;
			}
		}
		return null;
	}

}
