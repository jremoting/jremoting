package com.github.jremoting.serializer;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jremoting.core.Serializer;

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
	public void writeObject(Object obj, OutputStream out) {
		
		
	}

	@Override
	public Object readObject(Class<?> clazz , InputStream in) {
		return null;
	}

}
