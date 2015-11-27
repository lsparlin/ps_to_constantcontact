package com.lewismsparlin.cctool.api;

import java.util.Map;
import java.util.Scanner;

public interface HtmlTransformer {

	public String getOutputFilename();

	public String transformHtml(String htmlBody);

	public Map<String, String> promptForHrefPlaceholderReplacements(String output, Scanner inputScanner);

}
