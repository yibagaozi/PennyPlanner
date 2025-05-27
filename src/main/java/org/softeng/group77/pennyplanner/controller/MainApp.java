package org.softeng.group77.pennyplanner.controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.softeng.group77.pennyplanner.PennyPlannerApplication;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.BudgetService;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Controller;


import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Main application controller for the PennyPlanner application.
 * * This class initializes the Spring application context, manages the main application window,
 *
 * @author CHAI Jiayang
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 1.0.0
 */
@Controller
public class MainApp extends Application {
    private static Stage primaryStage;
    private static ConfigurableApplicationContext applicationContext;
    private static TransactionAdapter transactionAdapter;
    private static AuthService authService;
    // 添加当前视图跟踪变量，用于刷新数据时确定当前页面
    private static String currentView = "login";

    /**
     * Initialize Spring context.
     * This method is called when the application starts.
     */
    @Override
    public void init() {

        applicationContext = new SpringApplicationBuilder(PennyPlannerApplication.class)
                .run();
    }

    /**
     * Close the Spring context and exit the application.
     */
    @Override
    public void stop() {
        applicationContext.close();
        //clearFilesInDirectory("data");

        //SharedDataModel.clearUIData(); // 只清除UI上的数据，不清除存储的数据
        authService.logout();

        Platform.exit();
    }

    /**
     * Main entry point for the JavaFX application.
     * Runs the JavaFX application by calling the launch method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        // 在启动方法中添加
    }

    /**
     * Start the JavaFX application.
     * 
     * @param primaryStage main application stage
     * @throws Exception exception if there is an error during initialization
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApp.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        showLogin();
    }

    /**
     * Show the signup page.
     * 
     * @throws IOException if loading the FXML file fails
     */
    public static void showSignup() throws IOException {
        URL fxmlUrl = MainApp.class.getResource("/fxml/Signup_view.fxml");
        if (fxmlUrl == null) {
            throw new IOException("无法找到FXML文件: Signup_view.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);

        // 让 Spring 管理 FXML 控制器
        loader.setControllerFactory(applicationContext::getBean);

        Parent root = loader.load();

        Scene scene = new Scene(root, 400, 600);
        scene.getStylesheets().add(MainApp.class.getResource("/css/login.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Show the login page.
     * 
     * @throws IOException if loading the FXML file fails
     */
    public static void showLogin() throws IOException {
        URL fxmlUrl = MainApp.class.getResource("/fxml/Login_view.fxml");
        if (fxmlUrl == null) {
            throw new IOException("无法找到FXML文件: Login_view.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root, 400, 600);
        scene.getStylesheets().add(MainApp.class.getResource("/css/login.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
        SharedDataModel.refreshTransactionData();
    }

    /**
     * Show the home page.
     * 
     * @throws IOException if loading the FXML file fails
     */
    public static void showHome() throws IOException {
        currentView = "home";
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/home_view.fxml")
        );
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root,800,500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-home.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Show the history page.
     * Refreshes transaction data before displaying the page.
     * 
     * @throws IOException if loading the FXML file fails
     */
    public static void showhistory() throws IOException {
        currentView = "history";
        // 刷新交易数据
        SharedDataModel.refreshTransactionData();

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/History_view.fxml")
        );

        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root,800,500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-History.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Show the management page.
     * 
     * @throws IOException if loading the FXML file fails
     */
    public static void showmanagement() throws IOException {
        currentView = "management";
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/Management_view.fxml")
        );
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root,800,500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-Management.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Show the user profile page.
     * 
     * @throws IOException if loading the FXML file fails
     */
    public static void showuser() throws IOException {
        currentView = "user";
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/User.fxml")
        );
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();


        Scene scene = new Scene(root,800,500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-User.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Show the report page.
     * 
     * @throws IOException if loading the FXML file fails
     */
    public static void showReport() throws IOException {
        currentView = "report";
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/Report_view.fxml")
        );

        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root,800,500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-Report.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Show the financial assistant page.
     * This page provides AI-based financial assistance features.
     *
     * @throws IOException if loading the FXML file fails
     */
    public static void showFinancialAssistant() throws IOException {
        //记录来源页面（上一页面）
        FinancialAssistantController.setPreviousView(currentView);
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/fin_ast_view.fxml")
        );
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("PennyPlanner");
        primaryStage.show();
    }

    /**
     * Clear the data folder by deleting all files and subdirectories.
     * Deletes all files and directories in the specified path.
     * 
     * @param directoryPath directory path to clear
     */
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

    /**
     * Set the transaction and authentication services.
     * Initializes the TransactionAdapter with the provided services
     * 
     * @param transactionService service for handling transactions
     * @param authService service for handling authentication
     */
    @Autowired
    public void setServices(TransactionService transactionService, AuthService authService) {
        this.transactionAdapter = new TransactionAdapter(transactionService, authService);
        this.authService = authService;

        // 设置适配器到共享模型
        //SharedDataModel.setTransactionAdapter(transactionAdapter);
        HomeController.setTransactionAdapter(transactionAdapter);
        HomeController.setAuthService(authService);
    }

    /**
     * Get the TransactionAdapter instance.
     * @return the TransactionAdapter instance
     */
    public static TransactionAdapter getTransactionAdapter() {
        return transactionAdapter;
    }

    /**
     * Get the primary stage of the application.
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Refresh the data displayed in the UI.
     * Calls the appropriate method based on the current view.
     */
    public static void refreshData() {
        // 首先刷新共享数据模型
        SharedDataModel.refreshTransactionData();

        // 根据当前视图刷新对应页面
        try {
            switch (currentView) {
                case "home":
                    showHome();
                    break;
                case "history":
                    showhistory();
                    break;
                case "report":
                    showReport();
                    break;
                case "management":
                    // 管理页面通常不需要刷新，但如果需要也可以添加
                    break;
                case "user":
                    showuser();
                    break;
                default:
                    // 默认情况下不做任何操作
                    break;
            }
        } catch (IOException e) {
            System.err.println("刷新数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the current view name.
     * @return the name of the current view
     */
    public static String getCurrentView() {
        return currentView;
    }


}
