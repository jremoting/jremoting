package com.github.jremoting.core.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.dispatcher.NettyChannelBuffer;
import com.github.jremoting.serializer.JsonSerializer;

public class FastJsonTest {
	public static void main(String[] rgs) {
		ByteBuf buf =  Unpooled.buffer();
		ChannelBuffer buffer = new NettyChannelBuffer(buf);
		
		JsonSerializer jsonSerializer = new JsonSerializer();
	
		Object[] args = new Object[]{new HelloInput(),"1",1};
		
		jsonSerializer.writeObjects(args, buffer);
		
		Object[] objs = jsonSerializer.readObjects(new Class<?>[]{HelloInput.class, String.class, int.class}, buffer);
		
		System.out.println(new String(buf.array()));
	}
}
