package com.github.jremoting.serializer;

import java.util.concurrent.ConcurrentHashMap;

import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.SerializerFactory;

public class HessianSerializerFactory extends SerializerFactory {
	
	public static final SerializerFactory INSTANCE = new HessianSerializerFactory();

	private static final Object PLACEHOLDER = new Object();
	
	private HessianSerializerFactory(){}

	private ConcurrentHashMap<String, Object> _cachedNotFoundTypeDeserializerMap = new ConcurrentHashMap<String, Object>();

	@Override
	public Deserializer getDeserializer(String type)
			throws HessianProtocolException {
		if (_cachedNotFoundTypeDeserializerMap.containsKey(type)) {
			return null;
		}

		Deserializer deserializer = super.getDeserializer(type);
		if (deserializer == null) {
			_cachedNotFoundTypeDeserializerMap.put(type, PLACEHOLDER);
		}
		return deserializer;

	}
}
