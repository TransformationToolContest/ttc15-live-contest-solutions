package ttc15.tranj;

import com.beust.jcommander.JCommander;
import spoon.Launcher;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		final Commander commander = new Commander();
		new JCommander(commander, args);

		String processors = "";
		if (!commander.isAll()) {
			boolean hasProcessors = false;
			if (commander.isWithRetry()) {
				hasProcessors = true;
				processors += "ttc15.tranj.processors.RetryProcessor";
			}
			if (commander.isWithCache()) {
				if (hasProcessors) {
					processors += File.pathSeparator;
				}
				hasProcessors = true;
				processors += "ttc15.tranj.processors.CacheProcessor";
			}
			if (commander.isWithLogging()) {
				if (hasProcessors) {
					processors += File.pathSeparator;
				}
				processors += "ttc15.tranj.processors.LoggingProcessor";
			}
		}

		if (processors.isEmpty()) {
			processors = "ttc15.tranj.processors.RetryProcessor" + File.pathSeparator +
					"ttc15.tranj.processors.CacheProcessor" + File.pathSeparator +
					"ttc15.tranj.processors.LoggingProcessor";
		}

		new Launcher().run(new String[] {
				"-i", commander.getInput(),
				"-o", commander.getOutput(),
				"-p", processors
		});
	}
}
