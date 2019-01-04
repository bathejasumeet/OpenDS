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

package eu.opends.car;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.jme3.collision.CollisionResult;

//import org.newdawn.slick.util.BufferedImageUtil;

import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.plugins.fbx.SceneLoader;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Image;
/*import com.jme3.scene.Spatial;
import com.jme3x.jfx.media.TextureMovie;
import com.jme3x.jfx.media.TextureMovie.LetterboxMode;*/
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapAxis;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.TextureArray;

import eu.opends.basics.SimulationBasics;
import eu.opends.car.LightTexturesContainer.TurnSignalState;
import eu.opends.codriver.util.DataStructures.Output_data_str;
import eu.opends.drivingTask.DrivingTask;
import eu.opends.drivingTask.scenario.ScenarioLoader;
import eu.opends.drivingTask.scenario.ScenarioLoader.CarProperty;
import eu.opends.drivingTask.settings.SettingsLoader;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.effects.EffectCenter;
import eu.opends.effects.RainSettings;
import eu.opends.environment.Crosswind;
import eu.opends.environment.TrafficLightCenter;
import eu.opends.infrastructure.Segment;
import eu.opends.infrastructure.Waypoint;
import eu.opends.environment.TrafficLightCenter.TriggerType;
//import eu.opends.eyetracker.EyetrackerCenter.ColorMode;
import eu.opends.input.KeyMapping;
import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;
import eu.opends.simphynity.SimphynityController;
import eu.opends.tools.PanelCenter;
import eu.opends.tools.Util;
import eu.opends.traffic.FollowBox;
import eu.opends.traffic.FollowBoxSettings;
import eu.opends.traffic.PhysicalTraffic;
import eu.opends.traffic.TrafficObject;
//import eu.opends.traffic.Waypoint;
import eu.opends.trafficObjectLocator.TrafficObjectLocator;



/**
 * Driving Car
 * 
 * @author Rafael Math
 */
public class SteeringCar extends Car implements TrafficObject
{
	// minimum steering percentage to be reached for switching off the turn signal automatically
	// when moving steering wheel back towards neutral position
	private float turnSignalThreshold = 0.25f;
	
    private TrafficObjectLocator trafficObjectLocator;
    private boolean handBrakeApplied = false;
    
    // Simphynity Motion Seat
    private SimphynityController simphynityController;
    
    // adaptive cruise control
	private boolean isAdaptiveCruiseControl = false;
	private float minLateralSafetyDistance;
	private float minForwardSafetyDistance;
	private float emergencyBrakeDistance;
	private boolean suppressDeactivationByBrake = false;
	private float distInFront;
	private boolean whipersOn = false;
	private boolean direction = true;
	private Geometry gazeSphere;
	private ColorRGBA crossHairsColor = ColorRGBA.White;
	private ColorRGBA sphereColor = ColorRGBA.Red;
	
	private float updateRate;
	private float distanceToCarInFront;
	private float timeToCollide;
	// crosswind (will influence steering angle)
	private Crosswind crosswind = new Crosswind("left", 0, 0);
	private float angle;
	
	private FollowBox followBox;
	
	private Boolean isAutoPilot;
	
	private HashMap<String,Float> frictionMap;

	private RadarSensor radarSensor;

	float tpfCtr=0f;
	float tpfCtr2 =0f;
	int TimeHeadway=0;
	
	private boolean isRainEffect;
	
	Vector3f rot_point = new Vector3f(2.0f, 2.0f, 2.0f);
    float angleST = 0;
    Quaternion initialPositionSteering = new Quaternion();
    Quaternion rotationSteering = new Quaternion();
    
    Quaternion initialPositionWhipersLeft = new Quaternion();
    Quaternion rotationWhipersLeft = new Quaternion();
    
    private BitmapText crosshairs;
    private float scalingFactor = 2;
    float angleSteering;
    //float tpfCtr = 0f;
	//private RadarSensor radarSensor;
	
    public float getDistanceToCar() {
    	return distanceToCarInFront;
    }
    
    public float getTimeToCollide() {
    	return timeToCollide;
    }
    
    public float getHeadwayTime() {
    	return timeAheadDiff;
    }
    
    public void setWhipersState(boolean value){
    	whipersOn = value;
    }
    
    private TexturedMovie movie;
    private TexturedMovie movieSideL;
    private TexturedMovie movieSideR;
    
    
    RainSettings rainSettings;
    
    public Boolean getWhipersState(){
    	return whipersOn;
    }
    
    private long lastUpdateTime = System.currentTimeMillis();
    
    //List<Image> images;
    
    //TextureArray textureArray;
    
    public TexturedMovie getMovie(){
    	return movie;
    }
    
    public float getDistance(){
    	return distInFront;
    }
    
    public TexturedMovie getMovieSideL(){
    	return movieSideL;
    }
    
    public TexturedMovie getMovieSideR(){
    	return movieSideR;
    }
    
    private static float longitudeDistance;
    
    //ArrayList<Texture> textureArray = new ArrayList<Texture>(directory.listFiles().length);
    
    LinkedList<Texture> textureArray = new LinkedList<Texture>();
    
    //Vector3f coorOfFrontPoint = new Vector3f(0f, 0f, 0f);
    
    private int imageArrayCount = 0;
    
    Boolean lefthandSideSteering;
    
    public static int timeAheadPositionCount = 0;
    long timeAheadPositionTime;
    Geometry geomTimeAhead;
    long timeAheadPositionTime2;
    Boolean positionAdded = false;
    float timeAheadDiff=0f;
    
