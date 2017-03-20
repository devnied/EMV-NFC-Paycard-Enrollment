package com.github.devnied.emvnfccard.parser.apdu;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationData;
import com.github.devnied.emvnfccard.parser.apdu.annotation.Data;

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

}
