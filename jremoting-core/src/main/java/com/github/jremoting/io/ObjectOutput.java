package com.github.jremoting.io;

public interface ObjectOutput  {
	void writeString(String value);
	void writeObject(Object obj);
	void writeInt(int value);
	void close();
}
