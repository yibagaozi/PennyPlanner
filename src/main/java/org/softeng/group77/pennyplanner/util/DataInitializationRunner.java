package org.softeng.group77.pennyplanner.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DataInitializationRunner implements CommandLineRunner {

    @Autowired
    private TestDataGenerateUtil testDataGenerateUtil;

    @Override
    public void run(String... args) throws Exception {
        try {
            testDataGenerateUtil.generateTestUserData();
            System.out.println("Test user data has been generated successfully.");
        } catch (IOException e) {
            System.err.println("Error generating test user data: " + e.getMessage());
        }
    }
}
