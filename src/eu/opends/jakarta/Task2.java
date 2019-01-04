/*
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

public class Task2 {
	private static boolean front30to35cm = false;
	private static boolean front35to50cm = false;
	private static boolean frontLessThan30cm = false;
	private static boolean side2to5cm = false;
	private static boolean sideLessThan2cm = false;
	private static boolean idealLine = false;
	private static int score = 2;

	private static int imgWidth = 1920;
	private static int imgHeight = 1080;

	private static Simulator sim;
	
	public static boolean isSideLessThan2cm() {
		return sideLessThan2cm;
	}

	public static boolean isFrontLessThan30cm() {
		return frontLessThan30cm;
	}

	public static void setFrontLessThan30cm(boolean frontLessThan30cm) {
		Task2.frontLessThan30cm = frontLessThan30cm;
	}

	public static void setSideLessThan2cm(boolean sideLessThan2cm) {
		Task2.sideLessThan2cm = sideLessThan2cm;
	}

	public static boolean isFront30to35cm() {
		return front30to35cm;
	}

	public static void setFront30to35cm(boolean front30to35cm) {
		Task2.front30to35cm = front30to35cm;
	}

	public static boolean isFront35to50cm() {
		return front35to50cm;
	}

	public static void setFront35to50cm(boolean front35to50cm) {
		Task2.front35to50cm = front35to50cm;
	}

	public static boolean isSide2to5cm() {
		return side2to5cm;
	}

	public static void setSide2to5cm(boolean side2to5cm) {
		Task2.side2to5cm = side2to5cm;
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
		Task2.idealLine = idealLine;
	}

	public static int getScore() {
		return score;
	}

	public static void setScore(int score) {
		Task2.score = score;
	}
	
	public Task2(Simulator sim){
		Task2.sim = sim;
	}
	
	
	
	
	private static int getFinalScore() {
		if (isIdealLine())
			setScore(getScore()+2);
		if (isFront30to35cm() && !isFrontLessThan30cm() && !isFront35to50cm())
			setScore(getScore() + 4);
		if (isFront35to50cm() && !isFront30to35cm() && !isFrontLessThan30cm())
			setScore(getScore() + 2);
		if (isSide2to5cm() && !isSideLessThan2cm())
			setScore(getScore() + 2);
		// Uncomment when ideal line is ready
		// if (!idealLine)
		// setScore(0);
		System.out.println("IdealLine: " + idealLine);
		System.out.println("isFront30to35cm: " + isFront30to35cm());
		System.out.println("isFront35to50cm: " + isFront35to50cm());
		System.out.println("isSide2to5cm: " + isSide2to5cm());
		System.out.println("isSideLessThan2cm: " + isSideLessThan2cm());
		System.out.println("isFrontLessThan30cm: " + isFrontLessThan30cm());
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
			graphics.drawString("Crank test score = " + String.valueOf(finalScore), 20, 20);

			try {
				String screenshotPath = Simulator.screenshotPath;
				String filename = screenshotPath + "Task2_Trail" + new Date().getTime() + ".png";
				if (ImageIO.write(combined, "png", new File(filename))) {
					System.out.println("Task 2 trail img saved");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

public class Task2 {
	private static boolean front30to35cm = false;
	private static boolean front35to50cm = false;
	private static boolean frontLessThan30cm = false;
	private static boolean side2to5cm = false;
	private static boolean sideLessThan2cm = false;
	private static boolean idealLine = false;
	private static int score = 2;

	private static int imgWidth = 1920;
	private static int imgHeight = 1080;

	private static Simulator sim;
	
	private static JakartaTask2Report report;
	
	public static boolean isSideLessThan2cm() {
		return sideLessThan2cm;
	}

	public static boolean isFrontLessThan30cm() {
		return frontLessThan30cm;
	}

	public static void setFrontLessThan30cm(boolean frontLessThan30cm) {
		Task2.frontLessThan30cm = frontLessThan30cm;
	}

	public static void setSideLessThan2cm(boolean sideLessThan2cm) {
		Task2.sideLessThan2cm = sideLessThan2cm;
	}

	public static boolean isFront30to35cm() {
		return front30to35cm;
	}

	public static void setFront30to35cm(boolean front30to35cm) {
		Task2.front30to35cm = front30to35cm;
	}

	public static boolean isFront35to50cm() {
		return front35to50cm;
	}

	public static void setFront35to50cm(boolean front35to50cm) {
		Task2.front35to50cm = front35to50cm;
	}

	public static boolean isSide2to5cm() {
		return side2to5cm;
	}

	public static void setSide2to5cm(boolean side2to5cm) {
		Task2.side2to5cm = side2to5cm;
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
		Task2.idealLine = idealLine;
	}

	public static int getScore() {
		return score;
	}

	public static void setScore(int score) {
		Task2.score = score;
	}

	public Task2(Simulator sim) {
		Task2.sim = sim;
	}

	private static int getFinalScore() {
		if (isIdealLine())
			setScore(getScore() + 2);
		if (isFront30to35cm() && !isFrontLessThan30cm() && !isFront35to50cm())
			setScore(getScore() + 4);
		if (isFront35to50cm() && !isFront30to35cm() && !isFrontLessThan30cm())
			setScore(getScore() + 2);
		if (isSide2to5cm() && !isSideLessThan2cm())
			setScore(getScore() + 2);
		// Uncomment when ideal line is ready
		// if (!idealLine)
		// setScore(0);
		System.out.println("IdealLine: " + idealLine);
		System.out.println("isFront30to35cm: " + isFront30to35cm());
		System.out.println("isFront35to50cm: " + isFront35to50cm());
		System.out.println("isSide2to5cm: " + isSide2to5cm());
		System.out.println("isSideLessThan2cm: " + isSideLessThan2cm());
		System.out.println("isFrontLessThan30cm: " + isFrontLessThan30cm());
		System.out.println("Jakarta Task 2 score: " + score);
		
		return getScore();
	}

	// Draw and save trail of the driving car
	public void drawTrail(List<String> coordinates, List<String> obstacleCoordinates) {
		BufferedImage lastScreenshot = null;
		float aspect = 1.778f;

		if (CameraFactory.lastScreenshotPath.length() > 0) {
			try {
				lastScreenshot = ImageIO.read(new File(CameraFactory.lastScreenshotPath));
				imgWidth = lastScreenshot.getWidth();
				imgHeight = lastScreenshot.getHeight();
				aspect = (float) imgWidth / imgHeight;
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
			float x = ((imgWidth / 2.1f)
					- ((-1 * ((imgWidth / aspect) / 13.5f)) * (Float.parseFloat(xStr) - CameraFactory.bgHighCamXMid)));
			float y = ((imgHeight / 2.1f)
					- ((-1 * ((imgHeight / aspect) / 9f)) * (Float.parseFloat(yStr) - CameraFactory.bgHighCamYMid)));
			xCoords.add(x);
			yCoords.add(y);
		}

		/*
		 * graphics.setColor(Color.RED);
		 * 
		 * for (String obstacleCoord : obstacleCoordinates) { String[] parts =
		 * obstacleCoord.split(";"); Float spX = ((imgWidth / 2) - ((-75) *
		 * (Float.valueOf(parts[0]) - CameraFactory.bgHighCamXMid))); Float spY
		 * = ((imgHeight / 2) - ((-75) * (Float.valueOf(parts[1]) -
		 * CameraFactory.bgHighCamYMid))); graphics.drawRect(spX.intValue(),
		 * spY.intValue(), 2, 2); graphics.fillRect(spX.intValue(),
		 * spY.intValue(), 2, 2); }
		 */

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
			graphics.drawString("Crank test score = " + String.valueOf(finalScore), 20, 20);

			try {
				String screenshotPath = Simulator.screenshotPath;
				String filename = screenshotPath + "Task2_Trail" + new Date().getTime() + ".png";
				if (ImageIO.write(combined, "png", new File(filename))) {
					System.out.println("Task 2 trail img saved");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// Create report containing score and drivers name
	public static void close() {
		String driverName = sim.getDrivingTask().getSettingsLoader().getSetting(Setting.General_driverName, "Default Driver");
		report = new JakartaTask2Report();
		report.generateReport(driverName, front30to35cm, front35to50cm, frontLessThan30cm, side2to5cm, sideLessThan2cm, idealLine, getScore());
	}
	// >>>>>>>
	// c34598c3007ef5223d4bc5b3a744dff97371811d:jakarta_on/eu/opends/jakarta/Task2.java
}