package com.lewismsparlin.cctool.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;
import com.lewismsparlin.cctool.api.HtmlTransformer;

public class PsToCcTransformer implements HtmlTransformer {

	private static final String HREF_PLACEHOLDER = "HREF_PLACEHOLDER-%s";
	private static final Pattern HREF_FINDER = Pattern.compile("simpleurlproperty.*(HREF_PLACEHOLDER-[^\\\"]*)");
	private static final Pattern IMAGE_NAME_PATTERN = Pattern.compile("images/(.*)");

	private static final String IMAGE_PATH_PREFIX = "http://www.4statetrucks.com/";

	@Override
	public String getOutputFilename() {
		return "cc_ready.html";
	}

	public String transformHtml(String htmlBody) {
		Document origin = Jsoup.parse(htmlBody);

		Document output = origin.clone();
		output.select("tr").remove();
		Element outputTbody = output.select("tbody").first();

		Elements rows = origin.select("tr");
		rows.stream()
				.map(this::transformTr)
				.forEach(outputTbody::appendChild);

		return output.outerHtml();
	}

	public Map<String, String> promptForHrefPlaceholderReplacements(String output, Scanner inputScanner) {
		Map<String, String> hrefPlaceholderMap = new HashMap<>();
		List<String> placeholderHrefs = new ArrayList<>();
		Matcher placeholderMatcher = HREF_FINDER.matcher(output);
		while (placeholderMatcher.find()) {
			placeholderHrefs.add(placeholderMatcher.group(1));
		}

		String inputHref;
		for (String placeholderHref : placeholderHrefs) {
			Matcher imageNameMatcher = IMAGE_NAME_PATTERN.matcher(placeholderHref);
			imageNameMatcher.find();
			String imageName = imageNameMatcher.group(1);

			System.out.print("Link URL for " + imageName + ":  ");
			inputHref = inputScanner.next();
			hrefPlaceholderMap.put(placeholderHref, inputHref);
		}

		return hrefPlaceholderMap;
	}

	ImmutableList<Attribute> STATIC_ATTRIBUTES = ImmutableList.of(new Attribute("name", "mylink"),
			new Attribute("track", "true"));

	private Element transformTr(Element originalTr) {
		Element newTr = new Element(Tag.valueOf("tr"), "");
		originalTr.select("td").stream()
				.map(this::transformTd)
				.forEach(newTr::appendChild);

		return newTr;
	}

	private Element transformTd(Element originalTd) {
		Element newTd = new Element(Tag.valueOf("td"), "");
		if (originalTd.hasAttr("colspan")) {
			newTd.attr("colspan", originalTd.attr("colspan"));
		}
		if (originalTd.hasAttr("rowspan")) {
			newTd.attr("rowspan", originalTd.attr("rowspan"));
		}
		Element imageEl = originalTd.select("img").first();
		if (imageEl != null) {
			newTd.appendChild(this.transformImageToSimpleUrlProperty(imageEl));
		}

		return newTd;
	}

	private Element transformImageToSimpleUrlProperty(Element imageElement) {
		if (imageElement.attr("src").contains("spacer")) {
			return imageElement;
		}
		String imageUrl = imageElement.attr("src");
		Attributes attributes = new Attributes();
		STATIC_ATTRIBUTES.forEach(attributes::put);
		attributes.put("img", IMAGE_PATH_PREFIX + imageElement.attr("src"));

		Element simpleUrlProperty = new Element(Tag.valueOf("SimpleURLProperty"), "", attributes);
		Element img = imageElement.clone();
		img.attr("src", IMAGE_PATH_PREFIX + imageElement.attr("src"));
		Element a = new Element(Tag.valueOf("a"), "");
		a.attr("href", generateHrefPlaceholderForImageUrl(imageUrl));
		simpleUrlProperty.attr("href", generateHrefPlaceholderForImageUrl(imageUrl));
		a.appendChild(img);
		simpleUrlProperty.appendChild(a);

		return simpleUrlProperty;
	}

	private static String generateHrefPlaceholderForImageUrl(String imageUrl) {
		return String.format(HREF_PLACEHOLDER, imageUrl);
	}

}
