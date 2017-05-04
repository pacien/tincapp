package org.pacien.tincapp.util;

import com.annimon.stream.function.FunctionalInterface;

/**
 * @author pacien
 */
final public class Function {

	private Function() {
		// static class
	}

	@FunctionalInterface
	public interface CheckedFunction<T, R> {
		R apply(T t) throws Exception;
	}

	static public <T, R> R applyIgnoringExcept(CheckedFunction<T, R> func, T parm) {
		try {
			return func.apply(parm);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
