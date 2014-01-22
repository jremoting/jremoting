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
		buffer.writeUTF8(invocation.getServiceName());
		buffer.writeUTF8(invocation.getServiceVersion());
		buffer.writeUTF8(invocation.getMethodName());
		int argLength = invocation.getArgs() == null ? 0 : invocation.getArgs().length;
		buffer.writeByte(argLength);
		
		if(argLength == 0) {
			return;
		}
		
		for (Object arg : invocation.getArgs()) {
			buffer.writeUTF8(arg.getClass().getName());
		}
		
		ChannelBuffer lengthBuffer = buffer.slice(buffer.writerIndex(), argLength*4);
		lengthBuffer.writerIndex(0);
		buffer.writerIndex(buffer.writerIndex() + argLength*4);
		
		for (int i= 0; i <  argLength; i++) {
			int begin = buffer.writerIndex();
			serializer.writeObject(invocation.getArgs()[i], buffer);
			int end = buffer.writerIndex();
			lengthBuffer.writeInt(end - begin);
		}
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
		    Serializer serializer = serializers[serializeId];
			return  decodeRequestBody(invocationId, buffer, serializer);
			
		} catch (Exception e) {
			throw new RpcProtocalException("decode request body failed!", e);
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
			Invocation invocation = new DefaultInvocation(serviceName, serviceVersion, methodName, null, null, this, serializer.getId());
			invocation.setInvocationId(invocationId);
			return invocation;
		}
		
		Class<?>[] parameterTypes = new Class[argsLength];
		Object[] args = new Object[argsLength];
		int[] argLength = new int[argsLength];
		
		for (int i = 0; i < argsLength; i++) {
			String parameterClassName = buffer.readUTF8();
			parameterTypes[i] = this.getClass().getClassLoader().loadClass(parameterClassName);
		}
		
		for (int i = 0; i < argsLength; i++) {
			argLength[i] = buffer.readInt();
		}

		for (int i = 0; i < argsLength; i++) {
			args[i] = serializer.readObject(parameterTypes[i], buffer.slice(buffer.readerIndex(), argLength[i]));
			buffer.skipBytes(argLength[i]);
		}
		
		DefaultInvocation invocation = new DefaultInvocation(serviceName, serviceVersion, methodName, args, null,
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
		if(isServerError) {
			RpcServerErrorException exception = (RpcServerErrorException)invocationResult.getResult();
			buffer.writeUTF8(exception.getMessage());
		}
		else {
			Serializer serializer = serializers[invocationResult.getInvocation().getSerializerId()];
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
			if(isServerError) {
				String errorMsg = buffer.readUTF8();
				result = new RpcServerErrorException(errorMsg);
			}
			else if (invocation.getReturnType() != null) {
				Serializer serializer = serializers[invocation.getSerializerId()];
				result = serializer.readObject(invocation.getReturnType(), 
						buffer.slice(buffer.readerIndex(), bodyLength));
			}
			
			return new InvocationResult(result, invocation);
		} catch (Exception e) {
			throw new RpcProtocalException("decode response body failed!", e);
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
