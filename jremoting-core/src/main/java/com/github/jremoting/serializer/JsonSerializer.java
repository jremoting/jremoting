package com.github.jremoting.serializer;


import java.io.InputStream;
import java.io.OutputStream;

import com.github.jremoting.core.Serializer;
import com.github.jremoting.io.ObjectInput;
import com.github.jremoting.io.ObjectOutput;


public class JsonSerializer implements Serializer {

	@Override
	public int getId() {
		return 1;
	}

	@Override
	public String getName() {
		return "json";
	}

	@Override
	public ObjectOutput createObjectOutput(OutputStream out) {
		return new JsonObjectOutput(out);
	}

	@Override
	public ObjectInput createObjectInput(InputStream in) {
		return new JsonObjectInput(in);
	}
}
