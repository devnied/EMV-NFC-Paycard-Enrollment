package com.github.devnied.emvnfccard.utils;

import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AtrTest {

	@Test
	public void testDescriptionFromATR() {

		Assertions.assertThat(AtrUtils.getDescription("3B 02 14 50")).isEqualTo(Arrays.asList("Schlumberger Multiflex 3k"));
		Assertions.assertThat(AtrUtils.getDescription(null)).isEqualTo(null);
		Assertions
				.assertThat(AtrUtils.getDescription("3B 07 64 11.........."))
				.isEqualTo(
						Arrays.asList("HID Corporate 1000 Format"));
	}

	@Test
	public void testDescriptionFromATS() {

		Assertions.assertThat(AtrUtils.getDescriptionFromAts("20 63 CB A3 A0")).contains(
				"VISA card from Banque Populaire");
		Assertions.assertThat(AtrUtils.getDescription(null)).isEqualTo(null);
	}

	@Test
	public void testDescriptionFromATS2() {

		Assertions.assertThat(AtrUtils.getDescriptionFromAts("0F 55 B0 11 69 FF 4A 50 D0 80 00 49 54 03")).contains(
				"Sky (Italy) VideoGuard CAM card");
	}

	@Test
	public void testDescriptionFromATR_array() {
		Assertions.assertThat(AtrUtils.getDescription("3F 65 35 10 00 04 6C")).isEqualTo(Collections.singletonList("Postcard (Switzerland)"));
		Assertions.assertThat(AtrUtils.getDescription("3F 65 35 10 00 04 EC")).isEqualTo(Collections.singletonList("Postcard (Switzerland)"));
		Assertions.assertThat(AtrUtils.getDescription("3F 65 35 10 02 04 EC")).isEqualTo(Collections.singletonList("Postcard (Switzerland)"));
	}

	/**
	 * Descriptions that quote a link inside their text used to be discarded
	 * together with the lines that are only a link, which left three cards of
	 * the refreshed list described by nothing at all. This ATR is one of them,
	 * every description it carries quotes a source.
	 */
	@Test
	public void testDescriptionQuotingALinkIsKept() {
		Assertions.assertThat(AtrUtils.getDescription("3B 6F 00 00 80 5A 28 11 42 10 12 2B 27 64 E7 35 82"))
				.isEqualTo(Collections.singletonList(
						"Navigo pass, Paris (France) transport network (https://en.wikipedia.org/wiki/Navigo_pass) (Transport)"));
	}

	/**
	 * A line that is only a link is a reference to a document, not a
	 * description, and is still left out.
	 */
	@Test
	public void testBareLinkIsNotADescription() {
		Assertions.assertThat(AtrUtils.getDescription("3B 7B 18 00 00 00 31 C0 64 90 E3 11 00 82"))
				.isEqualTo(Collections.singletonList(
						"oberthur card in the middle http://postarca.posta.si/downloadfile.aspx?fileid=16918 (eID)"));
	}

	/**
	 * The ATR list shipped as a resource was replaced by a newer revision. The
	 * loader of {@link AtrUtils} classifies every line of it into one of a few
	 * shapes and logs an error for anything else, which a test cannot see, so
	 * the same classification is replayed here: no line of the file may fall
	 * through, and every ATR must carry at least one description.
	 * <p>
	 * The second assertion is the one the refreshed list broke. Its
	 * descriptions and its references to a document are indented alike, and the
	 * loader used to discard any line quoting a link, which left three cards
	 * described by nothing at all.
	 * </p>
	 */
	@Test
	public void testAtrListIsFullyParsed() throws Exception {
		List<String> unexpected = new ArrayList<String>();
		int atrCount = 0;
		int describedCount = 0;
		int descriptionsOfCurrent = 0;
		boolean hasCurrent = false;

		InputStream is = AtrUtils.class.getResourceAsStream("/smartcard_list.txt");
		Assertions.assertThat(is).isNotNull();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String trimmed = line.trim();
				boolean reference = (trimmed.startsWith("http://") || trimmed.startsWith("https://"))
						&& !StringUtils.containsWhitespace(trimmed);
				if (line.startsWith("#") || trimmed.length() == 0 || reference) {
					continue;
				} else if (line.startsWith("\t")) {
					// A description before any ATR would be dropped silently
					Assertions.assertThat(hasCurrent).isTrue();
					descriptionsOfCurrent++;
				} else if (line.startsWith("3")) {
					if (hasCurrent && descriptionsOfCurrent > 0) {
						describedCount++;
					}
					atrCount++;
					hasCurrent = true;
					descriptionsOfCurrent = 0;
				} else {
					unexpected.add(line);
				}
			}
		} finally {
			br.close();
		}
		if (hasCurrent && descriptionsOfCurrent > 0) {
			describedCount++;
		}

		Assertions.assertThat(unexpected).isEmpty();
		// The revision in the repository holds 5124 of them, the bound is kept
		// loose so that the next refresh of the list does not fail the build
		Assertions.assertThat(atrCount).isGreaterThan(5000);
		Assertions.assertThat(describedCount).isEqualTo(atrCount);
	}

}
