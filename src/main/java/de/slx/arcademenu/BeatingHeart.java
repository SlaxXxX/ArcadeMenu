package de.slx.arcademenu;

import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

@SuppressWarnings("restriction")
public class BeatingHeart extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) {
		//ImageView heart = new ImageView(HEART_IMAGE_LOC);
		Circle heart = new Circle(0, 0, 20);
		heart.relocate(-1000, 0);

		StackPane layout = new StackPane(heart);
		animateUsingScaleTransition(heart);

		layout.setPrefWidth(600);
		layout.setPrefHeight(600);

		Scene scene = new Scene(layout);
		stage.setScene(scene);
		stage.show();
	}

	private void animateUsingScaleTransition(Circle heart) {
		ScaleTransition scaleTransition = new ScaleTransition(
				Duration.seconds(1), heart);
		PathTransition pathTransition = new PathTransition();

		scaleTransition.setFromX(1);
		scaleTransition.setFromY(1);
		scaleTransition.setToX(1.2);
		scaleTransition.setToY(1.2);

		Path path = new Path();
		path.getElements().add(new MoveTo(heart.getLayoutX(), heart.getLayoutY()));

		ArcTo arcTo = new ArcTo(300, 300, 0, 400, 400, false, false);

		path.getElements().add(arcTo);

		pathTransition.setDuration(Duration.seconds(1));
		pathTransition.setNode(heart);
		pathTransition.setPath(path);
		pathTransition.setAutoReverse(false);
		pathTransition.setCycleCount(Animation.INDEFINITE);

		pathTransition.setOnFinished(event -> System.out.println(heart.getCenterX() + " | " + heart.getCenterX()));

		scaleTransition.setAutoReverse(true);
		scaleTransition.setCycleCount(Animation.INDEFINITE);
		//scaleTransition.play();
		pathTransition.play();
	}

	private static final String HEART_IMAGE_LOC = "http://icons.iconarchive.com/icons/mirella-gabriele/valentine/128/Heart-red-icon.png";
	// icon obtained from: http://www.iconarchive.com/show/valentine-icons-by-mirella-gabriele/Heart-red-icon.html
	// icon license: Free for non-commercial use, commercial use not allowed.
}