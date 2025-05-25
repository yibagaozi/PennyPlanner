package org.softeng.group77.pennyplanner.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Deprecated
@Component
public class DataInitializationRunner implements CommandLineRunner {

    @Autowired
    private TestDataGenerateUtil testDataGenerateUtil;

    @Override
    public void run(String... args) throws Exception {
        // try {
        //     clearFilesInDirectory("data");
        //     testDataGenerateUtil.generateTestUserData();
        //     System.out.println("Test user data has been generated successfully.");
        // } catch (IOException e) {
        //     System.err.println("Error generating test user data: " + e.getMessage());
        // }
    }

    private void clearFilesInDirectory(String directoryPath) {
        Path dirPath = Paths.get(directoryPath);

        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            System.out.println("Data folder cleared successfully.");
        } catch (IOException e) {
            System.err.println("Error clearing data folder: " + e.getMessage());
        }
    }
}
