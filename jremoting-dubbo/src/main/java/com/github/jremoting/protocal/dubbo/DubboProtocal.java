package com.github.jremoting.protocal.dubbo;

import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Protocal.Ping;
import com.github.jremoting.core.Protocal.Pong;
import com.github.jremoting.exception.RpcProtocalException;

public class DubboProtocal implements Protocal {
	 // header length.
    protected static final int      HEADER_LENGTH      = 16;

    // magic header.
    protected static final short    MAGIC              = (short) 0xdabb;
    // message flag.
    protected static final byte     FLAG_REQUEST       = (byte) 0x80; //10000000

    protected static final byte     FLAG_TWOWAY        = (byte) 0x40; //01000000

    protected static final byte     FLAG_EVENT     = (byte) 0x20;	  //00100000

    protected static final int      SERIALIZATION_MASK = 0x1f;		  //00011111

	private final Ping ping = new Ping(this);
	private final Pong pong = new Pong(ping);
	
	@Override
	public Ping getPing() {
		return ping;
	}

	@Override
	public Pong getPong() {
		return pong;
	}

	@Override
	public void writeRequest(Invocation invocation, ChannelBuffer buffer)
			throws RpcProtocalException {
		
		boolean isPingRequest = (invocation instanceof Ping);
		byte flag = (byte) ((isPingRequest ? FLAG_EVENT : FLAG_REQUEST) | FLAG_TWOWAY | invocation.getSerializerId()) ;

		buffer.writeShort(MAGIC);
		buffer.writeByte(flag);
		buffer.writeByte(0);
		buffer.writeLong(invocation.getInvocationId());
		int bodyLengthOffset = buffer.writerIndex();
		buffer.writeInt(0);
		if(isPingRequest) {
			return;
		}
		
		
		
		
		
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
