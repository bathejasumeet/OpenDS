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

package eu.opends.traffic;


/**
 * 
 * @author Rafael Math
 */
public class OpenDRIVECarData 
{
	private String name;
	private float mass;
	private float acceleration;
	private float decelerationBrake;
	private float decelerationFreeWheel;
	private boolean engineOn;
	private String modelPath;
	private boolean isSpeedLimitedToSteeringCar;
	private float distanceFromPath;
	private float maxSpeed;
	private String startRoadID;
	private int startLane;
	private double startS;
	
	
	public OpenDRIVECarData(String name, float mass, float acceleration,	float decelerationBrake, 
			float decelerationFreeWheel, boolean engineOn, String modelPath, boolean isSpeedLimitedToSteeringCar,
			Float distanceFromPath, Float maxSpeed, String startRoadID, Integer startLane, Double startS) 
	{
		this.name = name;
		this.mass = mass;
		this.acceleration = acceleration;
		this.decelerationBrake = decelerationBrake;
		this.decelerationFreeWheel = decelerationFreeWheel;
		this.engineOn = engineOn;
		this.modelPath = modelPath;
		this.isSpeedLimitedToSteeringCar = isSpeedLimitedToSteeringCar;
		this.distanceFromPath = distanceFromPath;
		this.maxSpeed = maxSpeed;
		this.startRoadID = startRoadID;
		this.startLane = startLane;
		this.startS = startS;
	}



	public String getName() {
		return name;
	}
	

	public float getMass() {
		return mass;
	}
	
	
	public void setMass(float mass)	{
		this.mass = mass;
	}


	public float getAcceleration() {
		return acceleration;
	}

	
	public void setAcceleration(float acceleration)	{
		this.acceleration = acceleration;
	}
	

	public float getDecelerationBrake() {
		return decelerationBrake;
	}

	
	public void setDecelerationBrake(float decelerationBrake) {
		this.decelerationBrake = decelerationBrake;
	}
	
	
	public float getDecelerationFreeWheel() {
		return decelerationFreeWheel;
	}

	
	public void setDecelerationFreeWheel(float decelerationFreeWheel) {
		this.decelerationFreeWheel = decelerationFreeWheel;
	}
	

	public boolean isEngineOn() {
		return engineOn;
	}

	
	public void setEngineOn(boolean engineOn) {
		this.engineOn = engineOn;
	}
	
	
	public String getModelPath() {
		return modelPath;
	}

	
	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}

	
	public boolean isSpeedLimitedToSteeringCar() {
		return isSpeedLimitedToSteeringCar;
	}
	

	public void setSpeedLimitedToSteeringCar(boolean isSpeedLimitedToSteeringCar) {
		this.isSpeedLimitedToSteeringCar = isSpeedLimitedToSteeringCar;
	}
	
	
	public float getDistanceFromPath() {
		return distanceFromPath;
	}

	
	public void setDistanceFromPath(float distanceFromPath) {
		this.distanceFromPath = distanceFromPath;
	}
	
	
	public float getMaxSpeed() {
		return maxSpeed;
	}

	
	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	
	
	public String getStartRoadID() {
		return startRoadID;
	}

	
	public void setStartRoadID(String startRoadID) {
		this.startRoadID = startRoadID;
	}
	
	
	public int getStartLane() {
		return startLane;
	}

	
	public void setStartLane(int startLane) {
		this.startLane = startLane;
	}
	
	
	public double getStartS() {
		return startS;
	}

	
	public void setStartS(double startS) {
		this.startS = startS;
	}

	
	public String toXML()
	{
		return "\t\t<vehicle id=\"" + name + "\">\n" +
			   "\t\t\t<modelPath>" + modelPath + "</modelPath>\n" + 
			   "\t\t\t<mass>"+ mass + "</mass>\n" + 
			   "\t\t\t<acceleration>"+ acceleration + "</acceleration>\n" + 
			   "\t\t\t<decelerationBrake>"+ decelerationBrake + "</decelerationBrake>\n" + 
			   "\t\t\t<decelerationFreeWheel>"+ decelerationFreeWheel + "</decelerationFreeWheel>\n" + 
			   "\t\t\t<maxSpeed>"+ maxSpeed + "</maxSpeed>\n" + 							  
			   "\t\t\t<engineOn>"+ engineOn + "</engineOn>\n" + 
			   "\t\t\t<distanceFromPath>"+ distanceFromPath + "</distanceFromPath>\n" + 
			   "\t\t\t<neverFasterThanSteeringCar>"+ isSpeedLimitedToSteeringCar + "</neverFasterThanSteeringCar>\n" + 
			   "\t\t\t<startRoadID>"+ startRoadID + "</startRoadID>\n" + 
			   "\t\t\t<startLane>"+ startLane + "</startLane>\n" + 
			   "\t\t\t<startS>"+ startS + "</startS>\n" + 
			   "\t\t</vehicle>";	
	}

}
