package cz.fio.translator.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cz.fio.translator.finder.service.TranslatorService;

@SpringBootApplication
public class TranslationFinderApplication implements ApplicationRunner {

	//export JAVA_HOME=/home/baros/install/java14/jdk-14.0.2
	//export MAVEN_OPTS="-Dhttp.proxyHost=proxy.private.fio.cz -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.private.fio.cz -Dhttps.proxyPort=8080"
	//./mvnw clean install


	// ./mvnw spring-boot:run
	// mvn spring-boot:run -Dspring-boot.run.arguments=--firstName=Sergey,--lastName=Kargopolov

	private static final Logger log = LoggerFactory.getLogger(TranslationFinderApplication.class);

	private final TranslatorService translator;

	public TranslationFinderApplication(TranslatorService translator) {
		this.translator = translator;
	}

	public static void main(String[] args) {
		SpringApplication.run(TranslationFinderApplication.class, args);
		log.info("APPLICATION FINISHED");
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("EXECUTING : command line runner");

//		if (!args.containsOption("project.path")) {
//			log.warn(
//					"Set an application property 'project.path', e.g. mvn spring-boot:run -Dspring-boot.run.arguments=--project.path=path-to-the-project");
//			return;
//		}
//		String projectPath = args.getOptionValues("project.path").get(0);

		var result = translator.findUnusedTranslationSections();
		log.info("List of unused sections count: {}", result.size());
		result.forEach(s -> log.info(s.toString()));

		result = translator.findEmptyTranslationSections();
		log.info("List of empty sections count: {}", result.size());
		result.forEach(s -> log.info(s.toString()));

//		var result = translator.findUnusedTranslations();
//		log.info("List of unused translations count: {}", result.size());



	}
}
