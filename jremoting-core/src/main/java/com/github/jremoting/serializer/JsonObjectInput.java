package com.github.jremoting.serializer;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.alibaba.fastjson.JSONReader;
import com.github.jremoting.io.ObjectInput;

public class JsonObjectInput implements ObjectInput  {

	private final JSONReader reader;
	public JsonObjectInput(InputStream in) {
		this.reader = new JSONReader(new InputStreamReader(in));
		this.startArray();
	}
	
	protected void startArray() {
		this.reader.startArray();
	}
	
	@Override
	public String readString() {
		return reader.readString();
	}

	@Override
	public Object readObject(Class<?> clazz) {
		return reader.readObject(clazz);
	}

	@Override
	public int readInt() {
		return reader.readInteger();
	}

	@Override
	public void close() {
		this.reader.endArray();
	}
}
