package de.slx.arcademenu;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

@SuppressWarnings("restriction")
public class TransitionTest extends Application {
	Rectangle rect;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		rect = new Rectangle(100, 100);
		Pane pane = new Pane(rect);
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
		path.getElements().add(new MoveTo(50 + rect.getTranslateX(), 50 + rect.getTranslateY()));

		if (e.getCode() == KeyCode.UP)
			path.getElements().add(new LineTo(50 + rect.getTranslateX(), -50 + rect.getTranslateY()));
		if (e.getCode() == KeyCode.DOWN)
			path.getElements().add(new LineTo(50 + rect.getTranslateX(), 150 + rect.getTranslateY()));
		if (e.getCode() == KeyCode.RIGHT)
			path.getElements().add(new LineTo(150 + rect.getTranslateX(), 50 + rect.getTranslateY()));
		if (e.getCode() == KeyCode.LEFT)
			path.getElements().add(new LineTo(-50 + rect.getTranslateX(), 50 + rect.getTranslateY()));

		pathTransition.setDuration(Duration.millis(100));
		pathTransition.setNode(rect);
		pathTransition.setPath(path);
		pathTransition.play();
	}
}
