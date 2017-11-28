package de.slx.arcademenu;

import java.awt.Point;
import java.util.ArrayList;

import javafx.animation.*;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

@SuppressWarnings("restriction")
public class BeatingHeart extends Application {
	Point d = new Point(1024, 768);
	final int radius = (int) (d.x / 4 * 2.5);
	final int stepsize = 12;
	final double offset = Math.toRadians(265);

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) {
		//ImageView heart = new ImageView(HEART_IMAGE_LOC);
		ArrayList<Rectangle> cubeList = new ArrayList<>();
		StackPane layout = new StackPane();
		for (int i = 0; i < 6; i++) {
			if (i == 2) {
				cubeList.add(new Rectangle(150, 150));
			} else {
				cubeList.add(new Rectangle(100, 100));
			}

			layout.getChildren().add(cubeList.get(cubeList.size() - 1));
		}
		Scene scene = new Scene(layout, d.x, d.y);

		stage.setScene(scene);
		stage.show();

		int step = stepsize;
		for (int i = 0; i < cubeList.size(); i++) {
			cubeList.get(i).relocate(d.x / 4 * 2.5, d.y + cubeList.get(i).getHeight() / 2);
			animateUsingScaleTransition(cubeList.get(i), i * 150, step);
			step += (int) (stepsize * cubeList.get(i).getWidth() * ((i < cubeList.size() - 1) ? cubeList.get(i + 1).getWidth() : 100) / 10000);
		}
	}

	private void animateUsingScaleTransition(Rectangle cube, int delay, int step) {
		//		ScaleTransition scaleTransition = new ScaleTransition(
		//				Duration.seconds(1), cube);

		//		scaleTransition.setFromX(1);
		//		scaleTransition.setFromY(1);
		//		scaleTransition.setToX(1.2);
		//		scaleTransition.setToY(1.2);

		PathTransition pathTransition = new PathTransition();

		Path path = new Path();
		Point delta = getDelta(cube, step);
		System.out.println(delta.x + " " + delta.y);

		path.getElements().add(new MoveTo(0, 0));

		ArcTo arcTo = new ArcTo(radius, radius,
				0, delta.x, delta.y, false, false);

		path.getElements().add(arcTo);

		pathTransition.setDelay(Duration.millis(delay));
		pathTransition.setDuration(Duration.millis(1000));
		pathTransition.setNode(cube);
		pathTransition.setPath(path);
		//		pathTransition.setAutoReverse(true);
		//pathTransition.setCycleCount(Animation.INDEFINITE);
		
		PathTransition transition = new PathTransition();

		Path pathb = new Path();

		pathb.getElements().add(new MoveTo(0, 0));
		pathb.getElements().add(new MoveTo(100, 0));
		
		transition.setDuration(Duration.millis(50));
		transition.setNode(cube);
		transition.setPath(pathb);
		transition.setAutoReverse(true);
		transition.setCycleCount(Animation.INDEFINITE);


		transition.play();
		pathTransition.play();

		//		scaleTransition.setAutoReverse(true);
		//		scaleTransition.setCycleCount(Animation.INDEFINITE);
		//		scaleTransition.play();
	}

	private Point getDelta(Rectangle cube, int step) {
		Point middle = new Point((int) cube.getLayoutX() - radius, (int) cube.getLayoutY());
		Point stop = new Point(middle.x + (int) (radius * Math.cos(offset + Math.toRadians(step))), middle.y + (int) (radius * Math.sin(offset + Math.toRadians(step))));
		return new Point(stop.x - (int) cube.getLayoutX(), stop.y - (int) cube.getLayoutY());
	}

	private static final String HEART_IMAGE_LOC = "http://icons.iconarchive.com/icons/mirella-gabriele/valentine/128/Heart-red-icon.png";
	// icon obtained from: http://www.iconarchive.com/show/valentine-icons-by-mirella-gabriele/Heart-red-icon.html
	// icon license: Free for non-commercial use, commercial use not allowed.
}