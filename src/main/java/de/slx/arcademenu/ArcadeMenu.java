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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javafx.embed.swing.*;
import javafx.scene.media.*;
import javafx.event.*;
import javafx.scene.input.*;

@SuppressWarnings("restriction")
public class ArcadeMenu extends Application {

	File mainFolder, dataFolder, gamesFolder;
	List<Element> games = new LinkedList<>();
	Pane layout;

	public static boolean debug = true;

	final int elementAngle = 19;
	final int selectedExtraSpace = 9;
	final float selRad = 1.1f;
	final int selIndex = 3;
	final int angleOffset = -18;

	int pathFinished = 0;
	final int elementCount = 8;

	final int duration = 150;
	int climbingDuration = duration;
	final double climbSpeed = 0.97;
	final int minDelay = 30;
	final double selectedScale = 1.8;
	final int imageSize = 150;
	final int frameWidth = 10;

	Dimension screenDim = new Dimension(1024, 768);
	Point rotCenter;
	final float radius = 0.65f;

	Media loop, start;
	MediaPlayer player;

	private void updateView(Stage stage, int duration) {
		screenDim = new Dimension((int) stage.getWidth(), (int) stage.getHeight());
		rotCenter = new Point((int) (screenDim.width * 0.1), (int) (screenDim.height * 0.8));

		for (int i = elementCount; i < games.size(); i++) {
			games.get(i).group.setTranslateX(radius * screenDim.height);
			games.get(i).group.setTranslateY(screenDim.height * 1.2);
		}

		for (int i = 0; i < games.size(); i++) {
			games.get(i).group.setTranslateX(radius * screenDim.height);
			games.get(i).group.setTranslateY(screenDim.height * 1.2);
			games.get(i).group.setVisible(false);
			if (i < elementCount)
				games.get(i).setPath(i, 0, duration);
		}

		games.get(selIndex).up(1);
	}

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
			layout.getChildren().add(game.group);
			game.setup();
		}

		for (int i = 0; i < selIndex; i++) {
			rotateList(1);
		}

		Scene scene = new Scene(layout, 1024, 768);

		scene.addEventFilter(KeyEvent.KEY_PRESSED, this::pressedKey);
		scene.setOnKeyReleased(keyEvent -> {
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

		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> updateView(stage, 1);

		stage.widthProperty().addListener(stageSizeListener);
		stage.heightProperty().addListener(stageSizeListener);

		stage.show();
		updateView(stage, 1);
	}

	private void pressedKey(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			try {
				Runtime.getRuntime().exec("cmd /c start \"\" \"" + gamesFolder + "\\" + games.get(0).file + "\"");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if ((e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.UP) && pathFinished >= elementCount) {
			int delta = e.getCode() == KeyCode.DOWN ? 1 : -1;
			rotateList(delta);
			pathFinished = 0;

			games.get(0).group.setVisible(false);
			games.get(elementCount - 1).group.setVisible(false);
			for (int i = 0; i < elementCount; i++) {
				games.get(i).setPath(i, 0, climbingDuration);
			}

			games.get(3).up(climbingDuration);
			games.get(3 + delta).down(climbingDuration);

			if (climbingDuration > minDelay)
				climbingDuration *= climbSpeed;
		}
	}

	private void rotateList(int delta) {
		if (delta > 0) {
			games.add(0, games.get(games.size() - 1));
			games.remove(games.size() - 1);
		} else {
			games.add(games.get(0));
			games.remove(0);
		}
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
		private double width;

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
			text.setFont(new Font("Rockwell Extra Bold", 20));
			text.relocate(imageSize + 10, imageSize / 10);
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
			width = group.getBoundsInParent().getWidth();

			fadeinTransition.setFromValue(0.0);
			fadeinTransition.setToValue(1.0);
			fadeoutTransition.setFromValue(1.0);
			fadeoutTransition.setToValue(0.0);
			upscaleTransition.setFromX(1);
			upscaleTransition.setFromY(1);
			upscaleTransition.setToX(selectedScale);
			upscaleTransition.setToY(selectedScale);
			downscaleTransition.setFromX(selectedScale);
			downscaleTransition.setFromY(selectedScale);
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

		private Point getNextPos(int pos) {
			int newAngle = pos * elementAngle + angleOffset
					+ ((pos >= selIndex) ? selectedExtraSpace * ((pos > selIndex) ? 2 : 1) : 0);
			Point stop = new Point(
					rotCenter.x - (int) (width + (radius * screenDim.height * (pos == selIndex ? selRad : 1))
							* Math.sin(Math.toRadians(360) - Math.toRadians(newAngle))),
					rotCenter.y - (int) ((radius * screenDim.height * (pos == selIndex ? selRad : 1))
							* Math.cos(Math.toRadians(360) - Math.toRadians(newAngle))));
			return stop;
		}

		private void setPath(int pos, int delay, int duration) {
			PathTransition pathTransition = new PathTransition();
			Path path = new Path();
			Point center = new Point(
					(int) (group.getBoundsInParent().getMinX() + group.getBoundsInParent().getWidth() / 2),
					(int) (group.getBoundsInParent().getMinY() + group.getBoundsInParent().getHeight() / 2));

			group.setVisible(true);
			path.getElements().add(new MoveTo(center.x, center.y));

			Point next = getNextPos(pos);
			ArcTo arcTo = new ArcTo((radius * screenDim.height), (radius * screenDim.height), 0, next.x, next.y, false,
					false);

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
