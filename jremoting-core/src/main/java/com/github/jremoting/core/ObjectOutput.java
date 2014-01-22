package com.github.jremoting.core;

public interface ObjectOutput  {
	void writeString(String value);
	void writeObject(Object obj);
	void writeInt(int value);
	void flush();
}
