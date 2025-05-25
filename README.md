<p align="center"><img src="https://service.cnsportiot.com/photos/pennyplanner.png" alt="PennyPlanner Logo" width="200"/></p>

<h1 align="center">PennyPlanner - Your AI Copilot for Financial Clarity</h1>

## About The Project

PennyPlanner is a user-friendly desktop application designed for personal financial management. 
It helps individuals, including students, professionals, and retirees, to track income and expenses, 
set budgets, and gain insights into their spending habits for better financial decision-making.

---

## Deployment Guide
### Prerequisites
Before you begin, ensure you have the following installed:
- Java 21 (JDK)
- JavaFX 21.0.6
- Apache Maven

---

### Option 1: Start from Jar File
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

### Option 2: Start from Source Code

#### Clone from GitHub
1. **Clone the Repository**
   ```bash
   git clone https://github.com/yibagaozi/PennyPlanner.git
   ```

2. **Navigate to the Project Directory**
   ```bash
   cd PennyPlanner
   ```

#### Running the Project
After you cloning the project, you can run the program using the following command:
```bash
mvn javafx:run
```

#### Additional Maven Commands
- **Clean the Project**: This command removes the `target` directory with all the build data before performing the actual build.
  ```bash
  mvn clean
  ```

### Troubleshooting
- **Maven not recognized**: Ensure Maven is installed and the `MAVEN_HOME` environment variable is set correctly.
- **Java version issues**: Ensure the correct Java version is installed and the `JAVA_HOME` environment variable is set correctly.
- **Dependency issues**: Run `mvn dependency:resolve` to resolve and download dependencies specified in the `pom.xml` file.

---

## Contributors

Thank you to all the people who have contributed to PennyPlanner! 
You can see all contributors [here](https://github.com/yibagaozi/PennyPlanner/graphs/contributors).

- [@yibagaozi](https://github.com/yibagaozi) - Project Maintainer & Spring Boot Developer
- [@Lucaschaai](https://github.com/LucasChaai) - JavaFX Developer
- [@wbsv88](https://github.com/wbsv88) - JavaFX Developer
- [@cccyh-cyh](https://github.com/cccyh-cyh) - Spring Boot Developer
- [@jiangmengnan12345](https://github.com/jiangmengnan12345) - Spring Boot Developer
- [@xxxxxxxxx12345](https://github.com/xxxxxxxxx12345) - Spring Boot Developer
- [@Github Copilot](https://github.com/copilot) - AI Copilot