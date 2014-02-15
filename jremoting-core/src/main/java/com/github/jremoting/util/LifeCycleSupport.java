package com.github.jremoting.util;

public class LifeCycleSupport {

	protected volatile boolean started = false;
	protected volatile boolean closed = false;
	
	public void start(Runnable callback) {
		if(started) {
			return;
		}
		
		synchronized (this) {
			if(started) {
				return;
			}
			
			callback.run();
			started = true;
		}
	}

	public void close(Runnable callback) {
		if(closed) {
			return;
		}
		synchronized (this) {
			if(closed) {
				return;
			}
			
			if(started) {
				callback.run();
			}
			closed = true;
		}
	}
	
	public boolean isStarted() {
		return started;
	}

	public boolean isClosed() {
		return closed;
	}

}
