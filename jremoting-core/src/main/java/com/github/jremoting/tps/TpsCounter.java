package com.github.jremoting.tps;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TpsCounter {
	
	private final AtomicReference<Counter> currentCounter;
	private final long timeWindow;
	private final int rate;
	private final AtomicInteger peak;
	
	public TpsCounter(long timeWindow, int rate, int peak) {
		this.timeWindow = timeWindow;
		this.rate = rate;
		this.currentCounter = new AtomicReference<Counter>(new Counter(timeWindow, rate));
		this.peak = new AtomicInteger(peak);
	}
	
	public boolean beginInvoke() {
		int leftPeak =  peak.decrementAndGet();
		if(leftPeak < 0) {
			peak.incrementAndGet();
			return false;
		}
		
		Counter oldCounter = currentCounter.get();
		if(oldCounter.isExpired()) {
			Counter newCounter = new Counter(timeWindow, rate);
			while (!currentCounter.compareAndSet(oldCounter, newCounter)) {
				oldCounter = currentCounter.get();
				if(!oldCounter.isExpired()) {
					break;
				}
				else {
					newCounter = new Counter(timeWindow, rate);
				}
			};
		}
		
		return currentCounter.get().grant();
	}
	
	public void endInvoke() {
		peak.incrementAndGet();
	}

	private static class Counter {
		
		public Counter(long timeWindow,int rate) {
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
			return this.rate.decrementAndGet() >= 0;
		}
	}
	
}
