package com.github.jremoting.io;

public interface ObjectInput {
	String readString();
	int readInt();
	Object readObject(Class<?> clazz);
}
