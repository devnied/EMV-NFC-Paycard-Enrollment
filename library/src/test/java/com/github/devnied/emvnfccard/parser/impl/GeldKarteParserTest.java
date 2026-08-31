package com.github.devnied.emvnfccard.parser.impl;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

/**
 * The GeldKarte purse and the girocard applications are built on the same
 * German RID 'D276000025': the parser has to handle the first ones and leave
 * the second ones to the EMV parser.
 */
public class GeldKarteParserTest {

	private static final String GELDKARTE_AID = "D27600002545500200";

	private static final String GIROCARD_AID = "D27600002547410100";

	@Test
	public void testOnlyThePurseAidsAreHandled() {
		GeldKarteParser parser = new GeldKarteParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());

		// The two purse AIDs of the scheme
		Assertions.assertThat(parser.getId().matcher(GELDKARTE_AID).matches()).isTrue();
		Assertions.assertThat(parser.getId().matcher("D27600002545500100").matches()).isTrue();
		// The girocard application, and the RID alone, are not purses
		Assertions.assertThat(parser.getId().matcher(GIROCARD_AID).matches()).isFalse();
		Assertions.assertThat(parser.getId().matcher("D276000025").matches()).isFalse();
		// The RID is declared in the scheme but must not be in the pattern
		Assertions.assertThat(EmvCardScheme.GELDKARTE.getAid()).contains("D2 76 00 00 25");
	}

}
