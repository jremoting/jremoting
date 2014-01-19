package com.github.jremoting.util;


public interface Logger {

    public void trace(String msg);

    public void trace(String msg, Throwable e);

	public void debug(String msg);
	
	public void debug(String msg, Throwable e);

	public void info(String msg);
	
	public void info(String msg, Throwable e);

	public void warn(String msg);

	public void warn(String msg, Throwable e);

	public void error(String msg);

	public void error(String msg, Throwable e);

    public boolean isTraceEnabled();

	public boolean isDebugEnabled();

	public boolean isInfoEnabled();

	public boolean isWarnEnabled();

	public boolean isErrorEnabled();

}