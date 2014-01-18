package com.github.jremoting.core;

import java.util.HashMap;
import java.util.Map;

public class ProtocalFactory {
	private final Map<Integer, Protocol> supportProtocals;
	
	public ProtocalFactory(Protocol[] protocols) {
		Map<Integer, Protocol> temp = new HashMap<Integer, Protocol>();
		for(Protocol protocol : protocols) {
			temp.put(protocol.getMagic(), protocol);
		}
		this.supportProtocals = temp;
	}
	
	public Protocol getProtocol(int magic) {
		return supportProtocals.get(magic);
	}
}
