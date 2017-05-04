package org.pacien.tincapp.service;

import android.net.VpnService;

import com.annimon.stream.Stream;
import com.annimon.stream.function.BiFunction;
import com.annimon.stream.function.Consumer;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

import static org.pacien.tincapp.util.Function.applyIgnoringExcept;

/**
 * @author pacien
 */
final public class VpnInterfaceConfigurator {

	private VpnInterfaceConfigurator() {
		// static class
	}

	static final public String KEY_ADDRESSES = "Address";
	static final public String KEY_ROUTES = "Route";
	static final public String KEY_DNS_SERVERS = "DNSServer";
	static final public String KEY_SEARCH_DOMAINS = "SearchDomain";
	static final public String KEY_ALLOWED_APPLICATIONS = "AllowApplication";
	static final public String KEY_DISALLOWED_APPLICATIONS = "DisallowApplication";
	static final public String KEY_ALLOWED_FAMILIES = "AllowFamily";
	static final public String KEY_ALLOW_BYPASS = "AllowBypass";
	static final public String KEY_BLOCKING = "Blocking";
	static final public String KEY_MTU = "MTU";

	static private Stream<String> getStringStream(Configuration cfg, String key) {
		return Stream.ofNullable(cfg.getList(String.class, key));
	}

	static private Stream<Integer> getIntegerStream(Configuration cfg, String key) {
		return Stream.ofNullable(cfg.getList(Integer.class, key));
	}

	static private void doIf(Configuration cfg, String key, Consumer<Boolean> func) {
		if (cfg.getBoolean(key, false)) func.accept(true);
	}

	static private void doWithInt(Configuration cfg, String key, Consumer<Integer> func) {
		Stream.ofNullable(cfg.getInteger(key, null)).forEach(func);
	}

	static private <R> R applyAddressNetmask(BiFunction<String, Integer, R> func, String addrStr) {
		String[] addrParts = addrStr.split("/", 2);
		return func.apply(addrParts[0], Integer.parseInt(addrParts[1]));
	}

	static public VpnService.Builder applyConfiguration(VpnService.Builder net, Configuration cfg) {
		getStringStream(cfg, KEY_ADDRESSES).forEach(str -> applyAddressNetmask(net::addAddress, str));
		getStringStream(cfg, KEY_ROUTES).forEach(str -> applyAddressNetmask(net::addRoute, str));
		getStringStream(cfg, KEY_DNS_SERVERS).forEach(net::addDnsServer);
		getStringStream(cfg, KEY_SEARCH_DOMAINS).forEach(net::addSearchDomain);
		getStringStream(cfg, KEY_ALLOWED_APPLICATIONS).forEach(str -> applyIgnoringExcept(net::addAllowedApplication, str));
		getStringStream(cfg, KEY_DISALLOWED_APPLICATIONS).forEach(str -> applyIgnoringExcept(net::addDisallowedApplication, str));
		getIntegerStream(cfg, KEY_ALLOWED_FAMILIES).forEach(net::allowFamily);
		doIf(cfg, KEY_ALLOW_BYPASS, v -> net.allowBypass());
		doIf(cfg, KEY_BLOCKING, net::setBlocking);
		doWithInt(cfg, KEY_MTU, net::setMtu);

		return net;
	}

	static public VpnService.Builder applyConfiguration(VpnService.Builder net, File cfg) {
		try {
			return applyConfiguration(net, new Configurations().properties(cfg));
		} catch (ConfigurationException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

}
