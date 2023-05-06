package com.labs.java_lab1.notif;

import com.labs.java_lab1.common.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableSecurity
@SpringBootApplication
public class NotifServer {

	public static void main(String[] args) {
		SpringApplication.run(NotifServer.class, args);
	}

}
