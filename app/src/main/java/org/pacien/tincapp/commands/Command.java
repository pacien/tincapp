package org.pacien.tincapp.commands;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pacien
 */
class Command {

	static private class Option {
		final String key;
		final Optional<java.lang.String> val;

		Option(String key, String val) {
			this.key = key;
			this.val = Optional.ofNullable(val);
		}

		@Override
		public String toString() {
			return val.isPresent() ? "--" + key + "=" + val.get() : "--" + key;
		}
	}

	final private String cmd;
	final private List<Option> opts;
	final private List<String> args;

	public Command(String cmd) {
		this.cmd = cmd;
		this.opts = new LinkedList<>();
		this.args = new LinkedList<>();
	}

	public Command withOption(String key, String val) {
		this.opts.add(new Option(key, val));
		return this;
	}

	public Command withOption(String key) {
		return this.withOption(key, null);
	}

	public Command withArguments(String... args) {
		this.args.addAll(Arrays.asList(args));
		return this;
	}

	public List<String> asList() {
		return Collections.unmodifiableList(
				Stream.concat(Stream.of(Collections.singleton(this.cmd)),
						Stream.concat(Stream.of(this.opts).map(Option::toString), Stream.of(this.args)))
						.collect(Collectors.toList()));
	}

	public String[] asArray() {
		return this.asList().toArray(new String[0]);
	}

}