    Date curDate;
    Box b;
    //Node timeAheadNode;
    Spatial bridge;
    int captured;
	@SuppressWarnings("unused")
	public SteeringCar(Simulator sim) 
	{		
		this.sim = sim;
		
		DrivingTask drivingTask = SimulationBasics.getDrivingTask();
		ScenarioLoader scenarioLoader = drivingTask.getScenarioLoader();
		
		initialPosition = scenarioLoader.getStartLocation();
		if(initialPosition == null)
			initialPosition = SimulationDefaults.initialCarPosition;
		
		this.initialRotation = scenarioLoader.getStartRotation();
		if(this.initialRotation == null)
			this.initialRotation = SimulationDefaults.initialCarRotation;
			
		// add start position as reset position
		Simulator.getResetPositionList().add(new ResetPosition(initialPosition,initialRotation));
		
		mass = scenarioLoader.getChassisMass();
		
		minSpeed = scenarioLoader.getCarProperty(CarProperty.engine_minSpeed, SimulationDefaults.engine_minSpeed);
		maxSpeed = scenarioLoader.getCarProperty(CarProperty.engine_maxSpeed, SimulationDefaults.engine_maxSpeed);
			
		decelerationBrake = scenarioLoader.getCarProperty(CarProperty.brake_decelerationBrake, 
				SimulationDefaults.brake_decelerationBrake);
		maxBrakeForce = 0.004375f * decelerationBrake * mass;
		
		decelerationFreeWheel = scenarioLoader.getCarProperty(CarProperty.brake_decelerationFreeWheel, 
				SimulationDefaults.brake_decelerationFreeWheel);
		maxFreeWheelBrakeForce = 0.004375f * decelerationFreeWheel * mass;
		
		engineOn = scenarioLoader.getCarProperty(CarProperty.engine_engineOn, SimulationDefaults.engine_engineOn);
		if(!engineOn)
			showEngineStatusMessage(engineOn);
		
		frictionMap = scenarioLoader.getFrictionMap();
		
		Float lightIntensityObj = scenarioLoader.getCarProperty(CarProperty.light_intensity, SimulationDefaults.light_intensity);
		if(lightIntensityObj != null)
			lightIntensity = lightIntensityObj;
		
		transmission = new Transmission(this);
		powerTrain = new PowerTrain(this);
		
		modelPath = scenarioLoader.getModelPath();
		
		init(this.getName());
		//init();

        // allows to place objects at current position
        trafficObjectLocator = new TrafficObjectLocator(sim, this);
        
        // load settings of adaptive cruise control
        isAdaptiveCruiseControl = scenarioLoader.getCarProperty(CarProperty.cruiseControl_acc, SimulationDefaults.cruiseControl_acc);
    	minLateralSafetyDistance = scenarioLoader.getCarProperty(CarProperty.cruiseControl_safetyDistance_lateral, SimulationDefaults.cruiseControl_safetyDistance_lateral);
    	minForwardSafetyDistance = scenarioLoader.getCarProperty(CarProperty.cruiseControl_safetyDistance_forward, SimulationDefaults.cruiseControl_safetyDistance_forward);
    	emergencyBrakeDistance = scenarioLoader.getCarProperty(CarProperty.cruiseControl_emergencyBrakeDistance, SimulationDefaults.cruiseControl_emergencyBrakeDistance);
    	suppressDeactivationByBrake = scenarioLoader.getCarProperty(CarProperty.cruiseControl_suppressDeactivationByBrake, SimulationDefaults.cruiseControl_suppressDeactivationByBrake);
    	
    	// if initialSpeed > 0 --> cruise control will be on at startup
    	targetSpeedCruiseControl = scenarioLoader.getCarProperty(CarProperty.cruiseControl_initialSpeed, SimulationDefaults.cruiseControl_initialSpeed);
		isCruiseControl = (targetSpeedCruiseControl > 0);
    	
		SettingsLoader settingsLoader = SimulationBasics.getSettingsLoader();
        if(settingsLoader.getSetting(Setting.Simphynity_enableConnection, SimulationDefaults.Simphynity_enableConnection))
		{
        	String ip = settingsLoader.getSetting(Setting.Simphynity_ip, SimulationDefaults.Simphynity_ip);
			if(ip == null || ip.isEmpty())
				ip = "127.0.0.1";
			int port = settingsLoader.getSetting(Setting.Simphynity_port, SimulationDefaults.Simphynity_port);
			
	    	simphynityController = new SimphynityController(sim, this, ip, port);
		}
        
        // AutoPilot **************************************************************	
        FollowBoxSettings followBoxSettings = scenarioLoader.getAutoPilotFollowBoxSettings(false);
        isAutoPilot = scenarioLoader.isAutoPilot();
        if(isAutoPilot != null)
			followBox = new FollowBox(sim, this, followBoxSettings, isAutoPilot, true);
        // AutoPilot **************************************************************
        
        //activate raining effect here
        
        rainSettings = scenarioLoader.getRainSettings();
        //final File directory = new File("E://OpenDS/GitJakarta_13_02_17/opends/OpenDS4.0/assets/Textures/WindschieldV5");
        isRainEffect = rainSettings.getStatus();
        lefthandSideSteering = Simulator.getDrivingTask().getScenarioLoader().getCarProperty(CarProperty.steeringWheel_left, SimulationDefaults.leftHandSideSteering);
        
        if (rainSettings.getStatus()){
        	final File directory = new File(rainSettings.getPath());
	        if (directory.isDirectory()){
	        	for (final File f : directory.listFiles()){       		
	    			Texture img = sim.getAssetManager().loadTexture(f.getParentFile().getParentFile().getName()+"/"+f.getParentFile().getName()+"/"+f.getName()); 
					img.setWrap(Texture.WrapMode.Repeat);
	        		if (img != null){
	        			textureArray.add(img);
	        		}
	        		else{
	        			System.out.println("Nothing added");
	        		}
	        		
	        		System.out.println("Image = " + f.getName() + " read");
	        	}
	        }
	        System.out.println("Files read = " + textureArray.size());
	        
	       try {

	        	Spatial glassToAdjust = carModel.getGlassFrontRainLayer();
	        	movie = new TexturedMovie(sim, glassToAdjust, textureArray);
	        	
	        	Spatial glassToAdjustSideL = carModel.getGlassSideL();
	        	movieSideL = new TexturedMovie(sim, glassToAdjustSideL, textureArray);
	        	
	        	Spatial glassToAdjustSideR = carModel.getGlassSideR();
	        	movieSideR = new TexturedMovie(sim, glassToAdjustSideR, textureArray);
	        }
	        catch (Exception e){
	        	System.out.println(e);
	        }
	        
        }
        
		BitmapFont guiFont = sim.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
		crosshairs = new BitmapText(guiFont, false);
		crosshairs.setSize(guiFont.getCharSet().getRenderedSize() * scalingFactor);
		crosshairs.setText("+");
		crosshairs.setColor(crossHairsColor);
			

		
		// init a colored sphere to mark the target
		Sphere sphere = new Sphere(30, 30, 0.2f);
		gazeSphere = new Geometry("gazeSphere", sphere);
		Material sphere_mat = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		sphere_mat.setColor("Color", sphereColor);
		gazeSphere.setMaterial(sphere_mat);
		
		curDate = new Date();
        
		radarSensor = new RadarSensor(sim, carNode);
		
	}


