package com.tamdao.restful_api_design;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RestfulApiDesignApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestfulApiDesignApplication.class, args);
	}

}
