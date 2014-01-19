package com.github.jremoting.protocal;

import java.util.HashMap;
import java.util.Map;

import com.github.jremoting.core.DefaultInvocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.exception.RpcException;

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
	
	public static final byte OK = 88;
	
	
	private final Map<String, Serializer> serializerMap = new HashMap<String, Serializer>();
	
	private final  Serializer[] serializerArray;
	
	public JRemotingProtocal(Serializer[] serializes) {
		int maxId = 0;
		for (Serializer serialize : serializes) {
			if(serialize.getId() > maxId) {
				maxId = serialize.getId();
			}
		}
		
		serializerArray = new Serializer[maxId];
		
		for (Serializer serialize : serializes) {
			serializerArray[serialize.getId()] = serialize;
			serializerMap.put(serialize.getName(), serialize); 
		}
	}
	
	

	@Override
	public boolean writeRequest(Invocation invocation, ChannelBuffer buffer) {
		if(!NAME.equals(invocation.getProtocalName())) {
			return false;
		}
		
		buffer.writeShort(MAGIC);

		int flag = 0 | MESSAGE_REQUEST | INVOKE_TWO_WAY ;
		
		buffer.writeByte(flag);
		
		Serializer serializer = serializerMap.get(invocation.getSerializeName());
		
		buffer.writeByte(serializer.getId());
		
		buffer.writeLong(invocation.getInvocationId());
		
		int bodyLengthOffset = buffer.writerIndex();
		
		buffer.writeInt(0);
		
		ChannelBufferOutputStream out = new ChannelBufferOutputStream(buffer);
		
		serializer.writeObject(invocation.getServiceName(), out);
		serializer.writeObject(invocation.getServiceVersion(), out);
		serializer.writeObject(invocation.getMethodName(), out);
		serializer.writeObject(invocation.getReturnType().getName(), out);
		serializer.writeObject(invocation.getArgs().length, out);
		for (Object arg : invocation.getArgs()) {
			serializer.writeObject(arg.getClass().getName(), out);
			serializer.writeObject(arg, out);
		}
		
		
		
		int bodyLength = buffer.writerIndex() - bodyLengthOffset - 4;
		
		int savedWriterIndex = buffer.writerIndex();
		
		buffer.writerIndex(bodyLengthOffset);
		buffer.writeInt(bodyLength);
		buffer.writerIndex(savedWriterIndex);
		
		
		return true;
	}
	
	@Override
	public Invocation readRequest(ChannelBuffer buffer) {
		short magic = buffer.readShort();
		if(magic != MAGIC) {
			return null;
		}
		
		byte flag = buffer.readByte();
		
		boolean isPingRequest = (flag & MESSAGE_TYPE_MASK) == MESSAGE_PING;
		byte serializeId = buffer.readByte();
		long invocationId = buffer.readLong();
		int bodyLength = buffer.readInt();
		
		if(isPingRequest) {
			return Protocal.PING;
		}
		
		
		
		int bodyEndOffset = buffer.readerIndex() + bodyLength;
		
	
		try {
			
			ChannelBufferInputStream in = new ChannelBufferInputStream(buffer, bodyLength);
			
			final Serializer serializer = serializerArray[serializeId];

			String serviceName = (String)serializer.readObject(String.class,in);
			String serviceVersion = (String)serializer.readObject(String.class,in);
			String methodName = (String)serializer.readObject( String.class,in);
			String returnClassName = (String)serializer.readObject(String.class,in);
			
			Class<?> returnType = this.getClass().getClassLoader().loadClass(returnClassName);
			
			int argsLength = (Integer)serializer.readObject(Integer.class,in);
			
			Class<?>[] parameterTypes = new Class[argsLength];
			
			Object[] args = new Object[argsLength];
			
			for (int i = 0; i < argsLength; i++) {
				String parameterClassName = (String)serializer.readObject(String.class,in);
				parameterTypes[i] = this.getClass().getClassLoader().loadClass(parameterClassName);
				args[i] = serializer.readObject(parameterTypes[i], in);
			}
			
			return new DefaultInvocation(serviceName, serviceVersion, methodName, args, returnType, invocationId) {
				@Override
				public String getProtocalName() {
					return JRemotingProtocal.NAME;
				}
				
				@Override
				public String getSerializeName() {
					return serializer.getName();
					
				}
			};
			
		} catch (Exception e) {
			throw new RpcException("decode request body failed!", e);
		}
		finally{
			buffer.readerIndex(bodyEndOffset);;
		}
	}
	
	@Override
	public boolean writeResponse(Invocation invocation, InvocationResult invocationResult,
			ChannelBuffer buffer) {
		if(!NAME.equals(invocation.getProtocalName())){
			return false;
		}
		buffer.writeShort(MAGIC);
		byte flag = 0 | MESSAGE_RESPONSE | INVOKE_TWO_WAY;
		buffer.writeByte(flag);
		buffer.writeByte(OK);
		buffer.writeLong(invocation.getInvocationId());
		
		if(invocation == PONG) {
			buffer.writeInt(0);
			return true;
		}
		
		int bodyLengthOffset = buffer.writerIndex();
		buffer.writeInt(0);
		
		ChannelBufferOutputStream out = new ChannelBufferOutputStream(buffer);

		Serializer serializer = serializerMap.get(invocation.getSerializeName());
		
		serializer.writeObject(invocationResult.getResult(), out);
		
		int bodyLength = buffer.writerIndex() - bodyLengthOffset - 4;
		
		int savedWriterIndex = buffer.writerIndex();
		
		buffer.writerIndex(bodyLengthOffset);
		buffer.writeInt(bodyLength);
		buffer.writerIndex(savedWriterIndex);
		return true;
		
	}
	

	@Override
	public InvocationResult readResponse(InvocationHolder holder, ChannelBuffer buffer) {
		short magic = buffer.readShort();
		if(magic != MAGIC) {
			return null;
		}
		
		byte flag = buffer.readByte();
		byte status = buffer.readByte();
		
		boolean isPongResponse = (flag & MESSAGE_TYPE_MASK) == MESSAGE_PONG;
		
		long  invocationId = buffer.readLong();
		int bodyLength = buffer.readInt();
		
		if(isPongResponse && status == OK) {
			return Protocal.PONG;
		}
		
		int bodyEndOffset = buffer.readerIndex() + bodyLength;
		
		Invocation invocation = holder.getInvocation(invocationId);
		//invocation may be null because of client timeout
		if(invocation == null) {
			buffer.skipBytes(bodyLength);
			return null;
		}
		
		ChannelBufferInputStream in = new ChannelBufferInputStream(buffer, bodyLength);
		
		try {
			Serializer serializer = serializerMap.get(invocation.getSerializeName());
			
			Object result = serializer.readObject(invocation.getReturnType(),in);
			
			return new InvocationResult(invocationId, result);
		} catch (Exception e) {
			throw new RpcException("decode  response body failed!", e);
		}
		finally {
			buffer.readerIndex(bodyEndOffset);
		}

	}

}
