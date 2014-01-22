package com.github.jremoting.serializer;

import java.io.IOException;
import java.io.InputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.github.jremoting.core.ObjectInput;
import com.github.jremoting.exception.RpcSerializeException;

public class HessianObjectInput implements ObjectInput {

	private final Hessian2Input input;
	
	public HessianObjectInput(InputStream in) {
		this.input = new Hessian2Input(in);
	}
	
	@Override
	public String readString() {
		
		try {
			return input.readString();
		} catch (Exception e) {
			throw new RpcSerializeException("hessian read string failed", e);
		}
	}

	@Override
	public Object readObject(Class<?> clazz) {
		try {
			Object result = input.readObject(clazz);
			return result;
		} catch (IOException e) {
			throw new RpcSerializeException("hessian read obj failed", e);
		}
		
	}

	@Override
	public int readInt() {
		try {
			return input.readInt();
		} catch (IOException e) {
			throw new RpcSerializeException("hessian read int failed", e);
		}
	}
}
