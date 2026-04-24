package org.cityuhk.CourseRegistrationSystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class CourseRegistrationApplicationTest {

    @Test
    void main_shouldCallRun_withoutCliArg() {
        try (MockedConstruction<SpringApplication> mocked =
                     mockConstruction(SpringApplication.class, (mock, context) -> {
                         when(mock.run(any(String[].class)))
                                 .thenReturn(mock(ConfigurableApplicationContext.class));
                     })) {

            String[] args = {"--server.port=8080"};
            CourseRegistrationApplication.main(args);

            SpringApplication applicationMock = mocked.constructed().get(0);

            verify(applicationMock).run(args);
            verify(applicationMock, never()).setDefaultProperties(anyMap());
        }
    }

    @Test
    void main_shouldSetCliDefaultsAndCallRun_whenCliArgPresent() {
        try (MockedConstruction<SpringApplication> mocked =
                     mockConstruction(SpringApplication.class, (mock, context) -> {
                         when(mock.run(any(String[].class)))
                                 .thenReturn(mock(ConfigurableApplicationContext.class));
                     })) {

            String[] args = {"--cli"};
            CourseRegistrationApplication.main(args);

            SpringApplication applicationMock = mocked.constructed().get(0);

            @SuppressWarnings("unchecked")
            var captor = org.mockito.ArgumentCaptor.forClass(Map.class);

            verify(applicationMock).setDefaultProperties(captor.capture());
            verify(applicationMock).run(args);

            Map<String, Object> props = captor.getValue();
            assertEquals(true, props.get("app.cli.enabled"));
            assertEquals(0, props.get("server.port"));
            assertEquals(false, props.get("spring.jpa.show-sql"));
            assertEquals("warn", props.get("logging.level.org.hibernate"));
            assertEquals("off", props.get("logging.level.org.hibernate.SQL"));
            assertEquals("off", props.get("logging.level.org.hibernate.orm.jdbc.bind"));
        }
    }

    @Test
    void containsCliArg_shouldReturnTrue_whenCliPresent() throws Exception {
        assertTrue(invokeContainsCliArg(new String[] {"--cli"}));
    }

    @Test
    void containsCliArg_shouldReturnTrue_caseInsensitive() throws Exception {
        assertTrue(invokeContainsCliArg(new String[] {"--CLI"}));
    }

    @Test
    void containsCliArg_shouldReturnFalse_whenCliAbsent() throws Exception {
        assertFalse(invokeContainsCliArg(new String[] {"--server.port=8080"}));
    }

    @Test
    void containsCliArg_shouldReturnFalse_whenArgsEmpty() throws Exception {
        assertFalse(invokeContainsCliArg(new String[] {}));
    }

    private boolean invokeContainsCliArg(String[] args) throws Exception {
        Method method = CourseRegistrationApplication.class
                .getDeclaredMethod("containsCliArg", String[].class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, (Object) args);
    }
}


