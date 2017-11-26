package de.slx.arcademenu;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.io.File;

public class ArcadeMenu extends JFrame {
    String musicPath = "src/main/resources/Music.mp3";

    public static void main(String[] args) {
        new ArcadeMenu();
    }

    public ArcadeMenu(){
        //this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //DO_NOTHING_ON_CLOSE

        Media music = new Media(new File(musicPath).toURI().toString());
        MediaPlayer player = new MediaPlayer(music);
        player.play();
    }
}
