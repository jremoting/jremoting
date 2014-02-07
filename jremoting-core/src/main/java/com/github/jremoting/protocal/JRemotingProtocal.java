package com.github.jremoting.protocal;

import com.github.jremoting.core.ErrorMessage;
import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.Message;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.core.SerializerUtil;
import com.github.jremoting.exception.ProtocalException;
import com.github.jremoting.io.ByteBuffer;
import com.github.jremoting.io.ByteBufferInputStream;
import com.github.jremoting.io.ByteBufferOutputStream;
import com.github.jremoting.io.ObjectInput;
import com.github.jremoting.io.ObjectOutput;
import com.github.jremoting.util.ReflectionUtil;

public class JRemotingProtocal implements Protocal {
	
	public static final String NAME = "jremoting";
	
	public static final short MAGIC = (short) 0xBABE; //1011101010111110
	
	 // header length.
    protected static final int      HEAD_LENGTH      = 16;
    // message flag.
    protected static final int     FLAG_REQUEST       =  0x80; //10000000

    protected static final int     FLAG_TWOWAY        =  0x40; //01000000

    protected static final int     FLAG_EVENT     =  0x20;	  //00100000

    protected static final int      SERIALIZATION_MASK = 0x1f;		  //00011111
    
    
    protected static final int      STATUS_ERROR = 50;
    protected static final int      STATUS_OK = 20;
    
    private static final String NULL = "NULL";
    

	private final Serializer[] serializers;
	
	public JRemotingProtocal(Serializer[] serializers) {
		this.serializers = SerializerUtil.reindex(serializers);
	}
	
	@Override
	public void encode(Message msg, ByteBuffer buffer) throws ProtocalException {
		
		boolean isHeartbeatMessage = msg instanceof HeartbeatMessage;
		boolean isTwoWay = msg.isTwoWay();
		boolean isRequest = msg instanceof Invoke;
		boolean isErrorMsg = msg instanceof ErrorMessage;
		
		
		int flag = (isRequest ? FLAG_REQUEST : 0)
				| (isTwoWay ? FLAG_TWOWAY : 0) 
				| (isHeartbeatMessage ? FLAG_EVENT : 0)
				| msg.getSerializerId();
		
		int status = isErrorMsg ? STATUS_ERROR : STATUS_OK;
		
		//encode head
		buffer.writeShort(MAGIC);
		buffer.writeByte(flag);
		buffer.writeByte(status);
		buffer.writeLong(msg.getId());
		
		int bodyLengthOffset = buffer.writerIndex();
		buffer.writeInt(0);
		
		if(isHeartbeatMessage) {
			return;
		}
		
		ObjectOutput output =  serializers[msg.getSerializerId()].createObjectOutput(new ByteBufferOutputStream(buffer));
		
		if(isErrorMsg) {
			ErrorMessage errorMessage = (ErrorMessage)msg;
			output.writeString(errorMessage.getErrorMsg());
		}
		else if(isRequest) {
			Invoke invoke = (Invoke)msg;
			encodeRequestBody(invoke, output);
		}
		else {
			InvokeResult invokeResult = (InvokeResult)msg;
			if(invokeResult.getResult() == null) {
				output.writeString(NULL);
			}
			else {
				output.writeString(invokeResult.getResult().getClass().getName());
				output.writeObject(invokeResult.getResult());
			}
		}
		
		output.close();
		
		//write body length
		int bodyLength = buffer.writerIndex() - bodyLengthOffset - 4;
		int savedWriterIndex = buffer.writerIndex();
		buffer.writerIndex(bodyLengthOffset);
		buffer.writeInt(bodyLength);
		buffer.writerIndex(savedWriterIndex);
		
	}
	
	private void encodeRequestBody(Invoke invoke, ObjectOutput output) {
		
		int argLength = invoke.getArgs() == null ? 0 : invoke.getArgs().length;
		output.writeString(invoke.getServiceName());
		output.writeString(invoke.getServiceVersion());
		output.writeString(invoke.getMethodName());
		output.writeInt(argLength);

		if(argLength == 0) {
			return;
		}
		
		for (int i= 0; i <  argLength; i++) {
			output.writeString(invoke.getParameterTypes()[i].getName());
			output.writeObject(invoke.getArgs()[i]);
		}
	}


	@Override
	public Message decode(ByteBuffer buffer) throws ProtocalException {
		if(buffer.readableBytes() < HEAD_LENGTH) {
			return ErrorMessage.NEED_MORE_INPUT_MESSAGE;
		}
		buffer.markReaderIndex();
		
		short magic = buffer.readShort();
		if(magic != MAGIC) {
			buffer.resetReaderIndex();
			return null;
		}
		
		int flag = buffer.readByte();
		int status = buffer.readByte();
		long msgId = buffer.readLong();
		int bodyLength = buffer.readInt();
		
		if(buffer.readableBytes() < bodyLength) {
			buffer.resetReaderIndex();
			return ErrorMessage.NEED_MORE_INPUT_MESSAGE;
		}
		
		boolean isHeartbeat = (flag & FLAG_EVENT) > 0;
		boolean isRequest = (flag & FLAG_REQUEST) > 0;
		boolean isTwoWay = (flag & FLAG_TWOWAY) > 0 ;
		int serializerId = (flag & SERIALIZATION_MASK);
		boolean isErrorMsg = (status != STATUS_OK);
		
		if(isHeartbeat) {
			return new HeartbeatMessage(isTwoWay, this, serializerId);
		}
		
		//decode body
		int bodyEndOffset = buffer.readerIndex() + bodyLength;
		
		try {
			
			ObjectInput input = serializers[serializerId].createObjectInput(new ByteBufferInputStream(buffer, bodyLength));
			Message msg = null;
			if(isErrorMsg) {
				String errorMsg = input.readString();
			    msg =  new ErrorMessage(errorMsg, msgId ,this, serializerId);
			}
			else if(isRequest) {
				msg =  decodeRequestBody(msgId,serializerId ,input);
			}
			else {
				Object result = null;
				String resultClassName = input.readString();
				if(!NULL.equals(resultClassName)) {
					Class<?> resultClass = ReflectionUtil.findClass(resultClassName);
				    result = input.readObject(resultClass);
				}
				
				msg= new InvokeResult(result, msgId,this, serializerId);
			}
			input.close();
			return msg;
			
			
		} catch (Exception e) {
			throw new ProtocalException("decode msg  failed!", null ,e);
		}
		finally {
			buffer.readerIndex(bodyEndOffset);
		}
	
	}
	


	private Invoke decodeRequestBody(long msgId, int serializerId,ObjectInput input) throws ClassNotFoundException {
		
		String serviceName = input.readString();
		String serviceVersion =  input.readString();
		String methodName =  input.readString();
		int argsLength = input.readInt();
		
		
		if(argsLength == 0) {
			Invoke invoke = new Invoke(serviceName, serviceVersion, methodName,null, null, null, this, serializerId);
			invoke.setId(msgId);
			return invoke;
		}
		
		Class<?>[] parameterTypes = new Class[argsLength];
		Object[]  args = new Object[argsLength];
		
		for (int i = 0; i < argsLength; i++) {
			String parameterClassName = input.readString();
			parameterTypes[i] = ReflectionUtil.findClass(parameterClassName);
			args[i] = input.readObject(parameterTypes[i]);
		}

		Invoke invoke = new Invoke(serviceName, serviceVersion, methodName, args,parameterTypes ,null,
				this, serializerId);
		invoke.setId(msgId);
		return invoke;
	}
	
}
