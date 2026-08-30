package com.github.devnied.emvnfccard.utils;

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.CVM;
import com.github.devnied.emvnfccard.model.enums.CvmConditionEnum;
import com.github.devnied.emvnfccard.model.enums.CvmMethodEnum;

import fr.devnied.bitlib.BytesUtils;

/**
 * The CVM List encoding is specified by EMV 4.3 Book 3, Annex C3:
 *
 * <pre>
 * bytes 1-4 : amount field X
 * bytes 5-8 : amount field Y
 * then 2 bytes per Cardholder Verification Rule:
 *   byte 1 : b7    = apply succeeding CV Rule if this CVM is unsuccessful
 *            b6-b1 = CVM code
 *   byte 2 : CVM condition code
 * </pre>
 */
public class CvmUtilsTest {

	/**
	 * X = 0, Y = 0 then the rules:
	 *
	 * <pre>
	 * 42 01 -&gt; enciphered PIN verified online, try next rule, if unattended cash
	 * 44 03 -&gt; enciphered PIN verified by ICC, try next rule, if the terminal supports the CVM
	 * 41 03 -&gt; plaintext PIN verified by ICC,  try next rule, if the terminal supports the CVM
	 * 1E 03 -&gt; signature,                      last attempt,  if the terminal supports the CVM
	 * 1F 02 -&gt; no CVM required,                last attempt,  if not unattended/manual cash nor cashback
	 * </pre>
	 */
	private static final String CVM_LIST = "00000000 00000000 4201 4403 4103 1E03 1F02";

	@Test
	public void testParseCvmList() {
		List<CVM> list = CvmUtils.parseCvmList(BytesUtils.fromString(CVM_LIST));
		Assertions.assertThat(list).hasSize(5);

		Assertions.assertThat(list.get(0).getMethod()).isEqualTo(CvmMethodEnum.ENCIPHERED_PIN_ONLINE);
		Assertions.assertThat(list.get(0).getCondition()).isEqualTo(CvmConditionEnum.UNATTENDED_CASH);
		Assertions.assertThat(list.get(0).isApplyNextIfUnsuccessful()).isTrue();
		Assertions.assertThat(list.get(0).getMethod().isPin()).isTrue();
		Assertions.assertThat(list.get(0).getRawMethod()).isEqualTo(0x02);
		Assertions.assertThat(list.get(0).getRawCondition()).isEqualTo(0x01);

		Assertions.assertThat(list.get(1).getMethod()).isEqualTo(CvmMethodEnum.ENCIPHERED_PIN_OFFLINE);
		Assertions.assertThat(list.get(1).getCondition()).isEqualTo(CvmConditionEnum.TERMINAL_SUPPORTS_CVM);

		Assertions.assertThat(list.get(2).getMethod()).isEqualTo(CvmMethodEnum.PLAINTEXT_PIN);
		Assertions.assertThat(list.get(2).isApplyNextIfUnsuccessful()).isTrue();

		Assertions.assertThat(list.get(3).getMethod()).isEqualTo(CvmMethodEnum.SIGNATURE);
		Assertions.assertThat(list.get(3).isApplyNextIfUnsuccessful()).isFalse();
		Assertions.assertThat(list.get(3).getMethod().isPin()).isFalse();

		Assertions.assertThat(list.get(4).getMethod()).isEqualTo(CvmMethodEnum.NO_CVM);
		Assertions.assertThat(list.get(4).getCondition())
				.isEqualTo(CvmConditionEnum.NOT_UNATTENDED_NOT_MANUAL_NOT_CASHBACK);
	}

	@Test
	public void testAmounts() {
		// X = 0x00001388 = 5000, Y = 0x000003E8 = 1000, in the minor unit of
		// the application currency
		byte[] cvmList = BytesUtils.fromString("00001388 000003E8 1F06 1E07");
		Assertions.assertThat(CvmUtils.getAmountX(cvmList)).isEqualTo(5000L);
		Assertions.assertThat(CvmUtils.getAmountY(cvmList)).isEqualTo(1000L);

		List<CVM> list = CvmUtils.parseCvmList(cvmList);
		Assertions.assertThat(list).hasSize(2);
		Assertions.assertThat(list.get(0).getCondition()).isEqualTo(CvmConditionEnum.UNDER_X);
		Assertions.assertThat(list.get(1).getCondition()).isEqualTo(CvmConditionEnum.OVER_X);
	}

	@Test
	public void testReservedRanges() {
		// '20' to '2F' are reserved for the payment systems, '30' to '3E' for
		// the issuer and '3F' is not available for use
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x22, (byte) 0x00).getMethod())
				.isEqualTo(CvmMethodEnum.PAYMENT_SYSTEM_SPECIFIC);
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x35, (byte) 0x00).getMethod())
				.isEqualTo(CvmMethodEnum.ISSUER_SPECIFIC);
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x3F, (byte) 0x00).getMethod())
				.isEqualTo(CvmMethodEnum.NOT_AVAILABLE);
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x00, (byte) 0x00).getMethod()).isEqualTo(CvmMethodEnum.FAIL);
		// Condition codes above '09' are reserved and left undecoded
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x1F, (byte) 0x7F).getCondition()).isNull();
		Assertions.assertThat(CvmUtils.parseRule((byte) 0x1F, (byte) 0x7F).getRawCondition()).isEqualTo(0x7F);
	}

	@Test
	public void testEmptyAndTruncatedList() {
		Assertions.assertThat(CvmUtils.parseCvmList(null)).isEmpty();
		// A list holding only the X and Y amounts has no rule
		Assertions.assertThat(CvmUtils.parseCvmList(BytesUtils.fromString("0000000000000000"))).isEmpty();
		// A trailing incomplete rule is ignored
		Assertions.assertThat(CvmUtils.parseCvmList(BytesUtils.fromString("00000000000000001F0242"))).hasSize(1);
		Assertions.assertThat(CvmUtils.getAmountX(null)).isEqualTo(-1L);
		Assertions.assertThat(CvmUtils.getAmountY(BytesUtils.fromString("0000"))).isEqualTo(-1L);
	}

}
