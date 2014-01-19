package com.github.jremoting.core;

public interface Protocal {
	
	public static final Ping PING = new Ping();
	public static final Pong PONG = new Pong();
	
	boolean writeRequest(Invocation invocation, ChannelBuffer buffer);
	InvocationResult readResponse(InvocationHolder holder, ChannelBuffer buffer);
	
	Invocation readRequest(ChannelBuffer buffer);
	boolean writeResponse(InvocationResult invocationResult, ChannelBuffer buffer);
	
	public static class Pong implements InvocationResult {

		private Pong(){}
		@Override
		public Object getResult() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getInvocationId() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	public static class Ping implements Invocation {
		private Ping() {
			
		}

		@Override
		public Object[] getArgs() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<?>[] getParameterTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<?> getReturnType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceVersion() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getMethodName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRemoteAddress() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getInvocationId() {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
}
