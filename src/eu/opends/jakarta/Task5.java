/*<<<<<<< HEAD:OpenDS4.0/src/eu/opends/jakarta/Task5.java
package eu.opends.jakarta;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import eu.opends.camera.CameraFactory;
import eu.opends.main.Simulator;

public class Task5 {
	private static boolean within20cm = false;
	private static boolean moreThan20cm = false;
	private static boolean destination = false;
	private static boolean collisionWithCones = false;
	private static boolean idealLine = false;
	private static int score = 0;
	
	private static int imgWidth = 1920;
	private static int imgHeight = 1080;

	private static Simulator sim;
	
	public static boolean isWithin20cm() {
		return within20cm;
	}

	public Task5(Simulator sim){
		Task5.sim = sim;
	}
	
	
	public static void setWithin20cm(boolean within20cm) {
		Task5.within20cm = within20cm;
	}

	public static boolean isMoreThan20cm() {
		return moreThan20cm;
	}

	public static void setMoreThan20cm(boolean moreThan20cm) {
		Task5.moreThan20cm = moreThan20cm;
	}

	public static boolean isDestination() {
		return destination;
	}

	public static void setDestination(boolean destination) {
		Task5.destination = destination;
	}

	public static boolean isCollisionWithCones() {
		return collisionWithCones;
	}

	public static void setCollisionWithCones(boolean collisionWithCones) {
		Task5.collisionWithCones = collisionWithCones;
	}

	public static boolean isIdealLine() {
		IdealLineCalculator getIdealLineObject = new IdealLineCalculator(sim);
		float idealLineCoeff = getIdealLineObject.getAreaBetweenCurrentIdeal();
		if (idealLineCoeff > 10.0f) {
			setIdealLine(false);
		}
		else {
			setIdealLine(true);
		}
		return idealLine;
	}

	public static void setIdealLine(boolean idealLine) {
		Task5.idealLine = idealLine;
	}

	public static void setScore(int score) {
		Task5.score = score;
	}

	public static int getScore() {
		return score;
	}

	private static int getFinalScore() {
		if (!isIdealLine())
			setScore(0);
		else {
			setScore(getScore()+2);
			if (isDestination())
				setScore(getScore() + 2);
			if (isWithin20cm())
				setScore(getScore() + 6);
			if (!isWithin20cm())
				setScore(getScore() + 2);
			if (isCollisionWithCones())
				setScore(0);
		}
		System.out.println("IdealLine: " + idealLine);
		System.out.println("within20cm: " + within20cm);
		System.out.println("moreThan20cm: " + moreThan20cm);
		System.out.println("destination: " + destination);
		System.out.println("collisionWithCones: " + collisionWithCones);
		System.out.println("Jakarta Task 5 score: " + score);
		return getScore();
	}

	// Draw and save trail of the driving car
	public void drawTrail(List<String> coordinates, List<String> obstacleCoordinates) {
		BufferedImage combined = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
		float opacity = 1f;
		Graphics2D graphics = combined.createGraphics();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		if (CameraFactory.lastScreenshotPath.length() > 0) {
			try {
				BufferedImage lastScreenshot = ImageIO.read(new File(CameraFactory.lastScreenshotPath));
				graphics.drawImage(lastScreenshot, 0, 0, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ArrayList<Float> xCoords = new ArrayList<>();
		ArrayList<Float> yCoords = new ArrayList<>();
		for (String coordinate : coordinates) {
			String[] parts = coordinate.split(";");
			String xStr = parts[0];
			String yStr = parts[1];
			float x = ((imgWidth / 2) - ((-65) * (Float.parseFloat(xStr) - CameraFactory.bgHighCamXMid)));
			float y = ((imgHeight / 2) - ((-65) * (Float.parseFloat(yStr) - CameraFactory.bgHighCamYMid)));
			xCoords.add(x);
			yCoords.add(y);
		}

		graphics.setColor(Color.RED);

		for (String obstacleCoord : obstacleCoordinates) {
			String[] parts = obstacleCoord.split(";");
			Float spX = ((imgWidth / 2) - ((-65) * (Float.valueOf(parts[0]) - CameraFactory.bgHighCamXMid)));
			Float spY = ((imgHeight / 2) - ((-65) * (Float.valueOf(parts[1]) - CameraFactory.bgHighCamYMid)));
			graphics.drawRect(spX.intValue(), spY.intValue(), 2, 2);
			graphics.fillRect(spX.intValue(), spY.intValue(), 2, 2);
		}

		graphics.setColor(Color.BLUE);
		Path2D polyline = new Path2D.Float();

		// Start from the second coordinate as the first one is always (0,0)
		// before car got placed on the map
		if (xCoords.size() > 1) {
			graphics.drawString("START", xCoords.get(1), yCoords.get(1));
			graphics.drawString("END", xCoords.get(xCoords.size() - 1), yCoords.get(yCoords.size() - 1));
			polyline.moveTo(xCoords.get(1), yCoords.get(1));

			for (int i = 2; i < xCoords.size(); i++) {
				graphics.drawRect(xCoords.get(i).intValue(), yCoords.get(i).intValue(), 2, 2);
				graphics.fillRect(xCoords.get(i).intValue(), yCoords.get(i).intValue(), 2, 2);
				polyline.lineTo(xCoords.get(i), yCoords.get(i));
			}
			graphics.draw(polyline);

			// Draw the turning score in the upper left corner
			graphics.setColor(Color.BLUE);
			int finalScore = getFinalScore();
			graphics.drawString("Zig-zag score = " + String.valueOf(finalScore), 20, 20);

			try {
				String screenshotPath = Simulator.screenshotPath;
				String filename = screenshotPath + "Task5_Trail" + new Date().getTime() + ".png";
				if (ImageIO.write(combined, "png", new File(filename))) {
					System.out.println("Task 5 trail img saved");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
=======*/
package eu.opends.jakarta;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import eu.opends.camera.CameraFactory;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;

