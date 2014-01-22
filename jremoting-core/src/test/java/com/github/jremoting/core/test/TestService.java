package com.github.jremoting.core.test;

public interface TestService {
	
	public HelloOutput hello(HelloInput input, Integer id);
	
	public void hello1();
	
	public static class HelloInput {
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		private long id;
		private String msg = "xhan";
		
		@Override
		public boolean equals(Object obj) {
			HelloInput that = (HelloInput)obj;
			return id == that.id && this.msg.equals(that.msg);
		}
	}
	
	public static class HelloOutput {
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		private long id;
		private String msg;
	}
}
