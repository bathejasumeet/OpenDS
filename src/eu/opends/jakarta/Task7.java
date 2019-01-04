package eu.opends.jakarta;

import com.jme3.scene.Spatial;

import eu.opends.car.Car;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;

public class Task7 {
	private int score = 10;
	private Car car;
	private Simulator sim;

	private static JakartaTask7Report report;

	public Task7(Simulator sim) {
		this.car = sim.getCar();
		this.sim = sim;
	}

	public void update(float tpf) {
		try {
			Spatial parkingBuilding = sim.getSceneNode().getChild("Parking_Building");
			Spatial sceneCar = null;
			sceneCar = sim.getSceneNode().getChild("car1");

			int collision = car.getCarNode().getWorldBound().collideWith(parkingBuilding.getWorldBound());
			float distance = sceneCar.getLocalTranslation().clone()
					.distance(car.getCarNode().getLocalTranslation().clone());
			if (collision == 0 && car.getCurrentSpeedKmh() >= 43 && (distance >= 12.5f && distance <= 24.5f)
					&& score > 5)
				score -= 5;
			if (collision > 0 && car.getCurrentSpeedKmh() >= 23 && (distance >= 10.5f && distance <= 12.5f)
					&& score > 0)
				score -= 5;
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public int getScore() {
		return score;
	}

	public void close() {
		// create and print report
		String driverName = sim.getDrivingTask().getSettingsLoader().getSetting(Setting.General_driverName, "Default Driver");
		report = new JakartaTask7Report();
		report.generateReport(driverName, getScore());
	}
}