public class Task5 {
	private static boolean within20cm = false;
	private static boolean moreThan20cm = false;
	private static boolean destination = false;
	private static boolean collisionWithCones = false;
	private static boolean idealLine = false;
	private static int score = 0;

	private static int imgWidth = 1920;
	private static int imgHeight = 1080;

	private static Simulator sim;

	private static JakartaTask5Report report;

	public static boolean isWithin20cm() {
		return within20cm;
	}

	public Task5(Simulator sim) {
		Task5.sim = sim;
	}

	public static void setWithin20cm(boolean within20cm) {
		Task5.within20cm = within20cm;
	}

	public static boolean isMoreThan20cm() {
		return moreThan20cm;
	}

	public static void setMoreThan20cm(boolean moreThan20cm) {
		Task5.moreThan20cm = moreThan20cm;
	}

	public static boolean isDestination() {
		return destination;
	}

	public static void setDestination(boolean destination) {
		Task5.destination = destination;
	}

	public static boolean isCollisionWithCones() {
		return collisionWithCones;
	}

	public static void setCollisionWithCones(boolean collisionWithCones) {
		Task5.collisionWithCones = collisionWithCones;
	}

	public static boolean isIdealLine() {
		IdealLineCalculator getIdealLineObject = new IdealLineCalculator(sim);
		float idealLineCoeff = getIdealLineObject.getAreaBetweenCurrentIdeal();
		if (idealLineCoeff > 10.0f) {
			setIdealLine(false);
		} else {
			setIdealLine(true);
		}
		return idealLine;
	}

	public static void setIdealLine(boolean idealLine) {
		Task5.idealLine = idealLine;
	}

	public static void setScore(int score) {
		Task5.score = score;
	}

	public static int getScore() {
		return score;
	}

	private static int getFinalScore() {
		if (!isIdealLine())
			setScore(0);
		else {
			setScore(getScore() + 2);
			if (isDestination())
				setScore(getScore() + 2);
			if (isWithin20cm())
				setScore(getScore() + 6);
			if (!isWithin20cm())
				setScore(getScore() + 2);
			if (isCollisionWithCones())
				setScore(0);
		}
		System.out.println("IdealLine: " + idealLine);
		System.out.println("within20cm: " + within20cm);
		System.out.println("moreThan20cm: " + moreThan20cm);
		System.out.println("destination: " + destination);
		System.out.println("collisionWithCones: " + collisionWithCones);
		System.out.println("Jakarta Task 5 score: " + score);

		// create and print report
		String driverName = sim.getDrivingTask().getSettingsLoader().getSetting(Setting.General_driverName, "Default Driver");
		report = new JakartaTask5Report();
		report.generateReport(driverName, within20cm, moreThan20cm, destination, collisionWithCones, idealLine,
				getScore());

		return getScore();
	}

	// Draw and save trail of the driving car
	public void drawTrail(List<String> coordinates, List<String> obstacleCoordinates) {
		BufferedImage lastScreenshot = null;

		if (CameraFactory.lastScreenshotPath.length() > 0) {
			try {
				lastScreenshot = ImageIO.read(new File(CameraFactory.lastScreenshotPath));
				imgWidth = lastScreenshot.getWidth();
				imgHeight = lastScreenshot.getHeight();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BufferedImage combined = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
		float opacity = 1f;
		Graphics2D graphics = combined.createGraphics();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

		if (lastScreenshot != null)
			graphics.drawImage(lastScreenshot, 0, 0, null);

		ArrayList<Float> xCoords = new ArrayList<>();
		ArrayList<Float> yCoords = new ArrayList<>();
		for (String coordinate : coordinates) {
			String[] parts = coordinate.split(";");
			String xStr = parts[0];
			String yStr = parts[1];
			float x = ((imgWidth / 2) - ((-65) * (Float.parseFloat(xStr) - CameraFactory.bgHighCamXMid)));
			float y = ((imgHeight / 2) - ((-65) * (Float.parseFloat(yStr) - CameraFactory.bgHighCamYMid)));
			xCoords.add(x);
			yCoords.add(y);
		}

		graphics.setColor(Color.BLUE);
		Path2D polyline = new Path2D.Float();

		// Start from the second coordinate as the first one is always (0,0)
		// before car got placed on the map
		if (xCoords.size() > 1) {
			graphics.drawString("START", xCoords.get(1), yCoords.get(1));
			graphics.drawString("END", xCoords.get(xCoords.size() - 1), yCoords.get(yCoords.size() - 1));
			polyline.moveTo(xCoords.get(1), yCoords.get(1));

			for (int i = 2; i < xCoords.size(); i++) {
				graphics.drawRect(xCoords.get(i).intValue(), yCoords.get(i).intValue(), 2, 2);
				graphics.fillRect(xCoords.get(i).intValue(), yCoords.get(i).intValue(), 2, 2);
				polyline.lineTo(xCoords.get(i), yCoords.get(i));
			}

			graphics.draw(polyline);

			// Draw the turning score in the upper left corner
			graphics.setColor(Color.BLUE);
			int finalScore = getFinalScore();
			graphics.drawString("Zig-zag score = " + String.valueOf(finalScore), 20, 20);

			try {
				String screenshotPath = Simulator.screenshotPath;
				String filename = screenshotPath + "Task5_Trail" + new Date().getTime() + ".png";
				if (ImageIO.write(combined, "png", new File(filename))) {
					System.out.println("Task 5 trail img saved");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
// >>>>>>>
// c34598c3007ef5223d4bc5b3a744dff97371811d:jakarta_on/eu/opends/jakarta/Task5.java
