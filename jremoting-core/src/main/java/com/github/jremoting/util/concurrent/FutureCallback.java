package com.github.jremoting.util.concurrent;

/**
 * used to implement server async invoke
 * @author hanjie
 *
 * @param <V> service method return type
 */
public interface FutureCallback<V> {
	/**
	 * finish server async , invoke call this method will write result to client
	 * @param result invoke result 
	 */
	void onSuccess(V result);
	
	/**
	 * write error msg to client
	 * @param t
	 */
	void onFailure(Throwable t);
}