	public TrafficObjectLocator getObjectLocator()
	{
		return trafficObjectLocator;
	}
	
	
	public boolean isHandBrakeApplied()
	{
		return handBrakeApplied;
	}
	
	
	public void applyHandBrake(boolean applied)
	{
		handBrakeApplied = applied;
	}

	
	// start applying crosswind and return to 0 (computed in update loop)
	public void setupCrosswind(String direction, float force, int duration)
	{
		crosswind = new Crosswind(direction, force, duration);
	}
	
	
	Vector3f lastVelocity = new Vector3f(0,0,0);
	long m_nLastChangeTime = 0;
	
	public void setAutoPilot(Boolean isAutoPilot)
	{
		if(followBox == null || this.isAutoPilot == isAutoPilot)
			return;
		
		this.isAutoPilot = isAutoPilot;
		if(!isAutoPilot)
		{
			steer(0);
			brakePedalIntensity = 0;
			acceleratorPedalIntensity = 0;
			PanelCenter.getMessageBox().addMessage("Auto Pilot off", 3);
			Simulator.getDrivingTaskLogger().reportText("Auto Pilot off", new Date());
		}
		else
		{
			PanelCenter.getMessageBox().addMessage("Auto Pilot on", 3);
			Simulator.getDrivingTaskLogger().reportText("Auto Pilot on", new Date());
		}
	}
	
	public boolean isAutoPilot()
	{
		return isAutoPilot;
	}
	
