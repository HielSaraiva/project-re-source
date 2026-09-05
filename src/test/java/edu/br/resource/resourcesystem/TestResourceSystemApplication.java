package edu.br.resource.resourcesystem;

import org.springframework.boot.SpringApplication;

public class TestResourceSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(ResourceSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
