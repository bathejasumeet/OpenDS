package eu.opends.jakarta;

import java.util.ArrayList;
import java.util.Map;

import com.jme3.math.Vector3f;

import eu.opends.analyzer.DeviationComputer;
import eu.opends.analyzer.IdealLine;
import eu.opends.analyzer.IdealLine.IdealLineStatus;
import eu.opends.basics.SimulationBasics;
import eu.opends.drivingTask.scenario.IdealTrackContainer;
import eu.opends.main.Simulator;

public class IdealLineCalculator {
	
	public ArrayList<IdealLine> idealLineList = new ArrayList<IdealLine>();
	private ArrayList<Vector3f> carPositionList;
	private static float area;
	
	public void setIdealLineList(ArrayList<IdealLine> list){
		this.idealLineList = list; 
	}
	
	public ArrayList<IdealLine> getIdealLineList(){
		return idealLineList;
	}
	
	public void setPositionList(ArrayList<Vector3f> arrayOfPositions){
		this.carPositionList = arrayOfPositions;
	}
	
	public ArrayList<Vector3f> getPositionList(){
		return carPositionList;
	}
	

	public float getAreaBetweenCurrentIdeal(){
		return IdealLineCalculator.area;
	}
	
	public float setAreaBetweenCurrentIdeal(float value){
		return IdealLineCalculator.area = value;
	}
	
	
	@SuppressWarnings("static-access")
	public IdealLineCalculator(Simulator sim){
		
		try {
			carPositionList = sim.getTrail3D(); 
	
			Map<String, IdealTrackContainer> idealTrack = SimulationBasics.getDrivingTask().getScenarioLoader().getIdealTrackMap();//sim.getDrivingTask().getScenarioLoader().getIdealTrackMap();
			
			DeviationComputer devComp = new DeviationComputer(carPositionList, idealTrack);
			
			idealLineList = devComp.getIdealLines();
			
			for(IdealLine idealLine : idealLineList)
			{
				if(idealLine.getStatus() != IdealLineStatus.Unavailable)
				{
					String id = idealLine.getId();
					setAreaBetweenCurrentIdeal(idealLine.getArea());
					float length = idealLine.getLength();
					System.out.println("Area between ideal line (" + id + ") and driven line: " + getAreaBetweenCurrentIdeal());
					System.out.println("Length of ideal line: " + length);
					System.out.println("Mean deviation: " + (float)area/length);
					System.out.println("Status: " + idealLine.getStatus() + "\n");
				}
			}
		} catch (NullPointerException e){
			System.out.println(e);
		}
	}
	
}
