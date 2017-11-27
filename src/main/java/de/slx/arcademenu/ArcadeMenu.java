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
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

@SuppressWarnings("restriction")
public class ArcadeMenu extends Application {

	File mainFolder, dataFolder, gamesFolder;
	SortedMap<String, Assets> games = new TreeMap<>();
	int menuIndex = 0;
	GraphicsContext gc;

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
		 player.play();
	}

	private String strip(String s) {
		return s.split("\\.")[0];
	}

	@Override
	public void start(Stage stage) {
		Canvas canvas = new Canvas(1024, 768);
		gc = canvas.getGraphicsContext2D();
		drawImage(0);

		Group root = new Group();
		VBox vBox = new VBox();
		vBox.getChildren().addAll(canvas);
		root.getChildren().add(vBox);
		Scene scene = new Scene(root, 1024, 768);
		scene.setCursor(Cursor.NONE);

		scene.addEventFilter(KeyEvent.KEY_PRESSED, this::pressedKey);

		stage.setScene(scene);
		// stage.setFullScreen(true);
		stage.setFullScreenExitHint("");
		// stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		// stage.setOnCloseRequest(Event::consume);
		stage.show();
	}

	private void drawImage(int direction) {
		long time = System.currentTimeMillis();
		for (int l=0;l<Math.abs(direction);l++) {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		ArrayList<String> keys = new ArrayList<>(games.keySet());
		int pointer = menuIndex;
		int xPos = 120;
		int size = 200;
		gc.setFont(Font.font("Arial", 30));
		for (int i = 0; i < 5; i++) {
			if (pointer < 0)
				pointer += keys.size();
			if (pointer > keys.size() - 1)
				pointer -= keys.size();
			gc.drawImage(games.get(keys.get(pointer)).image, 50, xPos, size, size);
			gc.fillText(keys.get(pointer), size + 60, xPos + 10 + gc.getFont().getSize());
			gc.setFont(Font.font("Arial", 20));
			xPos += size + 10;
			size = 100;
			pointer++;
		}
		gc.drawImage(games.get(keys.get(menuIndex > 0 ? menuIndex - 1 : menuIndex + keys.size() - 1)).image, 50, 10, 100, 100);
		gc.fillText(keys.get(menuIndex > 0 ? menuIndex - 1 : menuIndex + keys.size() - 1), size + 60, 20 + gc.getFont().getSize());
		}
		System.out.println(System.currentTimeMillis() - time);
	}

	private void pressedKey(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			ArrayList<String> keys = new ArrayList<>(games.keySet());
			System.out.println(gamesFolder + "\\\"" + keys.get(menuIndex) + "\"");
			try {
				Runtime.getRuntime().exec("cmd /c start \"\" \"" + gamesFolder + "\\" + games.get(keys.get(menuIndex)).file + "\"");
			} catch (Exception ex) {
			}
		}
		if (e.getCode() == KeyCode.UP) {
			menuIndex--;
			if (menuIndex < 0)
				menuIndex += games.size();
			drawImage(-1);
		}
		if (e.getCode() == KeyCode.DOWN) {
			menuIndex++;
			if (menuIndex > games.size() - 1)
				menuIndex -= games.size();
			drawImage(1);
		}
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
			} catch (Exception e) {}
		}
	}
}