	private int previousManoeuvreMsgID = 0;
	// will be called, in every frame
	@Override
	public void update(float tpf, ArrayList<TrafficObject> vehicleList)
	{
		Output_data_str manoeuvreMsg = sim.getCodriverConnector().getLatestManoeuvreMsg();

		if(manoeuvreMsg != null)

		{
			long manoeuvreMsgTimestamp = sim.getCodriverConnector().getLatestManoeuvreMsgTimestamp();
			int manoeuvreMsgID = sim.getCodriverConnector().getLatestManoeuvreMsgID();
			
			if(manoeuvreMsgID > previousManoeuvreMsgID)	
			{
				computeAcceleration(tpf, manoeuvreMsg, manoeuvreMsgTimestamp, manoeuvreMsgID);
				//computeSteering(manoeuvreMsg, manoeuvreMsgTimestamp);
				previousManoeuvreMsgID = manoeuvreMsgID;
			}
			else
			{
				usePreviousAcceleration();
			}
		}
		else
		{
			//System.err.println(getHeadingDegree());
			// AutoPilot **************************************************************
			if(followBox!= null)
			{
				// update movement of follow box according to vehicle's position
				Vector3f vehicleCenterPos = centerGeometry.getWorldTranslation();
				followBox.update(tpf, vehicleCenterPos);
				
				if(!sim.isPause() && isAutoPilot)
				{
					// update steering
					Vector3f wayPoint = followBox.getPosition();
					steerTowardsPosition(tpf, wayPoint);
					
					// update speed
					updateSpeed(tpf, vehicleList);
				}		
			}
			// AutoPilot **************************************************************	
			if(isAutoPilot == null || !isAutoPilot)
			{
				// accelerate
				float pAccel = 0;
				if(!engineOn)
				{
					// apply 0 acceleration when engine not running
					pAccel = powerTrain.getPAccel(tpf, 0) * 30f;
				}
				else if(isAutoAcceleration && (getCurrentSpeedKmh() < minSpeed))
				{
					// apply maximum acceleration (= -1 for forward) to maintain minimum speed
					pAccel = powerTrain.getPAccel(tpf, -1) * 30f;
				}
				else if(isCruiseControl && (getCurrentSpeedKmh() < targetSpeedCruiseControl))
				{
					// apply maximum acceleration (= -1 for forward) to maintain target speed
					pAccel = powerTrain.getPAccel(tpf, -1) * 30f;
			
					if(isAdaptiveCruiseControl)
					{
						// lower speed if leading car is getting to close
						pAccel = getAdaptivePAccel(pAccel);
					}
				}
				else
				{
					// apply acceleration according to gas pedal state
					pAccel = powerTrain.getPAccel(tpf, acceleratorPedalIntensity) * 30f;
				}
				transmission.performAcceleration(pAccel);
			
				// brake lights
				setBrakeLight(brakePedalIntensity > 0);
				
				if(handBrakeApplied)
				{
					// hand brake
					carControl.brake(maxBrakeForce);
					PanelCenter.setHandBrakeIndicator(true);
				}
				else
				{
					// brake	
					float appliedBrakeForce = brakePedalIntensity * maxBrakeForce;
					float currentFriction = powerTrain.getFrictionCoefficient() * maxFreeWheelBrakeForce;
					carControl.brake(appliedBrakeForce + currentFriction);
					PanelCenter.setHandBrakeIndicator(false);
				}
			}
		
			// lights
			leftHeadLight.setColor(ColorRGBA.White.mult(lightIntensity));
	        leftHeadLight.setPosition(carModel.getLeftLightPosition());
	        leftHeadLight.setDirection(carModel.getLeftLightDirection());
	        
	        rightHeadLight.setColor(ColorRGBA.White.mult(lightIntensity));
	        rightHeadLight.setPosition(carModel.getRightLightPosition());
	        rightHeadLight.setDirection(carModel.getRightLightDirection());
	        
	        // cruise control indicator
	        if(isCruiseControl)
	        	PanelCenter.setCruiseControlIndicator(targetSpeedCruiseControl);
	        else
	        	PanelCenter.unsetCruiseControlIndicator();
	        
        	trafficObjectLocator.update();
        
	        // switch off turn signal after turn        
	        if(hasFinishedTurn())
	        {
	        	lightTexturesContainer.setTurnSignal(TurnSignalState.OFF);
	        }
        
	        lightTexturesContainer.update();
	        
			steeringInfluenceByCrosswind = crosswind.getCurrentSteeringInfluence();
			
			if(!frictionMap.isEmpty())
				updateFrictionSlip();
	        
	        //updateWheel();
	        
			radarSensor.update();

	        if(simphynityController != null)
	        	simphynityController.update();
			    //simphynityController.updateNervtehInstructions();
        
        	Boolean shadowModelActive = SimulationBasics.getSettingsLoader().getSetting(Setting.HighPolygon_carModel, SimulationDefaults.HighPolygon_carModel);

	        try {
		        if (shadowModelActive){
		        	//angleSteering += tpf;
		        	//angleSteering = angleSteering % FastMath.TWO_PI ;
			        Node steeringWheel = Util.findNode(carNode, "SteeringWheel");
			        Node whipersLeft = Util.findNode(carNode, "whipersLeft");
			        Node whipersRight = Util.findNode(carNode, "whipersRight");
			        Geometry glass = Util.findGeom(carNode, "windowglassFront");
	        
			        float currentPosition = getSteeringWheelStateNoNoise(sim.getCar().getSteeringWheelState());
			
			        //System.out.println("Wheel state" + sim.getCar().getSteeringWheelState());
			        if (currentPosition == 0.0f){
			        	steeringWheel.setLocalRotation(initialPositionSteering);
			        	
			        }
			        else
			        {
			        	float angleST = sim.getCar().getSteeringWheelState()*2 ;
			        	float angleST2 = FastMath.sin(angleST);
			        	rotationSteering.fromAngleAxis(angleST2, new Vector3f(0.0f, 1f, 0.0f ));
			        	steeringWheel.setLocalRotation(rotationSteering);
			        }
			        
			        sim.getEffectCenter();
					// separate loop is required for whipers
			        if (EffectCenter.getRainingPercentage() > 0){
	
			        	float multiplier;
			        	@SuppressWarnings("static-access")
						float intensity = Math.max(sim.getEffectCenter().getRainingPercentage(), sim.getEffectCenter().getSnowingPercentage());
			        	if (intensity != 0 && intensity <= 30 ) 
			        		multiplier = intensity*0.1f;
			        	else if (intensity != 0 && intensity > 30)
			        		multiplier = 3f;
			        	else
			        		multiplier = 1f;
			        	if (lefthandSideSteering){
			        		angle += tpf*multiplier;
			        	} else { 
			        		angle -= tpf*multiplier;
			        	}
			        	
			         	angle = angle % FastMath.PI ;
			        	float angle2 = FastMath.sin(angle);
	
			        	rotationWhipersLeft.fromAngleAxis(angle2*1.5f, new Vector3f(0f, -1f, 0.7f ));
	
		        		whipersLeft.setLocalRotation(rotationWhipersLeft);
		        		
		        		whipersRight.setLocalRotation(rotationWhipersLeft);
		        		
		        		//if (isRainEffect){
		        			//System.out.println("Entering the loop with textures");
				        	if (System.currentTimeMillis() > lastUpdateTime + (int)Math.min(rainSettings.getFPS() - sim.getCar().getCurrentSpeedMs(),0.0)){
				        		//System.out.println("Current speed is = " + sim.getCar().getCurrentSpeedMs());
				        		movie.applyTexture();
				        		movieSideL.applyTexture();
				        		movieSideR.applyTexture();
				        		lastUpdateTime = System.currentTimeMillis();
					        }
		        		//}
			        }
		        }
	        } catch (Exception e){
	        	//e.printStackTrace();
	        	System.out.println("Error =" + e);
	        }
		}
	}
	
	float lastAccelerationForce = 0;
	float lastBrakeForce = 0;
	float lastSteer = 0;
	double lastUpdate = 0;
	private void usePreviousAcceleration()
	{
		if(sim.getBulletAppState().getElapsedSecondsSinceStart() - lastUpdate > 0.009)
		{
			carControl.accelerate(lastAccelerationForce);
			carControl.brake(lastBrakeForce);
			carControl.steer(lastSteer);
			//System.err.println("Updated pysics at: " + System.currentTimeMillis());
			lastUpdate = sim.getBulletAppState().getElapsedSecondsSinceStart();
		}
	}	


	private long prevMS = 0;
	private float prevSpeed = 0;
	
