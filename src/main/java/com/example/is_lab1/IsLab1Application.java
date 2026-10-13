package com.example.is_lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class IsLab1Application {

    public static void main(String[] args) {
        SpringApplication.run(IsLab1Application.class, args);
    }

}
