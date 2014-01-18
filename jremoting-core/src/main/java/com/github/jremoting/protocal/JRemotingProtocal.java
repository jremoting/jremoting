package com.github.jremoting.protocal;

import com.github.jremoting.core.Channel;
import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Protocol;

public class JRemotingProtocal implements Protocol {

	@Override
	public int getMagic() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeRequest(ChannelBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object readResponse(Channel channel, ChannelBuffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private static class ProtocalRequestFormat {
		public  static final short magic = (short) 0xabcd;
		// messageType[0:request,1:response,2 ping, 3 pong], invokeType[0:twoway, 1:onway] 
		public static final byte messageType= 0; /*messageType(4) + invokeType(4) + */
		public static final byte serializeType = 0; 
		public static final long  requestId = 0;
		public static final int bodyLength = 0;
		
	}
	
	private static class ProtocalResponseFormat {
		public static final short magic = (short) 0xabcd;
		// messageType[0:request,1:response,2 ping, 3 pong], invokeType[0:twoway, 1:onway] 
		public static final byte messageType= 0; /*messageType(4) + invokeType(4) + */
		public static final byte status = 0; 
		public static final long  requestId = 0;
		public static final int bodyLength = 0;
	}

}
