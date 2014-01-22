package com.github.jremoting.serializer;

import com.github.jremoting.core.Serializer;


public class Serializers {
	
	private  Serializer[] serializers;
	public Serializers(Serializer[] serializes) {
		int maxId = 0;
		for (Serializer serialize : serializes) {
			if(serialize.getId() > maxId) {
				maxId = serialize.getId();
			}
		}
		
		maxId++;
		
		serializers = new Serializer[maxId];
		
		for (Serializer serialize : serializes) {
			serializers[serialize.getId()] = serialize;
		}
	}
	
	public Serializer[] getSerializers() {
		return serializers;
	}

}
