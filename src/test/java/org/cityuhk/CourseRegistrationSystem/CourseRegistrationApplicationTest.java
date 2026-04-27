package org.cityuhk.CourseRegistrationSystem;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.boot.SpringApplication;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class CourseRegistrationApplicationTest {

    @Test
    void main_WhenCliArgumentProvided_SetsDefaultPropertiesAndRuns() {

        try (MockedConstruction<SpringApplication> mocked =
                     mockConstruction(SpringApplication.class,
                             (mock, context) -> {
                                 when(mock.run(any(String[].class))).thenReturn(null);
                             })) {

            CourseRegistrationApplication.main(new String[]{"--cli"});

            SpringApplication app = mocked.constructed().get(0);

            verify(app).setDefaultProperties(anyMap());
            verify(app).run(new String[]{"--cli"});
        }
    }

    @Test
    void main_WhenNoCliArgument_DoesNotSetDefaultsButRuns() {

        try (MockedConstruction<SpringApplication> mocked =
                     mockConstruction(SpringApplication.class,
                             (mock, context) -> {
                                 when(mock.run(any(String[].class))).thenReturn(null);
                             })) {

            CourseRegistrationApplication.main(new String[]{});

            SpringApplication app = mocked.constructed().get(0);


            verify(app, never()).setDefaultProperties(anyMap());
            verify(app).run(new String[]{});
        }
    }

    @Test
    void containsCliArg_WhenNonCliBeforeCli_ExecutesFalseThenTrueBranch() throws Exception {
        Method method = CourseRegistrationApplication.class
                .getDeclaredMethod("containsCliArg", String[].class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(
                null,
                (Object) new String[] { "--foo", "--cli" }
        );

        assertTrue(result);
    }

}


