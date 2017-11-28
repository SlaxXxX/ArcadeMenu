package de.slx.arcademenu;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TransitionTest extends Application {
    Rectangle rect;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        rect = new Rectangle(100, 100);
        StackPane pane = new StackPane(rect);
        Scene scene = new Scene(pane, 800, 800);
        primaryStage.setScene(scene);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::pressedKey);
        primaryStage.show();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(rect.getLayoutX() + "\t/\t" + rect.getLayoutY() + "\t||\t" + rect.getTranslateX() + "\t/\t" + rect.getTranslateY());
            }
        }).start();
    }

    private void pressedKey(KeyEvent e) {
        PathTransition pathTransition = new PathTransition();

        Path path = new Path();

        rect.setLayoutX(rect.getTranslateX());
        rect.setLayoutY(rect.getTranslateY());
        rect.setTranslateX(0);
        rect.setTranslateY(0);
        path.getElements().add(new MoveTo(0,0));

        if (e.getCode() == KeyCode.UP)
            path.getElements().add(new LineTo(0, -100));
        if (e.getCode() == KeyCode.DOWN)
            path.getElements().add(new LineTo(0, 100));
        if (e.getCode() == KeyCode.RIGHT)
            path.getElements().add(new LineTo(100, 0));
        if (e.getCode() == KeyCode.LEFT)
            path.getElements().add(new LineTo(-100, 0));

        pathTransition.setDuration(Duration.millis(1000));
        pathTransition.setNode(rect);
        pathTransition.setPath(path);
        pathTransition.play();
    }
}