	private double previousAcc = 0;
    private double previousAngle = 0;
    private void computeAcceleration(float tpf, Output_data_str manoeuvreMsg, long manoeuvreMsgTimestamp, int ID) //TODO
    {
    	float accelerationForce = 0;
    	float brakeForce = 0.2f * maxFreeWheelBrakeForce; // = default friction value
    	double currentAngle = 0;
    	
    	// if manoeuvre message is older than 1 second --> return 0 acceleration
    	if(System.currentTimeMillis()-manoeuvreMsgTimestamp <= 1000)
    	{
	    	double J0f = manoeuvreMsg.J0f;
	    	double S0f = manoeuvreMsg.S0f;
	    	double Cr0f = manoeuvreMsg.Cr0f;
	    	double ts = 0.05;
	    	
	    	double Jreq = J0f + S0f * ts + 0.5 * Cr0f * ts * ts;
	    	
	    	if(getCurrentSpeedMs() <= 0.01f)
	    		previousAcc = 0;
	    		
	    	double currentAcc = previousAcc + (ts * Jreq);
	    	currentAcc = Math.max(Math.min(currentAcc, 1.0), -3.5);
	    	
	    	previousAcc = currentAcc;
		    	
			if(currentAcc > 0)
				accelerationForce = (float) -currentAcc * (mass/3.85f) - 70;
			else if(currentAcc < 0)
				brakeForce = -2.0f * (float) currentAcc;
			
			
			
	    	double Jdelta0f = manoeuvreMsg.Jdelta0f;
	    	double Sdelta0f = manoeuvreMsg.Sdelta0f;
	    	double Crdelta0f = manoeuvreMsg.Crdelta0f;
	    	double Ksteer = 46.3; //145;
	    	float VlgtFild = getCurrentSpeedMs();
	    	
	    	double Jdeltareq = Jdelta0f + Sdelta0f * ts + 0.5 * Crdelta0f * ts * ts;
	    	
	    	double Jlat = (Jdeltareq*Ksteer) / (VlgtFild*VlgtFild);
	    	
	    	//deltasw(n + 1) = deltasw(n) + ts * Jlat
	    	currentAngle = previousAngle + (ts * Jlat);
	    	currentAngle = Math.max(Math.min(currentAngle, 4.0*Math.PI), -4.0*Math.PI);
	    	
	    	previousAngle = currentAngle;
	    	
    	}
    	
    	PanelCenter.setFixRPM(1000);
    	lastAccelerationForce = accelerationForce;
    	carControl.accelerate(accelerationForce);
    	lastBrakeForce = brakeForce;
    	carControl.brake(brakeForce);

    	float steer = (float)currentAngle/(5.5555555f*FastMath.PI);
    	lastSteer = steer;
    	steer(steer);
    	
    	
    	
	}
	private float getSteeringWheelStateNoNoise(float currentValue){
		if  ( currentValue > -0.003f && currentValue < 0.003f )
		{	
			return 0.0f;
		}
		else {
			return currentValue;
		}
	}

    float leftWheelsPos = 2.2f;
    float backAxleHeight = -3.0f;
    float backAxlePos = 2.45f;
    long prevTime = 0;
	private void updateWheel() 
	{     
		long time = System.currentTimeMillis();
		if(time - prevTime > 1000)
		{/*
			Vector3f wheelDirection = new Vector3f(0, -1, 0);
			Vector3f wheelAxle = new Vector3f(-1, 0, 0);
			float wheelRadius = 0.5f;
			float suspensionLenght = 0.2f;
		
			carControl.removeWheel(3);
		
			backAxlePos += 0.05f;
		
			// add back left wheel
			Geometry geom_wheel_fl = Util.findGeom(carNode, "WheelBackLeft");
			geom_wheel_fl.setLocalScale(wheelRadius*2);
			geom_wheel_fl.center();
			BoundingBox box = (BoundingBox) geom_wheel_fl.getModelBound();
			carControl.addWheel(geom_wheel_fl.getParent(), 
        		box.getCenter().add(leftWheelsPos, backAxleHeight, backAxlePos),
                wheelDirection, wheelAxle, suspensionLenght, wheelRadius, true);

			System.out.println("backAxlePos: " + backAxlePos);
			
			prevTime = time;
			*/
		}
		//System.out.println("prevTime: " + prevTime + "  time: " + time);
	}


	private void updateFrictionSlip() 
	{
		for(int i=0; i<carControl.getNumWheels(); i++)
		{
			float friction = getWheelFriction(i);
			carControl.setFrictionSlip(i, friction);
		}
	}
	
	private float getWheelFriction(int wheel)
	{		
		float friction = carModel.getDefaultFrictionSlip();
		
		// cast ray downwards to find geometry at
		Vector3f collisionLocation = carControl.getBulletWheel(wheel).getCollisionLocation();
		Vector3f wheelLocation = new Vector3f();
		carControl.getBulletWheel(wheel).getWheelWorldLocation(wheelLocation);
		Vector3f direction = collisionLocation.subtract(wheelLocation);
		direction.normalizeLocal();
		Ray ray = new Ray(wheelLocation, direction);
		CollisionResults results = new CollisionResults();
		sim.getSceneNode().collideWith(ray, results); 

		if (results.size() > 0) 
		{
			float distance = 1000;
			Geometry geometry = null;
			
			// get geometry with shortest distance to wheel
			for(int k=0; k< results.size(); k++)
			{			
				if(!results.getCollision(k).getGeometry().hasAncestor(carNode) && 
					results.getCollision(k).getDistance() < distance)
				{
					distance = results.getCollision(k).getDistance();
					geometry = results.getCollision(k).getGeometry();	
				}
			}
			
			// look up friction value of respective geometry
			if(geometry!=null && frictionMap.containsKey(geometry.getName()))
			{
				friction = frictionMap.get(geometry.getName());
				//System.err.println("Wheel" + wheel + ": " + geometry.getName() + "  -->  " + friction);
			}
		}
		
		return friction;
	}

	private boolean hasStartedTurning = false;
	private boolean hasFinishedTurn() 
	{
		TurnSignalState turnSignalState = lightTexturesContainer.getTurnSignal();
		float steeringWheelState = getSteeringWheelState();
		
		if(turnSignalState == TurnSignalState.LEFT)
		{
			if(steeringWheelState > turnSignalThreshold)
				hasStartedTurning = true;
			else if(hasStartedTurning)
			{
				hasStartedTurning = false;
				return true;
			}
		}
		
		if(turnSignalState == TurnSignalState.RIGHT)
		{
			if(steeringWheelState < -turnSignalThreshold)
				hasStartedTurning = true;
			else if(hasStartedTurning)
			{
				hasStartedTurning = false;
				return true;
			}
		}
		
		return false;
	}


	// Adaptive Cruise Control ***************************************************	
	
