package ttc15.tranj;

import com.beust.jcommander.Parameter;

public class Commander {
	@Parameter(names = "-all", description = "Executes all annotations")
	private boolean all = false;

	@Parameter(names = "-cache", description = "Executes cacheable annotation")
	private boolean withCache = false;

	@Parameter(names = "-logging", description = "Executes loggable annotation")
	private boolean withLogging = false;

	@Parameter(names = "-retry", description = "Executes retry on failure annotation")
	private boolean withRetry = false;

	@Parameter(names = "-input", description = "Path for the input")
	private String input = "./src/main/java/ttc15/tranj/examples/FinalURLDownload.java";

	@Parameter(names = "-output", description = "Path for the output")
	private String output = "./target";

	public boolean isAll() {
		return all;
	}

	public boolean isWithCache() {
		return withCache;
	}

	public boolean isWithLogging() {
		return withLogging;
	}

	public boolean isWithRetry() {
		return withRetry;
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}
}
