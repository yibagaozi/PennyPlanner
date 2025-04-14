package org.softeng.group77.pennyplanner.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class MainApp extends Application {
    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
        // 在启动方法中添加
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApp.primaryStage = primaryStage;
        //showSignup();
        showLogin();
        //showHome();
        //showReport();
        //showhistory();
        //showmanagement();
        //showuser();
    }

    public static void showSignup() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/org/softeng/group77/pennyplanner/Signup_view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 400, 600);
        scene.getStylesheets().add(MainApp.class.getResource("login.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/org/softeng/group77/pennyplanner/Login_view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 400, 600);
        scene.getStylesheets().add(MainApp.class.getResource("login.css").toExternalForm());
        primaryStage.setTitle("PennyPlanner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


public static void showHome() throws IOException {
    FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/group77/demo1/home_view.fxml")
    );
    Parent root = loader.load();

    Scene scene = new Scene(root,800,500);
    scene.getStylesheets().add(MainApp.class.getResource("style-home.css").toExternalForm());
    primaryStage.setTitle("PennyPlanner");
    primaryStage.setScene(scene);
    primaryStage.show();
    }
}