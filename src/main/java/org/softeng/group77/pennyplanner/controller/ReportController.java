package org.softeng.group77.pennyplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.IOException;

public class ReportController {
    @FXML
    private TextArea myTextArea; // 必须与FXML中的fx:id一致
    @FXML
    private void initialize() {
        // 设置初始文本
        String text="AI-generated forecast or report";
        myTextArea.setText(text);


    }
    @FXML
    private void turntoHome() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showHome();
    }
    @FXML
    private void turntoReport() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showReport();
    }@FXML
    private void turntoHistory() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showhistory();
    }@FXML
    private void turntoManagement() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showmanagement();
    }@FXML
    private void turntoUser() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showuser();
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
    @FXML
    private void useAI() throws IOException {
        System.out.println("Call API for AI-generated content");
    }
}
