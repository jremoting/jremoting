package com.github.jremoting.util.concurrent;

/**
 * used to implement server async invoke
 * @author hanjie
 *
 * @param <V> service method return type
 */
public interface AsyncCallback<V> {
	/**
	 * finish server async invoke call this method will write result to client
	 * @param result invoke result or Throwable if some error happen 
	 */
	void setResult(V result);
}
