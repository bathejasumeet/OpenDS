package eu.opends.jakarta;

import java.util.ArrayList;

import eu.opends.car.Car;
import eu.opends.car.Transmission;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;

public class Task4 {
	public static boolean isWatching = false;
	public static boolean initialForward = true;
	public static ArrayList<String> track;
	public static float distanceDrivenBack = 0f;
	private static Car car;
	private static boolean rollingBack = false;
	private static com.jme3.math.Vector3f locationRollBack = new com.jme3.math.Vector3f();
	public static boolean passed = false;
	private static Simulator sim;
	
	private static JakartaTask4Report report;

	public Task4(Simulator sim) {
		Task4.car = sim.getCar();
	}

	public static void startWatching() {
		if (!isWatching) {
			isWatching = true;
			if (car.getCarControl().getCurrentVehicleSpeedKmHour() > 0)
				initialForward = true;
			else if (car.getCarControl().getCurrentVehicleSpeedKmHour() <= 0)
				initialForward = false;
		}
	}

	public static void stopWatching() {
		isWatching = false;
	}

	public void update(float tpf) {
		if (isWatching && ((car.getCarControl().getCurrentVehicleSpeedKmHour() > 0 && !initialForward)
				|| (car.getCarControl().getCurrentVehicleSpeedKmHour() <= 0 && initialForward))) {
			getChangeLocation();
			distanceDrivenBack = car.getCarNode().getLocalTranslation().distance(locationRollBack);
			System.out.println("Distance Driven Back = " + distanceDrivenBack);
		}
	}

	private static void getChangeLocation() {
		if (!rollingBack) {
			locationRollBack = car.getCarNode().getLocalTranslation().clone();
			rollingBack = true;
		}
	}
	
	public static void evaluate() {
		if (distanceDrivenBack < 0.05f) {
			passed = true;
			System.out.println("Test 4 passed");
		} else {
			System.out.println("Test 4 failed");
		}
		String driverName = sim.getDrivingTask().getSettingsLoader().getSetting(Setting.General_driverName, "Default Driver");
		report = new JakartaTask4Report();
		report.generateReport(driverName, !passed, passed);
	}
}
