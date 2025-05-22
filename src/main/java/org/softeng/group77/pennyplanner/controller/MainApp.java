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
 * MainApp 负责管理应用程序的生命周期，包括初始化 Spring 应用上下文、加载 FXML 页面、
 * 管理窗口的展示及跳转、清理文件夹等任务。它是整个应用的入口类。
 */
@Controller
public class MainApp extends Application {

    private static Stage primaryStage;
    private static ConfigurableApplicationContext applicationContext;
    private static TransactionAdapter transactionAdapter;
    private static AuthService authService;

    /**
     * 初始化 Spring 应用上下文。
     * 在启动时加载 Spring 配置并运行应用程序。
     */
    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(PennyPlannerApplication.class)
                .run();
    }

    /**
     * 停止应用时关闭 Spring 上下文，并清理相关数据。
     * 退出应用前注销用户并清除 UI 数据。
     */
    @Override
    public void stop() {
        applicationContext.close();
        SharedDataModel.clearUIData(); // 只清除UI上的数据，不清除存储的数据
        authService.logout();
        Platform.exit();
    }

    /**
     * 启动应用程序，初始化 Spring 上下文并显示登录页面。
     * 
     * @param primaryStage 主窗口
     * @throws Exception 启动过程中发生的任何异常
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApp.primaryStage = primaryStage;

        applicationContext = new AnnotationConfigApplicationContext("org.softeng.group77.pennyplanner");
        showLogin();
    }

    /**
     * 显示注册页面。
     * 
     * @throws IOException 如果加载 FXML 文件失败
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
     * 显示登录页面。
     * 
     * @throws IOException 如果加载 FXML 文件失败
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
     * 显示主页页面。
     * 
     * @throws IOException 如果加载 FXML 文件失败
     */
    public static void showHome() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/home_view.fxml")
        );
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-home.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 显示历史记录页面。
     * 刷新交易数据并展示历史记录。
     * 
     * @throws IOException 如果加载 FXML 文件失败
     */
    public static void showhistory() throws IOException {
        // 刷新交易数据 —— ensure 强制刷新
        SharedDataModel.refreshTransactionData();

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/History_view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-History.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 显示管理页面。
     * 
     * @throws IOException 如果加载 FXML 文件失败
     */
    public static void showmanagement() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/Management_view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-Management.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 显示用户页面。
     * 
     * @throws IOException 如果加载 FXML 文件失败
     */
    public static void showuser() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/User_view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-User.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 显示报告页面。
     * 
     * @throws IOException 如果加载 FXML 文件失败
     */
    public static void showReport() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/fxml/Report_view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style-Report.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 清理指定目录中的文件。
     * 通过遍历目录删除所有文件和子目录。
     * 
     * @param directoryPath 需要清理的目录路径
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
     * 设置服务，包括事务服务和认证服务。
     * 通过依赖注入将事务服务和认证服务传递到 MainApp 中。
     * 
     * @param transactionService 事务服务
     * @param authService 认证服务
     */
    @Autowired
    public void setServices(TransactionService transactionService, AuthService authService) {
        this.transactionAdapter = new TransactionAdapter(transactionService);
        this.authService = authService;

        // 设置适配器到共享模型
        SharedDataModel.setTransactionAdapter(transactionAdapter);
        HomeController.setTransactionAdapter(transactionAdapter);
        HomeController.setAuthService(authService);
    }
}
