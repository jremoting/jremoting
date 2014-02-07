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
import com.github.jremoting.serializer.JsonSerializer;

public class FastJsonTest {
	public static void main(String[] rgs) {
		ByteBuf buf =  Unpooled.buffer();
		ByteBuffer buffer = new DefaultByteBuffer(buf);
		
		ByteBufferOutputStream out = new ByteBufferOutputStream(buffer);

		JsonSerializer jsonSerializer = new JsonSerializer();
		
		ObjectOutput output = jsonSerializer.createObjectOutput(out);

		output.writeInt(4);
		output.writeInt(4);
		output.writeObject(new HelloInput());
		output.writeString("hehe");
		output.writeObject(false);
		output.close();
		
		System.out.println(new String(buf.array()));
		
		
		ByteBufferInputStream in = new ByteBufferInputStream(buffer);
		ObjectInput input = jsonSerializer.createObjectInput(in);
		
	
		int i = input.readInt();
		int j = input.readInt();
		HelloInput helloInput = (HelloInput) input.readObject(HelloInput.class);
		String s = input.readString();
		boolean b = (Boolean) input.readObject(boolean.class);
		
		System.out.println(s);
		
	
	}
}
