package eu.opends.jakarta;



import eu.opends.car.Car;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;

public class Task8 {
	public static boolean isWatching40km = false;
	public static boolean isWatching20km = false;
	public static boolean isWatchingFullStop = false;
	public static boolean isWatchingEnd = false;
	private static Car car;
	public static boolean passed40kmZone = false;
	public static boolean passed20kmZone = false;
	public static boolean passedFullStopZone = false;
	public static int grade = 10;
	public static float maxSpeed40km = 0.0f;
	public static float maxSpeed20km = 0.0f;
	public static float minSpeedFullStop = 0.15f;
	public static boolean passed = false;
	
	public static boolean isWatching40Triggered = false;
	public static boolean isWatching20Triggered = false;
	public static boolean isWatchingFullStopTriggered = false;
	
	private static Simulator sim;
	
	
	private static JakartaTask8Report report;
	
	
	public Task8(Simulator sim) {
		Task8.car = sim.getCar();
	}

	public static void startWatching40km() {
		isWatching40km = true;
		isWatching40Triggered = true;
	}

	public static void stopWatching40km() {
		isWatching40km = false;
	}

	public static void startWatching20km() {
		isWatching20km = true;
		isWatching20Triggered = true;
	}

	public static void stopWatching20km() {
		isWatching20km = false;
	}

	public static void startWatchingFullStop() {
		isWatchingFullStop = true;
		isWatchingFullStopTriggered = true;
	}
	
	public static void stopWatchingFullStop() {
		isWatchingFullStop = false;
	}

	
	
	
	public void update(float tpf) {
		if (isWatching40km){
			maxSpeed40km = Math.max(Math.abs(car.getCarControl().getCurrentVehicleSpeedKmHour()), maxSpeed40km);
			//System.out.println("Current speed in 40km zone = " + car.getCarControl().getCurrentVehicleSpeedKmHour());
			//System.out.println("Current max speed in 40km zone= " + maxSpeed40km);
		} else if (isWatching20km){
			maxSpeed20km = Math.max(Math.abs(car.getCarControl().getCurrentVehicleSpeedKmHour()), maxSpeed20km);
		} else if (isWatchingFullStop){
			minSpeedFullStop = Math.min(Math.abs(car.getCarControl().getCurrentVehicleSpeedKmHour()), minSpeedFullStop);
			//System.out.println("Current speed in Stop zone = " + car.getCarControl().getCurrentVehicleSpeedKmHour());
			//System.out.println("Current min speed in Stop zone= " + minSpeedFullStop);
		}
	}


	public static void evaluate() {
		if (isWatching40Triggered && maxSpeed40km <= 42f) {
			passed40kmZone = true;
			System.out.println("Test 8 (40km zone) passed");
		} else {
			System.out.println("Test 8 (40km zone) failed");
		}
		
		if (isWatching20Triggered && maxSpeed20km <= 22f) {
			passed20kmZone = true;
			System.out.println("Test 8 (20km zone) passed");
		} else {
			System.out.println("Test 8 (20km zone) failed");
		}
		
		if (isWatchingFullStopTriggered && minSpeedFullStop <= 0.15f){
			passedFullStopZone = true;
			System.out.println("Test 8 (Full stop zone) passed");
		} else {
			System.out.println("Test 8 (Full stop zone) failed");
		}
		
		if (passed40kmZone && passed20kmZone && passedFullStopZone){
			passed = true;
			System.out.println("Overall score Test 8 - passed");
		}
		else {
			System.out.println("Test 8 - failed");
		}
		
		// create and print report
		String driverName = sim.getDrivingTask().getSettingsLoader().getSetting(Setting.General_driverName, "Default Driver");
		report = new JakartaTask8Report();
		report.generateReport(driverName, passed40kmZone, passed20kmZone, passed);
	}
}
