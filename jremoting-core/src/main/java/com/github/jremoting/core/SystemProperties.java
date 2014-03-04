package com.github.jremoting.core;

public class SystemProperties {
	public static final String APP_NAME = System.getProperty("jremoting.app.name","jremoting");
	public static final boolean DEV_MODE = Boolean.parseBoolean(System.getProperty("jremoting.dev.mode", "false"));
}
