package org.pacien.tincapp.service;

import android.app.Service;
import android.content.Intent;
import android.net.VpnService;

import org.pacien.tincapp.BuildConfig;
import org.pacien.tincapp.commands.Tinc;
import org.pacien.tincapp.commands.Tincd;
import org.pacien.tincapp.context.AppPaths;

import java.io.IOException;

import static org.pacien.tincapp.util.Function.applyIgnoringExcept;

/**
 * @author pacien
 */
public class TincVpnService extends VpnService {

	static final public String INTENT_EXTRA_NET_NAME = "netName";

	private String netName;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.netName = intent.getStringExtra(INTENT_EXTRA_NET_NAME);

		Builder net = new Builder().setSession(this.netName);
		VpnInterfaceConfigurator.applyConfiguration(net, AppPaths.netConfFile(this, this.netName));
		applyIgnoringExcept(net::addDisallowedApplication, BuildConfig.APPLICATION_ID);

		try {
			Tincd.start(this, this.netName, net.establish().detachFd());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		try {
			Tinc.stop(this, this.netName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
