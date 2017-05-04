package org.pacien.tincapp.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pacien
 */
final class Executor {

	private Executor() {
		// static class
	}

	static {
		System.loadLibrary("exec");
	}

	/**
	 * @return -1 on error, forked child PID otherwise
	 */
	static private native int forkExec(String[] argcv);

	static public void forkExec(Command cmd) throws IOException {
		if (forkExec(cmd.asArray()) == -1)
			throw new IOException();
	}

	static public List<String> call(Command cmd) throws IOException {
		Process proc = new ProcessBuilder(cmd.asList()).start();
		BufferedReader outputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		String line;
		List<String> list = new LinkedList<>();
		while ((line = outputReader.readLine()) != null) list.add(line);

		return Collections.unmodifiableList(list);
	}

}
