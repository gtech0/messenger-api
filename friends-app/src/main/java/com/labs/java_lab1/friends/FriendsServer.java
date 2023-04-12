package com.labs.java_lab1.friends;

import com.labs.java_lab1.common.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@EnableSecurity
@SpringBootApplication
public class FriendsServer {

	public static void main(String[] args) {
		SpringApplication.run(FriendsServer.class, args);
	}

}
