package com.github.jremoting.serializer;

import java.io.IOException;
import java.io.OutputStream;

import com.caucho.hessian.io.Hessian2Output;
import com.github.jremoting.exception.SerializeException;
import com.github.jremoting.io.ObjectOutput;

public class HessianObjectOutput implements ObjectOutput{

	private final Hessian2Output output;
	public HessianObjectOutput(OutputStream out) {
		output = new Hessian2Output(out);
	}
	@Override
	public void writeString(String value) {
		try {
			output.writeString(value);
		} catch (IOException e) {
			throw new SerializeException("hessian write string failed", e);
		}
	}

	@Override
	public void writeObject(Object obj) {
		try {
			output.writeObject(obj);
		} catch (IOException e) {
			throw new SerializeException("hessian write obj failed", e);
		}
	}
	@Override
	public void writeInt(int value) {
		try {
			output.writeInt(value);
		} catch (IOException e) {
			throw new SerializeException("hessian write int failed", e);
		}
	}

	@Override
	public void flush() {
		try {
			output.flushBuffer();
		} catch (IOException e) {
			throw new SerializeException("hessian write end failed", e);
		}
	}

}
