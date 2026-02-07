package com.willows.rta.config;

import com.willows.rta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Autowired
    public DataInitializer(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if it doesn't exist
        if (!userService.usernameExists("admin")) {
            userService.createUser("admin", "admin123", "ROLE_ADMIN");
            System.out.println("========================================");
            System.out.println("Default admin user created:");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("PLEASE CHANGE THIS PASSWORD IMMEDIATELY!");
            System.out.println("========================================");
        }
    }
}
