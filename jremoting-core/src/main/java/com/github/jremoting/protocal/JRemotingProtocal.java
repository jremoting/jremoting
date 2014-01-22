package com.github.jremoting.protocal;

import com.github.jremoting.core.DefaultInvocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.exception.RpcProtocalException;
import com.github.jremoting.exception.RpcServerErrorException;
import com.github.jremoting.serializer.Serializers;
import com.github.jremoting.util.ReflectionUtil;

public class JRemotingProtocal implements Protocal {
	
	public static final String NAME = "jremoting";
	
	public static final short MAGIC = (short) 0xBABE; //1011101010111110
	
	public static final byte MESSAGE_TYPE_MASK = (byte) 0x1F;  //00011111
	public static final byte MESSAGE_REQUEST = 0;
	public static final byte MESSAGE_RESPONSE = 1;
	public static final byte MESSAGE_PING = 2;
	public static final byte MESSAGE_PONG = 3;
	
	
	public static final byte INVOKE_TYPE_MASK = (byte) 0xE0;   //11100000
	public static final byte INVOKE_TWO_WAY = (byte) (0 << 5);
	public static final byte INVOKE_ONE_WAY = (byte) (1 << 5);
	
	public static final int HEAD_LENGTH = 16;
	
	
	public static final byte STATUS_OK = 86;
	public static final byte STATUS_SERVER_ERROR = 50;
	
	private final Ping ping = new Ping(this);
	private final Pong pong = new Pong(ping);

	
	private final Serializer[] serializers;
	
	public JRemotingProtocal(Serializers serializers) {
		this.serializers = serializers.getSerializers();
	}
	

	@Override
	public void writeRequest(Invocation invocation, ChannelBuffer buffer) {
		
		boolean isPingRequest = invocation instanceof Ping;

		buffer.writeShort(MAGIC);

		byte flag = (byte) (0 |( isPingRequest ? MESSAGE_PING : MESSAGE_REQUEST) | INVOKE_TWO_WAY) ;
		
	
		buffer.writeByte(flag);
		buffer.writeByte(invocation.getSerializerId());
		buffer.writeLong(invocation.getInvocationId());
		
		int bodyLengthOffset = buffer.writerIndex();
		buffer.writeInt(0);
		
		if(isPingRequest) {
			return;
		}
		
		Serializer serializer = serializers[invocation.getSerializerId()];
		encodeRequestBody(invocation, buffer, serializer);

		int bodyLength = buffer.writerIndex() - bodyLengthOffset - 4;
		
		int savedWriterIndex = buffer.writerIndex();
		
		buffer.writerIndex(bodyLengthOffset);
		buffer.writeInt(bodyLength);
		buffer.writerIndex(savedWriterIndex);
	}


	private void encodeRequestBody(Invocation invocation, ChannelBuffer buffer,
			Serializer serializer) {
		
		int argLength = invocation.getArgs() == null ? 0 : invocation.getArgs().length;
		
		buffer.writeUTF8(invocation.getServiceName());
		buffer.writeUTF8(invocation.getServiceVersion());
		buffer.writeUTF8(invocation.getMethodName());
		buffer.writeByte(argLength);
		
		if(argLength == 0) {
			return;
		}
		
		for (int i= 0; i <  argLength; i++) {
			buffer.writeUTF8(invocation.getParameterTypes()[i].getName());
		}

		serializer.writeObjects(invocation.getArgs(), buffer);
		
	}

	
	@Override
	public Invocation readRequest(ChannelBuffer buffer) {
		if(buffer.readableBytes() < HEAD_LENGTH) {
			return null;
		}
		buffer.markReaderIndex();
		
		short magic = buffer.readShort();
		if(magic != MAGIC) {
			buffer.resetReaderIndex();
			return null;
		}
		
		byte flag = buffer.readByte();
		
		boolean isPingRequest = (flag & MESSAGE_TYPE_MASK) == MESSAGE_PING;
		byte serializeId = buffer.readByte();
		long invocationId = buffer.readLong();
		int bodyLength = buffer.readInt();
		
		if(isPingRequest) {
			return ping;
		}
		
		if(buffer.readableBytes() < bodyLength) {
			buffer.resetReaderIndex();
			return null;
		}
		
		int bodyEndOffset = buffer.readerIndex() + bodyLength;

		try {
			ChannelBuffer bodyBuffer = buffer.slice(buffer.readerIndex(), bodyLength);
		    Serializer serializer = serializers[serializeId];
			return  decodeRequestBody(invocationId,bodyBuffer, serializer);
		} catch (Exception e) {
			Invocation badInvocation = new DefaultInvocation(null, null, null, null,
					null, null, this, serializeId);
			badInvocation.setInvocationId(invocationId);
			
			throw new RpcProtocalException("invalid request body !", badInvocation ,e);
		}
		finally{
			buffer.readerIndex(bodyEndOffset);;
		}
	}


