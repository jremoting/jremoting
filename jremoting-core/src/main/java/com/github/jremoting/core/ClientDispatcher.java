package com.github.jremoting.core;


public  interface ClientDispatcher  {
	RpcFuture dispatch(Invocation invocation); 
}
