package com.github.jremoting.serializer;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.github.jremoting.core.ChannelBuffer;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.exception.RpcSerializeException;


public class JsonSerializer implements Serializer {

	@Override
	public int getId() {
		return 1;
	}

	@Override
	public String getName() {
		return "json";
	}

	@Override
	public void writeObject(Object obj, ChannelBuffer out)   {

		if(obj instanceof Integer){
			out.writeInt((Integer)obj);
			return;
		}
		
		if(obj instanceof String){
			out.writeUTF8((String)obj);
			return;
		}
		
		
		JSONWriter writer = null;
		try {
			writer = new JSONWriter(new PrintWriter(new ChannelBufferOutputStream(out)));
			writer.writeObject(obj);
		} 
		finally{
			try {
				if(writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				throw new RpcSerializeException("json encode failed !", e);
			}
		}
	}

	@Override
	public Object readObject(Class<?> clazz, ChannelBuffer in) {
	
		if(clazz == Integer.class){
			return in.readInt();
		}
		if(clazz == String.class){
			return in.readUTF8();
		}
		
		JSONReader reader = null;
		try {
			 reader = new JSONReader(new InputStreamReader(new ChannelBufferInputStream(in)));
			return reader.readObject(clazz);
		} catch(Exception e) {
			throw new RpcSerializeException("josn decode failed", e);
		}
		finally {
			if(reader != null) {
				reader.close();
			}
		}
	}



}
