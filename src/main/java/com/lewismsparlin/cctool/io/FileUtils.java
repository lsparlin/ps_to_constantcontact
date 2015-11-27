package com.lewismsparlin.cctool.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import com.lewismsparlin.cctool.api.HtmlTransformer;

public class FileUtils {

	public static Optional<String> readHtmlFile(String directoryName, HtmlTransformer transformer) {
		Optional<String> filename = findHtmlFileName(directoryName, transformer.getOutputFilename());

		return filename
				.flatMap(path -> Optional.ofNullable(readFileAsString(path)));
	}

	private static Optional<String> findHtmlFileName(String directoryPath, String outputFileName) {
		File directory = new File(directoryPath);
		if (directory.exists()) {
			File[] htmlFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".html") && !name.toLowerCase().equals(outputFileName));

			return Stream.of(htmlFiles).findFirst()
					.flatMap(file -> Optional.of(file.getAbsolutePath()));
		} else {
			return Optional.empty();
		}
	}

	private static String readFileAsString(String filename) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(filename));
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
