package eu.opends.jakarta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.jme3.util.IntMap.Entry;

import eu.opends.car.Car;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.input.KeyMapping;
import eu.opends.main.Simulator;

public class Task9 {
	public static boolean isWatching = false;
	private static Car car;
	private static Simulator sim;
	public static HashMap<String, Boolean> listOfActions = new HashMap<String, Boolean>();
	public static LinkedList<Task9Table> table = new LinkedList<Task9Table>();

	public static Integer counter = 0;

	// changed by the keys
	/*
	 * public static boolean activeFullRTurn = false; public static boolean
	 * active2RTurn = false; public static boolean active1RTurn = false;
	 * 
	 * public static boolean activeFullLTurn = false; public static boolean
	 * active2LTurn = false; public static boolean active1LTurn = false;
	 * 
	 * public static boolean activeStraight = false; public static boolean
	 * activeStop = false;
	 */

	public static boolean passedFullRTurn = false;
	public static boolean passed2RotRTurn = false;
	public static boolean passed1RotRTurn = false;

	public static boolean passedFullLTurn = false;
	public static boolean passed2RotLTurn = false;
	public static boolean passed1RotLTurn = false;

	public static boolean passedStraight = false;
	public static boolean passedStop = false;

	public static int grade = 10;
	public static boolean overallScore = true;

	public static String currentActivatedAction;

	private static JakartaTask9Report report;

	public Task9(Simulator sim) {
		Task9.car = sim.getCar();
		Task9.sim = sim;
		listOfActions.put("active1RTurn", false);
		listOfActions.put("active2RTurn", false);
		listOfActions.put("activeFullRTurn", false);
		listOfActions.put("active1LTurn", false);
		listOfActions.put("active2LTurn", false);
		listOfActions.put("activeFullLTurn", false);
		listOfActions.put("activeStraight", false);
		listOfActions.put("activeStop", false);
	}

	public List<Task9Table> getTable() {
		return table;
	}

	public static void setOperation(String valueToSearch) {
		for (String name : listOfActions.keySet()) {
			if (name == valueToSearch) {
				System.out.println("Value before " + listOfActions.get(name));
				listOfActions.put(name, true);
				System.out.println("Value after " + listOfActions.get(name));
				currentActivatedAction = name;
			} else {
				listOfActions.put(name, false);
			}
		}
	}

	public static HashMap<String, Boolean> getListOfActions() {
		return listOfActions;
	}

	public static void startWatching() {
		Task9.isWatching = true;
	}

	public static void stopWatching() {
		Task9.isWatching = false;
	}

	// update a map with another pair

	public void update(float tpf) {
		if (isWatching) {
			if ("active1LTurn" == currentActivatedAction) {
				// System.out.println("Current steering wheel position" +
				// sim.getCar().getSteeringWheelState());
				if (sim.getCar().getSteeringWheelState() > 0.49f && sim.getCar().getSteeringWheelState() < 0.51f
						&& sim.getCar().getCurrentSpeedKmh() > 1f) {
					table.getLast().result = true;
				}
			} else if ("active2LTurn" == currentActivatedAction) {
				// System.out.println("Current steering wheel position" +
				// sim.getCar().getSteeringWheelState());
				if (sim.getCar().getSteeringWheelState() > 0.99f && sim.getCar().getSteeringWheelState() < 1.01f
						&& sim.getCar().getCurrentSpeedKmh() > 1f) {
					table.getLast().result = true;
				}
			} else if ("activeFullLTurn" == currentActivatedAction) {
				// System.out.println("Current steering wheel position" +
				// sim.getCar().getSteeringWheelState());
				if (sim.getCar().getSteeringWheelState() > 1.05f && sim.getCar().getCurrentSpeedKmh() > 1f) {
					table.getLast().result = true;
				}
			}

			if ("active1RTurn" == currentActivatedAction) {
				// System.out.println("Current steering wheel position" +
				// sim.getCar().getSteeringWheelState());
				if (sim.getCar().getSteeringWheelState() > -0.49f && sim.getCar().getSteeringWheelState() < -0.51f
						&& sim.getCar().getCurrentSpeedKmh() > 1f) {
					table.getLast().result = true;
				}
			} else if ("active2RTurn" == currentActivatedAction) {
				// System.out.println("Current steering wheel position" +
				// sim.getCar().getSteeringWheelState());
				if (sim.getCar().getSteeringWheelState() > -0.99f && sim.getCar().getSteeringWheelState() < -1.01f
						&& sim.getCar().getCurrentSpeedKmh() > 1f) {
					table.getLast().result = true;
				}
			} else if ("activeFullRTurn" == currentActivatedAction) {
				if (sim.getCar().getSteeringWheelState() > -1.05f && sim.getCar().getCurrentSpeedKmh() > 1f) {
					table.getLast().result = true;
				}
				// System.out.println("Current steering wheel position" +
				// sim.getCar().getSteeringWheelState());
			} else if ("activeStraight" == currentActivatedAction) {
				if (sim.getCar().getSteeringWheelState() > -0.05f && sim.getCar().getSteeringWheelState() < 0.05f
						&& sim.getCar().getCurrentSpeedKmh() > 1f) {
					table.getLast().result = true;
					// System.out.println("Current steering wheel position" +
					// sim.getCar().getSteeringWheelState());
				}
			} else if ("activeStop" == currentActivatedAction) {
				if (sim.getCar().getCurrentSpeedKmh() < 0.1f) {
					// System.out.println("Current steering wheel position" +
					// sim.getCar().getCurrentSpeedKmh());
					table.getLast().result = true;
				}
			}
		} else if (!isWatching) {
			System.out.println("No Steering wheel position");
		}
	}

	public static void evaluate() {
		System.out.println("table size is " + table.size());
		
		for (int i = 0; i < table.size(); i++) {
			if (!table.get(i).result){
				overallScore = false;
			}
			System.out.println("The result of Task " + table.get(i).name + " is = " + table.get(i).result);
		}

		// create and print report
		String driverName = sim.getDrivingTask().getSettingsLoader().getSetting(Setting.General_driverName, "Default Driver");
		report = new JakartaTask9Report();
		report.generateReport(driverName, overallScore);
	}

}
