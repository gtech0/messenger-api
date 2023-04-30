package com.labs.java_lab1.chat;

import com.labs.java_lab1.common.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@EnableSecurity
@SpringBootApplication
public class ChatServer {

	public static void main(String[] args) {
		SpringApplication.run(ChatServer.class, args);
	}

}