	private float getAdaptivePAccel(float pAccel)
	{
		brakePedalIntensity = 0f;

		// check distance from traffic vehicles
		for(TrafficObject vehicle : PhysicalTraffic.getTrafficObjectList())
		{
			if(belowSafetyDistance(vehicle.getPosition()))
			{
				pAccel = 0;
			
				if(vehicle.getPosition().distance(getPosition()) < emergencyBrakeDistance)
					brakePedalIntensity = 1f;
			}
		}
		
		return pAccel;
	}

	
	private boolean belowSafetyDistance(Vector3f obstaclePos) 
	{	
		float distance = obstaclePos.distance(getPosition());
		
		// angle between driving direction of traffic car and direction towards obstacle
		// (consider 3D space, because obstacle could be located on a bridge above traffic car)
		Vector3f carFrontPos = frontGeometry.getWorldTranslation();
		Vector3f carCenterPos = centerGeometry.getWorldTranslation();
		float angle = Util.getAngleBetweenPoints(carFrontPos, carCenterPos, obstaclePos, false);
		
		float lateralDistance = distance * FastMath.sin(angle);
		float forwardDistance = distance * FastMath.cos(angle);
		
		if((lateralDistance < minLateralSafetyDistance) && (forwardDistance > 0) && 
				(forwardDistance < Math.max(0.5f * getCurrentSpeedKmh(), minForwardSafetyDistance)))
		{
			return true;
		}
		
		return false;
	}

	public void getLateralDistance(Vector3f metalWall){
		float distance = metalWall.distance(getPosition());
		Vector3f carFrontPos = frontGeometry.getWorldTranslation();
		Vector3f carCenterPos = centerGeometry.getWorldTranslation();
		float angle = Util.getAngleBetweenPoints(carFrontPos, carCenterPos, metalWall, false);
		float lateralDistance = distance * FastMath.sin(angle);
		longitudeDistance = distance * FastMath.cos(angle);
		System.out.println("Lateral distance to wall = " + lateralDistance);
		System.out.println("Longitude distance = " + longitudeDistance);
	}
	
	public float getLongitudeDistance(){
		return longitudeDistance;
	}

	public void increaseCruiseControl(float diff) 
	{
		targetSpeedCruiseControl = Math.min(targetSpeedCruiseControl + diff, 260.0f);	
	}


	public void decreaseCruiseControl(float diff) 
	{
		targetSpeedCruiseControl = Math.max(targetSpeedCruiseControl - diff, 0.0f);
	}

	
	public void disableCruiseControlByBrake() 
	{
		if(!suppressDeactivationByBrake)
			setCruiseControl(false);
	}
	// Adaptive Cruise Control ***************************************************


	
	public float getDistanceToRoadSurface() 
	{
		// reset collision results list
		CollisionResults results = new CollisionResults();

		// aim a ray from the car's center downwards to the road surface
		Ray ray = new Ray(getPosition(), Vector3f.UNIT_Y.mult(-1));

		// collect intersections between ray and scene elements in results list.
		sim.getSceneNode().collideWith(ray, results);
		
		// return the result
		for (int i = 0; i < results.size(); i++) 
		{
			// for each hit, we know distance, contact point, name of geometry.
			float dist = results.getCollision(i).getDistance();
			Geometry geometry = results.getCollision(i).getGeometry();

			if(geometry.getName().contains("CityEngineTerrainMate"))
				return dist - 0.07f;
		}
		
		return -1;
	}
	
	
	
	@SuppressWarnings("static-access")
	public float getDistanceToCarInFront(float tpf) 
	{
		Vector3f carFrontPos = frontGeometryRadarVector.getWorldTranslation();
		Vector3f carCenterPos = frontGeometryRadar.getWorldTranslation();
		//System.out.println("Front position =" + carFrontPos);
		//System.out.println("Center position =" + carCenterPos);
	

		CollisionResults results = new CollisionResults();
		CollisionResults resultsFiltered = new CollisionResults();
		
		Vector3f worldPosFar = carFrontPos;

		// compute the world position on the near plane
		Vector3f worldPosNear = carCenterPos;
			
		// compute direction towards target (from camera)
		Vector3f direction = worldPosFar.subtract(worldPosNear);

		// normalize direction vector
		direction.normalizeLocal();		
		
		
		// aim a ray from the car's center downwards to the road surface
		Ray ray = new Ray(carFrontPos, direction);

		// collect intersections between ray and scene elements in results list.
		sim.getSceneNode().collideWith(ray, results);
		//sim.getCar().getCarNode().collideWith(ray, results);
		
		
		// return the result
		for (int i = 0; i < results.size(); i++) 
		{
			if (results.getCollision(i).getGeometry().getName().contains("Chassis")){
			// for each hit, we know distance, contact point, name of geometry.
				float dist = results.getCollision(i).getDistance();
				Vector3f contactPoint = results.getCollision(i).getContactPoint();
				Geometry geometry = results.getCollision(i).getGeometry();
				

				
				resultsFiltered.addCollision(results.getCollision(i));
		
			}
		}
		
		
		// use the results (we mark the hit object)
		if (resultsFiltered.size() > 0) 
		{
			// the closest collision point is what was truly hit
			CollisionResult closest = resultsFiltered.getClosestCollision();
			distInFront = closest.getDistance();
			closest.getGeometry().getMaterial().setColor("GlowColor", new ColorRGBA(1, 1, 0, 0.5f));
			
			bridge = (Geometry) closest.getGeometry().getParent().getParent().getParent().getParent().getParent().getChild("frontBoxRadarVector");
			
			
			String nameOfCarAhead = closest.getGeometry().getParent().getParent().getParent().getParent().getParent().getName();
			float carAheadSpeed =0.0f;
			//float timeToCollide;
			for (int i=0; i<sim.getPhysicalTraffic().getTrafficObjectList().size();i++) {
				if (nameOfCarAhead.equals(sim.getPhysicalTraffic().getTrafficObjectList().get(i).getName())) {
					//System.out.println("Name of all traffic vehicles "+ sim.getPhysicalTraffic().getTrafficObjectList().get(i).getName());
					//System.out.println("Current speed pf car ahead is =" +((Car)sim.getPhysicalTraffic().getTrafficObjectList().get(i)).getCurrentSpeedKmh());
					carAheadSpeed = ((Car)sim.getPhysicalTraffic().getTrafficObjectList().get(i)).getCurrentSpeedKmh();
					
				}
			}
			
			float steeringCarSpeed = getCurrentSpeedKmh();
			float differenceInSpeed;
			if (carAheadSpeed>steeringCarSpeed) {
				//System.out.println("No collision possible");
				timeToCollide = 0.0f;
			}
			else if (carAheadSpeed<steeringCarSpeed && steeringCarSpeed > 0.15f && carAheadSpeed > 0.15f) {
				differenceInSpeed = ((steeringCarSpeed - carAheadSpeed)*1000f)/3600f;
				timeToCollide = distInFront/differenceInSpeed;
				//System.out.println("Time to collide with car in front = " + timeToCollide);
			}
			else {
				//System.out.println("Looks like both cars are not mooving");
				timeToCollide = 0.0f;
			}
			
			
			
			
			return distInFront;
		}
		return 0.0f;
		
	}
	
	
	
