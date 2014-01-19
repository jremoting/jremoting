package com.github.jremoting.protocal;

import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;

public class JRemotingProtocal implements Protocal {

	@Override
	public boolean writeRequest(Invocation invocation, ChannelBuffer buffer) {
		return false;
	}

	@Override
	public InvocationResult readResponse(InvocationHolder holder, ChannelBuffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public boolean writeResponse(InvocationResult invocationResult,
			ChannelBuffer buffer) {
		return false;
		
	}

	@Override
	public Invocation readRequest(ChannelBuffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}


	
	
/*	private static class ProtocalRequestFormat {
		public  static final short magic = (short) 0xbabe;
		// messageType[0:request,1:response,2 ping, 3 pong], invokeType[0:twoway, 1:onway] 
		public static final byte messageType= 0; messageType(4) + invokeType(4) + 
		public static final byte serializeType = 0; 
		public static final long  requestId = 0;
		public static final int bodyLength = 0;
		
	}
	
	private static class ProtocalResponseFormat {
		public static final short magic = (short) 0xbabe;
		// messageType[0:request,1:response,2 ping, 3 pong], invokeType[0:twoway, 1:onway] 
		public static final byte messageType= 0; messageType(4) + invokeType(4) + 
		public static final byte status = 0; 
		public static final long  requestId = 0;
		public static final int bodyLength = 0;
	}*/

}
