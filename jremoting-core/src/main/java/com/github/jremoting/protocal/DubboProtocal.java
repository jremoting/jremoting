package com.github.jremoting.protocal;

import java.util.HashMap;
import java.util.Map;

import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.Message;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.exception.ProtocalException;
import com.github.jremoting.exception.SerializeException;
import com.github.jremoting.exception.ServerErrorException;
import com.github.jremoting.io.ByteBuffer;
import com.github.jremoting.io.ByteBufferInputStream;
import com.github.jremoting.io.ByteBufferOutputStream;
import com.github.jremoting.io.ObjectOutput;
import com.github.jremoting.serializer.HessianObjectInput;
import com.github.jremoting.serializer.HessianSerializer;
import com.github.jremoting.util.ReflectUtils;


public class DubboProtocal implements Protocal {
	 // header length.
    protected static final int      HEADER_LENGTH      = 16;

    // magic header.
    protected static final short    MAGIC              = (short) 0xdabb;
    // message flag.
    protected static final int     FLAG_REQUEST       =  0x80; //10000000

    protected static final int     FLAG_TWOWAY        =  0x40; //01000000

    protected static final int     FLAG_EVENT     = 0x20;	  //00100000
    

    protected static final int      SERIALIZATION_MASK = 0x1f;		  //00011111

    public static final byte DUBBO_HESSIAN_SERIALIZATION_ID = 2;
    public static final byte RESPONSE_WITH_EXCEPTION = 0;
    public static final byte RESPONSE_VALUE = 1;
    public static final byte RESPONSE_NULL_VALUE = 2;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    private static HessianSerializer hessionSerializer = new HessianSerializer();
    public static final byte OK                = 20;

    public static final byte SERVER_ERROR      = 80;
    //Attachments
    private static final Map<String, String> ATTACHMENTS = new HashMap<String, String>();
    
    private final ServiceRegistry registry;
    public DubboProtocal(ServiceRegistry registry) {
    	this.registry = registry;
    }

