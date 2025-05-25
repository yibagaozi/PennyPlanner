# Deployment Guide for PennyPlanner Project

## Prerequisites
Before you begin, ensure you have the following installed:
- Java 21 (JDK)
- JavaFx 21.0.6
- Apache Maven

---

# Start from Jar File
To run the project using the jar file created in the Github Release module, use the following command:
```bash
java --module-path "path/to/Java/jdk-21/javafx-sdk-21.0.6/lib" --add-modules javafx.controls,javafx.fxml -jar PennyPlanner-<version>.jar
```
**Replace** `module-path` and `<version>` with the actual module path to JavaFx on your device and version of the jar file.

For example:
```bash
java --module-path "C:/Program Files/Java/jdk-21/javafx-sdk-21.0.6/lib" --add-modules javafx.controls,javafx.fxml -jar PennyPlanner-2.0.0.jar
````

Or if using a JDK21 release with JavaFX included *(Liberica JDK)*, you can run:
```bash
java -jar PennyPlanner-<version>.jar
```
---

# Start from Source Code

## Clone from Github
1. **Clone the Repository**
   ```bash
   git clone https://github.com/yibagaozi/PennyPlanner.git
   ```

2. **Navigate to the Project Directory**
   ```bash
   cd PennyPlanner
   ```

## Running the Project
After you cloning the project, you can run the program using the following command:
```bash
mvn javafx:run
```

## Additional Maven Commands
- **Clean the Project**: This command removes the `target` directory with all the build data before performing the actual build.
  ```bash
  mvn clean
  ```

## Troubleshooting
- **Maven not recognized**: Ensure Maven is installed and the `MAVEN_HOME` environment variable is set correctly.
- **Java version issues**: Ensure the correct Java version is installed and the `JAVA_HOME` environment variable is set correctly.
- **Dependency issues**: Run `mvn dependency:resolve` to resolve and download dependencies specified in the `pom.xml` file.

---