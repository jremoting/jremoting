package com.github.jremoting.core;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {
	
	 int getId();
	
	 String getName();
	 
	 ObjectOutput createObjectOutput(OutputStream out);
	 
	 ObjectInput createObjectInput(InputStream in);
}
