package com.gth.posting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Module duy nhat chay duoc. No khong chua nghiep vu, chi lap rap:
 * domain-core + application-service + data-access + automation + ai + application.
 */
@SpringBootApplication(scanBasePackages = "com.gth.posting")
@EntityScan(basePackages = "com.gth.posting.dao")
@EnableJpaRepositories(basePackages = "com.gth.posting.dao")
public class PostingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostingApplication.class, args);
    }
}
