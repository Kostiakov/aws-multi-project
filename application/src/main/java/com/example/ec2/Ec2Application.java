package com.example.ec2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Ec2Application {

	public static void main(String[] args) {
		SpringApplication.run(Ec2Application.class, args);
	}

}
