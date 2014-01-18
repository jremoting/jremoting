package com.github.jremoting.core;


public class ClientFinalFilter extends FinalFilter {

	private final ClientDispatcher clientDispatcher;
	
	public ClientFinalFilter(ClientDispatcher clientDispatcher) {
		this.clientDispatcher = clientDispatcher;
	}
	
	@Override
	protected Object doRpcInvoke(Invocation invocation) {
		
		try {
			return clientDispatcher.dispatch(invocation).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
