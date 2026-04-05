run:
	./mvnw.cmd spring-boot:run

run-release: build
	java -jar ./target/gs-course-registration-system-0.0.1-SNAPSHOT.jar

build:
	./mvnw.cmd clean package
