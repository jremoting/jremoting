package com.github.jremoting.tps;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TpsCounter {
	
	private final AtomicReference<Token> currentToken;
	private final long timeWindow;
	private final int rate;
	
	public TpsCounter(long timeWindow, int rate) {
		this.timeWindow = timeWindow;
		this.rate = rate;
		this.currentToken = new AtomicReference<TpsCounter.Token>(new Token(timeWindow, rate));
	}
	
	public boolean check() {
		Token oldToken = currentToken.get();
		if(oldToken.isExpired()) {
			Token newToken = new Token(timeWindow, rate);
			while (!currentToken.compareAndSet(oldToken, newToken)) {
				oldToken = currentToken.get();
				if(!oldToken.isExpired()) {
					break;
				}
				else {
					newToken = new Token(timeWindow, rate);
				}
			};
		}
		
		return currentToken.get().grant();
	}
	
	
	private static class Token {
		
		public Token(long timeWindow,int rate) {
			this.startTime =System.currentTimeMillis();
			this.timeWindow = timeWindow;
			this.rate = new AtomicInteger(rate);
		}
		private final long startTime;
		private final long timeWindow;
		private final AtomicInteger rate;
		public boolean isExpired() {
			return System.currentTimeMillis() - this.startTime > timeWindow;
		}
		public boolean grant() {
			return this.rate.decrementAndGet() > 0;
		}
	}
}
