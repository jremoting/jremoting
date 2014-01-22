package com.github.jremoting.serializer;

import java.io.InputStream;
import java.io.OutputStream;

import com.github.jremoting.core.ObjectInput;
import com.github.jremoting.core.ObjectOutput;
import com.github.jremoting.core.Serializer;

public class HessianSerializer implements Serializer {

	@Override
	public int getId() {
		return 2;
	}

	@Override
	public String getName() {
		return "hessian";
	}

	@Override
	public ObjectOutput createObjectOutput(OutputStream out) {
		return new HessianObjectOutput(out);
	}

	@Override
	public ObjectInput createObjectInput(InputStream in) {
		return new HessianObjectInput(in);
	}

}
