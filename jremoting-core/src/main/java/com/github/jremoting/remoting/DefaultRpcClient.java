package com.github.jremoting.remoting;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.Serializer;
import com.github.jremoting.invoke.ClientInvokeFilterChain;

public class DefaultRpcClient implements RpcClient {
	
	private final Protocal protocal;
	private final Serializer serializer;
	private final String address;
	private final ClientInvokeFilterChain invokeFilterChain;
	
	public DefaultRpcClient(Protocal protocal, Serializer serializer,String address, ClientInvokeFilterChain invokeFilterChain) {
		this.protocal = protocal;
		this.serializer = serializer;
		this.address = address;
		this.invokeFilterChain = invokeFilterChain;
	}
	
	@Override
	public Object invoke(Invoke invoke) {
		if(invoke.getProtocal() == null) {
			invoke.setProtocal(protocal);
		}
		if(invoke.getSerializer() == null) {
			invoke.setSerializer(serializer);
		}
		if(invoke.getRemoteAddress() == null) {
			invoke.setRemoteAddress(address);
		}
		
		return this.invokeFilterChain.invoke(invoke);
	}

}
