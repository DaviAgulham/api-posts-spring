package com.example.api_spring;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.api_spring.user.Role;
import com.example.api_spring.user.User;
import com.example.api_spring.user.UserRepository;

@SpringBootApplication
public class ApiSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiSpringApplication.class, args);
	}

	@Bean
	WebMvcConfigurer staticFiles(@Value("${app.upload-dir}") String dir) {
	return new WebMvcConfigurer() {
		@Override public void addResourceHandlers(ResourceHandlerRegistry r) {
		String abs = Paths.get(dir).toAbsolutePath().toString();
		r.addResourceHandler("/uploads/**").addResourceLocations("file:" + abs + "/");
		}
	};
	}

	@Bean
	CommandLineRunner seed(UserRepository repo, PasswordEncoder enc) {
		return args -> {
			repo.findByEmail("admin@local").orElseGet(() -> {
				User u = new User();
				u.setName("Admin");
				u.setEmail("admin@local");
				u.setPassword(enc.encode("admin123"));
				u.setRole(Role.ADMIN);
				return repo.save(u);
			});
		};
	}
}
