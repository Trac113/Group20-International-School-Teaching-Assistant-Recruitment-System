package com.qq.recruitment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

/**
 * JavaFX application entry point.
 * Initializes the main stage with the login view and provides static navigation via setRoot().
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 960, 640);
        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        stage.setTitle("International School TA Recruitment System");
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch();
    }
}
