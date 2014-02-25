package com.github.jremoting.route;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;


import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;


public class JavaRouteRuleParser implements RouteRuleParser {

	private final Logger logger = LoggerFactory.getLogger(JavaRouteRuleParser.class);

	private AtomicInteger id = new AtomicInteger(0);

	@Override
	public RouteRule parse(String content) {
	
		return complieRouteRule(content);
		
	}

	public RouteRule complieRouteRule(String classSource) {

		try {

			String packageName = "com.github.jremoting.routerule"
					+ id.incrementAndGet();
			String className = packageName + ".RouteRule";
			classSource = "package " + packageName + ";\n" + classSource;
			JavaFileObject file = new JavaSourceFromString(className,
					classSource);

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

			Iterable<? extends JavaFileObject> compilationUnits = Arrays
					.asList(file);

			final List<ByteArrayJavaClass> classFileObjects = new ArrayList<ByteArrayJavaClass>();

			JavaFileManager fileManager = compiler.getStandardFileManager(
					diagnostics, null, null);
			fileManager = new ForwardingJavaFileManager<JavaFileManager>(
					fileManager) {
				@Override
				public JavaFileObject getJavaFileForOutput(Location location,
						String className, Kind kind,
						javax.tools.FileObject sibling) throws IOException {

					ByteArrayJavaClass fileObject = new ByteArrayJavaClass(
							className);
					classFileObjects.add(fileObject);
					return fileObject;
				}
			};

			CompilationTask task = compiler.getTask(null, fileManager,
					diagnostics, null, null, compilationUnits);

			boolean success = task.call();
			if (!success) {
				logComplieError(diagnostics.getDiagnostics());
				return null;
			}

			Map<String, byte[]> byteCodeMap = new HashMap<String, byte[]>();
			for (ByteArrayJavaClass cl : classFileObjects) {
				byteCodeMap.put(cl.getName().substring(1), cl.getBytes());
			}

			ClassLoader loader = new MapClassLoader(byteCodeMap);
			Object obj = loader.loadClass(className).newInstance();

			if (obj instanceof RouteRule) {
				return (RouteRule) obj;
			} else {
				logger.warn("route rule must implement ParameterRouteRule or ServiceRouteRule or MethodRouteRule!");
				return null;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private void logComplieError(List<Diagnostic<? extends JavaFileObject>> list) {
		for (Diagnostic<?> diagnostic : list) {
			logger.error(diagnostic.toString());
		}
	}

	public static class ByteArrayJavaClass extends SimpleJavaFileObject {
		private ByteArrayOutputStream stream;

		/**
		 * Constructs a new ByteArrayJavaClass.
		 * 
		 * @param name
		 *            the name of the class file represented by this file object
		 */
		public ByteArrayJavaClass(String name) {
			super(URI.create("bytes:///" + name), Kind.CLASS);
			stream = new ByteArrayOutputStream();
		}

		public OutputStream openOutputStream() throws IOException {
			return stream;
		}

		public byte[] getBytes() {
			return stream.toByteArray();
		}
	}

	public static class MapClassLoader extends ClassLoader {
		private Map<String, byte[]> classes;

		public MapClassLoader(Map<String, byte[]> classes) {
			this.classes = classes;
		}

		protected Class<?> findClass(String name) throws ClassNotFoundException {
			byte[] classBytes = classes.get(name);
			if (classBytes == null)
				throw new ClassNotFoundException(name);
			Class<?> cl = defineClass(name, classBytes, 0, classBytes.length);
			if (cl == null)
				throw new ClassNotFoundException(name);
			return cl;
		}
	}

	public static class JavaSourceFromString extends SimpleJavaFileObject {
		final String code;

		JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/')
					+ Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}

}
