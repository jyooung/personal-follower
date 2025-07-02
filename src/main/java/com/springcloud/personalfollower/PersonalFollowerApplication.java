package com.springcloud.personalfollower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class PersonalFollowerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalFollowerApplication.class, args);
    }

}
