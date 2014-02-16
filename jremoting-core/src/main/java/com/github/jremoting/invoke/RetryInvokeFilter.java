package com.github.jremoting.invoke;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.exception.RemotingException;

public class RetryInvokeFilter extends AbstractInvokeFilter {

	@Override
	public Object invoke(Invoke invoke) {
		int retry = invoke.getRetry();
		while (true) {
			try {
				return getNext().invoke(invoke);
			} catch (Exception e) {
				retry--;
				if(retry > 0) {
					continue;
				}
				else {
					throw new RemotingException(e);
				}
			}
		}
	}

}
