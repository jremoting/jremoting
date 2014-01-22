package com.github.jremoting.serializer;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.alibaba.fastjson.JSONReader;
import com.github.jremoting.core.ObjectInput;

public class JsonObjectInput implements ObjectInput  {

	private final JSONReader reader;
	public JsonObjectInput(InputStream in) {
		this.reader = new JSONReader(new InputStreamReader(in));
		this.reader.startArray();
	}
	
	@Override
	public String readString() {
		return reader.readString();
	}

	@Override
	public Object readObject(Class<?> clazz) {
		if(clazz == int.class) {
			return readInt();
		}
		if(clazz == long.class) {
			return reader.readLong();
		}
		return reader.readObject(clazz);
	}

	@Override
	public int readInt() {
		return reader.readInteger();
	}

	@Override
	public void end() {
		this.reader.endArray();
		
	}
}
