package com.github.devnied.emvnfccard;

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.DirectoryEntry;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.FileReferenceEnum;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.model.enums.KernelTypeEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.provider.TestProvider;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * Structure of the entries of a payment system directory, the Application
 * Templates (tag '61') of the PSE and of the PPSE.
 * <p>
 * The two interfaces put the very same template in two different places. EMV
 * 4.4 Book 1 Table 11 shows the contact one inside a record of the Payment
 * System Directory: "Tag '70' | Data Length (L) | Tag '61' | Length of
 * directory entry 1 | Directory entry 1 (ADF) | ...". EMV Contactless Book B
 * v2.12 Table 3-2 shows the contactless one inside the File Control
 * Information: "'6F' FCI Template M / '84' DF Name ('2PAY.SYS.DDF01') O6 / 'A5'
 * FCI Proprietary Template M / 'BF0C' FCI Issuer Discretionary Data M / '61'
 * Directory Entry M".
 * </p>
 */
public class DirectoryEntryTest {

	/** DF Name of the PSE, "1PAY.SYS.DDF01" */
	private static final String PSE_NAME = "315041592E5359532E4444463031";

	/** DF Name of the PPSE, "2PAY.SYS.DDF01" */
	private static final String PPSE_NAME = "325041592E5359532E4444463031";

	/**
	 * The entries of a contact Payment System Directory are kept as the card
	 * listed them, with the directory they were read from.
	 * <p>
	 * Replayed against the recorded UnionPayPseDirectory capture. Its FCI
	 * declares the directory in the SFI 1 ("A5 03 88 01 01") and its three
	 * records each hold one entry, which EMV 4.4 Book 1 section 12.2.3
	 * describes: "A Payment System Directory is a linear EF file identified by
	 * a SFI in the range 1 to 10".
	 * </p>
	 */
	@Test
	public void testPaymentSystemDirectoryEntriesAreModelled() throws CommunicationException {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new TestProvider("UnionPayPseDirectory"))
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();
		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		List<DirectoryEntry> entries = parser.getCard().getDirectoryEntries();
		Assertions.assertThat(entries).hasSize(3);

