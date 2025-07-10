package com.example.rippleTalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class RippleTalkApplication {

	public static void main(String[] args) {
		SpringApplication.run(RippleTalkApplication.class, args);
	}

}
