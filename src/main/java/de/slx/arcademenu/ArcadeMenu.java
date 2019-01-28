package de.slx.arcademenu;

import javafx.application.Application;
import javafx.beans.value.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javafx.embed.swing.*;
import javafx.scene.media.*;
import javafx.event.*;
import javafx.scene.input.*;

@SuppressWarnings("restriction")
public class ArcadeMenu extends Application {

	File mainFolder, dataFolder, gamesFolder;
	ArrayList<Element> games = new ArrayList<>();
	Pane layout;

	public static boolean debug = true;

	int[] angles = new int[] { -8, 7, 19, 33, 55, 67, 79, 91 };
	int pathFinished = 0;
	int elementCount = 8;
	int elementIndex = 0;

	int duration = 150;
	int climbingDuration = duration;
	double climbSpeed = 0.97;
	int minDelay = 30;
	double scaleMain = 1.8;
	int imageSize = 90;
	int frameWidth = 10;

	final Dimension d = new Dimension(1024, 768);
	final int radius = (int) (d.width / 4 * 2.5);

	Media loop, start;
	MediaPlayer player;

	public static void main(String[] args) {
		if (args.length > 0 && args[args.length - 1] == "debug") {
			args = Arrays.copyOfRange(args, 0, args.length - 1);
			debug = true;
		}
		launch(args);
	}

	@Override
	public void init() {
		try {
			mainFolder = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParentFile().toURI());
			dataFolder = new File(mainFolder.toURI().getPath(), "data");
			gamesFolder = new File(dataFolder.toURI().getPath(), "games");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

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

		start = new Media(dataFolder.toURI().toString() + "/start.mp3");
		loop = new Media(dataFolder.toURI().toString() + "/loop.mp3");
		player = new MediaPlayer(start);

		player.setOnEndOfMedia(() -> {
			player = new MediaPlayer(loop);
			player.play();
		});
		player.play();

	}

	private String strip(String s) {
		return s.split("\\.")[0];
	}

