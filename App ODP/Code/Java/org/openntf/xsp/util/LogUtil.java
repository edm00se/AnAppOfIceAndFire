package org.openntf.xsp.util;

public class LogUtil {
	public static final boolean DEBUG_ENABLED = true;
	public static final boolean TRACE_ENABLED = true;

	public static void debug(final String output) {
		if (DEBUG_ENABLED) {
			Throwable rug = new Throwable();
			StackTraceElement[] history = rug.getStackTrace();
			StackTraceElement lastOperation = history.length > 1 ? history[1] : history[0];
			String className = lastOperation.getClassName();
			String methodName = lastOperation.getMethodName();
			int lineNumber = lastOperation.getLineNumber();
			System.out.println(className + "." + methodName + "() [" + lineNumber + "]: " + output);
		}
	}

	public static void trace(final Throwable t) {
		if (TRACE_ENABLED) {
			t.printStackTrace();
		}
	}

}
