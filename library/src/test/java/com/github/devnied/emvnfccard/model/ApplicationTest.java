package com.github.devnied.emvnfccard.model;

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ApplicationTest {

    @Test
    public void testApplicationOrder(){
        Application application1 = new Application();
        application1.setPriority(1);

        Application application2 = new Application();
        application2.setPriority(2);

        Application application3 = new Application();
        application3.setPriority(3);

        List<Application> applicationList = Arrays.asList(application3, application1, application2);
        Collections.sort(applicationList);

        Assertions.assertThat(applicationList.get(0).getPriority()).isEqualTo(1);
        Assertions.assertThat(applicationList.get(1)).isEqualTo(application2);
        Assertions.assertThat(applicationList.get(2)).isEqualTo(application3);
    }

}
