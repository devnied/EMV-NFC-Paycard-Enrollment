package com.github.devnied.emvnfccard.parser.apdu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationData;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationUtils;
import com.github.devnied.emvnfccard.parser.apdu.annotation.Data;

import fr.devnied.bitlib.BytesUtils;

public class AnnotationDataTest {

	@Data(index = 1, tag = "", size = 12)
	public String value;

	@Data(index = 2, tag = "", size = 12)
	public String value2;

	@Test
	public void testEquals() {

		AnnotationData data1 = new AnnotationData();
		data1.initFromAnnotation(FieldUtils.getField(AnnotationDataTest.class, "value").getAnnotation(Data.class));

		AnnotationData data2 = new AnnotationData();
		data2.initFromAnnotation(FieldUtils.getField(AnnotationDataTest.class, "value").getAnnotation(Data.class));
		Assertions.assertThat(data1).isEqualTo(data2);

		AnnotationData data3 = new AnnotationData();
		data3.initFromAnnotation(FieldUtils.getField(AnnotationDataTest.class, "value2").getAnnotation(Data.class));
		Assertions.assertThat(data1).isNotEqualTo(data3);
	}

	/**
	 * The annotations are cached in a singleton shared by the whole process:
	 * the size a card asks for in its Log Format must be applied to a copy,
	 * otherwise a parsing running on another thread reads its fields at the
	 * wrong offsets.
	 */
	@Test
	public void testSharedAnnotationIsNotModifiedByAParsing() {
		Map<ITag, AnnotationData> shared = AnnotationUtils.getInstance().getMap().get(EmvTransactionRecord.class.getName());
		AnnotationData amount = shared.get(EmvTags.AMOUNT_AUTHORISED_NUMERIC);
		int size = amount.getSize();

		// A card whose log holds an amount on 3 bytes instead of 6
		List<TagAndLength> tags = new ArrayList<TagAndLength>();
		tags.add(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 3));
		EmvTransactionRecord record = new EmvTransactionRecord();
		record.parse(BytesUtils.fromString("000123"), tags);

		Assertions.assertThat(record.getAmount()).isEqualTo(123f);
		Assertions.assertThat(shared.get(EmvTags.AMOUNT_AUTHORISED_NUMERIC).getSize()).isEqualTo(size);
	}

}
