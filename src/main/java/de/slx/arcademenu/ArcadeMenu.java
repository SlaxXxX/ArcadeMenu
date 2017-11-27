package de.slx.arcademenu;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class ArcadeMenu extends Application {

    File mainFolder,dataFolder,gamesFolder;
    SortedMap<String, Assets> games = new TreeMap<>();
    int menuIndex = 0;
    GraphicsContext gc;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        try {
            mainFolder = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toURI());
            dataFolder = new File(mainFolder.toURI().getPath(), "data");
            gamesFolder = new File(dataFolder.toURI().getPath(), "games");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println(mainFolder.toURI().toString());
        System.out.println(dataFolder.toURI().toString());
        System.out.println(gamesFolder.toURI().toString());

        for (File file : gamesFolder.listFiles()) {
            String name = file.getName();
            if (name.endsWith(".url") || name.endsWith(".lnk"))
                games.put(strip(name), new Assets(name));
        }
        for (File file : gamesFolder.listFiles()) {
            String name = file.getName();
            if (name.endsWith(".png") || name.endsWith(".jpg"))
                games.get(strip(name)).putPicture(name);
        }
        games.keySet().forEach(System.out::println);
        games.values().forEach(asset -> System.out.println(asset.file + " // " + asset.picture));

        Media music = new Media(dataFolder.toURI().toString() + "/Music.mp3");
        MediaPlayer player = new MediaPlayer(music);
        //player.play();
    }

    private String strip(String s) {
        return s.split("\\.")[0];
    }

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(1024, 768);
        gc = canvas.getGraphicsContext2D();
        drawImage();

        Group root = new Group();
        VBox vBox = new VBox();
        vBox.getChildren().addAll(canvas);
        root.getChildren().add(vBox);
        Scene scene = new Scene(root, 1024, 768);
        scene.setCursor(Cursor.NONE);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::pressedKey);

        stage.setScene(scene);
        //stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        //stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        //stage.setOnCloseRequest(Event::consume);
        stage.show();
    }

    private void drawImage () {
        gc.clearRect(0,0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        ArrayList<String> keys = new ArrayList<>(games.keySet());
        int pointer = menuIndex;
        int xPos = 10;
        int size = 300;
        for (int i = 0; i < 3; i++) {
            if (pointer < 0)
                pointer += keys.size();
            if (pointer > keys.size() - 1)
                pointer -= keys.size();
            gc.drawImage(games.get(keys.get(pointer)).image, 50, xPos, size, size);
            xPos += size + 10;
            size = 150;
            pointer ++;
        }

    }

    private void pressedKey (KeyEvent e) {
        if(e.getCode() == KeyCode.ENTER) {
            ArrayList<String> keys = new ArrayList<>(games.keySet());
            System.out.println(gamesFolder.toURI() + "\"" + keys.get(menuIndex) + "\"");
            try {
                Runtime.getRuntime().exec
                        ("cmd /c start " + gamesFolder.toURI() + "\"" + keys.get(menuIndex) + "\"");
            } catch (Exception ex) {}
        }
        if (e.getCode() == KeyCode.UP)
            menuIndex--;
        if (e.getCode() == KeyCode.DOWN)
            menuIndex++;
        if (menuIndex < 0)
            menuIndex += games.size();
        if (menuIndex > games.size() - 1)
            menuIndex -= games.size();
        drawImage();
    }

    @Override
    public void stop() {
    }

    private class Assets {
        private String picture;
        private Image image;
        private String file;

        private Assets(String file) {
            this.file = file;
        }

        private void putPicture(String pic) {
            picture = pic;
            System.out.println(gamesFolder.toURI().getPath() + picture);
            try {
                image = new Image("file:" + gamesFolder.toURI().getPath() + picture);
            } catch (Exception e) {
                System.out.println("ERROR1");
                e.printStackTrace();
            }
        }
    }
}
