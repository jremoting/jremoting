package com.github.jremoting.core;
import java.lang.reflect.Method;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class DefaultProxyFactory implements com.github.jremoting.core.ProxyFactory {

	@Override
	public Object getProxy(Class<?> intefaceType, final InvokePipeline pipeline) {
		
		ProxyFactory factory = new ProxyFactory();
		
		factory.setSuperclass(Object.class);
		factory.setInterfaces(new Class[]{intefaceType});

		Class<?> proxyClass = factory.createClass();
		
		MethodHandler handler = new MethodHandler() {
			@Override
			public Object invoke(Object self, Method overridden, Method forwarder,
					Object[] args) throws Throwable {
				return pipeline.invoke(new DefaultInvocation(args));
			}
		};

		try {
			Object instance = proxyClass.newInstance();
			((ProxyObject) instance).setHandler(handler);
			return  instance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static class DefaultInvocation implements Invocation {
		private final Object[] args;
		
		public DefaultInvocation(Object[] args) {
			this.args = args;
		}

		@Override
		public Object[] getArgs() {
			return args;
		}
	}

}
