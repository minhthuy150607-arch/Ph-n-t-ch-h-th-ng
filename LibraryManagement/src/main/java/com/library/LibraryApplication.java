package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lớp khởi động ứng dụng Spring Boot.
 * Annotation @SpringBootApplication bao gồm:
 * - @Configuration: đánh dấu class cấu hình
 * - @EnableAutoConfiguration: tự động cấu hình Spring Boot
 * - @ComponentScan: quét các component trong package com.library
 */
@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