	@Override
	public void start(Stage stage) {
		layout = new Pane();
		layout.getChildren().add(new ImageView("file:" + dataFolder.toURI().getPath() + "background.png"));

		ArrayList<Element> initialList = new ArrayList<>(games);
		while (games.size() < elementCount)
			for (Element game : initialList)
				games.add(new Element(game));

		for (Element game : games) {
			game.group = new Group(game.image, game.text);
			game.setup();
			layout.getChildren().add(game.group);
		}

		Scene scene = new Scene(layout, d.width, d.height);

		scene.addEventFilter(KeyEvent.KEY_PRESSED, this::pressedKey);
		scene.setOnKeyReleased(ke -> {
			climbingDuration = duration;
		});

		stage.setScene(scene);
		scene.setCursor(Cursor.NONE);
		if (!debug) {
			stage.setFullScreen(true);
			stage.setFullScreenExitHint("");
			stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
			stage.setOnCloseRequest(Event::consume);
		}
		stage.focusedProperty().addListener((ov, t, t1) -> {
			if (t1)
				player.play();
			else
				player.pause();
		});

		stage.show();

		games.get(getIndex(3)).scale = 1.8;
		for (int i = 0; i < games.size(); i++) {
			games.get((i + games.size() - 3) % games.size()).group.relocate(radius, d.height + imageSize / 2);
			if (i < elementCount)
				games.get((i + games.size() - 3) % games.size()).setPath(i, i * 150, 1000);
		}
		games.get(getIndex(3)).up(1150);
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

			games.get(getIndex(0)).setPath(0, 0, climbingDuration,
					new Point(-imageSize / 2, (int) (d.height + imageSize / 2 - radius)));
			for (int i = 1; i < elementCount; i++) {
				games.get(getIndex(i)).setPath(i % elementCount, 0, climbingDuration);
			}

			games.get(getIndex(3)).up(climbingDuration);
			games.get(getIndex(4)).down(climbingDuration);

			if (climbingDuration > minDelay)
				climbingDuration *= climbSpeed;
		}
		if (e.getCode() == KeyCode.DOWN && pathFinished >= elementCount) {
			elementIndex = add(elementIndex, 1, games.size());
			pathFinished = 0;

			games.get(getIndex(3)).scale = 1.8;
			games.get(getIndex(2)).scale = 1.0;

			games.get(getIndex(elementCount - 1)).setPath(elementCount - 1, 0, climbingDuration,
					new Point(radius, (int) (d.height + imageSize / 2)));
			for (int i = 0; i < elementCount - 1; i++) {
				games.get(getIndex(i)).setPath(i, 0, climbingDuration);
			}

			games.get(getIndex(3)).up(climbingDuration);
			games.get(getIndex(2)).down(climbingDuration);

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

	// -------------------------------------------------------------------------------------//
	// -------------------------------------------------------------------------------------//
	// -------------------------------------------------------------------------------------//

	private class Element {

		ScaleTransition upscaleTransition = new ScaleTransition();
		ScaleTransition downscaleTransition = new ScaleTransition();
		FadeTransition fadeinTransition = new FadeTransition();
		FadeTransition fadeoutTransition = new FadeTransition();

		private Group group;
		private Label text;
		private ImageView image = new ImageView();

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
			text.setFont(new Font("Calibri", 20));
			text.relocate(110, 10);
			text.setOpacity(0.0);
			this.picture = picture;
			try {
				BufferedImage frame = ImageIO.read(new File(dataFolder, "frame.png"));
				BufferedImage icon = ImageIO.read(new File(gamesFolder, picture));
				Graphics2D g = frame.createGraphics();
				g.drawImage(icon, frameWidth, frameWidth, null);
				image.setImage(SwingFXUtils.toFXImage(frame, null));
				image.setFitHeight(imageSize);
				image.setFitWidth(imageSize);
			} catch (Exception e) {
				e.printStackTrace();
			}
			image.setAccessibleText(name);
		}

		private void setup() {
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
			fadeinTransition.setNode(text);
			fadeoutTransition.setNode(text);
			upscaleTransition.setNode(group);
			downscaleTransition.setNode(group);
		}

		private void up(int duration) {
			upscaleTransition.setDuration(Duration.millis(duration));
			upscaleTransition.play();
			fadeinTransition.setDuration(Duration.millis(duration));
			fadeinTransition.play();
		}

		private void down(int duration) {
			downscaleTransition.setDuration(Duration.millis(duration));
			downscaleTransition.play();
			fadeoutTransition.setDuration(Duration.millis(duration));
			fadeoutTransition.play();
		}

		private Point getLocal() {
			return new Point(
					(int) (group.getTranslateX()
							+ (group.getBoundsInLocal().getMaxX() - group.getBoundsInLocal().getMinX()) / 2),
					(int) (group.getTranslateY()
							+ (group.getBoundsInLocal().getMaxY() - group.getBoundsInLocal().getMinY()) / 2));
		}

		private Point getPos() {
			return new Point((int) (group.getLayoutX() + getLocal().x), (int) (group.getLayoutY() + getLocal().y));
		}

		private int getWidth() {
			return (int) (group.getBoundsInLocal().getMaxX() - group.getBoundsInLocal().getMinX());
		}

		private Point getDelta(int pos) {
			Point middle = new Point(getPos().x - radius, getPos().y);
			Point stop = new Point(
					middle.x + (int) (radius * Math.cos(Math.toRadians(270) + Math.toRadians(angles[pos]))),
					middle.y + (int) (radius * Math.sin(Math.toRadians(270) + Math.toRadians(angles[pos]))));
			return new Point(stop.x + (int) ((getWidth() * scale - getWidth()) / 2)
					+ (int) ((getWidth() - imageSize / 2) / 2) - getPos().x, stop.y - getPos().y);
		}

		private void setPath(int pos, int delay, int duration) {
			setPath(pos, delay, duration, getPos());
		}

		private void setPath(int pos, int delay, int duration, Point start) {
			PathTransition pathTransition = new PathTransition();

			Path path = new Path();
			Point delta = getDelta(pos);
			path.getElements()
					.add(new MoveTo(start.x - getPos().x + getLocal().x, start.y - getPos().y + getLocal().y));

			ArcTo arcTo = new ArcTo(radius, radius, 0, delta.x, delta.y, false, false);

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
