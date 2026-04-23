package org.cityuhk.CourseRegistrationSystem;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CourseRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(CourseRegistrationApplication.class);
		if (containsCliArg(args)) {
			Map<String, Object> cliDefaults = new HashMap<>();
			cliDefaults.put("app.cli.enabled", true);
			cliDefaults.put("server.port", 0);
			cliDefaults.put("spring.jpa.show-sql", false);
			cliDefaults.put("logging.level.org.hibernate", "warn");
			cliDefaults.put("logging.level.org.hibernate.SQL", "off");
			cliDefaults.put("logging.level.org.hibernate.orm.jdbc.bind", "off");
			application.setDefaultProperties(cliDefaults);
		}

		application.run(args);
	}

	private static boolean containsCliArg(String[] args) {
		for (String arg : args) {
			if ("--cli".equalsIgnoreCase(arg)) {
				return true;
			}
		}
		return false;
	}

}
