package com.github.jremoting.core.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.io.ByteBuffer;
import com.github.jremoting.io.ByteBufferInputStream;
import com.github.jremoting.io.ByteBufferOutputStream;
import com.github.jremoting.io.ObjectInput;
import com.github.jremoting.io.ObjectOutput;
import com.github.jremoting.remoting.DefaultByteBuffer;
import com.github.jremoting.serializer.HessianSerializer;
import com.github.jremoting.serializer.JsonSerializer;

public class HessianTest {
	public static void main(String[] args) {
		ByteBuf buf =  Unpooled.buffer();
		ByteBuffer buffer = new DefaultByteBuffer(buf);
		
		ByteBufferOutputStream out = new ByteBufferOutputStream(buffer);
		
		
		
		HessianSerializer hessianSerializer = new HessianSerializer();
		
		ObjectOutput output = hessianSerializer.createObjectOutput(out);
		
		
	
		output.writeInt(4);
		output.writeObject(new HelloInput());
		output.writeString("hehe");
		output.flush();
		
		
		ByteBufferInputStream in = new ByteBufferInputStream(buffer);
		ObjectInput input = hessianSerializer.createObjectInput(in);
		
		
		int i = input.readInt();
		HelloInput helloInput = (HelloInput) input.readObject(HelloInput.class);
		String s = input.readString();

		
		System.out.println(s);
		
	}
}
