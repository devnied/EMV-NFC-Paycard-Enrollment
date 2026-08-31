package com.github.devnied.emvnfccard.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.model.enums.KernelTypeEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * The model exposes everything the parsers read: the raw responses, the
 * status words, the decoded values the typed getters lose and the outcome of
 * every step of the reading. Each new field is a plain bean property, kept
 * non transient so that an integrator persisting the card (Gson, Java
 * serialization) keeps it.
 */
public class ModelFieldsTest {

	/**
	 * Types Gson maps without an adapter, the ones the application persisting
	 * the card can reload
	 */
	private static final List<Class<?>> GSON_FRIENDLY = Arrays.<Class<?>> asList(String.class, byte[].class, int.class,
			long.class, float.class, boolean.class, Integer.class, Float.class, Date.class, BigDecimal.class, List.class,
			Map.class, java.util.Collection.class);

	/**
	 * Every non static, non transient field of a model class must hold a value
	 * Gson can write and read back: a primitive, a string, a byte array, a
	 * date, an enum, a list or a map, or another model class.
	 *
	 * @param pClass
	 *            class to check
	 */
	private static void assertGsonFriendly(final Class<?> pClass) {
		for (Class<?> c = pClass; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field field : c.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
					continue;
				}
				Class<?> type = field.getType();
				boolean ok = GSON_FRIENDLY.contains(type) || type.isEnum() || AbstractData.class.isAssignableFrom(type);
				Assertions.assertThat(ok).as(pClass.getSimpleName() + "." + field.getName() + " : " + type.getName())
						.isTrue();
				Assertions.assertThat(type.isInterface() && !GSON_FRIENDLY.contains(type))
						.as(pClass.getSimpleName() + "." + field.getName() + " is an interface").isFalse();
			}
		}
	}

	/**
	 * Java serialization round trip, the strictest check that no non
	 * transient field holds something that cannot be persisted.
	 *
	 * @param pObject
	 *            object to serialize
	 * @return the deserialized copy
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Serializable> T roundTrip(final T pObject) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytes);
		out.writeObject(pObject);
		out.close();
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
		return (T) in.readObject();
	}

	@Test
	public void testModelClassesAreGsonFriendly() {
		for (Class<?> c : Arrays.<Class<?>> asList(EmvCard.class, Application.class, DirectoryEntry.class,
				EmvTransactionRecord.class, EmvTrack1.class, EmvTrack2.class, CPLC.class, DataEnvelopes.class,
				MagStripeData.class, RecordData.class, PaymentDirectory.class, TransactionLog.class,
				CardTransactionQualifiers.class, GeldKarteData.class, Afl.class, CardIdentification.class)) {
			assertGsonFriendly(c);
		}
	}

	// F01 - RecordData

	@Test
	public void testRecordData() {
		RecordData record = new RecordData();
		Assertions.assertThat(record.getSfi()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(record.getRecordNumber()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(record.getRaw()).isNull();
		Assertions.assertThat(record.getStatusWord()).isNull();

		record = new RecordData(2, 3, new byte[0], "6A83");
		Assertions.assertThat(record.getSfi()).isEqualTo(2);
		Assertions.assertThat(record.getRecordNumber()).isEqualTo(3);
		Assertions.assertThat(record.getRaw()).isEmpty();
		Assertions.assertThat(record.getStatusWord()).isEqualTo("6A83");

		record.setSfi(1);
		record.setRecordNumber(1);
		record.setRaw(BytesUtils.fromString("70 02 5A 00"));
		record.setStatusWord("9000");
		Assertions.assertThat(record.getSfi()).isEqualTo(1);
		Assertions.assertThat(record.getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(record.getRaw())).isEqualTo("7002" + "5A00");
		Assertions.assertThat(record.getStatusWord()).isEqualTo("9000");
	}

	// F01 - PaymentDirectory

	@Test
	public void testPaymentDirectoryDefaults() {
		PaymentDirectory directory = new PaymentDirectory();
		Assertions.assertThat(directory.getName()).isNull();
		Assertions.assertThat(directory.getRawFci()).isNull();
		Assertions.assertThat(directory.getSelectStatusWord()).isNull();
		Assertions.assertThat(directory.getDepth()).isEqualTo(0);
		Assertions.assertThat(directory.getRawSfi()).isNull();
		Assertions.assertThat(directory.getSfi()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(directory.isSfiValid()).isFalse();
		Assertions.assertThat(directory.getRecords()).isNotNull().isEmpty();
		Assertions.assertThat(directory.getEndStatusWord()).isNull();
		Assertions.assertThat(directory.isRecordLimitReached()).isFalse();
		Assertions.assertThat(directory.getSubDirectoryNames()).isNotNull().isEmpty();
		Assertions.assertThat(directory.getLanguagePreferences()).isNotNull().isEmpty();
		Assertions.assertThat(directory.getIssuerCodeTableIndex()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(directory.getDataObjects()).isNotNull().isEmpty();
	}

	@Test
	public void testPaymentDirectory() {
		PaymentDirectory directory = new PaymentDirectory();
		byte[] name = "2PAY.SYS.DDF01".getBytes();
		byte[] fci = BytesUtils.fromString("6F 10 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31");
		directory.setName(name);
		directory.setRawFci(fci);
		directory.setSelectStatusWord("9000");
		directory.setDepth(1);
		directory.setRawSfi(new byte[] { 0x01 });
		directory.setSfi(1);
		directory.setSfiValid(true);
		directory.getRecords().add(new RecordData(1, 1, new byte[] { 0x70, 0x00 }, "9000"));
		directory.getRecords().add(new RecordData(1, 2, new byte[0], "6A83"));
		directory.setEndStatusWord("6A83");
		directory.setRecordLimitReached(true);
		directory.getSubDirectoryNames().add(BytesUtils.fromString("A0 00 00 00 03"));
		directory.getLanguagePreferences().add("fr");
		directory.setIssuerCodeTableIndex(1);
		directory.getDataObjects().put("84", name);

		Assertions.assertThat(directory.getName()).isEqualTo(name);
		Assertions.assertThat(directory.getRawFci()).isEqualTo(fci);
		Assertions.assertThat(directory.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(directory.getDepth()).isEqualTo(1);
		Assertions.assertThat(directory.getRawSfi()).isEqualTo(new byte[] { 0x01 });
		Assertions.assertThat(directory.getSfi()).isEqualTo(1);
		Assertions.assertThat(directory.isSfiValid()).isTrue();
		Assertions.assertThat(directory.getRecords()).hasSize(2);
		Assertions.assertThat(directory.getRecords().get(1).getRaw()).isEmpty();
		Assertions.assertThat(directory.getRecords().get(1).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(directory.getEndStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(directory.isRecordLimitReached()).isTrue();
		Assertions.assertThat(directory.getSubDirectoryNames()).hasSize(1);
		Assertions.assertThat(directory.getLanguagePreferences()).containsExactly("fr");
		Assertions.assertThat(directory.getIssuerCodeTableIndex()).isEqualTo(1);
		Assertions.assertThat(directory.getDataObjects().get("84")).isEqualTo(name);

		// A null collection is an empty one, never null
		directory.setRecords(null);
		directory.setSubDirectoryNames(null);
		directory.setLanguagePreferences(null);
		directory.setDataObjects(null);
		Assertions.assertThat(directory.getRecords()).isNotNull().isEmpty();
		Assertions.assertThat(directory.getSubDirectoryNames()).isNotNull().isEmpty();
		Assertions.assertThat(directory.getLanguagePreferences()).isNotNull().isEmpty();
		Assertions.assertThat(directory.getDataObjects()).isNotNull().isEmpty();
		List<RecordData> records = new ArrayList<RecordData>();
		directory.setRecords(records);
		Assertions.assertThat(directory.getRecords()).isSameAs(records);
	}

	// F01, F02, F03, F06, F07, F08, F09 - EmvCard

	@Test
	public void testEmvCardDefaults() {
		EmvCard card = new EmvCard();
		Assertions.assertThat(card.getPaymentDirectories()).isNotNull().isEmpty();
		Assertions.assertThat(card.getPaymentEnvironmentStatusWord()).isNull();
		Assertions.assertThat(card.isDirectoryDepthLimitReached()).isFalse();
		Assertions.assertThat(card.isDirectoryCountLimitReached()).isFalse();
		Assertions.assertThat(card.getUnreadDdfNames()).isNotNull().isEmpty();
		Assertions.assertThat(card.getIgnoredDirectoryEntries()).isNotNull().isEmpty();
		Assertions.assertThat(card.getLockedStatusWord()).isNull();
		Assertions.assertThat(card.isReadWithAid()).isFalse();
		Assertions.assertThat(card.getUnparsedTrack1()).isNull();
		Assertions.assertThat(card.getUnparsedTrack2()).isNull();
		Assertions.assertThat(card.getOrphanTrack1DiscretionaryData()).isNull();
		Assertions.assertThat(card.getOrphanTrack2DiscretionaryData()).isNull();
		Assertions.assertThat(card.getRawAt()).isNull();
		Assertions.assertThat(card.isAtLookedUp()).isFalse();
		Assertions.assertThat(card.getRawCplcResponse()).isNull();
		Assertions.assertThat(card.getCplcStatusWord()).isNull();
		Assertions.assertThat(card.getAidType()).isNull();
		Assertions.assertThat(card.getTypeAid()).isNull();
		Assertions.assertThat(card.getIdentification()).isNull();
	}

	@Test
	public void testEmvCard() {
		EmvCard card = new EmvCard();
		PaymentDirectory directory = new PaymentDirectory();
		DirectoryEntry ignored = new DirectoryEntry();
		CardIdentification identification = new CardIdentification();
		byte[] ddf = BytesUtils.fromString("A0 00 00 00 04");
		byte[] track1 = "%B4999?".getBytes();
		byte[] track2 = BytesUtils.fromString("49 99 D1 50");
		byte[] disc1 = BytesUtils.fromString("31 32");
		byte[] disc2 = BytesUtils.fromString("12 3F");
		byte[] at = BytesUtils.fromString("3B 65 00 00 20 63 CB A0 00");
		byte[] cplc = BytesUtils.fromString("6A 88");
		byte[] aid = BytesUtils.fromString("A0 00 00 00 42 10 10");

		card.getPaymentDirectories().add(directory);
		card.setPaymentEnvironmentStatusWord("9000");
		card.setDirectoryDepthLimitReached(true);
		card.setDirectoryCountLimitReached(true);
		card.getUnreadDdfNames().add(ddf);
		card.getIgnoredDirectoryEntries().add(ignored);
		card.setLockedStatusWord("6283");
		card.setReadWithAid(true);
		card.setUnparsedTrack1(track1);
		card.setUnparsedTrack2(track2);
		card.setOrphanTrack1DiscretionaryData(disc1);
		card.setOrphanTrack2DiscretionaryData(disc2);
		card.setRawAt(at);
		card.setAtLookedUp(true);
		card.setRawCplcResponse(cplc);
		card.setCplcStatusWord("6A88");
		card.setAidType(EmvCardScheme.CB);
		card.setTypeAid(aid);
		card.setIdentification(identification);

		Assertions.assertThat(card.getPaymentDirectories()).containsExactly(directory);
		Assertions.assertThat(card.getPaymentEnvironmentStatusWord()).isEqualTo("9000");
		Assertions.assertThat(card.isDirectoryDepthLimitReached()).isTrue();
		Assertions.assertThat(card.isDirectoryCountLimitReached()).isTrue();
		Assertions.assertThat(card.getUnreadDdfNames()).containsExactly(ddf);
		Assertions.assertThat(card.getIgnoredDirectoryEntries()).containsExactly(ignored);
		Assertions.assertThat(card.getLockedStatusWord()).isEqualTo("6283");
		Assertions.assertThat(card.isReadWithAid()).isTrue();
		Assertions.assertThat(card.getUnparsedTrack1()).isEqualTo(track1);
		Assertions.assertThat(card.getUnparsedTrack2()).isEqualTo(track2);
		Assertions.assertThat(card.getOrphanTrack1DiscretionaryData()).isEqualTo(disc1);
		Assertions.assertThat(card.getOrphanTrack2DiscretionaryData()).isEqualTo(disc2);
		Assertions.assertThat(card.getRawAt()).isEqualTo(at);
		Assertions.assertThat(card.isAtLookedUp()).isTrue();
		Assertions.assertThat(card.getRawCplcResponse()).isEqualTo(cplc);
		Assertions.assertThat(card.getCplcStatusWord()).isEqualTo("6A88");
		Assertions.assertThat(card.getAidType()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(card.getTypeAid()).isEqualTo(aid);
		Assertions.assertThat(card.getIdentification()).isSameAs(identification);
		// The existing type is not touched by the provenance of the type
		Assertions.assertThat(card.getType()).isNull();

		card.setPaymentDirectories(null);
		card.setUnreadDdfNames(null);
		card.setIgnoredDirectoryEntries(null);
		Assertions.assertThat(card.getPaymentDirectories()).isNotNull().isEmpty();
		Assertions.assertThat(card.getUnreadDdfNames()).isNotNull().isEmpty();
		Assertions.assertThat(card.getIgnoredDirectoryEntries()).isNotNull().isEmpty();
	}

	// F01, F03 - Application selection

	@Test
	public void testApplicationSelectionOutcome() {
		Application application = new Application();
		Assertions.assertThat(application.isLanguagePreferencesFromDirectory()).isFalse();
		Assertions.assertThat(application.isIssuerCodeTableIndexFromDirectory()).isFalse();
		Assertions.assertThat(application.getSelectStatusWord()).isNull();
		Assertions.assertThat(application.getRequestedAid()).isNull();
		Assertions.assertThat(application.getDedicatedFileName()).isNull();
		Assertions.assertThat(application.getAidScheme()).isNull();
		Assertions.assertThat(application.getSelectedName()).isNull();
		Assertions.assertThat(application.isExtendedSelectionRefused()).isFalse();
		Assertions.assertThat(application.isExtendedSelectionTooLong()).isFalse();

		byte[] requested = BytesUtils.fromString("A0 00 00 00 03");
		byte[] dfName = BytesUtils.fromString("A0 00 00 00 03 10 10");
		byte[] selected = BytesUtils.fromString("A0 00 00 00 03 10 10 01");
		application.setLanguagePreferencesFromDirectory(true);
		application.setIssuerCodeTableIndexFromDirectory(true);
		application.setSelectStatusWord("6283");
		application.setRequestedAid(requested);
		application.setDedicatedFileName(dfName);
		application.setAidScheme(EmvCardScheme.VISA);
		application.setSelectedName(selected);
		application.setExtendedSelectionRefused(true);
		application.setExtendedSelectionTooLong(true);

		Assertions.assertThat(application.isLanguagePreferencesFromDirectory()).isTrue();
		Assertions.assertThat(application.isIssuerCodeTableIndexFromDirectory()).isTrue();
		Assertions.assertThat(application.getSelectStatusWord()).isEqualTo("6283");
		Assertions.assertThat(application.getRequestedAid()).isEqualTo(requested);
		Assertions.assertThat(application.getDedicatedFileName()).isEqualTo(dfName);
		Assertions.assertThat(application.getAidScheme()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(application.getSelectedName()).isEqualTo(selected);
		Assertions.assertThat(application.isExtendedSelectionRefused()).isTrue();
		Assertions.assertThat(application.isExtendedSelectionTooLong()).isTrue();
		// The requested AID never replaces the AID
		Assertions.assertThat(application.getAid()).isNull();
	}

	// F04, F05 - GPO and AFL

	@Test
	public void testApplicationGpoAndAfl() {
		Application application = new Application();
		Assertions.assertThat(application.getRawFci()).isNull();
		Assertions.assertThat(application.getRawPdol()).isNull();
		Assertions.assertThat(application.getPdol()).isNotNull().isEmpty();
		Assertions.assertThat(application.getGpoCommandTemplate()).isNull();
		Assertions.assertThat(application.getPdolTerminalValues()).isNotNull().isEmpty();
		Assertions.assertThat(application.getGpoStatusWord()).isNull();
		Assertions.assertThat(application.isGpoRetriedWithoutPdol()).isFalse();
		Assertions.assertThat(application.isGpoReplacedByReadRecord()).isFalse();
		Assertions.assertThat(application.getRawGpoResponse()).isNull();
		Assertions.assertThat(application.getRawAfl()).isNull();
		Assertions.assertThat(application.getAfl()).isNotNull().isEmpty();
		Assertions.assertThat(application.getRecords()).isNotNull().isEmpty();
		Assertions.assertThat(application.getRecordsRead()).isEqualTo(0);
		Assertions.assertThat(application.isAflTruncated()).isFalse();

		byte[] fci = BytesUtils.fromString("6F 00");
		byte[] rawPdol = BytesUtils.fromString("9F 66 04 9F 02 06");
		List<TagAndLength> pdol = Arrays.asList(new TagAndLength(EmvTags.TERMINAL_TRANSACTION_QUALIFIERS, 4),
				new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 6));
		byte[] template = BytesUtils.fromString("83 0A B6 00 C0 00 00 00 00 00 10 00");
		byte[] gpo = BytesUtils.fromString("80 06 1C 00 08 01 01 00");
		byte[] rawAfl = BytesUtils.fromString("08 01 01 00");
		Afl afl = new Afl();
		afl.setSfi(1);
		afl.setFirstRecord(1);
		afl.setLastRecord(1);
		RecordData record = new RecordData(1, 1, new byte[0], "6A83");

		application.setRawFci(fci);
		application.setRawPdol(rawPdol);
		application.setPdol(pdol);
		application.setGpoCommandTemplate(template);
		application.getPdolTerminalValues().put("9F66", BytesUtils.fromString("B6 00 C0 00"));
		application.getPdolTerminalValues().put("9F02", BytesUtils.fromString("00 00 00 00 10 00"));
		application.setGpoStatusWord("9000");
		application.setGpoRetriedWithoutPdol(true);
		application.setGpoReplacedByReadRecord(true);
		application.setRawGpoResponse(gpo);
		application.setRawAfl(rawAfl);
		application.getAfl().add(afl);
		application.getRecords().add(record);
		application.setRecordsRead(1);
		application.setAflTruncated(true);

		Assertions.assertThat(application.getRawFci()).isEqualTo(fci);
		Assertions.assertThat(application.getRawPdol()).isEqualTo(rawPdol);
		Assertions.assertThat(application.getPdol()).hasSize(2);
		Assertions.assertThat(application.getPdol().get(0).getTag()).isEqualTo(EmvTags.TERMINAL_TRANSACTION_QUALIFIERS);
		Assertions.assertThat(application.getGpoCommandTemplate()).isEqualTo(template);
		Assertions.assertThat(new ArrayList<String>(application.getPdolTerminalValues().keySet())).containsExactly("9F66",
				"9F02");
		Assertions.assertThat(application.getGpoStatusWord()).isEqualTo("9000");
		Assertions.assertThat(application.isGpoRetriedWithoutPdol()).isTrue();
		Assertions.assertThat(application.isGpoReplacedByReadRecord()).isTrue();
		Assertions.assertThat(application.getRawGpoResponse()).isEqualTo(gpo);
		Assertions.assertThat(application.getRawAfl()).isEqualTo(rawAfl);
		Assertions.assertThat(application.getAfl()).containsExactly(afl);
		Assertions.assertThat(application.getRecords()).containsExactly(record);
		Assertions.assertThat(application.getRecordsRead()).isEqualTo(1);
		Assertions.assertThat(application.isAflTruncated()).isTrue();

		// The PDOL is exposed like the other data object lists: a copy, read
		// only
		try {
			application.getPdol().add(new TagAndLength(EmvTags.AMOUNT_OTHER_NUMERIC, 6));
			org.junit.Assert.fail("The PDOL must not be modifiable");
		} catch (UnsupportedOperationException e) {
			// expected
		}
		application.setPdol(null);
		Assertions.assertThat(application.getPdol()).isNotNull().isEmpty();
		application.setPdolTerminalValues(null);
		application.setAfl(null);
		application.setRecords(null);
		Assertions.assertThat(application.getPdolTerminalValues()).isNotNull().isEmpty();
		Assertions.assertThat(application.getAfl()).isNotNull().isEmpty();
		Assertions.assertThat(application.getRecords()).isNotNull().isEmpty();
	}

	// F10 - kernel type on the application

	@Test
	public void testApplicationKernelTypeDefaults() {
		Application application = new Application();
		Assertions.assertThat(application.getKernelType()).isNull();
		Assertions.assertThat(application.getRequestedKernelId()).isNull();
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(AbstractData.UNKNOWN);
		application.setKernelIdentifier(null);
		Assertions.assertThat(application.getKernelType()).isNull();
		Assertions.assertThat(application.getRequestedKernelId()).isNull();
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(AbstractData.UNKNOWN);
		application.setKernelIdentifier(new byte[0]);
		Assertions.assertThat(application.getKernelType()).isNull();
		Assertions.assertThat(application.getRequestedKernelId()).isNull();
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(AbstractData.UNKNOWN);
	}

	@Test
	public void testApplicationKernelTypeInternational() {
		Application application = new Application();
		application.setKernelIdentifier(new byte[] { 0x02 });
		Assertions.assertThat(application.getKernelType()).isEqualTo(KernelTypeEnum.INTERNATIONAL);
		Assertions.assertThat(application.getRequestedKernelId()).isEqualTo(new byte[] { 0x02 });
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(2);
		// The existing rule is untouched
		Assertions.assertThat(application.getShortKernelId()).isEqualTo(2);
		Assertions.assertThat(application.getKernel()).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(application.isKernelDerived()).isFalse();
	}

	@Test
	public void testApplicationKernelTypeRfu() {
		Application application = new Application();
		application.setKernelIdentifier(new byte[] { 0x42 });
		Assertions.assertThat(application.getKernelType()).isEqualTo(KernelTypeEnum.RFU);
		Assertions.assertThat(application.getRequestedKernelId()).isEqualTo(new byte[] { 0x42 });
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(2);
		// A RFU identifier names no international kernel, as before
		Assertions.assertThat(application.getShortKernelId()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(application.getKernel()).isNull();
	}

	@Test
	public void testApplicationKernelTypeDomestic() {
		Application application = new Application();
		application.setKernelIdentifier(BytesUtils.fromString("81 02 50"));
		Assertions.assertThat(application.getKernelType()).isEqualTo(KernelTypeEnum.DOMESTIC_EMVCO_FORMAT);
		Assertions.assertThat(application.getRequestedKernelId()).isEqualTo(BytesUtils.fromString("81 02 50"));
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(1);
		Assertions.assertThat(application.getShortKernelId()).isEqualTo(AbstractData.UNKNOWN);

		// Longer than three bytes: the Requested Kernel ID is the three first
		application.setKernelIdentifier(BytesUtils.fromString("C3 02 50 AA BB"));
		Assertions.assertThat(application.getKernelType()).isEqualTo(KernelTypeEnum.DOMESTIC_PROPRIETARY_FORMAT);
		Assertions.assertThat(application.getRequestedKernelId()).isEqualTo(BytesUtils.fromString("C3 02 50"));
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(3);

		// A domestic identifier shorter than three bytes has no Requested
		// Kernel ID
		application.setKernelIdentifier(new byte[] { (byte) 0x81 });
		Assertions.assertThat(application.getKernelType()).isEqualTo(KernelTypeEnum.DOMESTIC_EMVCO_FORMAT);
		Assertions.assertThat(application.getRequestedKernelId()).isNull();
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(1);

		// A zero Short Kernel ID leaves the derivation out of scope, the raw
		// value is still reported
		application.setKernelIdentifier(BytesUtils.fromString("80 02 50"));
		Assertions.assertThat(application.getKernelType()).isEqualTo(KernelTypeEnum.DOMESTIC_EMVCO_FORMAT);
		Assertions.assertThat(application.getRequestedKernelId()).isNull();
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(0);

		// Setting a null identifier resets the derived view
		application.setKernelIdentifier(null);
		Assertions.assertThat(application.getKernelType()).isNull();
		Assertions.assertThat(application.getRequestedKernelId()).isNull();
		Assertions.assertThat(application.getRawShortKernelId()).isEqualTo(AbstractData.UNKNOWN);
	}

	@Test
	public void testApplicationKernelTypeMatchesDirectoryEntry() {
		for (String identifier : Arrays.asList("02", "03", "42", "81 02 50", "C1 00 00 00", "80 02 50", "81", "00")) {
			Application application = new Application();
			DirectoryEntry entry = new DirectoryEntry();
			application.setKernelIdentifier(BytesUtils.fromString(identifier));
			entry.setKernelIdentifier(BytesUtils.fromString(identifier));
			Assertions.assertThat(application.getKernelType()).as(identifier).isEqualTo(entry.getKernelType());
			Assertions.assertThat(application.getRequestedKernelId()).as(identifier).isEqualTo(entry.getRequestedKernelId());
			Assertions.assertThat(application.getRawShortKernelId()).as(identifier).isEqualTo(entry.getShortKernelId());
		}
	}

	// F09, F12, F13, F14, F15, F16, F17, F19 - Application data

	@Test
	public void testApplicationDataDefaults() {
		Application application = new Application();
		Assertions.assertThat(application.getIdentification()).isNull();
		Assertions.assertThat(application.getRawDataObjects()).isNotNull().isEmpty();
		Assertions.assertThat(application.getTransactionLog()).isNull();
		Assertions.assertThat(application.getGetDataStatusWords()).isNotNull().isEmpty();
		Assertions.assertThat(application.getRawOfflineBalance()).isNull();
		Assertions.assertThat(application.getOfflineBalanceTag()).isNull();
		Assertions.assertThat(application.getCurrencyCode()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(application.getIssuerCountryCodeNumeric()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(application.getIssuerCountryCodeAlpha3()).isNull();
		Assertions.assertThat(application.getIssuerCountryCodeAlpha2()).isNull();
		Assertions.assertThat(application.getReferenceCurrencyCodes()).isNotNull().isEmpty();
		Assertions.assertThat(application.getServiceCodeDigits()).isNull();
		Assertions.assertThat(application.getFciApplicationLabel()).isNull();
		Assertions.assertThat(application.getBic()).isNull();
		Assertions.assertThat(application.getIban()).isNull();
		Assertions.assertThat(application.getCardholderName()).isNull();
		Assertions.assertThat(application.getCardholderNameExtended()).isNull();
		Assertions.assertThat(application.getTerminalTransactionQualifiersEcho()).isNull();
		Assertions.assertThat(application.getCardTransactionQualifiersDecoded()).isNull();
		Assertions.assertThat(application.getResolvedKernel()).isNull();
		Assertions.assertThat(application.getRawCdol1()).isNull();
		Assertions.assertThat(application.getRawCdol2()).isNull();
		Assertions.assertThat(application.getRawDdol()).isNull();
		Assertions.assertThat(application.getRawTdol()).isNull();
		Assertions.assertThat(application.getGeldKarte()).isNull();
	}

	@Test
	public void testApplicationData() {
		Application application = new Application();
		CardIdentification identification = new CardIdentification();
		TransactionLog log = new TransactionLog();
		CardTransactionQualifiers ctq = new CardTransactionQualifiers(BytesUtils.fromString("36 00"));
		GeldKarteData geldKarte = new GeldKarteData();
		byte[] pan = BytesUtils.fromString("49 99 99 99 99 99 99 99");
		byte[] balance = BytesUtils.fromString("00 00 00 01 23 45");
		byte[] ttq = BytesUtils.fromString("B6 00 C0 00");
		byte[] cdol1 = BytesUtils.fromString("9F 02 06 9F 03 06");
		byte[] cdol2 = BytesUtils.fromString("8A 02");
		byte[] ddol = BytesUtils.fromString("9F 37 04");
		byte[] tdol = BytesUtils.fromString("9F 02 06");

		application.setIdentification(identification);
		application.getRawDataObjects().put("5A", pan);
		application.setTransactionLog(log);
		application.getGetDataStatusWords().put("9F17", "9000");
		application.getGetDataStatusWords().put("9F36", "6A88");
		application.setRawOfflineBalance(balance);
		application.setOfflineBalanceTag("9F79");
		application.setCurrencyCode(978);
		application.setIssuerCountryCodeNumeric(250);
		application.setIssuerCountryCodeAlpha3("FRA");
		application.setIssuerCountryCodeAlpha2("FR");
		application.getReferenceCurrencyCodes().add(0);
		application.getReferenceCurrencyCodes().add(840);
		application.setServiceCodeDigits("201");
		application.setFciApplicationLabel("VISA DEBIT");
		application.setBic("BNPAFRPP");
		application.setIban("FR7630004000031234567890143");
		application.setCardholderName("DOE/JOHN");
		application.setCardholderNameExtended("DOE/JOHN EXTENDED");
		application.setTerminalTransactionQualifiersEcho(ttq);
		application.setCardTransactionQualifiersDecoded(ctq);
		application.setResolvedKernel(KernelEnum.VISA);
		application.setRawCdol1(cdol1);
		application.setRawCdol2(cdol2);
		application.setRawDdol(ddol);
		application.setRawTdol(tdol);
		application.setGeldKarte(geldKarte);

		Assertions.assertThat(application.getIdentification()).isSameAs(identification);
		Assertions.assertThat(application.getRawDataObjects().get("5A")).isEqualTo(pan);
		Assertions.assertThat(application.getTransactionLog()).isSameAs(log);
		Assertions.assertThat(application.getGetDataStatusWords().get("9F17")).isEqualTo("9000");
		Assertions.assertThat(application.getGetDataStatusWords().get("9F36")).isEqualTo("6A88");
		Assertions.assertThat(application.getRawOfflineBalance()).isEqualTo(balance);
		Assertions.assertThat(application.getOfflineBalanceTag()).isEqualTo("9F79");
		Assertions.assertThat(application.getCurrencyCode()).isEqualTo(978);
		Assertions.assertThat(application.getIssuerCountryCodeNumeric()).isEqualTo(250);
		Assertions.assertThat(application.getIssuerCountryCodeAlpha3()).isEqualTo("FRA");
		Assertions.assertThat(application.getIssuerCountryCodeAlpha2()).isEqualTo("FR");
		Assertions.assertThat(application.getReferenceCurrencyCodes()).containsExactly(0, 840);
		Assertions.assertThat(application.getServiceCodeDigits()).isEqualTo("201");
		Assertions.assertThat(application.getFciApplicationLabel()).isEqualTo("VISA DEBIT");
		Assertions.assertThat(application.getBic()).isEqualTo("BNPAFRPP");
		Assertions.assertThat(application.getIban()).isEqualTo("FR7630004000031234567890143");
		Assertions.assertThat(application.getCardholderName()).isEqualTo("DOE/JOHN");
		Assertions.assertThat(application.getCardholderNameExtended()).isEqualTo("DOE/JOHN EXTENDED");
		Assertions.assertThat(application.getTerminalTransactionQualifiersEcho()).isEqualTo(ttq);
		Assertions.assertThat(application.getCardTransactionQualifiersDecoded()).isSameAs(ctq);
		Assertions.assertThat(application.getResolvedKernel()).isEqualTo(KernelEnum.VISA);
		Assertions.assertThat(application.getRawCdol1()).isEqualTo(cdol1);
		Assertions.assertThat(application.getRawCdol2()).isEqualTo(cdol2);
		Assertions.assertThat(application.getRawDdol()).isEqualTo(ddol);
		Assertions.assertThat(application.getRawTdol()).isEqualTo(tdol);
		Assertions.assertThat(application.getGeldKarte()).isSameAs(geldKarte);
		// The typed views are not touched by the raw ones
		Assertions.assertThat(application.getCurrency()).isNull();
		Assertions.assertThat(application.getIssuerCountryCode()).isNull();
		Assertions.assertThat(application.getReferenceCurrencies()).isEmpty();
		Assertions.assertThat(application.getServiceCode()).isNull();
		Assertions.assertThat(application.getApplicationLabel()).isNull();
		Assertions.assertThat(application.getCardTransactionQualifiers()).isNull();
		Assertions.assertThat(application.getCdol1()).isEmpty();
		Assertions.assertThat(application.getOfflineBalance()).isNull();

		// The live map is returned
		Map<String, byte[]> objects = new LinkedHashMap<String, byte[]>();
		application.setRawDataObjects(objects);
		Assertions.assertThat(application.getRawDataObjects()).isSameAs(objects);
		application.setRawDataObjects(null);
		application.setGetDataStatusWords(null);
		application.setReferenceCurrencyCodes(null);
		Assertions.assertThat(application.getRawDataObjects()).isNotNull().isEmpty();
		Assertions.assertThat(application.getGetDataStatusWords()).isNotNull().isEmpty();
		Assertions.assertThat(application.getReferenceCurrencyCodes()).isNotNull().isEmpty();
	}

	// F02 - DirectoryEntry

	@Test
	public void testDirectoryEntry() {
		DirectoryEntry entry = new DirectoryEntry();
		Assertions.assertThat(entry.getRaw()).isNull();
		Assertions.assertThat(entry.getRawApplicationPreferredName()).isNull();
		Assertions.assertThat(entry.getApplicationPreferredName()).isNull();
		Assertions.assertThat(entry.getDataObjects()).isNotNull().isEmpty();
		Assertions.assertThat(entry.getAdditionalAdfNames()).isNotNull().isEmpty();

		byte[] raw = BytesUtils.fromString("4F 07 A0 00 00 00 03 10 10 9F 12 04 56 49 53 41");
		byte[] rawName = BytesUtils.fromString("56 49 53 41");
		byte[] second = BytesUtils.fromString("A0 00 00 00 03 20 10");
		entry.setRaw(raw);
		entry.setRawApplicationPreferredName(rawName);
		entry.setApplicationPreferredName("VISA");
		entry.getDataObjects().put("4F", BytesUtils.fromString("A0 00 00 00 03 10 10"));
		entry.getDataObjects().put("9F12", rawName);
		entry.getAdditionalAdfNames().add(second);

		Assertions.assertThat(entry.getRaw()).isEqualTo(raw);
		Assertions.assertThat(entry.getRawApplicationPreferredName()).isEqualTo(rawName);
		Assertions.assertThat(entry.getApplicationPreferredName()).isEqualTo("VISA");
		Assertions.assertThat(new ArrayList<String>(entry.getDataObjects().keySet())).containsExactly("4F", "9F12");
		Assertions.assertThat(entry.getAdditionalAdfNames()).containsExactly(second);
		// The first ADF Name alone is the AID
		Assertions.assertThat(entry.getAid()).isNull();

		entry.setDataObjects(null);
		entry.setAdditionalAdfNames(null);
		Assertions.assertThat(entry.getDataObjects()).isNotNull().isEmpty();
		Assertions.assertThat(entry.getAdditionalAdfNames()).isNotNull().isEmpty();
	}

	// F13 - TransactionLog

	@Test
	public void testTransactionLogDefaults() {
		TransactionLog log = new TransactionLog();
		Assertions.assertThat(log.getRawLogEntry()).isNull();
		Assertions.assertThat(log.getLogEntryTag()).isNull();
		Assertions.assertThat(log.getSfi()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(log.getRawLogFormat()).isNull();
		Assertions.assertThat(log.getLogFormat()).isNotNull().isEmpty();
		Assertions.assertThat(log.getLogFormatStatusWord()).isNull();
		Assertions.assertThat(log.getRecords()).isNotNull().isEmpty();
		Assertions.assertThat(log.getSkippedTransactions()).isNotNull().isEmpty();
		Assertions.assertThat(log.getUnparsedRecords()).isNotNull().isEmpty();
		Assertions.assertThat(log.isRead()).isFalse();
	}

	@Test
	public void testTransactionLog() {
		TransactionLog log = new TransactionLog();
		byte[] entry = BytesUtils.fromString("0B 0A");
		byte[] format = BytesUtils.fromString("9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 9F 21 03");
		List<TagAndLength> parsed = Arrays.asList(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 6),
				new TagAndLength(EmvTags.CRYPTOGRAM_INFORMATION_DATA, 1));
		RecordData record = new RecordData(11, 1, BytesUtils.fromString("00 00 00 00 01 00 40 02 50 09 78 14 01 01 00 12 00 00"),
				"9000");
		EmvTransactionRecord skipped = new EmvTransactionRecord();
		skipped.setAmount(0f);
		byte[] unparsed = BytesUtils.fromString("FF");

		log.setRawLogEntry(entry);
		log.setLogEntryTag("9F4D");
		log.setSfi(11);
		log.setDeclaredRecordCount(10);
		log.setRawLogFormat(format);
		log.setLogFormat(parsed);
		log.setLogFormatStatusWord("9000");
		log.getRecords().add(record);
		log.getSkippedTransactions().add(skipped);
		log.getUnparsedRecords().add(unparsed);
		log.setRead(true);

		Assertions.assertThat(log.getRawLogEntry()).isEqualTo(entry);
		Assertions.assertThat(log.getLogEntryTag()).isEqualTo("9F4D");
		Assertions.assertThat(log.getSfi()).isEqualTo(11);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(10);
		Assertions.assertThat(log.getRawLogFormat()).isEqualTo(format);
		Assertions.assertThat(log.getLogFormat()).hasSize(2);
		Assertions.assertThat(log.getLogFormat().get(1).getTag()).isEqualTo(EmvTags.CRYPTOGRAM_INFORMATION_DATA);
		Assertions.assertThat(log.getLogFormatStatusWord()).isEqualTo("9000");
		Assertions.assertThat(log.getRecords()).containsExactly(record);
		Assertions.assertThat(log.getSkippedTransactions()).containsExactly(skipped);
		Assertions.assertThat(log.getUnparsedRecords()).containsExactly(unparsed);
		Assertions.assertThat(log.isRead()).isTrue();

		try {
			log.getLogFormat().clear();
			org.junit.Assert.fail("The Log Format must not be modifiable");
		} catch (UnsupportedOperationException e) {
			// expected
		}
		log.setLogFormat(null);
		log.setRecords(null);
		log.setSkippedTransactions(null);
		log.setUnparsedRecords(null);
		Assertions.assertThat(log.getLogFormat()).isNotNull().isEmpty();
		Assertions.assertThat(log.getRecords()).isNotNull().isEmpty();
		Assertions.assertThat(log.getSkippedTransactions()).isNotNull().isEmpty();
		Assertions.assertThat(log.getUnparsedRecords()).isNotNull().isEmpty();
	}

	// F13 - EmvTransactionRecord

	@Test
	public void testEmvTransactionRecord() {
		EmvTransactionRecord record = new EmvTransactionRecord();
		Assertions.assertThat(record.getRaw()).isNull();
		Assertions.assertThat(record.getRecordNumber()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(record.getOtherTags()).isNotNull().isEmpty();
		Assertions.assertThat(record.getOriginalAmount()).isNull();
		Assertions.assertThat(record.getCurrencyCode()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(record.getTerminalCountryCodeNumeric()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(record.getTransactionTypeCode()).isEqualTo(AbstractData.UNKNOWN);

		byte[] raw = BytesUtils.fromString("00 00 00 00 01 00 40 02 50 09 78 14 01 01 00 12 00 00");
		byte[] atc = BytesUtils.fromString("00 12");
		record.setRaw(raw);
		record.setRecordNumber(3);
		record.getOtherTags().put("9F36", atc);
		record.setOriginalAmount(1500000001f);
		record.setCurrencyCode(978);
		record.setTerminalCountryCodeNumeric(250);
		record.setTransactionTypeCode(0x09);

		Assertions.assertThat(record.getRaw()).isEqualTo(raw);
		Assertions.assertThat(record.getRecordNumber()).isEqualTo(3);
		Assertions.assertThat(record.getOtherTags().get("9F36")).isEqualTo(atc);
		Assertions.assertThat(record.getOriginalAmount()).isEqualTo(1500000001f);
		Assertions.assertThat(record.getCurrencyCode()).isEqualTo(978);
		Assertions.assertThat(record.getTerminalCountryCodeNumeric()).isEqualTo(250);
		Assertions.assertThat(record.getTransactionTypeCode()).isEqualTo(9);
		// The decoded views are untouched by the raw ones
		Assertions.assertThat(record.getAmount()).isNull();
		Assertions.assertThat(record.getCurrency()).isNull();
		Assertions.assertThat(record.getTerminalCountry()).isNull();
		Assertions.assertThat(record.getTransactionType()).isNull();

		record.setOtherTags(null);
		Assertions.assertThat(record.getOtherTags()).isNotNull().isEmpty();
	}

	/**
	 * The annotation driven parsing of the record ignores the new fields: they
	 * carry no annotation and the parser only visits the annotated ones.
	 */
	@Test
	public void testEmvTransactionRecordStillParses() {
		EmvTransactionRecord record = new EmvTransactionRecord();
		List<TagAndLength> format = Arrays.asList(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 6),
				new TagAndLength(EmvTags.TRANSACTION_DATE, 3));
		record.parse(BytesUtils.fromString("00 00 00 00 12 34 14 01 01"), format);
		Assertions.assertThat(record.getAmount()).isEqualTo(1234f);
		Assertions.assertThat(record.getDate()).isNotNull();
		Assertions.assertThat(record.getRecordNumber()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(record.getOtherTags()).isNotNull().isEmpty();
	}

	// F14 - DataEnvelopes

	@Test
	public void testDataEnvelopesOversizedTags() {
		DataEnvelopes envelopes = new DataEnvelopes();
		Assertions.assertThat(envelopes.getOversizedTags()).isNotNull().isEmpty();
		envelopes.getOversizedTags().add("9F70");
		Assertions.assertThat(envelopes.getOversizedTags()).containsExactly("9F70");
		// An oversized envelope does not make the data present
		Assertions.assertThat(envelopes.isPresent()).isFalse();
		envelopes.setOversizedTags(null);
		Assertions.assertThat(envelopes.getOversizedTags()).isNotNull().isEmpty();
	}

	// F16 - CardTransactionQualifiers

	@Test
	public void testCardTransactionQualifiersAllSet() {
		CardTransactionQualifiers ctq = new CardTransactionQualifiers(BytesUtils.fromString("FE C0"));
		Assertions.assertThat(ctq.getRaw()).isEqualTo(BytesUtils.fromString("FE C0"));
		Assertions.assertThat(ctq.isOnlinePinRequired()).isTrue();
		Assertions.assertThat(ctq.isSignatureRequired()).isTrue();
		Assertions.assertThat(ctq.isGoOnlineIfOdaFailsAndReaderOnlineCapable()).isTrue();
		Assertions.assertThat(ctq.isSwitchInterfaceIfOdaFails()).isTrue();
		Assertions.assertThat(ctq.isGoOnlineIfApplicationExpired()).isTrue();
		Assertions.assertThat(ctq.isSwitchInterfaceForCashTransactions()).isTrue();
		Assertions.assertThat(ctq.isSwitchInterfaceForCashbackTransactions()).isTrue();
		Assertions.assertThat(ctq.isConsumerDeviceCvmPerformed()).isTrue();
		Assertions.assertThat(ctq.isIssuerUpdateProcessingSupported()).isTrue();
	}

	@Test
	public void testCardTransactionQualifiersEachBit() {
		// Byte 1, bit 8 down to bit 2
		Assertions.assertThat(new CardTransactionQualifiers(BytesUtils.fromString("80 00")).isOnlinePinRequired()).isTrue();
		Assertions.assertThat(new CardTransactionQualifiers(BytesUtils.fromString("40 00")).isSignatureRequired()).isTrue();
		Assertions.assertThat(new CardTransactionQualifiers(BytesUtils.fromString("20 00"))
				.isGoOnlineIfOdaFailsAndReaderOnlineCapable()).isTrue();
		Assertions.assertThat(new CardTransactionQualifiers(BytesUtils.fromString("10 00")).isSwitchInterfaceIfOdaFails())
				.isTrue();
		Assertions.assertThat(new CardTransactionQualifiers(BytesUtils.fromString("08 00")).isGoOnlineIfApplicationExpired())
				.isTrue();
		Assertions.assertThat(
				new CardTransactionQualifiers(BytesUtils.fromString("04 00")).isSwitchInterfaceForCashTransactions()).isTrue();
		Assertions.assertThat(
				new CardTransactionQualifiers(BytesUtils.fromString("02 00")).isSwitchInterfaceForCashbackTransactions())
				.isTrue();
		// Byte 1 bit 1 is RFU, it sets nothing
		CardTransactionQualifiers rfu = new CardTransactionQualifiers(BytesUtils.fromString("01 3F"));
		Assertions.assertThat(rfu.isOnlinePinRequired()).isFalse();
		Assertions.assertThat(rfu.isSwitchInterfaceForCashbackTransactions()).isFalse();
		Assertions.assertThat(rfu.isConsumerDeviceCvmPerformed()).isFalse();
		Assertions.assertThat(rfu.isIssuerUpdateProcessingSupported()).isFalse();
		// Byte 2, bits 8 and 7
		Assertions.assertThat(new CardTransactionQualifiers(BytesUtils.fromString("00 80")).isConsumerDeviceCvmPerformed())
				.isTrue();
		Assertions.assertThat(
				new CardTransactionQualifiers(BytesUtils.fromString("00 40")).isIssuerUpdateProcessingSupported()).isTrue();
		// The usual Visa personalisation: Online PIN, signature and go online
		// if expired
		CardTransactionQualifiers usual = new CardTransactionQualifiers(BytesUtils.fromString("C8 00"));
		Assertions.assertThat(usual.isOnlinePinRequired()).isTrue();
		Assertions.assertThat(usual.isSignatureRequired()).isTrue();
		Assertions.assertThat(usual.isGoOnlineIfOdaFailsAndReaderOnlineCapable()).isFalse();
		Assertions.assertThat(usual.isGoOnlineIfApplicationExpired()).isTrue();
		Assertions.assertThat(usual.isConsumerDeviceCvmPerformed()).isFalse();
	}

	@Test
	public void testCardTransactionQualifiersTolerance() {
		// One byte: the flags of the missing byte are false
		CardTransactionQualifiers one = new CardTransactionQualifiers(new byte[] { (byte) 0x80 });
		Assertions.assertThat(one.getRaw()).hasSize(1);
		Assertions.assertThat(one.isOnlinePinRequired()).isTrue();
		Assertions.assertThat(one.isConsumerDeviceCvmPerformed()).isFalse();
		Assertions.assertThat(one.isIssuerUpdateProcessingSupported()).isFalse();
		// Empty and null: nothing set, no exception
		CardTransactionQualifiers empty = new CardTransactionQualifiers(new byte[0]);
		Assertions.assertThat(empty.getRaw()).isEmpty();
		Assertions.assertThat(empty.isOnlinePinRequired()).isFalse();
		CardTransactionQualifiers none = new CardTransactionQualifiers((byte[]) null);
		Assertions.assertThat(none.getRaw()).isNull();
		Assertions.assertThat(none.isSignatureRequired()).isFalse();
		// Default constructor
		CardTransactionQualifiers bean = new CardTransactionQualifiers();
		Assertions.assertThat(bean.getRaw()).isNull();
		Assertions.assertThat(bean.isOnlinePinRequired()).isFalse();
		bean.setRaw(BytesUtils.fromString("00 00"));
		bean.setOnlinePinRequired(true);
		bean.setSignatureRequired(true);
		bean.setGoOnlineIfOdaFailsAndReaderOnlineCapable(true);
		bean.setSwitchInterfaceIfOdaFails(true);
		bean.setGoOnlineIfApplicationExpired(true);
		bean.setSwitchInterfaceForCashTransactions(true);
		bean.setSwitchInterfaceForCashbackTransactions(true);
		bean.setConsumerDeviceCvmPerformed(true);
		bean.setIssuerUpdateProcessingSupported(true);
		Assertions.assertThat(bean.getRaw()).isEqualTo(BytesUtils.fromString("00 00"));
		Assertions.assertThat(bean.isOnlinePinRequired()).isTrue();
		Assertions.assertThat(bean.isSignatureRequired()).isTrue();
		Assertions.assertThat(bean.isGoOnlineIfOdaFailsAndReaderOnlineCapable()).isTrue();
		Assertions.assertThat(bean.isSwitchInterfaceIfOdaFails()).isTrue();
		Assertions.assertThat(bean.isGoOnlineIfApplicationExpired()).isTrue();
		Assertions.assertThat(bean.isSwitchInterfaceForCashTransactions()).isTrue();
		Assertions.assertThat(bean.isSwitchInterfaceForCashbackTransactions()).isTrue();
		Assertions.assertThat(bean.isConsumerDeviceCvmPerformed()).isTrue();
		Assertions.assertThat(bean.isIssuerUpdateProcessingSupported()).isTrue();
	}

	// F16 - MagStripeData

	@Test
	public void testMagStripeApplicationVersionNumber() {
		MagStripeData data = new MagStripeData();
		Assertions.assertThat(data.getApplicationVersionNumber()).isEqualTo(AbstractData.UNKNOWN);
		data.setApplicationVersionNumber(1);
		Assertions.assertThat(data.getApplicationVersionNumber()).isEqualTo(1);
		// The version number alone does not make the mag-stripe data present
		Assertions.assertThat(data.isPresent()).isFalse();
	}

	// F19 - GeldKarteData

	@Test
	public void testGeldKarteDataDefaults() {
		GeldKarteData data = new GeldKarteData();
		Assertions.assertThat(data.getRawEfId()).isNull();
		Assertions.assertThat(data.getEfIdStatusWord()).isNull();
		Assertions.assertThat(data.getIndustryKey()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(data.getBankSortCode()).isNull();
		Assertions.assertThat(data.getCardNumber()).isNull();
		Assertions.assertThat(data.getCheckDigit()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(data.getActivationDate()).isNull();
		Assertions.assertThat(data.getCountryCode()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(data.getCurrency()).isNull();
		Assertions.assertThat(data.getCurrencyUnit()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(data.getChipType()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(data.getOsVersion()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(data.getRawEfBetrag()).isNull();
		Assertions.assertThat(data.getEfBetragStatusWord()).isNull();
		Assertions.assertThat(data.getMaximumBalance()).isNull();
		Assertions.assertThat(data.getMaximumTransaction()).isNull();
		Assertions.assertThat(data.getRawEfBlogRecords()).isNotNull().isEmpty();
		Assertions.assertThat(data.getEfBlogEndStatusWord()).isNull();
	}

	@Test
	public void testGeldKarteData() {
		GeldKarteData data = new GeldKarteData();
		byte[] efId = BytesUtils.fromString("67 12 34 56 12 34 56 78 90 70 00 00 14 01 01 02 80 45 55 52 02 01 00 05");
		byte[] efBetrag = BytesUtils.fromString("00 01 00 02 00 00 01 00 00");
		byte[] blog = BytesUtils.fromString("01 02 03");
		Date date = new Date();
		data.setRawEfId(efId);
		data.setEfIdStatusWord("9000");
		data.setIndustryKey(0x67);
		data.setBankSortCode("123456");
		data.setCardNumber("1234567890");
		data.setCheckDigit(7);
		data.setActivationDate(date);
		data.setCountryCode(280);
		data.setCurrency("EUR");
		data.setCurrencyUnit(2);
		data.setChipType(1);
		data.setOsVersion(5);
		data.setRawEfBetrag(efBetrag);
		data.setEfBetragStatusWord("9000");
		data.setMaximumBalance(new BigDecimal("200.00"));
		data.setMaximumTransaction(new BigDecimal("100.00"));
		data.getRawEfBlogRecords().add(blog);
		data.setEfBlogEndStatusWord("6A83");

		Assertions.assertThat(data.getRawEfId()).isEqualTo(efId);
		Assertions.assertThat(data.getEfIdStatusWord()).isEqualTo("9000");
		Assertions.assertThat(data.getIndustryKey()).isEqualTo(0x67);
		Assertions.assertThat(data.getBankSortCode()).isEqualTo("123456");
		Assertions.assertThat(data.getCardNumber()).isEqualTo("1234567890");
		Assertions.assertThat(data.getCheckDigit()).isEqualTo(7);
		Assertions.assertThat(data.getActivationDate()).isEqualTo(date);
		Assertions.assertThat(data.getCountryCode()).isEqualTo(280);
		Assertions.assertThat(data.getCurrency()).isEqualTo("EUR");
		Assertions.assertThat(data.getCurrencyUnit()).isEqualTo(2);
		Assertions.assertThat(data.getChipType()).isEqualTo(1);
		Assertions.assertThat(data.getOsVersion()).isEqualTo(5);
		Assertions.assertThat(data.getRawEfBetrag()).isEqualTo(efBetrag);
		Assertions.assertThat(data.getEfBetragStatusWord()).isEqualTo("9000");
		Assertions.assertThat(data.getMaximumBalance()).isEqualTo(new BigDecimal("200.00"));
		Assertions.assertThat(data.getMaximumTransaction()).isEqualTo(new BigDecimal("100.00"));
		Assertions.assertThat(data.getRawEfBlogRecords()).containsExactly(blog);
		Assertions.assertThat(data.getEfBlogEndStatusWord()).isEqualTo("6A83");

		data.setRawEfBlogRecords(null);
		Assertions.assertThat(data.getRawEfBlogRecords()).isNotNull().isEmpty();
	}

	// F20 - tracks

	@Test
	public void testTrack1Fields() {
		EmvTrack1 track = new EmvTrack1();
		Assertions.assertThat(track.isStartSentinel()).isFalse();
		Assertions.assertThat(track.isEndSentinel()).isFalse();
		Assertions.assertThat(track.getOptionalField()).isNull();
		Assertions.assertThat(track.getNameField()).isNull();
		Assertions.assertThat(track.getExpirationField()).isNull();
		Assertions.assertThat(track.getServiceCodeField()).isNull();
		Assertions.assertThat(track.getRawDiscretionaryField()).isNull();

		track.setStartSentinel(true);
		track.setEndSentinel(true);
		track.setOptionalField("?d");
		track.setNameField("DOE/JOHN MR         ");
		track.setExpirationField("^");
		track.setServiceCodeField("^");
		track.setRawDiscretionaryField("1234567890000000");

		Assertions.assertThat(track.isStartSentinel()).isTrue();
		Assertions.assertThat(track.isEndSentinel()).isTrue();
		Assertions.assertThat(track.getOptionalField()).isEqualTo("?d");
		Assertions.assertThat(track.getNameField()).isEqualTo("DOE/JOHN MR         ");
		Assertions.assertThat(track.getExpirationField()).isEqualTo("^");
		Assertions.assertThat(track.getServiceCodeField()).isEqualTo("^");
		Assertions.assertThat(track.getRawDiscretionaryField()).isEqualTo("1234567890000000");
		// The decoded views stay what they were
		Assertions.assertThat(track.getHolderLastname()).isNull();
		Assertions.assertThat(track.getExpireDate()).isNull();
		Assertions.assertThat(track.getServiceCode()).isNull();
		Assertions.assertThat(track.getDiscretionaryData()).isNull();
	}

	@Test
	public void testTrack2Fields() {
		EmvTrack2 track = new EmvTrack2();
		Assertions.assertThat(track.getExpirationField()).isNull();
		Assertions.assertThat(track.getServiceCodeField()).isNull();
		Assertions.assertThat(track.getTrailingField()).isNull();

		track.setExpirationField("1509");
		track.setServiceCodeField("FFF");
		track.setTrailingField("FFF1234567890FFFF");

		Assertions.assertThat(track.getExpirationField()).isEqualTo("1509");
		Assertions.assertThat(track.getServiceCodeField()).isEqualTo("FFF");
		Assertions.assertThat(track.getTrailingField()).isEqualTo("FFF1234567890FFFF");
		Assertions.assertThat(track.getExpireDate()).isNull();
		Assertions.assertThat(track.getServiceCode()).isNull();
		Assertions.assertThat(track.getDiscretionaryData()).isNull();
	}

	// F21 - CPLC

	@Test
	public void testCplcFields() {
		CPLC cplc = new CPLC();
		Assertions.assertThat(cplc.getRaw()).isNull();
		Assertions.assertThat(cplc.getParseError()).isNull();
		byte[] raw = BytesUtils.fromString(
				"47 90 50 40 47 91 81 02 31 00 83 58 00 12 34 56 71 23 40 10 82 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		Assertions.assertThat(raw).hasSize(CPLC.SIZE);
		cplc.setRaw(raw);
		cplc.setParseError("Invalid day of year: 999");
		Assertions.assertThat(cplc.getRaw()).isEqualTo(raw);
		Assertions.assertThat(cplc.getParseError()).isEqualTo("Invalid day of year: 999");
		// The annotation driven parsing ignores the new fields
		Assertions.assertThat(cplc.getIcFabricator()).isNull();
	}

	// Persistence

	/**
	 * A card with every new field set survives a Java serialization round
	 * trip: nothing non transient holds an object that cannot be persisted,
	 * and the transient data object lists come back empty rather than null.
	 */
	@Test
	public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
		EmvCard card = new EmvCard();
		PaymentDirectory directory = new PaymentDirectory();
		directory.setName("2PAY.SYS.DDF01".getBytes());
		directory.getRecords().add(new RecordData(1, 1, new byte[] { 0x70 }, "9000"));
		directory.getDataObjects().put("84", "2PAY.SYS.DDF01".getBytes());
		directory.getLanguagePreferences().add("fr");
		directory.getSubDirectoryNames().add(new byte[] { 0x01 });
		card.getPaymentDirectories().add(directory);
		card.setPaymentEnvironmentStatusWord("9000");
		card.getUnreadDdfNames().add(new byte[] { 0x02 });
		DirectoryEntry ignored = new DirectoryEntry();
		ignored.setRaw(new byte[] { 0x61, 0x00 });
		ignored.getDataObjects().put("50", "VISA".getBytes());
		ignored.getAdditionalAdfNames().add(new byte[] { 0x03 });
		ignored.setApplicationPreferredName("VISA");
		card.getIgnoredDirectoryEntries().add(ignored);
		card.setRawAt(new byte[] { 0x3B });
		card.setRawCplcResponse(new byte[] { 0x6A, (byte) 0x88 });
		card.setAidType(EmvCardScheme.VISA);
		card.setIdentification(new CardIdentification());
		CPLC cplc = new CPLC();
		cplc.setRaw(new byte[CPLC.SIZE]);
		cplc.setParseError("error");
		card.setCplc(cplc);
		EmvTrack1 track1 = new EmvTrack1();
		track1.setNameField("DOE/JOHN");
		track1.setStartSentinel(true);
		card.setTrack1(track1);
		EmvTrack2 track2 = new EmvTrack2();
		track2.setTrailingField("FFF");
		card.setTrack2(track2);

		Application application = new Application();
		application.setKernelIdentifier(new byte[] { 0x03 });
		application.setPdol(Collections.singletonList(new TagAndLength(EmvTags.TERMINAL_TRANSACTION_QUALIFIERS, 4)));
		application.getPdolTerminalValues().put("9F66", new byte[] { (byte) 0xB6 });
		application.getRawDataObjects().put("5A", new byte[] { 0x49 });
		application.getGetDataStatusWords().put("9F17", "9000");
		application.getReferenceCurrencyCodes().add(840);
		Afl afl = new Afl();
		afl.setSfi(1);
		application.getAfl().add(afl);
		application.getRecords().add(new RecordData(1, 1, new byte[] { 0x70 }, "9000"));
		application.setIdentification(new CardIdentification());
		application.setAidScheme(EmvCardScheme.VISA);
		application.setResolvedKernel(KernelEnum.VISA);
		application.setCardTransactionQualifiersDecoded(new CardTransactionQualifiers(new byte[] { (byte) 0xC8, 0 }));
		TransactionLog log = new TransactionLog();
		log.setLogFormat(Collections.singletonList(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 6)));
		log.getRecords().add(new RecordData(11, 1, new byte[] { 0x00 }, "9000"));
		EmvTransactionRecord skipped = new EmvTransactionRecord();
		skipped.setRaw(new byte[] { 0x00 });
		skipped.setRecordNumber(2);
		skipped.getOtherTags().put("9F36", new byte[] { 0x00, 0x12 });
		skipped.setOriginalAmount(0f);
		log.getSkippedTransactions().add(skipped);
		log.getUnparsedRecords().add(new byte[] { (byte) 0xFF });
		log.setRead(true);
		application.setTransactionLog(log);
		application.getDataEnvelopes().getOversizedTags().add("9F70");
		application.getMagStripeData().setApplicationVersionNumber(1);
		GeldKarteData geldKarte = new GeldKarteData();
		geldKarte.setMaximumBalance(new BigDecimal("200.00"));
		geldKarte.setActivationDate(new Date());
		geldKarte.getRawEfBlogRecords().add(new byte[] { 0x01 });
		application.setGeldKarte(geldKarte);
		card.getApplications().add(application);

		EmvCard copy = roundTrip(card);

		Assertions.assertThat(copy.getPaymentDirectories()).hasSize(1);
		Assertions.assertThat(copy.getPaymentDirectories().get(0).getRecords().get(0).getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(copy.getPaymentDirectories().get(0).getDataObjects().get("84")).isEqualTo(
				"2PAY.SYS.DDF01".getBytes());
		Assertions.assertThat(copy.getPaymentDirectories().get(0).getLanguagePreferences()).containsExactly("fr");
		Assertions.assertThat(copy.getPaymentEnvironmentStatusWord()).isEqualTo("9000");
		Assertions.assertThat(copy.getUnreadDdfNames()).hasSize(1);
		Assertions.assertThat(copy.getIgnoredDirectoryEntries().get(0).getApplicationPreferredName()).isEqualTo("VISA");
		Assertions.assertThat(copy.getIgnoredDirectoryEntries().get(0).getDataObjects().get("50")).isEqualTo("VISA".getBytes());
		Assertions.assertThat(copy.getIgnoredDirectoryEntries().get(0).getAdditionalAdfNames()).hasSize(1);
		Assertions.assertThat(copy.getRawAt()).isEqualTo(new byte[] { 0x3B });
		Assertions.assertThat(copy.getAidType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(copy.getIdentification()).isNotNull();
		Assertions.assertThat(copy.getCplc().getRaw()).hasSize(CPLC.SIZE);
		Assertions.assertThat(copy.getCplc().getParseError()).isEqualTo("error");
		Assertions.assertThat(copy.getTrack1().getNameField()).isEqualTo("DOE/JOHN");
		Assertions.assertThat(copy.getTrack1().isStartSentinel()).isTrue();
		Assertions.assertThat(copy.getTrack2().getTrailingField()).isEqualTo("FFF");

		Application app = copy.getApplications().get(0);
		Assertions.assertThat(app.getKernelType()).isEqualTo(KernelTypeEnum.INTERNATIONAL);
		Assertions.assertThat(app.getRequestedKernelId()).isEqualTo(new byte[] { 0x03 });
		Assertions.assertThat(app.getRawShortKernelId()).isEqualTo(3);
		// Transient, like the other data object lists
		Assertions.assertThat(app.getPdol()).isNotNull().isEmpty();
		Assertions.assertThat(app.getPdolTerminalValues().get("9F66")).isEqualTo(new byte[] { (byte) 0xB6 });
		Assertions.assertThat(app.getRawDataObjects().get("5A")).isEqualTo(new byte[] { 0x49 });
		Assertions.assertThat(app.getGetDataStatusWords().get("9F17")).isEqualTo("9000");
		Assertions.assertThat(app.getReferenceCurrencyCodes()).containsExactly(840);
		Assertions.assertThat(app.getAfl().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(app.getRecords().get(0).getRaw()).isEqualTo(new byte[] { 0x70 });
		Assertions.assertThat(app.getIdentification()).isNotNull();
		Assertions.assertThat(app.getAidScheme()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(app.getResolvedKernel()).isEqualTo(KernelEnum.VISA);
		Assertions.assertThat(app.getCardTransactionQualifiersDecoded().isOnlinePinRequired()).isTrue();
		Assertions.assertThat(app.getTransactionLog().getLogFormat()).isNotNull().isEmpty();
		Assertions.assertThat(app.getTransactionLog().getRecords()).hasSize(1);
		Assertions.assertThat(app.getTransactionLog().getSkippedTransactions().get(0).getRecordNumber()).isEqualTo(2);
		Assertions.assertThat(app.getTransactionLog().getSkippedTransactions().get(0).getOtherTags().get("9F36")).isEqualTo(
				new byte[] { 0x00, 0x12 });
		Assertions.assertThat(app.getTransactionLog().getUnparsedRecords()).hasSize(1);
		Assertions.assertThat(app.getTransactionLog().isRead()).isTrue();
		Assertions.assertThat(app.getDataEnvelopes().getOversizedTags()).containsExactly("9F70");
		Assertions.assertThat(app.getMagStripeData().getApplicationVersionNumber()).isEqualTo(1);
		Assertions.assertThat(app.getGeldKarte().getMaximumBalance()).isEqualTo(new BigDecimal("200.00"));
		Assertions.assertThat(app.getGeldKarte().getActivationDate()).isNotNull();
		Assertions.assertThat(app.getGeldKarte().getRawEfBlogRecords()).hasSize(1);
	}

	// Existing behaviour

	/**
	 * The new fields change nothing to the reading of a card the library
	 * reads today: the existing values are the same, and the new collections
	 * are never null, filled or not.
	 */
	@Test
	public void testExistingCardStillReadsTheSame() throws CommunicationException {
		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpse")) //
				.setConfig(EmvTemplate.Config().setReadCplc(true)) //
				.build();
		EmvCard card = parser.readEmvCard();

		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getApplications()).hasSize(2);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo(
				"A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions()).hasSize(16);
		Assertions.assertThat(card.getApplications().get(1).getListTransactions()).hasSize(1);

		Assertions.assertThat(card.getPaymentDirectories()).isNotNull();
		Assertions.assertThat(card.getUnreadDdfNames()).isNotNull();
		Assertions.assertThat(card.getIgnoredDirectoryEntries()).isNotNull();
		for (Application application : card.getApplications()) {
			Assertions.assertThat(application.getPdol()).isNotNull();
			Assertions.assertThat(application.getPdolTerminalValues()).isNotNull();
			Assertions.assertThat(application.getAfl()).isNotNull();
			Assertions.assertThat(application.getRecords()).isNotNull();
			Assertions.assertThat(application.getRawDataObjects()).isNotNull();
			Assertions.assertThat(application.getGetDataStatusWords()).isNotNull();
			Assertions.assertThat(application.getReferenceCurrencyCodes()).isNotNull();
			Assertions.assertThat(application.getDataEnvelopes().getOversizedTags()).isNotNull();
			for (EmvTransactionRecord record : application.getListTransactions()) {
				Assertions.assertThat(record.getOtherTags()).isNotNull();
			}
		}
		for (DirectoryEntry entry : card.getDirectoryEntries()) {
			Assertions.assertThat(entry.getDataObjects()).isNotNull();
			Assertions.assertThat(entry.getAdditionalAdfNames()).isNotNull();
		}
	}

}
