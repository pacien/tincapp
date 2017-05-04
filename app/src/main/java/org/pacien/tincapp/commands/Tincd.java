package org.pacien.tincapp.commands;

import android.content.Context;

import org.pacien.tincapp.context.AppPaths;

import java.io.IOException;

/**
 * @author pacien
 */
final public class Tincd {

	private Tincd() {
		// static class
	}

	static public void start(Context ctx, String netName, Integer fd) throws IOException {
		Executor.forkExec(new Command(AppPaths.tincd(ctx).getAbsolutePath())
				.withOption("no-detach")
				.withOption("config", AppPaths.confDir(ctx, netName).getAbsolutePath())
				.withOption("pidfile", AppPaths.pidFile(ctx, netName).getAbsolutePath())
				.withOption("logfile", AppPaths.logFile(ctx, netName).getAbsolutePath())
				.withOption("option", "DeviceType=fd")
				.withOption("option", "Device=" + fd));
	}

}
