package de.slx.arcademenu;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.animation.PathTransition;
import javafx.scene.*;
import javafx.event.*;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@SuppressWarnings("restriction")
public class ArcadeMenu extends Application {

	File mainFolder, dataFolder, gamesFolder;
	ArrayList<Element> games = new ArrayList<>();
	StackPane layout;

	int[] angles = new int[] { 7, 19, 37, 55, 67, 79 };
	int removeElement = -1;
	int elementCount = 6;

	int delay = 1000;
	int climbingDelay = delay;
	double climbSpeed = 0.98;
	int minDelay = 40;

	final Dimension d = new Dimension(1024, 768);
	final int radius = (int) (d.width / 4 * 2.5);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
		try {
			mainFolder = new File(new File(getClass().getProtectionDomain().getCodeSource()
					.getLocation().toURI()).getParentFile().toURI());
			dataFolder = new File(mainFolder.toURI().getPath(), "data");
			gamesFolder = new File(dataFolder.toURI().getPath(), "games");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println(gamesFolder);

		for (File file : gamesFolder.listFiles()) {
			String name = file.getName();
			if (name.endsWith(".url") || name.endsWith(".lnk"))
				games.add(new Element(name));
		}
		for (File file : gamesFolder.listFiles()) {
			String name = file.getName();
			if (name.endsWith(".png") || name.endsWith(".jpg"))
				games.stream().filter(el -> el.name.equals(strip(name))).forEach(el -> el.putPicture(name));
		}

		games.removeAll(games.stream().filter(game -> game.image == null).collect(Collectors.toList()));

		//		 Media music = new Media(dataFolder.toURI().toString() + "/Music.mp3");
		//		 MediaPlayer player = new MediaPlayer(music);
		//		 player.play();
	}

	private String strip(String s) {
		return s.split("\\.")[0];
	}

	@Override
	public void start(Stage stage) {
		layout = new StackPane();
		int oldSize = games.size();
		for (int i = oldSize; i < elementCount; i++)
			games.add(new Element(games.get(i % oldSize)));
		games.forEach(element -> layout.getChildren().add(element.image));

		Scene scene = new Scene(layout, d.width, d.height);
		scene.setCursor(Cursor.NONE);
		scene.addEventFilter(KeyEvent.KEY_PRESSED, this::pressedKey);
		scene.setOnKeyReleased(ke -> {
			climbingDelay = delay;
		});
		stage.setScene(scene);

		// stage.setFullScreen(true);
		// stage.setFullScreenExitHint("");
		// stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		// stage.setOnCloseRequest(Event::consume);
		stage.show();
		for (int i = 0; i < games.size(); i++) {
			games.get((i + games.size() - 2) % games.size()).image.relocate(radius, d.height + games.get(i).image.getFitHeight() / 2);
			if (i < elementCount)
				games.get((i + games.size() - 2) % games.size()).setPath(i, i * 150, 1000);
		}
	}

	private int add(int num, int add, int max) {
		num += add;
		if (num > max)
			num -= max;
		if (num < 0)
			num += max;
		return num;
	}

	private void pressedKey(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			try {
				Runtime.getRuntime().exec("cmd /c start \"\" \"" + gamesFolder + "\\" + games.get(0).file + "\"");
			} catch (Exception ex) {
			}
		}
		if (e.getCode() == KeyCode.UP && removeElement == -1) {
			removeElement = 0;
			games.add(new Element(games.get(0)));
			layout.getChildren().add(games.get(games.size() - 1).image);
			games.get(games.size() - 1).image.relocate(radius, d.height + games.get(games.size() - 1).image.getFitHeight() / 2);
			for (int i = 0; i < elementCount; i++) {
				games.get((i + elementCount - 2) % elementCount).setPath(i, 0, climbingDelay);
			}
			if (climbingDelay > minDelay)
				climbingDelay *= climbSpeed;
		}
		if (e.getCode() == KeyCode.DOWN && removeElement == -1) {
			removeElement = games.size();
			games.add(0, new Element(games.get(games.size() - 1)));
			layout.getChildren().add(games.get(0).image);
			games.get(0).image.relocate(0, radius + games.get(0).image.getFitHeight() / 2);
			for (int i = 0; i < elementCount; i++) {
				games.get((i + elementCount - 2) % elementCount).setPath(i, 0, climbingDelay);
			}
			if (climbingDelay > minDelay)
				climbingDelay *= climbSpeed;
		}
	}

	@Override
	public void stop() {
	}

	private class Element {
		private String picture;
		private ImageView image;
		private String file;
		private String name;
		private PathTransition path;

		private Element(String file) {
			this.file = file;
			name = strip(file);
		}

		private Element(Element element) {
			this.file = element.file;
			name = element.name;
			putPicture(element.picture);
		}

		private void putPicture(String picture) {
			this.picture = picture;
			//System.out.println(gamesFolder.toURI().getPath() + picture);
			try {
				image = new ImageView("file:" + gamesFolder.toURI().getPath() + picture);
				image.setFitHeight(100);
				image.setFitWidth(100);
			} catch (Exception e) {
			}
		}

		private Point getPos(Node node) {
			return new Point((int) (node.getLayoutX() + node.getTranslateX()), (int) (node.getLayoutY() + node.getTranslateY()));
		}

		private Point getDelta(ImageView image, int pos) {
			Point middle = new Point(getPos(image).x - radius, getPos(image).y);
			Point stop = new Point(middle.x + (int) (radius * Math.cos(Math.toRadians(270) + Math.toRadians(angles[pos]))), middle.y + (int) (radius * Math.sin(Math.toRadians(270) + Math.toRadians(angles[pos]))));
			return new Point(stop.x - getPos(image).x, stop.y - getPos(image).y);
		}

		private void setPath(int pos, int delay, int duration) {
			PathTransition pathTransition = new PathTransition();

			Path path = new Path();
			Point delta = getDelta(image, pos);
			path.getElements().add(new MoveTo(image.getTranslateX() + image.getFitWidth() / 2, image.getTranslateY() + image.getFitHeight() / 2));

			ArcTo arcTo = new ArcTo(radius, radius,
					0, delta.x, delta.y, false, false);

			path.getElements().add(arcTo);

			pathTransition.setDelay(Duration.millis(delay));
			pathTransition.setDuration(Duration.millis(duration));
			pathTransition.setNode(image);
			pathTransition.setPath(path);

			pathTransition.setOnFinished(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (removeElement != -1) {
						games.remove(removeElement);
						removeElement = -1;
					}
					if (event.getSource() instanceof PathTransition) {
						PathTransition source = (PathTransition) event.getSource();
						Node node = source.getNode();
						System.out.println(node.getLayoutX() + " / " + node.getTranslateX() + " || " + node.getLayoutY() + " / " + node.getTranslateY());
					}
				}
			});

			pathTransition.play();
		}
	}
}
