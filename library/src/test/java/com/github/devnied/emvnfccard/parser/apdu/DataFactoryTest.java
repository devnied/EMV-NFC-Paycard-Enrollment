package com.github.devnied.emvnfccard.parser.apdu;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.parser.apdu.impl.DataFactory;

public class DataFactoryTest {

	/**
	 * A CPLC date only carries the last digit of the year and the day of year,
	 * so the decade is inferred: the expected date is the most recent one that
	 * is not in the future. Computing it here keeps the test independent of the
	 * date it is run at.
	 *
	 * @param pYearDigit
	 *            last digit of the year encoded in the CPLC date
	 * @param pDayOfYear
	 *            day of year encoded in the CPLC date
	 * @return the date the parser is expected to return
	 */
	private static Date expectedCplcDate(final int pYearDigit, final int pDayOfYear) {
		Calendar now = new GregorianCalendar();
		int year = now.get(Calendar.YEAR) - now.get(Calendar.YEAR) % 10 + pYearDigit;
		Calendar expected = new GregorianCalendar();
		expected.clear();
		expected.set(Calendar.YEAR, year);
		expected.set(Calendar.DAY_OF_YEAR, pDayOfYear);
		while (expected.after(now)) {
			expected.clear();
			expected.set(Calendar.YEAR, year -= 10);
			expected.set(Calendar.DAY_OF_YEAR, pDayOfYear);
		}
		return expected.getTime();
	}

	@Test
	public void testNullCplcDate() {
		Date date = DataFactory.calculateCplcDate(new byte[] { 0, 0 });
		Assertions.assertThat(date).isNull();
	}

	@Test
	public void testCplcDate() {
		// 0x8102 -> year digit 8, day of year 102
		Date date = DataFactory.calculateCplcDate(new byte[] { (byte) 0x81, 0x02 });
		Assertions.assertThat(date).isNotNull();
		Assertions.assertThat(date).isEqualTo(expectedCplcDate(8, 102));
	}

	@Test
	public void testCplcDate2() {
		// 0x6066 -> year digit 6, day of year 66
		Date date = DataFactory.calculateCplcDate(new byte[] { 0x60, 0x66 });
		Assertions.assertThat(date).isNotNull();
		Assertions.assertThat(date).isEqualTo(expectedCplcDate(6, 66));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCplcDateWrongLength() {
		DataFactory.calculateCplcDate(new byte[] { 0x60 });
	}

}