	private Invocation decodeRequestBody(long invocationId,
			ChannelBuffer buffer, Serializer serializer)
			throws ClassNotFoundException {
		String serviceName = buffer.readUTF8();
		String serviceVersion =  buffer.readUTF8();
		String methodName =  buffer.readUTF8();
		int argsLength = buffer.readByte();
		
		
		if(argsLength == 0) {
			Invocation invocation = new DefaultInvocation(serviceName, serviceVersion, methodName,null, null, null, this, serializer.getId());
			invocation.setInvocationId(invocationId);
			return invocation;
		}
		
		Class<?>[] parameterTypes = new Class[argsLength];
		
		for (int i = 0; i < argsLength; i++) {
			String parameterClassName = buffer.readUTF8();
			parameterTypes[i] = ReflectionUtil.findClass(parameterClassName);
		}

		
		Object[]  args = serializer.readObjects(parameterTypes, buffer);

		DefaultInvocation invocation = new DefaultInvocation(serviceName, serviceVersion, methodName, args,parameterTypes ,null,
				this, serializer.getId());
		invocation.setInvocationId(invocationId);
		return invocation;
	}
	

	
	@Override
	public void writeResponse(InvocationResult invocationResult,
			ChannelBuffer buffer) {
		
		boolean isPongResponse =(invocationResult instanceof Pong);
		boolean isServerError = invocationResult.getResult() instanceof RpcServerErrorException;

		buffer.writeShort(MAGIC);
		
		byte flag = (byte) (0 |   (isPongResponse ? MESSAGE_PONG :MESSAGE_RESPONSE) | INVOKE_TWO_WAY);
		
		buffer.writeByte(flag);
		byte status = isServerError ? STATUS_SERVER_ERROR : STATUS_OK;

		buffer.writeByte(status);
		buffer.writeLong(invocationResult.getInvocation().getInvocationId());
		
		
		int bodyLengthOffset = buffer.writerIndex();
		buffer.writeInt(0);
	
		//pong or void response 
		if(isPongResponse || invocationResult.getResult() == null) {
			return;
		}
		
		//encode body
		Serializer serializer = serializers[invocationResult.getInvocation().getSerializerId()];
		if(isServerError) {
			RpcServerErrorException exception = (RpcServerErrorException)invocationResult.getResult();
			serializer.writeObject(exception.getMessage(), buffer);
		}
		else {
			
			serializer.writeObject(invocationResult.getResult(), buffer);
		}
		
		int bodyLength = buffer.writerIndex() - bodyLengthOffset - 4;
		
		int savedWriterIndex = buffer.writerIndex();
		buffer.writerIndex(bodyLengthOffset);
		buffer.writeInt(bodyLength);
		buffer.writerIndex(savedWriterIndex);
	}
	

	@Override
	public InvocationResult readResponse(InvocationHolder holder, ChannelBuffer buffer) {
		
		if(buffer.readableBytes() < HEAD_LENGTH) {
			return null;
		}
		
		buffer.markReaderIndex();
		
		short magic = buffer.readShort();
		if(magic != MAGIC) {
			buffer.resetReaderIndex();
			return null;
		}
		
		byte flag = buffer.readByte();
		byte status = buffer.readByte();
		
		boolean isPongResponse = (flag & MESSAGE_TYPE_MASK) == MESSAGE_PONG;
		boolean isServerError = status == STATUS_SERVER_ERROR;
		
		long  invocationId = buffer.readLong();
		int bodyLength = buffer.readInt();
		
		if(isPongResponse) {
			return pong;
		}
		
		if(buffer.readableBytes() < bodyLength) {
			buffer.resetReaderIndex();
			return null;
		}
		

		Invocation invocation = holder.getInvocation(invocationId);
		//invocation may be null because of client timeout
		if(invocation == null) {
			buffer.skipBytes(bodyLength);
			return null;
		}
		
		//void response
		if(bodyLength == 0) {
			return new InvocationResult(null, invocation);
		}
		
		//decode body
		int bodyEndOffset = buffer.readerIndex() + bodyLength;
		
		try {
			Object result = null;
			Serializer serializer = serializers[invocation.getSerializerId()];
			ChannelBuffer bodyBuffer = buffer.slice(buffer.readerIndex(), bodyLength);
			if(isServerError) {
				String errorMsg = (String) serializer.readObject(String.class, bodyBuffer);
				result = new RpcServerErrorException(errorMsg);
			}
			else if (invocation.getReturnType() != Void.class) {
				result = serializer.readObject(invocation.getReturnType(),bodyBuffer);
			}
			
			return new InvocationResult(result, invocation);
		} catch (Exception e) {
			throw new RpcProtocalException("decode response body failed!", null ,e);
		}
		finally {
			buffer.readerIndex(bodyEndOffset);
		}
	}


	@Override
	public Ping getPing() {
		return ping;
	}


	@Override
	public Pong getPong() {
		return pong;
	}

}
