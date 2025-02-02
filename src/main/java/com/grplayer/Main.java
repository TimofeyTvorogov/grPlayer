package com.grplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {
    @Override
    public void stop() throws Exception {
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemResource("grPlayer.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 900, 1000);

        scene.getStylesheets().add(ClassLoader.getSystemResource("Theme.css").toExternalForm());

//        scene.setFill(Color.TRANSPARENT);

        primaryStage.getIcons().add(new Image(ClassLoader.getSystemResource("images/logo.png").toExternalForm()));
        primaryStage.setTitle("GR_player");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }

}

