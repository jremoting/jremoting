package com.github.jremoting.core;

import com.github.jremoting.exception.RpcSerializeException;

public interface Serializer {
	
	public int getId();
	
	public String getName();
	
	public void writeObject(Object obj, ChannelBuffer out) throws RpcSerializeException;
	
	public Object readObject(Class<?> clazz, ChannelBuffer in) throws RpcSerializeException;
	
	public void writeObjects(Object obj[], ChannelBuffer out) throws RpcSerializeException;
	
	public Object[] readObjects(Class<?> clazz[], ChannelBuffer in) throws RpcSerializeException;
}
