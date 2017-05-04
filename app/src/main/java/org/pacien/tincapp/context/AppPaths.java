package org.pacien.tincapp.context;

import android.content.Context;

import java.io.File;

/**
 * @author pacien
 * @implNote Logs and PID files are stored in the cache directory for easy clean up.
 */
final public class AppPaths {

	private AppPaths() {
		// static class
	}

	static final private String CONFDIR = "conf";
	static final private String LOGDIR = "log";
	static final private String PIDDIR = "pid";

	static final private String TINCD_BIN = "libtincd.so";
	static final private String TINC_BIN = "libtinc.so";

	static final private String LOGFILE_FORMAT = "tinc.%s.log";
	static final private String PIDFILE_FORMAT = "tinc.%s.pid";

	static final private String NET_CONF_FILE = "network.conf";

	static private File createDirIfNotExists(File basePath, String newDir) {
		File f = new File(basePath, newDir);
		f.mkdirs();
		return f;
	}

	static public File confDir(Context ctx) {
		return ctx.getDir(CONFDIR, Context.MODE_PRIVATE);
	}

	static public File confDir(Context ctx, String netName) {
		return new File(confDir(ctx), netName);
	}

	static public File logDir(Context ctx) {
		return createDirIfNotExists(ctx.getCacheDir(), LOGDIR);
	}

	static public File pidDir(Context ctx) {
		return createDirIfNotExists(ctx.getCacheDir(), PIDDIR);
	}

	static public File logFile(Context ctx, String netName) {
		return new File(logDir(ctx), String.format(LOGFILE_FORMAT, netName));
	}

	static public File pidFile(Context ctx, String netName) {
		return new File(pidDir(ctx), String.format(PIDFILE_FORMAT, netName));
	}

	static public File netConfFile(Context ctx, String netName) {
		return new File(confDir(ctx, netName), NET_CONF_FILE);
	}

	static public File binDir(Context ctx) {
		return new File(ctx.getApplicationInfo().nativeLibraryDir);
	}

	static public File tincd(Context ctx) {
		return new File(binDir(ctx), TINCD_BIN);
	}

	static public File tinc(Context ctx) {
		return new File(binDir(ctx), TINC_BIN);
	}

}