		DirectoryEntry credit = entries.get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(credit.getAid())).isEqualTo("A000000333010102");
		Assertions.assertThat(credit.getApplicationLabel()).isEqualTo("UICC CREDIT");
		Assertions.assertThat(credit.getPriority()).isEqualTo(1);
		Assertions.assertThat(credit.isPriorityKnown()).isTrue();
		Assertions.assertThat(credit.getApplicationPriorityIndicator()).isEqualTo(0x01);
		Assertions.assertThat(credit.isCardholderConfirmationRequired()).isFalse();
		// The entry was read in the record 1 of the SFI the FCI declares
		Assertions.assertThat(credit.isReadFromDirectoryFile()).isTrue();
		Assertions.assertThat(credit.getDirectorySfi()).isEqualTo(1);
		Assertions.assertThat(credit.getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(credit.getDedicatedFileName())).isEqualTo(PSE_NAME);
		// This card carries nothing else in its entries
		Assertions.assertThat(credit.getDdfName()).isNull();
		Assertions.assertThat(credit.getPath()).isNull();
		Assertions.assertThat(credit.getPathType()).isNull();
		Assertions.assertThat(credit.getKernelIdentifier()).isNull();
		Assertions.assertThat(credit.getExtendedSelection()).isNull();
		Assertions.assertThat(credit.getApplicationSelectionRegisteredProprietaryData()).isNull();
		Assertions.assertThat(credit.getDirectoryDiscretionaryTemplate()).isNull();
		Assertions.assertThat(credit.getRawApplicationPreferredName()).isNull();
		Assertions.assertThat(credit.getApplicationPreferredName()).isNull();
		Assertions.assertThat(credit.getAdditionalAdfNames()).isEmpty();
		// The whole entry and its flat view
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(credit.getRaw()))
				.isEqualTo("4F08A000000333010102500B5549434320435245444954870101");
		Assertions.assertThat(new java.util.ArrayList<String>(credit.getDataObjects().keySet())).containsExactly("4F", "50", "87");
		Assertions.assertThat(credit.getDataObjects().get("87")).isEqualTo(new byte[] { 0x01 });
		// The directory file the entries were read from
		Assertions.assertThat(parser.getCard().getPaymentDirectories()).hasSize(1);
		Assertions.assertThat(parser.getCard().getPaymentDirectories().get(0).getRecords()).hasSize(4);
		Assertions.assertThat(parser.getCard().getPaymentDirectories().get(0).getEndStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(parser.getCard().getIgnoredDirectoryEntries()).isEmpty();

		DirectoryEntry debit = entries.get(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(debit.getAid())).isEqualTo("A000000333010101");
		Assertions.assertThat(debit.getApplicationLabel()).isEqualTo("UICC DEBIT");
		Assertions.assertThat(debit.getPriority()).isEqualTo(2);
		Assertions.assertThat(debit.getRecordNumber()).isEqualTo(2);

		DirectoryEntry quasiCredit = entries.get(2);
		Assertions.assertThat(quasiCredit.getApplicationLabel()).isEqualTo("UICC QUASICREDIT");
		Assertions.assertThat(quasiCredit.getPriority()).isEqualTo(3);
		Assertions.assertThat(quasiCredit.getRecordNumber()).isEqualTo(3);

		// The selection is unchanged: the same three applications are found,
		// and each one holds the entry it was found in
		Assertions.assertThat(apps).hasSize(3);
		Assertions.assertThat(apps.get(0).getDirectoryEntry()).isSameAs(credit);
		Assertions.assertThat(apps.get(1).getDirectoryEntry()).isSameAs(debit);
		Assertions.assertThat(apps.get(2).getDirectoryEntry()).isSameAs(quasiCredit);
	}

	/**
	 * A PPSE has no directory file at all: EMV Contactless Book B puts the
	 * entries in the FCI Issuer Discretionary Data (tag 'BF0C') of the answer
	 * to the SELECT, so there is neither a SFI nor a record number to report.
	 * <p>
	 * Replayed against the recorded MasterCardPpse capture, whose 'BF0C' holds
	 * two entries.
	 * </p>
	 */
	@Test
	public void testProximityPaymentSystemDirectoryEntriesAreModelled() throws CommunicationException {
		EmvCard card = EmvTemplate.Builder() //
				.setProvider(new TestProvider("MasterCardPpse")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadCplc(true)) //
				.build().readEmvCard();

		List<DirectoryEntry> entries = card.getDirectoryEntries();
		Assertions.assertThat(entries).hasSize(2);

		DirectoryEntry cb = entries.get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(cb.getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(cb.getPriority()).isEqualTo(1);
		Assertions.assertThat(cb.isReadFromDirectoryFile()).isFalse();
		Assertions.assertThat(cb.getDirectorySfi()).isEqualTo(DirectoryEntry.UNKNOWN);
		Assertions.assertThat(cb.getRecordNumber()).isEqualTo(DirectoryEntry.UNKNOWN);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getDedicatedFileName())).isEqualTo(PPSE_NAME);

		DirectoryEntry masterCard = entries.get(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(masterCard.getAid())).isEqualTo("A0000000041010");
		Assertions.assertThat(masterCard.getApplicationLabel()).isEqualTo("MASTERCARD");
		Assertions.assertThat(masterCard.getPriority()).isEqualTo(2);
		Assertions.assertThat(masterCard.isReadFromDirectoryFile()).isFalse();

		// The selection is unchanged: the two applications of the capture are
		// still read, in the priority order, each one holding its entry
		Assertions.assertThat(card.getApplications()).hasSize(2);
		Assertions.assertThat(card.getApplications().get(0).getDirectoryEntry()).isSameAs(cb);
		Assertions.assertThat(card.getApplications().get(1).getDirectoryEntry()).isSameAs(masterCard);
	}

	/**
	 * The Application Priority Indicator is not coded the same way on the two
	 * interfaces, so the cardholder confirmation flag is only reported for an
	 * entry read in a Payment System Directory file.
	 * <p>
	 * EMV 4.4 Book 1 Table 13: "b8 = 1 : Application cannot be selected without
	 * confirmation by the cardholder ; b8 = 0 : Application may be selected
	 * without confirmation by the cardholder ; b7-b5 = xxx : RFU ; b4-b1 = 0000
	 * : No priority assigned". EMV Contactless Book B v2.12 Table 3-3: "b8-b5 =
	 * xxxx : Each bit RFU ; b4-b1 = 0000 : No priority assigned".
	 * </p>
	 */
	@Test
	public void testCardholderConfirmationFlagBelongsToTheContactInterface() {
		DirectoryEntry contact = new DirectoryEntry();
		contact.setReadFromDirectoryFile(true);
		contact.setApplicationPriorityIndicator((byte) 0x81);
		Assertions.assertThat(contact.getPriority()).isEqualTo(1);
		Assertions.assertThat(contact.isCardholderConfirmationFlag()).isTrue();
		Assertions.assertThat(contact.isCardholderConfirmationRequired()).isTrue();

		// The same byte in a PPSE directory entry: the bit is RFU there, it
		// does not ask for the confirmation of the cardholder
		DirectoryEntry contactless = new DirectoryEntry();
		contactless.setApplicationPriorityIndicator((byte) 0x81);
		Assertions.assertThat(contactless.getPriority()).isEqualTo(1);
		Assertions.assertThat(contactless.isCardholderConfirmationFlag()).isTrue();
		Assertions.assertThat(contactless.isCardholderConfirmationRequired()).isFalse();

		// "If the Application Priority Indicator is absent from a Directory
		// Entry, then Entry Point shall assume b4-b1 to have a value of 0000b"
		// (Book B requirement 3.3.1.1): the absence is reported as such
		DirectoryEntry absent = new DirectoryEntry();
		Assertions.assertThat(absent.isPriorityKnown()).isFalse();
		Assertions.assertThat(absent.getPriority()).isEqualTo(DirectoryEntry.UNKNOWN);
		Assertions.assertThat(absent.getApplicationPriorityIndicator()).isEqualTo(DirectoryEntry.UNKNOWN);
	}

	/**
	 * The nature of a File reference data element (tag '51') is given by its
	 * length alone.
	 * <p>
	 * ISO/IEC 7816-4:2005 section 5.3.1.2: "An empty data object references the
	 * MF. - If the length is one and if bits 8 to 4 of the data element are not
	 * all equal and if bits 3 to 1 are set to 000, then bits 8 to 4 encode a
	 * number from one to thirty that is a short EF identifier. - If the length
	 * is two, then the data element is a file identifier. - If the length is
	 * more than two, then the data element is a path."
	 * </p>
	 * <p>
	 * Every value below is a byte string the specification itself names.
	 * </p>
	 */
	@Test
	public void testFileReferenceDataElementIsDecodedByItsLength() {
		// "An empty data object references the MF"
		Assertions.assertThat(path(new byte[0]).getPathType()).isEqualTo(FileReferenceEnum.MASTER_FILE);
		Assertions.assertThat(path(new byte[0]).isSelectionByPath()).isFalse();

		// ISO/IEC 7816-4 section 8.2.1.1: "At MF level, the short EF identifier
		// 30, i.e., 11110 in binary, references EF.DIR if present". The
		// identifier sits on the bits 8 to 4, so 11110 000 = 'F0'
		DirectoryEntry thirty = path(BytesUtils.fromString("F0"));
		Assertions.assertThat(thirty.getPathType()).isEqualTo(FileReferenceEnum.SHORT_FILE_IDENTIFIER);
		Assertions.assertThat(thirty.getShortFileIdentifier()).isEqualTo(30);
		// One byte is not a path, the selection stays a selection by DF name
		// (ISO/IEC 7816-4 section 8.2.2.3 needs "two or more bytes")
		Assertions.assertThat(thirty.isSelectionByPath()).isFalse();

		// The SFI 1 the UnionPayPseDirectory capture declares for its directory
		// ("88 01 01"), coded the same way: 00001 000 = '08'
		DirectoryEntry one = path(BytesUtils.fromString("08"));
		Assertions.assertThat(one.getPathType()).isEqualTo(FileReferenceEnum.SHORT_FILE_IDENTIFIER);
		Assertions.assertThat(one.getShortFileIdentifier()).isEqualTo(1);

		// "bits 8 to 4 [...] not all equal": '00' is the number zero, which
		// "references the current EF", and 'F8' is thirty-one
		Assertions.assertThat(path(BytesUtils.fromString("00")).getPathType()).isNull();
		Assertions.assertThat(path(BytesUtils.fromString("F8")).getPathType()).isNull();
		// "bits 3 to 1 are set to 000"
		Assertions.assertThat(path(BytesUtils.fromString("F1")).getPathType()).isNull();
		Assertions.assertThat(path(BytesUtils.fromString("F1")).getShortFileIdentifier())
				.isEqualTo(DirectoryEntry.UNKNOWN);

		// "If the length is two, then the data element is a file identifier".
		// ISO/IEC 7816-4 section 5.3.1.1: "The value '3F00' is reserved for
		// referencing the MF"
		DirectoryEntry masterFile = path(BytesUtils.fromString("3F00"));
		Assertions.assertThat(masterFile.getPathType()).isEqualTo(FileReferenceEnum.FILE_IDENTIFIER);
		Assertions.assertThat(masterFile.getFileIdentifier()).isEqualTo(0x3F00);

		// ISO/IEC 7816-4 section 8.2.1.1: "the path '3F002F00' references
		// EF.DIR", an absolute path since it starts with the MF identifier
		DirectoryEntry absolute = path(BytesUtils.fromString("3F002F00"));
		Assertions.assertThat(absolute.getPathType()).isEqualTo(FileReferenceEnum.ABSOLUTE_PATH);
		Assertions.assertThat(absolute.isSelectionByPath()).isTrue();

		// ISO/IEC 7816-4 section 5.3.1.1: "If the identifier of the current DF
		// is not known, then the value '3FFF' (reserved value) can be used at
		// the beginning of the path". It is not '3F00', so the path is relative
		DirectoryEntry relative = path(BytesUtils.fromString("3FFF2F00"));
		Assertions.assertThat(relative.getPathType()).isEqualTo(FileReferenceEnum.RELATIVE_PATH);

		// "If the length is odd, then the path is qualified. The data element
		// is either an absolute path without '3F00' [...] followed by a byte to
		// use as P1 in one or more SELECT commands". Section 8.3: "If the value
		// of P1 is '08' or '09', then the card shall support a SELECT command
		// where the qualified path specifies P1"
		DirectoryEntry qualified = path(BytesUtils.fromString("2F0008"));
		Assertions.assertThat(qualified.getPathType()).isEqualTo(FileReferenceEnum.QUALIFIED_PATH);
		Assertions.assertThat(qualified.getPathSelectionParameter()).isEqualTo(0x08);
	}

	/**
	 * The Kernel Identifier (tag '9F2A') names the kernel the card wants the
	 * application processed on, and EMV Contactless Book B derives the
	 * Requested Kernel ID from it.
	 * <p>
	 * Book B v2.6 section 3.3.2, whose wording is unchanged in substance in
	 * v2.12: "If byte 1, b8 and b7 of the Kernel Identifier have the value 00b
	 * or 01b, then Requested Kernel ID is equal to the value of byte 1 of the
	 * Kernel Identifier. If byte 1, b8 and b7 of the Kernel Identifier have the
	 * value 10b or 11b, then: - If the length of the Kernel Identifier value
	 * field is less than 3 bytes, then Entry Point shall return to bullet A and
	 * proceed with the next Directory Entry. - If the Short Kernel ID is
	 * different from 000000b, then the Requested Kernel ID is equal to value of
	 * the byte 1 to byte 3 of the Kernel Identifier. - If the Short Kernel ID is
	 * equal to 000000b, then the determination of the Requested Kernel ID is out
	 * of scope of this specification."
	 * </p>
	 */
	@Test
	public void testKernelIdentifierIsDecoded() {
		// Eight bytes with the Short Kernel ID 3, the value the FailErrorCard
		// and LockedCard captures show a real card returning
		DirectoryEntry visa = new DirectoryEntry();
		visa.setKernelIdentifier(BytesUtils.fromString("0300000000000000"));
		Assertions.assertThat(visa.getKernelType()).isEqualTo(KernelTypeEnum.INTERNATIONAL);
		Assertions.assertThat(visa.getShortKernelId()).isEqualTo(3);
		Assertions.assertThat(visa.getKernel()).isEqualTo(KernelEnum.VISA);
		Assertions.assertThat(visa.getRequestedKernelId()).isEqualTo(BytesUtils.fromString("03"));

		// '01b' is RFU: the Requested Kernel ID is the whole first byte, so
		// '42' is not the international Kernel 2
		DirectoryEntry rfu = new DirectoryEntry();
		rfu.setKernelIdentifier(BytesUtils.fromString("42"));
		Assertions.assertThat(rfu.getKernelType()).isEqualTo(KernelTypeEnum.RFU);
		Assertions.assertThat(rfu.getShortKernelId()).isEqualTo(2);
		Assertions.assertThat(rfu.getKernel()).isNull();
		Assertions.assertThat(rfu.getRequestedKernelId()).isEqualTo(BytesUtils.fromString("42"));

		// '11b' is a domestic kernel in a proprietary format, named by the
		// three first bytes as a whole
		DirectoryEntry domestic = new DirectoryEntry();
		domestic.setKernelIdentifier(BytesUtils.fromString("C2FFFF"));
		Assertions.assertThat(domestic.getKernelType()).isEqualTo(KernelTypeEnum.DOMESTIC_PROPRIETARY_FORMAT);
		Assertions.assertThat(domestic.getKernel()).isNull();
		Assertions.assertThat(domestic.getRequestedKernelId()).isEqualTo(BytesUtils.fromString("C2FFFF"));

		// A domestic identifier shorter than three bytes has its entry rejected
		// by Entry Point: there is no Requested Kernel ID to report
		DirectoryEntry tooShort = new DirectoryEntry();
		tooShort.setKernelIdentifier(BytesUtils.fromString("C2"));
		Assertions.assertThat(tooShort.getKernelType()).isEqualTo(KernelTypeEnum.DOMESTIC_PROPRIETARY_FORMAT);
		Assertions.assertThat(tooShort.getRequestedKernelId()).isNull();

		// A domestic identifier whose Short Kernel ID is '000000b' leaves the
		// derivation "out of scope of this specification"
		DirectoryEntry outOfScope = new DirectoryEntry();
		outOfScope.setKernelIdentifier(BytesUtils.fromString("C0FFFF"));
		Assertions.assertThat(outOfScope.getShortKernelId()).isZero();
		Assertions.assertThat(outOfScope.getRequestedKernelId()).isNull();

		// "If the length of the Kernel Identifier value field is zero, then
		// Entry Point shall use a default value for the Requested Kernel ID,
		// based on the matching AID": the default belongs to the reader
		DirectoryEntry empty = new DirectoryEntry();
		empty.setKernelIdentifier(new byte[0]);
		Assertions.assertThat(empty.getKernelType()).isNull();
		Assertions.assertThat(empty.getShortKernelId()).isEqualTo(DirectoryEntry.UNKNOWN);
		Assertions.assertThat(empty.getRequestedKernelId()).isNull();
	}

	/**
	 * A whole PPSE directory entry, with the elements EMV Contactless Book B
	 * and ISO/IEC 7816-4 allow next to the ADF Name.
	 * <p>
	 * The FCI below is a structure built for this test, not a capture: no
	 * recorded trace carries a Kernel Identifier, a Path or a Directory
	 * Discretionary Template inside an Application Template. Its AID, label and
	 * priority are those of the MasterCardPpse capture, its Kernel Identifier
	 * that of the FailErrorCard capture, and its Path the one ISO/IEC 7816-4
	 * section 8.2.1.1 gives for EF.DIR. The Extended Selection content is
	 * "payment system proprietary" (Book B Table A-1), no document defines a
	 * value for it: the two bytes are the ones ApplicationSelectionTest already
	 * uses and only show that the value is carried through.
	 * </p>
	 */
	@Test
	public void testDirectoryEntryCarriesEveryElementOfTheTemplate() {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		List<DirectoryEntry> entries = ReflectionTestUtils.invokeMethod(parser, "parseDirectoryEntries",
				BytesUtils.fromString("6F 4A 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 38 BF 0C 35"
						+ " 61 33 4F 07 A0 00 00 00 04 10 10"
						+ " 50 0A 4D 41 53 54 45 52 43 41 52 44"
						+ " 87 01 02"
						+ " 9F 2A 08 03 00 00 00 00 00 00 00"
						+ " 9F 29 02 12 34"
						+ " 51 04 3F 00 2F 00"
						+ " 73 03 9F 0A 00"));

		Assertions.assertThat(entries).hasSize(1);
		DirectoryEntry entry = entries.get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(entry.getAid())).isEqualTo("A0000000041010");
		Assertions.assertThat(entry.getApplicationLabel()).isEqualTo("MASTERCARD");
		Assertions.assertThat(entry.getPriority()).isEqualTo(2);
		Assertions.assertThat(entry.getKernel()).isEqualTo(KernelEnum.VISA);
		Assertions.assertThat(entry.getExtendedSelection()).isEqualTo(BytesUtils.fromString("1234"));
		Assertions.assertThat(entry.getPathType()).isEqualTo(FileReferenceEnum.ABSOLUTE_PATH);
		Assertions.assertThat(entry.isSelectionByPath()).isTrue();
		// EMV 4.4 Book 1 Table 12 nests the ASRPD in the Directory
		// Discretionary Template, both are reported
		Assertions.assertThat(entry.getDirectoryDiscretionaryTemplate()).isEqualTo(BytesUtils.fromString("9F0A00"));
		Assertions.assertThat(entry.getApplicationSelectionRegisteredProprietaryData()).isEqualTo(new byte[0]);
		Assertions.assertThat(entry.isDirectoryDefinitionFile()).isFalse();
		// Every primitive data object, the content of the Directory
		// Discretionary Template included, keyed by its tag
		Assertions.assertThat(new java.util.ArrayList<String>(entry.getDataObjects().keySet())).containsExactly("4F", "50", "87", "9F2A", "9F29", "51", "9F0A");
		Assertions.assertThat(entry.getDataObjects().get("9F29")).isEqualTo(BytesUtils.fromString("1234"));
		Assertions.assertThat(entry.getDataObjects().get("9F0A")).isEqualTo(new byte[0]);
		Assertions.assertThat(entry.getRaw()).hasSize(0x33);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(entry.getRaw())).startsWith("4F07A0000000041010");
		Assertions.assertThat(entry.getAdditionalAdfNames()).isEmpty();
	}

	/**
	 * A directory entry may reference a sub directory instead of an
	 * application: EMV 4.4 Book 1 Table 16 gives it a DDF Name (tag '9D') and
	 * no ADF Name. It is modelled as an entry, and it describes no application.
	 * <p>
	 * The record below is the one
	 * ApplicationSelectionTest#testDirectoryDefinitionFileIsFollowed scripts,
	 * built on EMV 4.4 Book 1 Table 11: a record ('70') holding two entries.
	 * </p>
	 */
	@Test
	public void testDirectoryDefinitionFileEntryDescribesNoApplication() {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		byte[] record = BytesUtils.fromString("70 20 61 10 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01"
				+ " 61 0C 9D 07 A0 00 00 00 03 00 00 87 01 02");

		List<DirectoryEntry> entries = ReflectionTestUtils.invokeMethod(parser, "parseDirectoryEntries", record);
		Assertions.assertThat(entries).hasSize(2);
		Assertions.assertThat(entries.get(0).isDirectoryDefinitionFile()).isFalse();
		Assertions.assertThat(entries.get(0).getDdfName()).isNull();

		DirectoryEntry subDirectory = entries.get(1);
		Assertions.assertThat(subDirectory.isDirectoryDefinitionFile()).isTrue();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(subDirectory.getDdfName())).isEqualTo("A0000000030000");
		Assertions.assertThat(subDirectory.getAid()).isNull();
		Assertions.assertThat(subDirectory.getPriority()).isEqualTo(2);

		// The sub directory entry must not be mistaken for an application: the
		// candidate list holds the one application of the record
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "getApplicationTemplate", record);
		Assertions.assertThat(apps).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(apps.get(0).getAid())).isEqualTo("A0000000421010");
	}

	/**
	 * Build an entry holding the File reference data element in parameter
	 *
	 * @param pPath
	 *            raw value of the tag '51'
	 * @return the entry
	 */
	private DirectoryEntry path(final byte[] pPath) {
		DirectoryEntry entry = new DirectoryEntry();
		entry.setPath(pPath);
		return entry;
	}

}
