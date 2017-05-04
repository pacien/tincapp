package org.pacien.tincapp.commands;

import android.annotation.SuppressLint;
import android.content.Context;

import com.annimon.stream.Stream;

import org.pacien.tincapp.context.AppPaths;

import java.io.File;

/**
 * @author pacien
 */
final public class PermissionFixer {

	private PermissionFixer() {
		// static class
	}

	@SuppressLint({"SetWorldReadable", "SetWorldWritable"})
	static private Boolean setAllRWXPermissions(File f) {
		return f.setReadable(true, false)
				&& f.setWritable(true, false)
				&& f.setExecutable(true, false);
	}

	static public Boolean makePrivateDirsPublic(Context ctx) {
		return Stream.of(AppPaths.confDir(ctx), AppPaths.logDir(ctx), AppPaths.pidDir(ctx))
				.map(PermissionFixer::setAllRWXPermissions)
				.reduce((x, y) -> x && y).get();
	}

}
