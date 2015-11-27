package com.lewismsparlin.cctool;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.google.common.io.Files;
import com.lewismsparlin.cctool.api.HtmlTransformer;
import com.lewismsparlin.cctool.io.FileUtils;

public class CCToolApplication {

	private static HtmlTransformer transformer = InstanceFactory.transformer();

	public static void main(String[] args) {

		Scanner userInputScanner = new Scanner(System.in);
		String projectDirectory = askForDirectoryName(userInputScanner);
		Optional<String> htmlBody = FileUtils.readHtmlFile(getDirectoryAbsolutePath(projectDirectory), transformer);

		if (htmlBody.isPresent()) {
			String output = transformer.transformHtml(htmlBody.get());
			Map<String, String> placeholderMap = transformer.promptForHrefPlaceholderReplacements(output, userInputScanner);

			for (String placeholder : placeholderMap.keySet()) {
				output = output.replaceAll(placeholder, placeholderMap.get(placeholder));
			}

			try {
				Files.write(output.getBytes(), outputFile(getDirectoryAbsolutePath(projectDirectory)));
			} catch (IOException e) {
				System.err.println("Failed to write output file");
			}
		} else {
			System.out.println("ERROR: Unable to find html file or directory: Desktop/" + projectDirectory);
		}

		userInputScanner.close();
	}

	private static String askForDirectoryName(Scanner inputScanner) {
		String projectDirectory;
		System.out.print("Project Directory Name: ");
		projectDirectory = inputScanner.next();

		return projectDirectory;
	}

	private static String getDirectoryAbsolutePath(String directoryName) {
		String userDirecotry = System.getProperty("user.home");
		return String.format("%s/Desktop/%s/", userDirecotry, directoryName);
	}

	private static File outputFile(String directoryName) {
		File output = new File(directoryName + transformer.getOutputFilename());

		return output;
	}

}
