package com.github.jremoting.util;

import java.lang.reflect.Method;

public class ReflectionUtil {
	
	
	public static Class<?> findClass(String className) throws ClassNotFoundException {
		if("int".equals(className)){
			return int.class;
		}
		if("boolean".equals(className)){
			return boolean.class;
		}
		else if ("long".equals(className)) {
			return long.class;
		}
		else if ("short".equals(className)) {
			return short.class;
		}
		else if ("float".equals(className)) {
			return float.class;
		}
		else if("double".equals(className)) {
			return double.class;
		}
		else if("byte".equals(className)){
			return byte.class;
		}
		else if ("char".equals(className)) {
			return char.class;
		}
		
		try {
			return ReflectionUtil.class.getClassLoader().loadClass(className);
		} catch (Exception e) {
			return null;
		}

	}

	
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {

		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
			for (Method method : methods) {
				if (methodName.equals(method.getName()) && isParameterTypeMatch(paramTypes, method.getParameterTypes())) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		
		return null;
	}
	
	private static boolean isParameterTypeMatch(Class<?>[] actual ,Class<?>[] expected) {
		if(actual == null && expected != null) {
			return false;
		}
		
		if(actual != null && expected == null) {
			return false;
		}
		
		if(actual == null && expected == null) {
			return true;
		}
		
		if(actual.length != expected.length) {
			return false;
		}
		for (int i=0;i<actual.length ;i++) {
			if(actual[i] != expected[i]) {
				return false;
			}
		}
		return true;
	}
}
