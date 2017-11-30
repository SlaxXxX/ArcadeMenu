package de.slx.arcademenu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class CreateLnks {

	File mainFolder, dataFolder, gamesFolder;
	int max = 255;
	String[] endings = { "st", "nd", "rd" };

	public static void main(String[] args) {
		new CreateLnks();
	}

	private String endOrDefault(int num) {
		return (num < 4) ? endings[num - 1] : "th";
	}

	public CreateLnks() {
		try {
			mainFolder = new File(new File(getClass().getProtectionDomain().getCodeSource()
					.getLocation().toURI()).getParentFile().toURI());
			dataFolder = new File(mainFolder.toURI().getPath(), "data");
			gamesFolder = new File(dataFolder.toURI().getPath(), "games");
		} catch (Exception e) {
			e.printStackTrace();
		}

		gamesFolder.mkdirs();

		//<!>
		Arrays.asList(gamesFolder.listFiles()).forEach(File::delete);
		//<!>

		long before = System.currentTimeMillis();
		for (int i = 1; i <= max; i++) {
			String name = String.format("%03d", i) + endOrDefault(i) + " shade of grey";
			try {
				new File(gamesFolder.toURI().getPath(), name + ".lnk").createNewFile();

				BufferedImage b_img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = b_img.createGraphics();

				graphics.setPaint(new Color(255 - i * (255 / max), 255 - i * (255 / max), 255 - i * (255 / max)));
				graphics.fillRect(0, 0, b_img.getWidth(), b_img.getHeight());

				ImageIO.write(b_img, "png", new File(gamesFolder.toURI().getPath(), name + ".png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.printf("Done in " + "%d.2" + " seconds.", (System.currentTimeMillis() - before)/1000);
	}
}
