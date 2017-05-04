package org.pacien.tincapp.commands;

import android.content.Context;

import com.annimon.stream.Stream;

import org.pacien.tincapp.context.AppPaths;

import java.io.IOException;
import java.util.List;

/**
 * @author pacien
 */
final public class Tinc {

	private Tinc() {
		// static class
	}

	static private Command newCommand(Context ctx, String netName) {
		return new Command(AppPaths.tinc(ctx).getAbsolutePath())
				.withOption("config", AppPaths.confDir(ctx, netName).getAbsolutePath())
				.withOption("pidfile", AppPaths.pidFile(ctx, netName).getAbsolutePath());
	}

	// independently runnable commands

	static public List<String> network(Context ctx) throws IOException {
		return Executor.call(new Command(AppPaths.tinc(ctx).getAbsolutePath())
				.withOption("config", AppPaths.confDir(ctx).getAbsolutePath())
				.withArguments("network"));
	}

	static public List<String> fsck(Context ctx, String netName, Boolean fix) throws IOException {
		Command cmd = newCommand(ctx, netName).withArguments("fsck");
		if (fix) cmd = cmd.withOption("force");
		return Executor.call(cmd);
	}

	// commands requiring a running tinc daemon

	static public void stop(Context ctx, String netName) throws IOException {
		Executor.call(newCommand(ctx, netName).withArguments("stop"));
	}

	static public List<String> dumpNodes(Context ctx, String netName, Boolean reachable) throws IOException {
		Command cmd = reachable
				? newCommand(ctx, netName).withArguments("dump", "reachable", "nodes")
				: newCommand(ctx, netName).withArguments("dump", "nodes");

		return Executor.call(cmd);
	}

	static public String info(Context ctx, String netName, String node) throws IOException {
		List<String> output = Executor.call(newCommand(ctx, netName).withArguments("info", node));
		return Stream.of(output).reduce((l, r) -> l + '\n' + r).get();
	}

}
