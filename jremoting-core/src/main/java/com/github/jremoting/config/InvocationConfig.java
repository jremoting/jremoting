package com.github.jremoting.config;

public interface InvocationConfig {
	public String getStringConfig(String key);
	public int getIntConfig(String key);
	public long getLongConfig(String key);
	public String[] getStringsConfig(String key);
}
