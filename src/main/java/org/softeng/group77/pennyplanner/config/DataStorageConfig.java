package org.softeng.group77.pennyplanner.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class responsible for setting up and initializing the data storage directories used by the PennyPlanner
 * application.
 *
 * @author MA Ruize
 * @version 2.0.0
 */
@Configuration
public class DataStorageConfig {

    /**
     * Data storage path, defaults to "data" directory
     */
    @Value("${app.data.path:data}")
    private String dataPath;

    /**
     * Initializes the data storage environment after the bean has been constructed.
     *
     * @throws IOException if there are problems creating directories or writing the .gitignore file
     */
    @PostConstruct
    public void init() throws IOException {
        if (!StringUtils.hasText(dataPath)) {
            dataPath = "data";
        }

        Path path = Paths.get(dataPath);
        Files.createDirectories(path);

        Path gitignore = path.resolve(".gitignore");
        if (!Files.exists(gitignore)) {
            Files.write(gitignore, "*.json\n*.backup\n".getBytes());
        }
    }
}
