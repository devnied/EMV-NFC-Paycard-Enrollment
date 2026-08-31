package com.github.devnied.emvnfccard.parser.impl;

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * The name actually sent in the SELECT of an application is recorded, with
 * whether the Extended Selection was refused by the card or too long to be
 * appended.
 */
public class ApplicationSelectionNameTestP2 {

	private static final String AID = "A0000000041010";

	private static final String EXTENDED_SELECTION = "1234";

	private static final String EXTENDED_SELECT = "00A4040009" + AID + EXTENDED_SELECTION + "00";

	private static final String PLAIN_SELECT = "00A4040007" + AID + "00";

	private Application application(final String pAid, final String pExtendedSelection) {
		Application app = new Application();
		app.setAid(BytesUtils.fromString(pAid));
		if (pExtendedSelection != null) {
			app.setExtendedSelection(BytesUtils.fromString(pExtendedSelection));
		}
		return app;
	}

	private static String hex(final byte[] pValue) {
		return pValue == null ? null : BytesUtils.bytesToStringNoSpace(pValue);
	}

	@Test
	public void testTheExtendedSelectionIsAppended() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(EXTENDED_SELECT, "6F 09 84 07 " + AID + " 9000");
		Application app = application(AID, EXTENDED_SELECTION);

		new EmvParser(EmvTemplate.Builder().setProvider(provider).build()).selectApplication(app);

		Assertions.assertThat(provider.getReceived()).isEqualTo(Arrays.asList(EXTENDED_SELECT));
		Assertions.assertThat(hex(app.getSelectedName())).isEqualTo(AID + EXTENDED_SELECTION);
		Assertions.assertThat(app.isExtendedSelectionRefused()).isFalse();
		Assertions.assertThat(app.isExtendedSelectionTooLong()).isFalse();
	}

	@Test
	public void testTheExtendedSelectionRefusedFallsBackOnTheAid() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(EXTENDED_SELECT, "6A82").on(PLAIN_SELECT,
				"6F 09 84 07 " + AID + " 9000");
		Application app = application(AID, EXTENDED_SELECTION);

		byte[] response = new EmvParser(EmvTemplate.Builder().setProvider(provider).build()).selectApplication(app);

		Assertions.assertThat(hex(response)).endsWith("9000");
		Assertions.assertThat(provider.getReceived()).isEqualTo(Arrays.asList(EXTENDED_SELECT, PLAIN_SELECT));
		Assertions.assertThat(hex(app.getSelectedName())).isEqualTo(AID);
		Assertions.assertThat(app.isExtendedSelectionRefused()).isTrue();
		Assertions.assertThat(app.isExtendedSelectionTooLong()).isFalse();
	}

	@Test
	public void testTheExtendedSelectionTooLongIsNotAppended() throws CommunicationException {
		// A 15 bytes AID plus 2 bytes exceeds the 16 bytes of a name
		String longAid = "A00000000410100000000000000000";
		String select = "00A404000F" + longAid + "00";
		ScriptedProvider provider = new ScriptedProvider().on(select, "9000");
		Application app = application(longAid, EXTENDED_SELECTION);

		new EmvParser(EmvTemplate.Builder().setProvider(provider).build()).selectApplication(app);

		Assertions.assertThat(provider.getReceived()).isEqualTo(Arrays.asList(select));
		Assertions.assertThat(hex(app.getSelectedName())).isEqualTo(longAid);
		Assertions.assertThat(app.isExtendedSelectionTooLong()).isTrue();
		Assertions.assertThat(app.isExtendedSelectionRefused()).isFalse();
	}

	@Test
	public void testWithoutExtendedSelection() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(PLAIN_SELECT, "9000");
		Application app = application(AID, null);

		new EmvParser(EmvTemplate.Builder().setProvider(provider).build()).selectApplication(app);

		Assertions.assertThat(hex(app.getSelectedName())).isEqualTo(AID);
		Assertions.assertThat(app.isExtendedSelectionRefused()).isFalse();
		Assertions.assertThat(app.isExtendedSelectionTooLong()).isFalse();

		// The next occurrence of a partial AID is selected with the same name
		Application next = application(AID, null);
		next.setNextOccurrence(true);
		new EmvParser(EmvTemplate.Builder().setProvider(provider).build()).selectApplication(next);
		Assertions.assertThat(hex(next.getSelectedName())).isEqualTo(AID);
		Assertions.assertThat(provider.getReceived().get(1)).isEqualTo("00A4040207" + AID + "00");
	}

}
