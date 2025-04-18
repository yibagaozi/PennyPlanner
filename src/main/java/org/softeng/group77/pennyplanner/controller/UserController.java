package org.softeng.group77.pennyplanner.controller;

import javafx.fxml.FXML;

import java.io.IOException;

public class UserController {
    @FXML
    private void turntoHome() throws IOException {
        System.out.println("转到home页面");
        MainApp.showHome();
    }
    @FXML
    private void turntoReport() throws IOException {
        System.out.println("转到home页面");
        //MainApp.showReport();
    }@FXML
    private void turntoHistory() throws IOException {
        System.out.println("转到home页面");
        MainApp.showhistory();
    }@FXML
    private void turntoManagement() throws IOException {
        System.out.println("转到home页面");
        MainApp.showmanagement();
    }@FXML
    private void turntoUser() throws IOException {
        System.out.println("转到home页面");
        MainApp.showuser();
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
}
