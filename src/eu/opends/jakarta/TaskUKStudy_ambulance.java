package eu.opends.jakarta;



import eu.opends.car.Car;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;
import eu.opends.tools.PanelCenter;
import eu.opends.traffic.PhysicalTraffic;
import eu.opends.traffic.TrafficCar;
import eu.opends.traffic.TrafficCarData;

public class TaskUKStudy_ambulance {
	public static boolean isSimulationStarted = false;

	private static Car car;
	private static Simulator sim;

	
	
	public static void simulationStarted(){
		isSimulationStarted = true;
	}
	
	long startTime = System.currentTimeMillis();
	long elapsedTime = 0L;
	
	
	
	public TaskUKStudy_ambulance(Simulator sim) {
		TaskUKStudy_ambulance.car = sim.getCar();
		TaskUKStudy_ambulance.sim = sim;
		addAmbulance();
	}

	
	@SuppressWarnings("static-access")
	public void update(float tpf) {
		if (isSimulationStarted){
			
		} 
	}

	
	@SuppressWarnings("static-access")
	public void addAmbulance(){
		for (TrafficCarData vehicleData : sim.getPhysicalTraffic().getSpecialVehicleDataList()){
			sim.getPhysicalTraffic().getTrafficObjectList().add(new TrafficCar(sim, vehicleData));
		}
	}

	public static void evaluate() {

		

	}
}