	// AutoPilot *****************************************************************
	
	private void steerTowardsPosition(float tpf, Vector3f wayPoint) 
	{
		// get relative position of way point --> steering direction
		// -1: way point is located on the left side of the vehicle
		//  0: way point is located in driving direction 
		//  1: way point is located on the right side of the vehicle
		int steeringDirection = getRelativePosition(wayPoint);
		
		// get angle between driving direction and way point direction --> steering intensity
		// only consider 2D space (projection of WPs to xz-plane)
		Vector3f carFrontPos = frontGeometry.getWorldTranslation();
		Vector3f carCenterPos = centerGeometry.getWorldTranslation();
		float steeringAngle = Util.getAngleBetweenPoints(carFrontPos, carCenterPos, wayPoint, true);
		
		// compute steering intensity in percent
		//  0     degree =   0%
		//  11.25 degree =  50%
		//  22.5  degree = 100%
		// >22.5  degree = 100%
		float steeringIntensity = Math.max(Math.min(4*steeringAngle/FastMath.PI,1f),0f);
		
		// apply steering instruction
		steer(/*smooth(tpf,*/ steeringDirection*steeringIntensity/*)*/);
		
		//System.out.println(steeringDirection*steeringIntensity);
	}


	private float previousSteeringInstruction = 0;
	private float smooth(float tpf, float currentSteeringInstruction)
	{
		float maxAngle = 20f * FastMath.DEG_TO_RAD * tpf;
		
		if(FastMath.abs(currentSteeringInstruction - previousSteeringInstruction) < maxAngle)
			previousSteeringInstruction = currentSteeringInstruction;
		else if(currentSteeringInstruction > previousSteeringInstruction)
			previousSteeringInstruction += maxAngle;
		else
			previousSteeringInstruction -= maxAngle;
		
		return previousSteeringInstruction;
	}

	private void steerTowardsPosition(Vector3f wayPoint) 
	{
		// get relative position of way point --> steering direction
		// -1: way point is located on the left side of the vehicle
		//  0: way point is located in driving direction 
		//  1: way point is located on the right side of the vehicle
		int steeringDirection = getRelativePosition(wayPoint);
		
		// get angle between driving direction and way point direction --> steering intensity
		// only consider 2D space (projection of WPs to xz-plane)
		Vector3f carFrontPos = frontGeometry.getWorldTranslation();
		Vector3f carCenterPos = centerGeometry.getWorldTranslation();
		float steeringAngle = Util.getAngleBetweenPoints(carFrontPos, carCenterPos, wayPoint, true);
		
		// compute steering intensity in percent
		//  0     degree =   0%
		//  11.25 degree =  50%
		//  22.5  degree = 100%
		// >22.5  degree = 100%
		float steeringIntensity = Math.max(Math.min(4*steeringAngle/FastMath.PI,1f),0f);
		
		// apply steering instruction
		steer(steeringDirection*steeringIntensity);
		
		//System.out.println(steeringDirection*steeringIntensity);
	}

	
	private int getRelativePosition(Vector3f wayPoint)
	{
		// get vehicles center point and point in driving direction
		Vector3f frontPosition = frontGeometry.getWorldTranslation();
		Vector3f centerPosition = centerGeometry.getWorldTranslation();
		
		// convert Vector3f to Point2D.Float, as needed for Line2D.Float
		Point2D.Float centerPoint = new Point2D.Float(centerPosition.getX(),centerPosition.getZ());
		Point2D.Float frontPoint = new Point2D.Float(frontPosition.getX(),frontPosition.getZ());
		
		// line in direction of driving
		Line2D.Float line = new Line2D.Float(centerPoint,frontPoint);
		
		// convert Vector3f to Point2D.Float
		Point2D point = new Point2D.Float(wayPoint.getX(),wayPoint.getZ());

		// check way point's relative position to the line
		if(line.relativeCCW(point) == -1)
		{
			// point on the left --> return -1
			return -1;
		}
		else if(line.relativeCCW(point) == 1)
		{
			// point on the right --> return 1
			return 1;
		}
		else
		{
			// point on line --> return 0
			return 0;
		}
	}

	
	private void updateSpeed(float tpf, ArrayList<TrafficObject> vehicleList) 
	{
		//float targetSpeed = getTargetSpeed();
		
		//if(overwriteSpeed >= 0)
		//	targetSpeed = Math.min(targetSpeed, overwriteSpeed);
		float targetSpeed = followBox.getSpeed();
		// stop car in order to avoid collision with other traffic objects and driving car
		// also for red traffic lights
		if(obstaclesInTheWay(vehicleList))
			targetSpeed = 0;
		
		float currentSpeed = getCurrentSpeedKmh();
		
		//System.out.print(name + ": " + targetSpeed + " *** " + currentSpeed);
		
		
		// set pedal positions
		if(currentSpeed < targetSpeed)
		{
			// too slow --> accelerate
			setAcceleratorPedalIntensity(-1);
			setBrakePedalIntensity(0);
			//System.out.println("gas");
			//System.out.print(" *** gas");
		}
		else if(currentSpeed > targetSpeed+1)
		{
			// too fast --> brake
			
			// currentSpeed >= targetSpeed+3 --> brake intensity: 100%
			// currentSpeed == targetSpeed+2 --> brake intensity:  50%
			// currentSpeed <= targetSpeed+1 --> brake intensity:   0%
			float brakeIntensity = (currentSpeed - targetSpeed - 1)/2.0f;
			brakeIntensity = Math.max(Math.min(brakeIntensity, 1.0f), 0.0f);
			
			// formerly use
			//brakeIntensity = 1.0f;
			
			setBrakePedalIntensity(brakeIntensity);
			setAcceleratorPedalIntensity(0);
			
			//System.out.println("brake: " + brakeIntensity);
			//System.out.print(" *** brake");
		}
		else
		{
			// else release pedals
			setAcceleratorPedalIntensity(0);
			setBrakePedalIntensity(0);
			//System.out.print(" *** free");
		}
		
		
		
		// accelerate
		if(engineOn)
			//carControl.accelerate(acceleratorPedalIntensity * accelerationForce);
			transmission.performAcceleration(powerTrain.getPAccel(tpf, acceleratorPedalIntensity) * 30f);
		else
			//carControl.accelerate(0);
			transmission.performAcceleration(0);
		//System.out.print(" *** " + gasPedalPressIntensity * accelerationForce);
		
		// brake	
		float appliedBrakeForce = brakePedalIntensity * maxBrakeForce;
		float currentFriction = 0.2f * maxFreeWheelBrakeForce;
		carControl.brake(appliedBrakeForce + currentFriction);
		
		//System.out.print(" *** " + appliedBrakeForce + currentFriction);
		//System.out.println("");
	}