	@Override
	public void encode(Message msg, ByteBuffer buffer) throws ProtocalException {

		try {
			boolean isHeartbeatMessage = msg instanceof HeartbeatMessage;
			boolean isTwoWay = msg.isTwoWay();
			boolean isRequest = msg instanceof Invoke || msg == HeartbeatMessage.PING;
			boolean isErrorMsg = (msg instanceof InvokeResult)
					&& ((InvokeResult) msg).getResult() instanceof Throwable;

			int serializeId =  DUBBO_HESSIAN_SERIALIZATION_ID;

			int flag = (isRequest ? FLAG_REQUEST : 0)
					| (isTwoWay ? FLAG_TWOWAY : 0)
					| (isHeartbeatMessage ? FLAG_EVENT : 0) | serializeId;

			int status = isErrorMsg ? SERVER_ERROR : OK;

			buffer.writeShort(MAGIC);
			buffer.writeByte(flag);
			buffer.writeByte(status);
			buffer.writeLong(msg.getId());
			
			int bodyLengthOffset = buffer.writerIndex();
			buffer.writeInt(0);

			ObjectOutput output = hessionSerializer.createObjectOutput(new ByteBufferOutputStream(buffer));
			
			if (isHeartbeatMessage) {
				output.writeObject(null);
			}
			else if(isErrorMsg) {
				InvokeResult invokeResult = (InvokeResult) msg;
				Throwable error = (Throwable)invokeResult.getResult();
				output.writeString(error.getMessage());
			}
			else if(isRequest) {
				Invoke invoke = (Invoke)msg;
				
				output.writeString("2.5.3");
				output.writeString(invoke.getInterfaceName());
				output.writeString(invoke.getVersion());
				output.writeString(invoke.getMethodName());
				output.writeString(ReflectUtils.getDesc(invoke
						.getParameterTypes()));
				if (invoke.getArgs() != null) {
					for (int i = 0; i < invoke.getArgs().length; i++) {
						output.writeObject(invoke.getArgs()[i]);
					}
				}
				output.writeObject(ATTACHMENTS);
			}
			else {
				
				InvokeResult invokeResult = (InvokeResult) msg;
				if(invokeResult.getResult() == null) {
					output.writeInt(RESPONSE_NULL_VALUE);
				}
				else if(invokeResult.getResult() instanceof Throwable){
					output.writeInt(RESPONSE_WITH_EXCEPTION);
					output.writeObject(invokeResult.getResult());
				}
				else {
					output.writeInt(RESPONSE_VALUE);
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
		} catch (Exception e) {
			throw new ProtocalException("encode dubbo request error!", e);
		}
	}

	@Override
	public Message decode(ByteBuffer buffer) throws ProtocalException {
		
		try {
			if(buffer.readableBytes() < HEADER_LENGTH) {
				return Message.NEED_MORE;
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
				return Message.NEED_MORE;
			}
			
			boolean isHeartbeat = (flag & FLAG_EVENT) > 0;	
			boolean isRequest = (flag & FLAG_REQUEST) > 0;
			boolean isTwoWay = (flag & FLAG_TWOWAY) > 0 ;
			boolean isErrorMsg = (!isRequest && status != OK);
					
			HessianObjectInput input = (HessianObjectInput) hessionSerializer.createObjectInput(new ByteBufferInputStream(buffer, bodyLength));
			Message msg = null;
			if(isHeartbeat) {
				input.readObject();
				if(isTwoWay) {
					msg = HeartbeatMessage.PING;
				}
				else {
					msg = HeartbeatMessage.PONG;
				}
			}
			else if (isErrorMsg) {
				String errorMsg = input.readString();
				ServerErrorException exception = new ServerErrorException(errorMsg);
				msg = new InvokeResult(exception, msgId, hessionSerializer);
			}
			else if(isRequest) {
				
				@SuppressWarnings("unused")
				String dubboVersion = input.readString();
				
				String interfaceName = input.readString();
				String version = input.readString();
				String methodName = input.readString();
				String methodDesc = input.readString();
				
			
				Object[] args;
				Class<?>[] parameterTypes;
				if (methodDesc.length() == 0) {
					parameterTypes = EMPTY_CLASS_ARRAY;
					args = EMPTY_OBJECT_ARRAY;
				} else {
					parameterTypes = ReflectUtils.desc2classArray(methodDesc);
					args = new Object[parameterTypes.length];
					for (int i = 0; i < args.length; i++) {
						args[i] = input.readObject(parameterTypes[i]);
					}
				}
				@SuppressWarnings({ "unused", "unchecked" })
				Map<String, String> attachment = (Map<String, String>) input
						.readObject(HashMap.class);

				Invoke invoke = new Invoke(interfaceName, version, methodName,
						hessionSerializer, args, parameterTypes);
				invoke.setId(msgId);

				msg = invoke;
				
			}
			else {
				  int resultType = input.readInt();
				  if(resultType == RESPONSE_NULL_VALUE) {
					  msg = new InvokeResult(null, msgId, hessionSerializer);
				  }
				  else if (resultType == RESPONSE_WITH_EXCEPTION){
					  Object result = input.readObject();
					  if(result instanceof Throwable) {
						   msg = new InvokeResult(result, msgId, hessionSerializer);
					  } 
					  else {
						  SerializeException error = new SerializeException("Response data error, expect Throwable, but get " + result);
						  msg = new InvokeResult(error, msgId, hessionSerializer);
					  }
				  }
				  else { 
					
					  Object result = input.readObject();
					  msg = new InvokeResult(result, msgId, hessionSerializer);
				  }
			}

			return msg;
		} catch (Exception e) {
			throw new ProtocalException("decode dubbo response error!", e);
		}
	}

	@Override
	public ServiceRegistry getRegistry() {
		return registry;
	}

}
