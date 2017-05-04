package org.pacien.tincapp.context;

import android.content.res.Resources;
import android.os.Build;

import com.annimon.stream.Stream;

import org.pacien.tincapp.BuildConfig;
import org.pacien.tincapp.R;

/**
 * @author pacien
 */
final public class AppInfo {

	private AppInfo() {
		// static class
	}

	static public String appVersion(Resources r) {
		return r.getString(R.string.info_version_format,
				BuildConfig.VERSION_NAME,
				BuildConfig.BUILD_TYPE);
	}

	static public String androidVersion(Resources r) {
		return r.getString(R.string.info_running_on_format,
				Build.VERSION.CODENAME,
				Build.VERSION.RELEASE);
	}

	static public String supportedABIs(Resources r) {
		return r.getString(R.string.info_supported_abis_format,
				Stream.of(Build.SUPPORTED_ABIS).reduce((x, y) -> x + "," + y).get());
	}

	static public String all(Resources r) {
		return Stream.of(appVersion(r), androidVersion(r), supportedABIs(r))
				.reduce((x, y) -> x + '\n' + y).get();
	}

}
