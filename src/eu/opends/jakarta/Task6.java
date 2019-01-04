package eu.opends.jakarta;

import com.jme3.scene.Spatial;

import eu.opends.car.Car;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;

public class Task6 {
	private boolean score = true;
	private Car car;
	private Simulator sim;
	
	private static JakartaTask6Report report;

	public Task6(Simulator sim) {
		this.car = sim.getCar();
		this.sim = sim;
	}
	
	public void update(float tpf) {
		try {
			Spatial parkingBuilding = sim.getSceneNode().getChild("Parking_Building");
			int collision = car.getCarNode().getWorldBound().collideWith(parkingBuilding.getWorldBound());
			if ((collision > 0 && car.getCurrentSpeedKmh() >= 20) || (collision == 0 && car.getCurrentSpeedKmh() >= 40))
				score = false;
		} catch (Exception e) {
			// TODO: handle exception
		}
			
	}
	
	public boolean getScore() {
		return score;
	}
	
	public void close() {
		// create and print report
		String driverName = sim.getDrivingTask().getSettingsLoader().getSetting(Setting.General_driverName, "Default Driver");
		report = new JakartaTask6Report();
		report.generateReport(driverName, getScore());
	}
}
