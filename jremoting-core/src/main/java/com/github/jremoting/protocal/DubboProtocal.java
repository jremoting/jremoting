package com.github.jremoting.protocal;

import com.github.jremoting.core.Message;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.exception.ProtocalException;
import com.github.jremoting.io.ByteBuffer;

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

	@Override
	public void encode(Message msg, ByteBuffer buffer) throws ProtocalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message decode(ByteBuffer buffer) throws ProtocalException {
		// TODO Auto-generated method stub
		return null;
	}





	
/*	public void writeRequest(Invoke invoke, ByteBuffer buffer)
			throws ProtocalException {
		boolean isPingRequest = (invoke instanceof Ping);
		byte flag = (byte) ((isPingRequest ? FLAG_EVENT : FLAG_REQUEST) | FLAG_TWOWAY | invoke.getSerializerId()) ;

		buffer.writeShort(MAGIC);
		buffer.writeByte(flag);
		buffer.writeByte(0);
		buffer.writeLong(invoke.getId());
		int bodyLengthOffset = buffer.writerIndex();
		buffer.writeInt(0);
		if(isPingRequest) {
			return;
		}
		
		
	}*/


}
