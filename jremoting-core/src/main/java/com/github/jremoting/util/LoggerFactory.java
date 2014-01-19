package com.github.jremoting.util;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class LoggerFactory {
	
	 public static Logger getLogger(Class<?> clazz) {
		 return  new NettyLoggerAdapter(InternalLoggerFactory.getInstance(clazz));
	 }

	 private static class NettyLoggerAdapter implements Logger {
		private InternalLogger nettyLogger;
		public NettyLoggerAdapter(InternalLogger nettyLogger) {
			this.nettyLogger = nettyLogger;
		}
		@Override
		public void trace(String msg) {
			nettyLogger.trace(msg);
			
		}
		@Override
		public void trace(String msg, Throwable e) {
			nettyLogger.trace(msg, e);
			
		}
		@Override
		public void debug(String msg) {
			nettyLogger.debug(msg);
			
		}
		@Override
		public void debug(String msg, Throwable e) {
			nettyLogger.debug(msg, e);
			
		}
		@Override
		public void info(String msg) {
			nettyLogger.info(msg);
			
		}
		@Override
		public void info(String msg, Throwable e) {
			nettyLogger.info(msg, e);
			
		}
		@Override
		public void warn(String msg) {
			nettyLogger.warn(msg);
			
		}
		@Override
		public void warn(String msg, Throwable e) {
			nettyLogger.warn(msg,e);
			
		}
		@Override
		public void error(String msg) {
			nettyLogger.error(msg);
		}
		
		@Override
		public void error(String msg, Throwable e) {
			nettyLogger.error(msg,e);
			
		}
		@Override
		public boolean isTraceEnabled() {
			return nettyLogger.isTraceEnabled();
		}
		@Override
		public boolean isDebugEnabled() {

			return nettyLogger.isDebugEnabled();
		}
		@Override
		public boolean isInfoEnabled() {
			return nettyLogger.isInfoEnabled();
		}
		@Override
		public boolean isWarnEnabled() {
			return nettyLogger.isWarnEnabled();
		}
		@Override
		public boolean isErrorEnabled() {
			return nettyLogger.isErrorEnabled();
		}
	 }
}
