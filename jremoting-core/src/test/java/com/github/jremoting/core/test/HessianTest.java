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
import com.github.jremoting.serializer.HessianSerializer;
import com.github.jremoting.serializer.JsonSerializer;

public class HessianTest {
	public static void main(String[] args) {
		ByteBuf buf =  Unpooled.buffer();
		ChannelBuffer buffer = new NettyChannelBuffer(buf);
		
		ChannelBufferOutputStream out = new ChannelBufferOutputStream(buffer);
		
		
		
		HessianSerializer hessianSerializer = new HessianSerializer();
		
		ObjectOutput output = hessianSerializer.createObjectOutput(out);
		
		
	
		output.writeInt(4);
		output.writeObject(new HelloInput());
		output.writeString("hehe");
		output.flush();
		
		
		ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);
		ObjectInput input = hessianSerializer.createObjectInput(in);
		
		
		int i = input.readInt();
		HelloInput helloInput = (HelloInput) input.readObject(HelloInput.class);
		String s = input.readString();

		
		System.out.println(s);
		
	}
}
