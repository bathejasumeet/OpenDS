/*
*  This file is part of OpenDS (Open Source Driving Simulator).
*  Copyright (C) 2016 Rafael Math
*
*  OpenDS is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  OpenDS is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with OpenDS. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.opends.codriver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;

import eu.opends.codriver.util.DataStructures.Input_data_str;
import eu.opends.main.Simulator;
import eu.opends.opendrive.processed.Intersection;
import eu.opends.opendrive.processed.ODLane;
import eu.opends.opendrive.processed.ODPoint;
import eu.opends.opendrive.processed.ODRoad;
import eu.opends.opendrive.processed.SpeedLimit;
import eu.opends.opendrive.processed.ODLane.AdasisLaneType;
import eu.opends.opendrive.util.AdasisCurvature;
import eu.opends.opendrive.util.ODVisualizer;
import eu.opends.tools.PanelCenter;
import eu.opends.tools.Vector3d;

public class ScenarioMessage 
{
	private boolean showMessageBox = true;
	private boolean printStatusMsg = false;
	private boolean printCSVMsg = false;
	private boolean sendToCodriver = true;
	private int rangeOfTrajectoryBackcast = 200;
	private int rangeOfTrajectoryForecast = 200;
	private int rangeOfSpeedLimitForecast = 200;
	private int rangeOfIntersectionForecast= 200;
	private float minTimeDiffForUpdate = 0.049f; // approx. 0.05
	private float minTimeDiffForUpdate2 = 0.049f;
	
	private Simulator sim;
	private ODVisualizer visualizer;
	private HashMap<String,ODRoad> roadMap;
	private ObjectWatch objectWatch;
	
	
	public ScenarioMessage(Simulator sim, ODVisualizer visualizer, HashMap<String,ODRoad> roadMap)
	{
		this.sim = sim;
		this.visualizer = visualizer;
		this.roadMap = roadMap;
		
		// create visual road markers (colored spheres indicating positions)
		if(sim.getCar() != null)
		{
			visualizer.createMarker("1", new Vector3f(0, 0, 0), sim.getCar().getPosition(), visualizer.blackMaterial, 0.5f, true);
			visualizer.createMarker("2", new Vector3f(0, 0, 0), sim.getCar().getPosition(), visualizer.whiteMaterial, 0.5f, true);
			visualizer.createMarker("3", new Vector3f(0, 0, 0), sim.getCar().getPosition(), visualizer.redMaterial, 0.5f, true);
			visualizer.createMarker("4", new Vector3f(0, 0, 0), sim.getCar().getPosition(), visualizer.greenMaterial, 0.5f, true);
			
			// suggested trajectory
			for(int i=1; i<=rangeOfTrajectoryForecast; i++)
				visualizer.createMarker("roadPoint_" + i, new Vector3f(0, 0, 0), sim.getCar().getPosition(), visualizer.yellowMaterial, 0.3f, false);
			
			// backward trajectory
			for(int i=1; i<=rangeOfTrajectoryBackcast; i++)
				visualizer.createMarker("roadPoint_back_" + i, new Vector3f(0, 0, 0), sim.getCar().getPosition(), visualizer.redMaterial, 0.3f, false);
		
		}
		
		// prepare column headings
		String AdasisCurvatureDist = enumerateString("AdasisCurvatureDist", ";", 1, 200);
		String AdasisCurvatureValues = enumerateString("AdasisCurvatureValues", ";", 1, 200);
		String AdasisSpeedLimitDist = enumerateString("AdasisSpeedLimitDist", ";", 1, 20);
		String AdasisSpeedLimitValues = enumerateString("AdasisSpeedLimitValues", ";", 1, 20);
		
		// print column headings (for CSV file)
		if(printCSVMsg)
			System.out.println("ID;Version;TimeStamp;RelativeTimeStamp;ECUtime;AVItime;Status;VLgtFild;ALgtFild;ALatFild;"
					+ "YawRateFild;SteerWhlAg;VehicleLen;VehicleWidth;RequestedCruisingSpeed;CurrentLane;NrObjs;LaneWidth;"
					+ "LatOffsLaneR;LatOffsLaneL;LaneHeading;LaneCrvt;DetectionRange;AdasisCurvatureNr;" + AdasisCurvatureDist + ";" 
					+ AdasisCurvatureValues + ";AdasisSpeedLimitNr;" + AdasisSpeedLimitDist + ";" + AdasisSpeedLimitValues 
					+ ";IntersectionDistance");
		
		elapsedBulletTimeAtLastUpdate = sim.getBulletAppState().getElapsedSecondsSinceStart();

		/*
		// generate type descriptin of columns (for Excel only)
		System.out.println(addTypePattern("ID", true) + ", " + addTypePattern("Version", false) + ", " + 
				addTypePattern("TimeStamp", true) + ", " addTypePattern("RelativeTimeStamp", true) + ", "+ 
				addTypePattern("ECUtime", true) + ", " + addTypePattern("AVItime", true) + ", " + 
				addTypePattern("Status", true) + ", " + 
				addTypePattern("VLgtFild", false) + ", " + addTypePattern("ALgtFild", false) + ", " + 
				addTypePattern("ALatFild", false) + ", " + addTypePattern("YawRateFild", false) + ", " + 
				addTypePattern("SteerWhlAg", false) + ", " + addTypePattern("VehicleLen", false) + ", " + 
				addTypePattern("VehicleWidth", false) + ", " + addTypePattern("RequestedCruisingSpeed", false) + ", " + 
				addTypePattern("CurrentLane", true) + ", " + addTypePattern("NrObjs", true) + ", " + 
				addTypePattern("LaneWidth", false) + ", " + addTypePattern("LatOffsLaneR", false) + ", " + 
				addTypePattern("LatOffsLaneL", false) + ", " + addTypePattern("LaneHeading", false) + ", " + 
				addTypePattern("LaneCrvt", false) + ", " + addTypePattern("DetectionRange", false) + ", " + 
				addTypePattern("AdasisCurvatureNr", true) + ", " + 
				enumerateAndTypeString("AdasisCurvatureDist", ", " , 1, 200, false) + ", " + 
				enumerateAndTypeString("AdasisCurvatureValues", ", " , 1, 200, false) + ", " + 
				addTypePattern("AdasisSpeedLimitNr", true) + ", " + 
				enumerateAndTypeString("AdasisSpeedLimitDist", ", " , 1, 20, false) + ", " + 
				enumerateAndTypeString("AdasisSpeedLimitValues", ", " , 1, 20, true) + ", " + 
				addTypePattern("IntersectionDistance", false));
		*/	
		
		objectWatch = new ObjectWatch(sim);
	}
	

	/**
	 * Computes collisions between car and lane in order to extract all relevant semantic information 
	 * to create a new scenario message
	 */
	private float elapsedBulletTimeAtLastUpdate;
	private float elapsedBulletTimeAtLastUpdate2;
	private float elapsedRendererTime = 0;
	public void update(float tpf)
	{
		//System.err.println("time: " + (tpf*1000));
		
		elapsedRendererTime += tpf;
			
		// current vehicle position
		Vector3f carPos = sim.getCar().getPosition();
		carPos.y = 0;
		
		// get most probable lane from result list according to least heading deviation
		ODLane lane = sim.getOpenDriveCenter().getMostProbableLane(carPos);
		if(lane != null)
		{
			long timeStampMS = System.currentTimeMillis();
			
			float elapsedBulletTime = sim.getBulletAppState().getElapsedSecondsSinceStart();
			float bulletTimeDiff = elapsedBulletTime - elapsedBulletTimeAtLastUpdate; // in seconds
			float bulletTimeDiff2 = elapsedBulletTime - elapsedBulletTimeAtLastUpdate2; // in seconds
			
			if(bulletTimeDiff >= minTimeDiffForUpdate)
			{
				// Scenario Message
				//------------------
	
				// header part
				int ID = 1;
				
				int Version = 1113;
				
				double TimeStamp = timeStampMS / 1000d;
				double RelativeTimeStamp = (timeStampMS - Simulator.getSimulationStartTime()) / 1000d;
				
				int ECUtime = (int) (elapsedBulletTime * 1000f); // in ms
				
				int AVItime = (int) (elapsedRendererTime * 1000f); // in ms
				
				/*
				System.err.println("elapsedSecondsSinceStart: " +	elapsedSecondsSinceStart + 
						"; elapsedRendererTime: " + elapsedRendererTime +
						"; diff: " + (elapsedRendererTime-elapsedSecondsSinceStart));
				
				System.err.println("elapsedSecondsSinceStart: " +	elapsedSecondsSinceStart + 
						"; elapsedBulletTime: " + elapsedBulletTime +
						"; diff: " + (elapsedBulletTime-elapsedSecondsSinceStart));
				
				System.err.println("elapsedBulletTime: " +	elapsedBulletTime + 
						"; elapsedRendererTime: " + elapsedRendererTime +
						"; diff: " + (elapsedRendererTime-elapsedBulletTime));
				*/				
				
				
				int Status = 0;
				
				
				// vehicle part
				float VLgtFild = sim.getCar().getCurrentSpeedMs();
				
				Vector3f acceleration = getAccelerationVector(bulletTimeDiff);
				float ALgtFild = acceleration.getX();
				float ALatFild = acceleration.getY();

				float YawRateFild = -getYawRateFild(bulletTimeDiff);	

				float SteerWhlAg = 1000 * sim.getCar().getSteeringWheelState() * FastMath.DEG_TO_RAD;
				
				
				//System.err.println("SteerWhlAg: " + sim.getCar().getSteeringWheelState() + "; YawRateFild: " + YawRateFild);
				
				/*
				double Ksteer = ((previousSteerWhlAg-SteerWhlAg) * YawRateFild)/VLgtFild;
				System.err.println("Ksteer: " + Ksteer);
				previousSteerWhlAg = SteerWhlAg;
				*/
				
				double VehicleLen = 4.4;
				
				double VehicleWidth = 1.8;
				
				double RequestedCruisingSpeed = 80 / 3.6f;
			

				// Adasis part
				int laneID = lane.getID();
				double s = lane.getCurrentInnerBorderPoint().getS();
				
				//double speedLimit = lane.getSpeedLimit(s);
				//System.err.println("current speed limit: " + speedLimit);
						
				float hdgDiff = lane.getHeadingDiff(sim.getCar().getHeadingDegree());
				boolean isWrongWay = (FastMath.abs(hdgDiff) > 90);
				
				AdasisLaneType adasisLaneType = lane.getAdasisLaneType(isWrongWay);
				int CurrentLane = adasisLaneType.ordinal();
					
				// Objects part
				objectWatch.update(bulletTimeDiff);
				int NrObjs = objectWatch.getNrObjs();
				int[] ObjID = objectWatch.getObjID();
				int[] ObjClass = objectWatch.getObjClass();
				int[] ObjSensorInfo = objectWatch.getObjSensorInfo();
				double[] ObjX = objectWatch.getObjX();
				double[] ObjY = objectWatch.getObjY();
				double[] ObjLen = objectWatch.getObjLen();
				double[] ObjWidth = objectWatch.getObjWidth();
				double[] ObjVel = objectWatch.getObjVel();
				double[] ObjCourse = objectWatch.getObjCourse();
				double[] ObjAcc = objectWatch.getObjAcc();
				double[] ObjCourseRate = objectWatch.getObjCourseRate();
				int[] ObjNContourPoints = objectWatch.getObjNContourPoints();
				
				double LaneWidth = lane.getCurrentWidth();

				Vector3d rightPos;
				if(!isWrongWay)
					rightPos = lane.getCurrentOuterBorderPoint().getPosition();
				else
					rightPos = lane.getCurrentInnerBorderPoint().getPosition();
				visualizer.setMarkerPosition("2", rightPos.toVector3f(), sim.getCar().getPosition(), visualizer.whiteMaterial, true);
				double LatOffsLaneR = -rightPos.toVector3f().distance(carPos);
				
				Vector3d leftPos;
				if(!isWrongWay)
					leftPos = lane.getCurrentInnerBorderPoint().getPosition();
				else
					leftPos = lane.getCurrentOuterBorderPoint().getPosition();
				visualizer.setMarkerPosition("1", leftPos.toVector3f(), sim.getCar().getPosition(), visualizer.blackMaterial, true);
				double LatOffsLaneL = leftPos.toVector3f().distance(carPos);
				

				if(isWrongWay)
					hdgDiff = (hdgDiff + 180) % 360;
				
				if(hdgDiff>180)
					hdgDiff -= 360;
				
				double LaneHeading = FastMath.DEG_TO_RAD * hdgDiff;
				
				//System.err.println("LaneHeading: \t\t\t\t" + LaneHeading + " \t" + hdgDiff);
	
				float length = -5;
				float hdgLane = FastMath.DEG_TO_RAD*(lane.getLaneHeading());
				if(isWrongWay)
					hdgLane += FastMath.PI;
				Vector3f frontPosLane = sim.getCar().getPosition().add(new Vector3f(length*FastMath.sin(hdgLane), 0, length*FastMath.cos(hdgLane)));
				visualizer.setMarkerPosition("3", frontPosLane, sim.getCar().getPosition(), visualizer.redMaterial, true);
				
				float carHdg = FastMath.DEG_TO_RAD*(- sim.getCar().getHeadingDegree());
				Vector3f frontPosCar = sim.getCar().getPosition().add(new Vector3f(length*FastMath.sin(carHdg), 0, length*FastMath.cos(carHdg)));
				visualizer.setMarkerPosition("4", frontPosCar, sim.getCar().getPosition(), visualizer.greenMaterial, true);
				
				double LaneCrvt = lane.getCurrentCurvature();
				if(isWrongWay)
					LaneCrvt = -LaneCrvt;
				
				double DetectionRange = 0; //rangeOfTrajectoryForecast;
				
				
				ArrayList<AdasisCurvature> curvatureDistList = new ArrayList<AdasisCurvature>();
				
				// get points on center of current lane for 200 meters behind of the current position
				for(int i=-rangeOfTrajectoryBackcast; i<=-1; i++)
				{
					ODPoint point = lane.getLaneCenterPointBack(isWrongWay, s, -i);
					if(point != null)
					{
						// visualize point (red)
						visualizer.setMarkerPosition("roadPoint_back_" + -i, point.getPosition().toVector3f(), sim.getCar().getPosition(), visualizer.redMaterial, false);
						
						// if lane curvature available add to list
						Double curvature = point.getLaneCurvature();
						if(curvature != null)
							curvatureDistList.add(new AdasisCurvature(i, curvature));
					}
					else
						visualizer.hideMarker("roadPoint_back_" + -i);
				}
				
				// get points on center of current lane for 200 meters ahead of the current position
				for(int i=1; i<=rangeOfTrajectoryForecast; i++)
				{
					ODPoint point = lane.getLaneCenterPointAhead(isWrongWay, s, i);
					if(point != null)
					{
						// visualize point (yellow)
						visualizer.setMarkerPosition("roadPoint_" + i, point.getPosition().toVector3f(), sim.getCar().getPosition(), visualizer.yellowMaterial, false);
						
						// if lane curvature available add to list
						Double curvature = point.getLaneCurvature();
						if(curvature != null)
							curvatureDistList.add(new AdasisCurvature(i, curvature));
					}
					else
						visualizer.hideMarker("roadPoint_" + i);
				}
				
				
				// remove redundant entries of the Adasis curvature distance list
				ArrayList<AdasisCurvature> reducedCurvatureDistList = reduceList(curvatureDistList);
				

				// add first 200 entries of reducedCurvatureDistList to the arrays
				int AdasisCurvatureNr = Math.min(reducedCurvatureDistList.size(),200);
				double[] AdasisCurvatureDist = getEmptyDoubleArray(200);
				double[] AdasisCurvatureValues = getEmptyDoubleArray(200);
				for(int i=0; i<AdasisCurvatureNr; i++)
				{
					AdasisCurvature item = reducedCurvatureDistList.get(i);
					AdasisCurvatureDist[i] = item.getDist();
					AdasisCurvatureValues[i] = item.getValue();
					//System.err.println("nr: " + i + " --> pos: " + item.getDist() + " --> curv: " + item.getValue());
				}

				if(reducedCurvatureDistList.size()>200)
					System.err.println("Too many curvature distance points: " + reducedCurvatureDistList.size() + 
							". Entries beyond 200 will be discarded");


				ArrayList<SpeedLimit> speedLimitList = lane.getSpeedLimitListAhead(isWrongWay, s, rangeOfSpeedLimitForecast);
				int AdasisSpeedLimitNr = Math.min(speedLimitList.size(), 20);
				double[] AdasisSpeedLimitDist = getEmptyDoubleArray(20);
				int[] AdasisSpeedLimitValues = getEmptyIntArray(20);
				for(int i=0; i<AdasisSpeedLimitNr; i++)
				{
					SpeedLimit speedLimit = speedLimitList.get(i);
					AdasisSpeedLimitDist[i] = speedLimit.getDistance();
					
					if(speedLimit.getSpeed() != null)
						AdasisSpeedLimitValues[i] = speedLimit.getSpeed().intValue();
					else
						AdasisSpeedLimitValues[i] = -1;
					
					//speedLimit.getDistance();
					//speedLimit.getSpeed();
					
					//System.err.println("SpeedLimit " + speedLimit.getSpeed() + " in: " + speedLimit.getDistance() + " m");
				}
				

				ArrayList<Intersection> intersectionList = lane.getIntersectionAhead(isWrongWay, s, rangeOfIntersectionForecast);
				for(Intersection intersection : intersectionList)
				{
					//intersection.getDistance();
					//intersection.getJunctionID();
					
					//System.err.println("Intersection " + intersection.getJunctionID() + " in: "+ intersection.getDistance() + " m");
				}
				
				double IntersectionDistance = -1;
				if(intersectionList.size()>0)
					IntersectionDistance = intersectionList.get(0).getDistance();
				
				
				// show message box with selected parameters in rendering frame
				if(showMessageBox)
				{
					DecimalFormat f = new DecimalFormat("#0.000");
					PanelCenter.getMessageBox().addMessage("Position [RoadID: " + lane.getODRoad().getID() + ", LaneID: " + laneID + 
							", s: " + f.format(s) + ", lane type: " + adasisLaneType + "]                                                       "
									+ "latOffsLaneL: " + f.format(LatOffsLaneL) + "             "
							        + "latOffsLaneR: " + f.format(LatOffsLaneR) + "             "
									+ "laneWidth: " + f.format(LaneWidth)+ "                                                                            "
									+ "hdgDiff: " + f.format(hdgDiff)+ "                                                                            "
									+ "laneCrvt: " + f.format(LaneCrvt), 1);
				}
				
				
				// print status message to command line
				if(printStatusMsg)
					System.out.println("road: " + lane.getODRoad().getID() + ", lane: " + laneID + 
						", s: " + s + ", type: " + lane.getType());
				
				
				// print values (for CSV generation) to command line
				if(printCSVMsg)
					System.out.println(ID + ";" + Version + ";" + TimeStamp + ";" + RelativeTimeStamp + ";" + ECUtime + ";" + 
						AVItime + ";" + Status + ";" + VLgtFild + ";" + ALgtFild + ";" + ALatFild + ";" + YawRateFild + ";" + 
						SteerWhlAg + ";" + VehicleLen + ";" + VehicleWidth + ";" + 
						RequestedCruisingSpeed + ";" + CurrentLane + ";" + NrObjs + ";" + LaneWidth + ";" + LatOffsLaneR + ";" + 
						LatOffsLaneL + ";" + LaneHeading + ";" + LaneCrvt + ";" + DetectionRange + ";" + AdasisCurvatureNr + ";" + 
						joinArrayToString(";", AdasisCurvatureDist) + ";" + joinArrayToString(";", AdasisCurvatureValues) + ";" + 
						AdasisSpeedLimitNr + ";" + joinArrayToString(";", AdasisSpeedLimitDist) + ";" + 
						joinArrayToString(";", AdasisSpeedLimitValues) + ";" + IntersectionDistance);
				
				
				if(sendToCodriver && (bulletTimeDiff2 >= minTimeDiffForUpdate2))
				{
					Input_data_str scenario_msg = new Input_data_str();
					scenario_msg.ID = ID;
					scenario_msg.Version = Version;
					scenario_msg.TimeStamp = TimeStamp;
					scenario_msg.ECUtime = ECUtime;
					scenario_msg.AVItime = AVItime;
					scenario_msg.Status = Status;
					scenario_msg.VLgtFild = VLgtFild;
					scenario_msg.ALgtFild = ALgtFild;
					scenario_msg.ALatFild = ALatFild;
					scenario_msg.YawRateFild = YawRateFild;
					scenario_msg.SteerWhlAg = SteerWhlAg;
					scenario_msg.VehicleLen = VehicleLen;
					scenario_msg.VehicleWidth = VehicleWidth;
					scenario_msg.RequestedCruisingSpeed = RequestedCruisingSpeed;
					scenario_msg.CurrentLane = CurrentLane;
					scenario_msg.NrObjs = NrObjs;
					scenario_msg.ObjID = ObjID;
					scenario_msg.ObjClass = ObjClass;
					scenario_msg.ObjSensorInfo = ObjSensorInfo;
					scenario_msg.ObjX = ObjX;
					scenario_msg.ObjY = ObjY;
					scenario_msg.ObjLen = ObjLen;
					scenario_msg.ObjWidth = ObjWidth;
					scenario_msg.ObjVel = ObjVel;
					scenario_msg.ObjCourse = ObjCourse;
					scenario_msg.ObjAcc = ObjAcc;
					scenario_msg.ObjCourseRate = ObjCourseRate;
					scenario_msg.ObjNContourPoints = ObjNContourPoints;
					scenario_msg.LaneWidth = LaneWidth;
					scenario_msg.LatOffsLaneR = LatOffsLaneR;
					scenario_msg.LatOffsLaneL = LatOffsLaneL;
					scenario_msg.LaneHeading = LaneHeading;
					scenario_msg.LaneCrvt = LaneCrvt;
					scenario_msg.DetectionRange = DetectionRange;
					scenario_msg.AdasisCurvatureNr = AdasisCurvatureNr;
					scenario_msg.AdasisCurvatureDist = AdasisCurvatureDist;
					scenario_msg.AdasisCurvatureValues = AdasisCurvatureValues;
					scenario_msg.AdasisSpeedLimitNr = AdasisSpeedLimitNr;
					scenario_msg.AdasisSpeedLimitDist = AdasisSpeedLimitDist;
					scenario_msg.AdasisSpeedLimitValues = AdasisSpeedLimitValues;
					scenario_msg.IntersectionDistance = IntersectionDistance;					
					sim.getCodriverConnector().sendScenarioMsg(scenario_msg);
					
					elapsedBulletTimeAtLastUpdate2 = elapsedBulletTime;
				}
				
				elapsedBulletTimeAtLastUpdate = elapsedBulletTime;
			}
		}
		else
		{
			// if no lane next to car --> hide all markers
			visualizer.hideMarker("1");
			visualizer.hideMarker("2");
			visualizer.hideMarker("3");
			visualizer.hideMarker("4");
			
			for(int i=1; i<=rangeOfTrajectoryForecast; i++)
				visualizer.hideMarker("roadPoint_" + i);
			
			for(int i=1; i<=rangeOfTrajectoryBackcast; i++)
				visualizer.hideMarker("roadPoint_back_" + i);
		}
	}


	public ArrayList<AdasisCurvature> reduceList(ArrayList<AdasisCurvature> curvatureDistList)
	{
		ArrayList<AdasisCurvature> reducedCurvatureDistList = new ArrayList<AdasisCurvature>();
		
		// keep first item (if available) in any case
		if(curvatureDistList.size()>=1)
			reducedCurvatureDistList.add(curvatureDistList.get(0));
		
		// iterate over items between first and last item
		if(curvatureDistList.size()>=3)
		{
			for(int i=1; i<curvatureDistList.size()-1; i++)
			{
				AdasisCurvature previous = curvatureDistList.get(i-1);
				AdasisCurvature current = curvatureDistList.get(i);
				AdasisCurvature next = curvatureDistList.get(i+1);
				
				// keep item if different to pedecessor or successor
				if(previous.getValue() != current.getValue() || current.getValue() != next.getValue())
					reducedCurvatureDistList.add(current);
			}
		}
		
		// keep last item (if available and not equal to first item) in any case
		if(curvatureDistList.size()>=2)
			reducedCurvatureDistList.add(curvatureDistList.get(curvatureDistList.size()-1));
		
		return reducedCurvatureDistList;
	}

	
	private String enumerateString(String string, String separator, int start, int end)
	{
		String returnString = "";
		for(int i=start; i<end; i++)
			returnString += string + String.format("%03d", i) + separator;

		returnString += string + String.format("%03d", end);
		
		return returnString;
	}
	

	private String joinArrayToString(String separator, double[] array)
	{
		String returnString = "";
		for(int i=0; i<array.length-1; i++)
			returnString += array[i] + separator;

		returnString += array[array.length-1];
		
		return returnString;
	}
	
	
	private String joinArrayToString(String separator, int[] array)
	{
		String returnString = "";
		for(int i=0; i<array.length-1; i++)
			returnString += array[i] + separator;

		returnString += array[array.length-1];
		
		return returnString;
	}


	private double[] getEmptyDoubleArray(int size)
	{
		double[] array = new double[size];
		
		for(int i=0; i<size; i++)
			array[i] = 0.0;

		return array;
	}

	
	private int[] getEmptyIntArray(int size)
	{
		int[] array = new int[size];
		
		for(int i=0; i<size; i++)
			array[i] = 0;

		return array;
	}

	private Vector3f previousSpeedVector = new Vector3f(0,0,0);
	private Vector3f getAccelerationVector(float timeDiff)
	{
	    Vector3f globalSpeedVector = sim.getCar().getCarControl().getLinearVelocity();
	    float heading = sim.getCar().getHeading();
	    float speedForward = FastMath.sin(heading) * globalSpeedVector.x - FastMath.cos(heading) * globalSpeedVector.z;
	    float speedLateral = FastMath.cos(heading) * globalSpeedVector.x + FastMath.sin(heading) * globalSpeedVector.z;
	    float speedVertical = globalSpeedVector.y;
	    Vector3f currentSpeedVector = new Vector3f(speedForward, speedLateral, speedVertical); // in m/s
	    Vector3f currentAccelerationVector = currentSpeedVector.subtract(previousSpeedVector).divide(timeDiff); // in m/s^2
	    
	    /*
	    if(sim.getCar().getCurrentSpeedKmh() < 3 && sim.getCar().getAcceleratorPedalIntensity() < 0.1f)
	    	currentAccelerationVector.x = 0;
	    */

	    previousSpeedVector = currentSpeedVector;

		return currentAccelerationVector;
	}

	
    // Filtered yaw-rate (rad/s)
	private float previousHeading = 0;
    public float getYawRateFild(float diffTime)
    {
    	float currentHeading = sim.getCar().getHeading();
    	
    	float diffHeading = currentHeading-previousHeading;
    	
    	if(diffHeading > FastMath.PI)  // 180
    		diffHeading -= FastMath.TWO_PI;  // 360
    	
    	if(diffHeading < -FastMath.PI)  // 180
    		diffHeading += FastMath.TWO_PI;  // 360
    	
    	previousHeading = currentHeading;
    	
    	return diffHeading/diffTime;
    }

	
	/*
	private String enumerateAndTypeString(String string, String separator, int start, int end, boolean isInt)
	{		
		String returnString = "";
		for(int i=start; i<end; i++)
			returnString += addTypePattern(string + String.format("%03d", i), isInt) + separator;

		returnString += addTypePattern(string + String.format("%03d", end), isInt);
		
		return returnString;
	}


	private String addTypePattern(String string, boolean isInt) 
	{
		if(isInt)
			return "{\"" + string + "\", Int32.Type}";
		else
			return "{\"" + string + "\", type number}";
	}
	*/

}





