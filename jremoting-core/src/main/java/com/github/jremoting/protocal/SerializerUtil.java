package com.github.jremoting.protocal;

import com.github.jremoting.core.Serializer;





public abstract class SerializerUtil {
	
	public static Serializer[] reindex(Serializer[] oldSerializers) {
		Serializer[] newSerializers;
		int maxId = 0;
		for (Serializer serialize : oldSerializers) {
			if(serialize.getId() > maxId) {
				maxId = serialize.getId();
			}
		}
		
		maxId++;
		
		newSerializers = new Serializer[maxId];
		
		for (Serializer serialize : oldSerializers) {
			newSerializers[serialize.getId()] = serialize;
		}
		return newSerializers;
	}
}