	public float getTargetSpeed() 
	{
		// maximum speed for current way point segment
		float regularSpeed = followBox.getSpeed();

		// reduced speed to reach next speed limit in time
		float reducedSpeed = followBox.getReducedSpeed();
		
		float targetSpeed = Math.max(Math.min(regularSpeed, reducedSpeed),0);
		
		// limit maximum speed to speed of steering car 
		//if(isSpeedLimitedToSteeringCar)
		//	targetSpeed = Math.min(sim.getCar().getCurrentSpeedKmh(), targetSpeed);
		
		return targetSpeed;
	}
	
	
	/**
	 * Returns the signum of the speed change between this and the previous way point: 
	 * 0 if speed has not changed (or no previous way point available), 
	 * 1 if speed has been increased,
	 * -1 if speed has been decreased.
	 * 
	 * @return
	 * 		The signum of the speed change between this and the previous way point
	 */
	public int getSpeedChange()
	{
		Waypoint previousWP = followBox.getPreviousWayPoint();
		Waypoint currentWP = followBox.getCurrentWayPoint();
		
		if(previousWP == null)
			return 0;
		else
			return (int) Math.signum(currentWP.getSpeed() - previousWP.getSpeed());
	}


	private boolean obstaclesInTheWay(ArrayList<TrafficObject> vehicleList)
	{
		// check distance from driving car
		if(obstacleTooClose(sim.getCar().getPosition()))
			return true;

		// check distance from other traffic (except oneself)
		for(TrafficObject vehicle : vehicleList)
		{
			if(obstacleTooClose(vehicle.getPosition())){
				System.out.println("Traffic object too close");
				return true;
			}
		}
		
		// check if red traffic light ahead
		Waypoint nextWayPoint = followBox.getNextWayPoint();
		if(TrafficLightCenter.hasRedTrafficLight(nextWayPoint))
			if(obstacleTooClose(nextWayPoint.getPosition()))
				return true;
		
		return false;
	}


	private boolean obstacleTooClose(Vector3f obstaclePos)
	{
		float distanceToObstacle = obstaclePos.distance(getPosition());
		
		// angle between driving direction of traffic car and direction towards obstacle
		// (consider 3D space, because obstacle could be located on a bridge above traffic car)
		Vector3f carFrontPos = frontGeometry.getWorldTranslation();
		Vector3f carCenterPos = centerGeometry.getWorldTranslation();
		float angle = Util.getAngleBetweenPoints(carFrontPos, carCenterPos, obstaclePos, false);
		if(belowSafetyDistance(angle, distanceToObstacle))
			return true;

		// considering direction towards next way point (if available)
		Waypoint nextWP = followBox.getNextWayPoint();
		if(nextWP != null)
		{
			// angle between direction towards next WP and direction towards obstacle
			// (consider 3D space, because obstacle could be located on a bridge above traffic car)
			angle = Util.getAngleBetweenPoints(nextWP.getPosition(), carCenterPos, obstaclePos, false);
			if(belowSafetyDistance(angle, distanceToObstacle))
				return true;
		}
		return false;
	}
	
	
	private boolean belowSafetyDistance(float angle, float distance) 
	{	
		float lateralDistance = distance * FastMath.sin(angle);
		float forwardDistance = distance * FastMath.cos(angle);
		
		//if(name.equals("car1"))
		//	System.out.println(lateralDistance + " *** " + forwardDistance);
		
		float speedDependentForwardSafetyDistance = 0;
		
		//if(useSpeedDependentForwardSafetyDistance)
		//	speedDependentForwardSafetyDistance = 0.5f * getCurrentSpeedKmh();
		
		if((lateralDistance < minLateralSafetyDistance) && (forwardDistance > 0) && 
				(forwardDistance < Math.max(speedDependentForwardSafetyDistance , minForwardSafetyDistance)))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public String getName() 
	{
		return "drivingCar";
	}


	@Override
	public void setToWayPoint(String wayPointID) 
	{
		//int index = followBox.getIndexOfWP(wayPointID);
		//if(index != -1)
		if(followBox!= null)
			followBox.setToWayPoint(wayPointID);
		else
			System.err.println("Invalid way point ID: " + wayPointID);
	}

	@Override
	public Segment getCurrentSegment()
	{
		if(followBox!= null)
			return followBox.getCurrentSegment();
		else
			return null;
	}

	
	@Override
	public float getTraveledDistance()
	{
		if(followBox!= null)
			return followBox.getTraveledDistance();
		else
			return 0;
	}
	

	@Override
	public float getDistanceToNextWP()
	{
		if(followBox!= null)
			return followBox.getDistanceToNextWP();
		else
			return Float.MAX_VALUE;
	}
	
	// AutoPilot *****************************************************************


	

}
