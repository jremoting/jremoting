package com.github.jremoting.core;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {
	
	public int getId();
	
	public String getName();
	
	public void writeObject(Object obj, OutputStream out);
	
	public Object readObject(Class<?> clazz, InputStream in);
}
