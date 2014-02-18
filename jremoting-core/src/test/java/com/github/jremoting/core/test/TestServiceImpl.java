package com.github.jremoting.core.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.util.concurrent.Executors;
import com.github.jremoting.util.concurrent.FutureListener;
import com.github.jremoting.util.concurrent.ListenableFuture;

public class TestServiceImpl implements TestService {

	@Override
	public HelloOutput hello(HelloInput input, int id) {
		HelloOutput output = new HelloOutput();
		output.setId(input.getId() + id);
		output.setMsg("hello," + input.getMsg());
		return output;
	}
	
	static Executor executor = Executors.newExecutor(1, 1, 44);
	
	public ListenableFuture<HelloOutput> $hello1(HelloInput input, int id) {
		
	
		
		final MockFuture  future =  new MockFuture();

		executor.execute(new Runnable() {
			@Override
			public void run() {
				future.onResult("hello");
			}
		});
		return future;
	}

	@Override
	public void hello1() {
		System.out.println("ok!");
	}

	@Override
	public void hello3(char c, boolean bb,int i, long l, double d, float f, short sb, byte b,
			String s, Date date, java.sql.Date dd) {
		System.out.println(c);
		System.out.println(bb);
		System.out.println(i);
		System.out.println(l);
		System.out.println(d);
		System.out.println(f);
		System.out.println(sb);
		System.out.println(b);
		System.out.println(s);
		System.out.println(date);
		System.out.println(dd);
	}

	
	   public static class MockFuture implements  ListenableFuture<TestService.HelloOutput> {

			final List<FutureListener<HelloOutput>> listeners = new ArrayList<FutureListener<HelloOutput>>();
	
			
			public void onResult(String msg) {
			
				for (FutureListener<HelloOutput> listener : listeners) {
					listener.operationComplete(this);
				}
			}
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public HelloOutput get() throws InterruptedException,
				ExecutionException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HelloOutput get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isCancelled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public void addListener(FutureListener<HelloOutput> listener,
				Executor executor) {
			listeners.add(listener);
		}

		@Override
		public void addListener(final FutureListener<HelloOutput> listener) {
			if(isDone()) {
				executor.execute(new Runnable() {
					public void run() {
						listener.operationComplete(MockFuture.this);
					}
				});
				return;
			}
			listeners.add(listener);
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public Throwable cause() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HelloOutput result() {
			HelloOutput output =  new HelloOutput();
			output.setMsg("server async result");
			return output;
		}
	};

}
