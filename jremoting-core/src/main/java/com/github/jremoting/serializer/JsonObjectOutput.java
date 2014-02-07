package com.github.jremoting.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.alibaba.fastjson.JSONWriter;
import com.github.jremoting.exception.SerializeException;
import com.github.jremoting.io.ObjectOutput;

public class JsonObjectOutput implements ObjectOutput {
	private final JSONWriter writer;
	public JsonObjectOutput(OutputStream out) {
		this.writer = new JSONWriter(new PrintWriter(out));
		this.writer.startArray();
	}
	@Override
	public void writeString(String value) {
		writer.writeObject(value);
	}

	@Override
	public void writeObject(Object obj) {
		writer.writeObject(obj);
	}
	@Override
	public void writeInt(int value) {
		writer.writeValue(value);
	}
	
	@Override
	public void flush() {
		try {
			writer.endArray();
			writer.flush();
		} catch (IOException e) {
			throw new SerializeException("json end failed!", e);
		}
	}

}
