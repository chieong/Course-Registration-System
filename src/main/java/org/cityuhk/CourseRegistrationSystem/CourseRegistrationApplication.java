package org.cityuhk.CourseRegistrationSystem;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CourseRegistrationApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(CourseRegistrationApplication.class);
		if (containsCliArg(args)) {
			Map<String, Object> cliDefaults = new HashMap<>();
			cliDefaults.put("app.cli.enabled", true);
			cliDefaults.put("server.port", 0);
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
