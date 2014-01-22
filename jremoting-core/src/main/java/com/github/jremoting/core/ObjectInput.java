package com.github.jremoting.core;

public interface ObjectInput {
	String readString();
	int readInt();
	Object readObject(Class<?> clazz);
	void end();
}
