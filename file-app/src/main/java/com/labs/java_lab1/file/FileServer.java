package com.labs.java_lab1.file;

import com.labs.java_lab1.common.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@EnableSecurity
@ConfigurationPropertiesScan
@SpringBootApplication
public class FileServer {

	public static void main(String[] args) {
		SpringApplication.run(FileServer.class, args);
	}

}
