package de.slx.arcademenu;

import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.animation.*;
import javafx.scene.*;
import javafx.scene.control.*;

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

	int[] angles = new int[] { -8, 7, 19, 35, 55, 67, 79, 91 };
	int pathFinished = 0;
	int elementCount = 8;
	int elementIndex = 0;

	int duration = 150;
	int climbingDuration = duration;
	double climbSpeed = 0.97;
	int minDelay = 50;
	double scaleMain = 1.8;
	int imageSize = 100;

	final Dimension d = new Dimension(1024, 768);
	final int radius = (int) (d.width / 4 * 2.5);

	ScaleTransition upscaleTransition = new ScaleTransition();
	ScaleTransition downscaleTransition = new ScaleTransition();
    FadeTransition fadeinTransition = new FadeTransition();
    FadeTransition fadeoutTransition = new FadeTransition();

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


		fadeinTransition.setFromValue(0.0);
		fadeinTransition.setToValue(1.0);
		fadeoutTransition.setFromValue(1.0);
		fadeoutTransition.setToValue(0.0);
		upscaleTransition.setFromX(1);
		upscaleTransition.setFromY(1);
		upscaleTransition.setToX(scaleMain);
		upscaleTransition.setToY(scaleMain);
		downscaleTransition.setFromX(scaleMain);
		downscaleTransition.setFromY(scaleMain);
		downscaleTransition.setToX(1);
		downscaleTransition.setToY(1);
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

		for (Element game : games) {
			game.group = new Group(game.image, game.text);
			layout.getChildren().add(game.group);
		}

		Scene scene = new Scene(layout, d.width, d.height);
		//		scene.setCursor(Cursor.NONE);
		scene.addEventFilter(KeyEvent.KEY_PRESSED, this::pressedKey);
		scene.setOnKeyReleased(ke -> {
			climbingDuration = duration;
		});
		stage.setScene(scene);

		// stage.setFullScreen(true);
		// stage.setFullScreenExitHint("");
		// stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		// stage.setOnCloseRequest(Event::consume);
		stage.show();

		games.get(getIndex(3)).scale = 1.8;
		for (int i = 0; i < games.size(); i++) {
			games.get((i + games.size() - 3) % games.size()).group.relocate(radius, d.height + imageSize / 2);
			if (i < elementCount)
				games.get((i + games.size() - 3) % games.size()).setPath(i, i * 150, 1000);
		}
		fadeinTransition.setNode(games.get(getIndex(3)).text);
		fadeinTransition.setDuration(Duration.millis(1150));
		fadeinTransition.play();
		upscaleTransition.setNode(games.get(getIndex(3)).group);
		upscaleTransition.setDuration(Duration.millis(1150));
		upscaleTransition.play();
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
		if (e.getCode() == KeyCode.UP && pathFinished >= elementCount) {
			elementIndex = add(elementIndex, -1, games.size());
			pathFinished = 0;

			games.get(getIndex(3)).scale = 1.8;
			games.get(getIndex(4)).scale = 1.0;

			
			games.get(getIndex(0)).setPath(0, 0, climbingDuration, new Point(-imageSize / 2, (int) (d.height + imageSize / 2 - radius)));
			for (int i = 1; i < elementCount; i++) {
				games.get(getIndex(i)).setPath(i % elementCount, 0, climbingDuration);
			}

			fadeinTransition.setNode(games.get(getIndex(3)).text);
			fadeoutTransition.setNode(games.get(getIndex(4)).text);
			fadeinTransition.setDuration(Duration.millis(climbingDuration));
			fadeoutTransition.setDuration(Duration.millis(climbingDuration));
			fadeinTransition.play();
			fadeoutTransition.play();
			upscaleTransition.setNode(games.get(getIndex(3)).group);
			downscaleTransition.setNode(games.get(getIndex(4)).group);
			upscaleTransition.setDuration(Duration.millis(climbingDuration));
			downscaleTransition.setDuration(Duration.millis(climbingDuration));
			upscaleTransition.play();
			downscaleTransition.play();

			if (climbingDuration > minDelay)
				climbingDuration *= climbSpeed;
		}
		if (e.getCode() == KeyCode.DOWN && pathFinished >= elementCount) {
			elementIndex = add(elementIndex, 1, games.size());
			pathFinished = 0;

			games.get(getIndex(3)).scale = 1.8;
			games.get(getIndex(2)).scale = 1.0;

			games.get(getIndex(elementCount - 1)).setPath(elementCount - 1, 0, climbingDuration, new Point(radius, (int) (d.height + imageSize / 2)));
			for (int i = 0; i < elementCount - 1; i++) {
				games.get(getIndex(i)).setPath(i, 0, climbingDuration);
			}

			fadeinTransition.setNode(games.get(getIndex(3)).text);
			fadeoutTransition.setNode(games.get(getIndex(2)).text);
			fadeinTransition.setDuration(Duration.millis(climbingDuration));
			fadeoutTransition.setDuration(Duration.millis(climbingDuration));
			fadeinTransition.play();
			fadeoutTransition.play();
			upscaleTransition.setNode(games.get(getIndex(3)).group);
			downscaleTransition.setNode(games.get(getIndex(2)).group);
			upscaleTransition.setDuration(Duration.millis(climbingDuration));
			downscaleTransition.setDuration(Duration.millis(climbingDuration));
			upscaleTransition.play();
			downscaleTransition.play();

			if (climbingDuration > minDelay)
				climbingDuration *= climbSpeed;

		}
	}

	private int getIndex(int iterator) {
		return ((elementIndex + (iterator + games.size() - 3) % games.size()) % games.size());
	}

	@Override
	public void stop() {
	}

	private class Element {
		private Group group;
		private Label text;
		private ImageView image;

		private String picture;
		private String file;
		private String name;

		private double scale = 1.0;

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
			text = new Label(name);
			text.setFont(new Font("Arial", 20));
			text.relocate(110, 10);
			text.setOpacity(0.0);
			this.picture = picture;
			//System.out.println(gamesFolder.toURI().getPath() + picture);
			try {
				image = new ImageView("file:" + gamesFolder.toURI().getPath() + picture);
				image.setFitHeight(imageSize);
				image.setFitWidth(imageSize);
			} catch (Exception e) {
			}
			image.setAccessibleText(name);
		}

		private Point getLocal() {
			return new Point((int) (group.getTranslateX() + (group.getBoundsInLocal().getMaxX() - group.getBoundsInLocal().getMinX()) / 2), (int) (group.getTranslateY() + (group.getBoundsInLocal().getMaxY() - group.getBoundsInLocal().getMinY()) / 2));
		}

		private Point getPos() {
			return new Point((int) (group.getLayoutX() + getLocal().x), (int) (group.getLayoutY() + getLocal().y));
		}

		private int getWidth() {
			return (int) (group.getBoundsInLocal().getMaxX() - group.getBoundsInLocal().getMinX());
		}

		private Point getDelta(int pos) {
			Point middle = new Point(getPos().x - radius, getPos().y);
			Point stop = new Point(middle.x + (int) (radius * Math.cos(Math.toRadians(270) + Math.toRadians(angles[pos]))), middle.y + (int) (radius * Math.sin(Math.toRadians(270) + Math.toRadians(angles[pos]))));
			return new Point(stop.x + (int) ((getWidth() * scale - getWidth()) / 2) + (int) ((getWidth() - imageSize / 2) / 2) - getPos().x, stop.y - getPos().y);
		}

		private void setPath(int pos, int delay, int duration) {
			setPath(pos, delay, duration, getPos());
		}

		private void setPath(int pos, int delay, int duration, Point start) {
			PathTransition pathTransition = new PathTransition();

			Path path = new Path();
			Point delta = getDelta(pos);
			path.getElements().add(new MoveTo(start.x - getPos().x + getLocal().x, start.y - getPos().y + getLocal().y));

			ArcTo arcTo = new ArcTo(radius, radius,
					0, delta.x, delta.y, false, false);

			path.getElements().add(arcTo);

			pathTransition.setDelay(Duration.millis(delay));
			pathTransition.setDuration(Duration.millis(duration));
			pathTransition.setNode(group);
			pathTransition.setPath(path);

			pathTransition.setOnFinished(event -> {
				pathFinished++;
			});

			pathTransition.play();
		}
	}
}
