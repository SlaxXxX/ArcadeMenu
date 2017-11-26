package de.slx.arcademenu;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

public class ArcadeMenu extends Application {

    String musicPath = "src/main/resources/Music.mp3";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        Media music = new Media(new File(musicPath).toURI().toString());
        MediaPlayer player = new MediaPlayer(music);
        player.play();
    }

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root);
        scene.setCursor(Cursor.NONE);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setOnCloseRequest(Event::consume);
        stage.show();
    }

    @Override
    public void stop() {

    }
}
