package de.benjaminaaron.ontoengine;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OntoEngineApplication {

    public static void main(String[] args) {
        BasicConfigurator.configure(); // TODO use logback-spring.xml again?
        SpringApplication.run(OntoEngineApplication.class, args);
    }

}
