package com.github.jremoting.core.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.ObjectInput;
import com.github.jremoting.core.ObjectOutput;
import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.dispatcher.NettyChannelBuffer;
import com.github.jremoting.protocal.ChannelBufferInputStream;
import com.github.jremoting.protocal.ChannelBufferOutputStream;
import com.github.jremoting.serializer.JsonSerializer;

public class FastJsonTest {
	public static void main(String[] rgs) {
		ByteBuf buf =  Unpooled.buffer();
		ChannelBuffer buffer = new NettyChannelBuffer(buf);
		
		ChannelBufferOutputStream out = new ChannelBufferOutputStream(buffer);

		JsonSerializer jsonSerializer = new JsonSerializer();
		
		ObjectOutput output = jsonSerializer.createObjectOutput(out);

		output.writeInt(4);
		output.writeInt(4);
		output.writeObject(new HelloInput());
		output.writeString("hehe");
		output.writeObject(false);
		output.flush();
		
		System.out.println(new String(buf.array()));
		
		
		ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);
		ObjectInput input = jsonSerializer.createObjectInput(in);
		
	
		int i = input.readInt();
		int j = input.readInt();
		HelloInput helloInput = (HelloInput) input.readObject(HelloInput.class);
		String s = input.readString();
		boolean b = (Boolean) input.readObject(boolean.class);
		
		System.out.println(s);
		
	
	}
}
