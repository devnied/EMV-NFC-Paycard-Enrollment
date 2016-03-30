package com.github.devnied.emvnfccard.enums;

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;

public class ApplicationStepEnumTest {

	@Test
	public void test() throws Exception {
		Application app = new Application();
		app.setReadingStep(ApplicationStepEnum.SELECTED);
		Application app2 = new Application();
		app2.setReadingStep(ApplicationStepEnum.NOT_SELECTED);
		
		Assertions.assertThat(ApplicationStepEnum.isAtLeast(null, ApplicationStepEnum.SELECTED)).isFalse();
		Assertions.assertThat(ApplicationStepEnum.isAtLeast(Arrays.asList((Application) null), ApplicationStepEnum.SELECTED)).isFalse();
		Assertions.assertThat(ApplicationStepEnum.isAtLeast(Arrays.asList(app,app2), ApplicationStepEnum.SELECTED)).isTrue();
		Assertions.assertThat(ApplicationStepEnum.isAtLeast(Arrays.asList(app,app2), ApplicationStepEnum.READ)).isFalse();
		
		app.setReadingStep(null);
		
		Assertions.assertThat(ApplicationStepEnum.isAtLeast(Arrays.asList(app,app2), ApplicationStepEnum.READ)).isFalse();
		Assertions.assertThat(ApplicationStepEnum.isAtLeast(Arrays.asList(app,app2), null)).isFalse();
	}

}
