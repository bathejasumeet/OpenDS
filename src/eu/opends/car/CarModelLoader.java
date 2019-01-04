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

import java.util.Properties;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;

import eu.opends.chrono.ChronoVehicleControl;
import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;
import eu.opends.tools.Util;

import eu.opends.basics.SimulationBasics;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.effects.RainSettings;
import eu.opends.drivingTask.scenario.ScenarioLoader;
import eu.opends.drivingTask.scenario.ScenarioLoader.CarProperty;

/**
 * 
 * @author Rafael Math
 */
public class CarModelLoader {
	private CullHint showHeadLightDebugBoxes = CullHint.Always;

	private boolean useChrono = false;
	public boolean isUseChrono() {
		return useChrono;
	}
	private Node carNode;

	public Node getCarNode() {
		return carNode;
	}

	private float frictionSlip;

	public float getDefaultFrictionSlip() {
		return frictionSlip;
	}

	private Vector3f egoCamPos;

	public Vector3f getEgoCamPos() {
		return egoCamPos;
	}

	private Vector3f staticBackCamPos;

	public Vector3f getStaticBackCamPos() {
		return staticBackCamPos;
	}

	private Vector3f leftMirrorPos;

	public Vector3f getLeftMirrorPos() {
		return leftMirrorPos;
	}

	private Vector3f centerMirrorPos;

	public Vector3f getCenterMirrorPos() {
		return centerMirrorPos;
	}

	private Vector3f rightMirrorPos;

	public Vector3f getRightMirrorPos() {
		return rightMirrorPos;
	}

	/*private VehicleControl carControl;

	public VehicleControl getCarControl() {
		return carControl;
	}*/
	
	private CarControl carControl;
	public CarControl getCarControl() {
		return carControl;
	}

	private Geometry leftLightSource;

	public Vector3f getLeftLightPosition() {
		return leftLightSource.getWorldTranslation();
	}

	private Geometry leftLightTarget;

	public Vector3f getLeftLightDirection() {
		return leftLightTarget.getWorldTranslation().subtract(getLeftLightPosition());
	}

	private Geometry rightLightSource;

	public Vector3f getRightLightPosition() {
		return rightLightSource.getWorldTranslation();
	}

	private Geometry rightLightTarget;

	public Vector3f getRightLightDirection() {
		return rightLightTarget.getWorldTranslation().subtract(getRightLightPosition());
	}

	public Node getCarExt() {
		return carNode_extCar;
	}

	Boolean shadowModelActive;

	Node highPolyChassis;
	Node highPolySteering;
	Node instrumnetCluster;
	Node highPolyWheelFrontLeft;
	Node highPolyWheelFrontRight;
	Node highPolyWheelBackLeft;
	Node highPolyWheelBackRight;
	Node whipersLeft;
	Node whipersRight;
	Node highPolyWindshield;
	Spatial glassFrontRainLayer;
	Spatial glassFront;
	Spatial glassSideL;
	Spatial glassSideR;
	Spatial glassOther;
	Boolean leftHandSideSteering;

	String modelPathSteeringWheel;
	String modelPathInstrumentCluster;
	String modelPathWindshield;
	String modelPathWhipersLeft;
	String modelPathWhipersRight;
	// childs have been added
	Node carNode_basicCar;
	Node carNode_extCar;

	public Spatial getGlassFrontRainLayer() {
		return glassFrontRainLayer;
	}

	public Spatial getGlassSideL() {
		return glassSideL;
	}

	public Spatial getGlassSideR() {
		return glassSideR;
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	public CarModelLoader(Simulator sim, Car car, String modelPath, float mass, Vector3f initialPos, Quaternion initialRot) {
		carNode = new Node();

		
		carNode_basicCar = (Node) sim.getAssetManager().loadModel(modelPath);
		carNode_basicCar.setName("carNode_basicCar");
		carNode_extCar = new Node();

		carNode_extCar.setName("carNode_extCar");

		carNode.attachChild(carNode_basicCar);
		carNode.attachChild(carNode_extCar);

		// activate shadowCarModel
		shadowModelActive = SimulationBasics.getSettingsLoader().getSetting(Setting.HighPolygon_carModel,
				SimulationDefaults.HighPolygon_carModel);
		
		leftHandSideSteering = Simulator.getDrivingTask().getScenarioLoader().getCarProperty(CarProperty.steeringWheel_left, SimulationDefaults.leftHandSideSteering);
		
		// activate realistic rain
		RainSettings rainSettings = Simulator.getDrivingTask().getScenarioLoader().getRainSettings();

		// set car's shadow mode
		carNode.setShadowMode(ShadowMode.Cast);

		// load settings from car properties file
		String propertiesPath = modelPath.replace(".j3o", ".properties");
		propertiesPath = propertiesPath.replace(".scene", ".properties");
		Properties properties = (Properties) sim.getAssetManager().loadAsset(propertiesPath);

		if(properties.getProperty("useChrono") != null)
			useChrono = Boolean.parseBoolean(properties.getProperty("useChrono"));
	
		// chassis properties
		Vector3f chassisScale = new Vector3f(getVector3f(properties, "chassisScale", 1));

		// ego camera properties
		egoCamPos = new Vector3f(getVector3f(properties, "egoCamPos", 0)).mult(chassisScale);

		if (leftHandSideSteering){
		// static back camera properties
			staticBackCamPos = new Vector3f(getVector3f(properties, "staticBackCamPos_left", 0)).mult(chassisScale);
		} else {
			staticBackCamPos = new Vector3f(getVector3f(properties, "staticBackCamPos_right", 0)).mult(chassisScale);
		}
			
		// left mirror properties
		leftMirrorPos = new Vector3f(getVector3f(properties, "leftMirrorPos", 0)).mult(chassisScale);
		if (leftMirrorPos.getX() == 0 && leftMirrorPos.getY() == 0 && leftMirrorPos.getZ() == 0) {
			// default: 1m to the left (x=-1), egoCam height, 1m to the front
			// (z=-1)
			leftMirrorPos = new Vector3f(-1, egoCamPos.getY(), -1);
		}

		// center mirror properties
		centerMirrorPos = new Vector3f(getVector3f(properties, "centerMirrorPos", 0)).mult(chassisScale);
		if (centerMirrorPos.getX() == 0 && centerMirrorPos.getY() == 0 && centerMirrorPos.getZ() == 0) {
			// default: 0m to the left (x=0), egoCam height, 1m to the front
			// (z=-1)
			centerMirrorPos = new Vector3f(0, egoCamPos.getY(), -1);
		}

		// right mirror properties
		rightMirrorPos = new Vector3f(getVector3f(properties, "rightMirrorPos", 0)).mult(chassisScale);
		if (rightMirrorPos.getX() == 0 && rightMirrorPos.getY() == 0 && rightMirrorPos.getZ() == 0) {
			// default: 1m to the right (x=1), egoCam height, 1m to the front
			// (z=-1)
			rightMirrorPos = new Vector3f(1, egoCamPos.getY(), -1);
		}

		// wheel properties
		float wheelScale;
		String wheelScaleString = properties.getProperty("wheelScale");
		if (wheelScaleString != null)
			wheelScale = Float.parseFloat(wheelScaleString);
		else
			wheelScale = chassisScale.getY();

		float frictionSlip = Float.parseFloat(properties.getProperty("wheelFrictionSlip"));

		// suspension properties
		float stiffness = Float.parseFloat(properties.getProperty("suspensionStiffness"));
		float compValue = Float.parseFloat(properties.getProperty("suspensionCompression"));
		float dampValue = Float.parseFloat(properties.getProperty("suspensionDamping"));
		float suspensionLenght = Float.parseFloat(properties.getProperty("suspensionLenght"));

		// center of mass
		Vector3f centerOfMass = new Vector3f(getVector3f(properties, "centerOfMass", 0)).mult(chassisScale);

		// wheel position
		float frontAxlePos = chassisScale.z * Float.parseFloat(properties.getProperty("frontAxlePos")) - centerOfMass.z;
		float backAxlePos = chassisScale.z * Float.parseFloat(properties.getProperty("backAxlePos")) - centerOfMass.z;
		float leftWheelsPos = chassisScale.x * Float.parseFloat(properties.getProperty("leftWheelsPos"))
				- centerOfMass.x;
		float rightWheelsPos = chassisScale.x * Float.parseFloat(properties.getProperty("rightWheelsPos"))
				- centerOfMass.x;
		float frontAxleHeight = chassisScale.y * Float.parseFloat(properties.getProperty("frontAxleHeight"))
				- centerOfMass.y;
		float backAxleHeight = chassisScale.y * Float.parseFloat(properties.getProperty("backAxleHeight"))
				- centerOfMass.y;

		// setup position and direction of head lights
		setupHeadLight(sim, properties);

		// setup reference points
		setupReferencePoints();

		// get chassis geometry and corresponding node
		//System.out.println("Tree of each car =" + Util.printTree(carNode_basicCar));
		if (!useChrono){
			Geometry chassis = Util.findGeom(carNode_basicCar, "Chassis");
	
			// compute extent of chassis
			BoundingBox chassisBox = (BoundingBox) chassis.getModelBound();
			Vector3f extent = new Vector3f();
			chassisBox.getExtent(extent);
			extent.multLocal(chassisScale);
			extent.multLocal(2);
			// System.out.println("extent of chassis: " + extent);
	
			// chassis.getMaterial().setColor("GlowColor", ColorRGBA.Orange);
			Node chassisNode = chassis.getParent();
	
			// scale chassis
			for (Geometry geo : Util.getAllGeometries(chassisNode))
				geo.setLocalScale(chassisScale);
	
			Util.findNode(carNode, "chassis").setLocalTranslation(centerOfMass.negate());
	
			// create a collision shape for the largest spatial (= hull) of the
			// chassis
			//Spatial largestSpatial = findLargestSpatial(chassisNode);
			
			Box box1 = new Box(0.85f,0.3f,1.95f);
		    Geometry blue = new Geometry("Box", box1);
		    blue.setLocalTranslation(new Vector3f(1f, 1.5f,1f));
		    Spatial largestSpatial = blue;
		    largestSpatial.setCullHint(CullHint.Never);
			CollisionShape carHull;
	
			// make default car invisible
			if (shadowModelActive) {
				if (car instanceof SteeringCar) {
					try {
						Material mat = new Material(sim.getAssetManager(), "Materials/Unshaded.j3md");
						mat.setColor("Color", new ColorRGBA(1, 1, 1, 0.0f));
						mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
						mat.getAdditionalRenderState().setDepthWrite(false);
						largestSpatial.setQueueBucket(Bucket.Transparent);
						chassis.setMaterial(mat);
	
						Node frontRight = Util.findNode(carNode, "front_right");
						Node frontLeft = Util.findNode(carNode, "front_left");
						Node backRight = Util.findNode(carNode, "back_right");
						Node backLeft = Util.findNode(carNode, "back_left");
	
						chassis.setCullHint(CullHint.Always);
	
						frontRight.setMaterial(mat);
						frontLeft.setMaterial(mat);
						backRight.setMaterial(mat);
						backLeft.setMaterial(mat);
	
						chassis.updateModelBound();
	
						// add high polygon model on top
						// chassis
						String modelPathChassis = sim.getDrivingTask().getSceneLoader().getChassis();
						highPolyChassis = (Node) sim.getSceneNode().getChild("shadowChassis");
	
						// properties file for chassis
						String propertiesPathChassis = modelPathChassis.replace(".scene", ".properties");
						if (!propertiesPathChassis.equals(null)) {
							propertiesPathChassis = modelPathChassis.replace(".j3o", ".properties");
						}
						Properties propertiesChassis = (Properties) sim.getAssetManager().loadAsset(propertiesPathChassis);
						Vector3f NewChassisScale = new Vector3f(getVector3f(propertiesChassis, "chassisScale", 1));
	
						// scale chassis
						highPolyChassis.scale(Float.parseFloat(propertiesChassis.getProperty("chassisScale.x")),
								Float.parseFloat(propertiesChassis.getProperty("chassisScale.y")),
								Float.parseFloat(propertiesChassis.getProperty("chassisScale.z")));
						// rotate chassis
						highPolyChassis.rotate(
								Float.parseFloat(propertiesChassis.getProperty("chassisRotation.x")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesChassis.getProperty("chassisRotation.y")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesChassis.getProperty("chassisRotation.z")) * FastMath.DEG_TO_RAD);
						// location chassis -- not required at the moment due to the
						// design
						highPolyChassis.setLocalTranslation(
								Float.parseFloat(propertiesChassis.getProperty("chassisTranslation.x")),
								Float.parseFloat(propertiesChassis.getProperty("chassisTranslation.y")),
								Float.parseFloat(propertiesChassis.getProperty("chassisTranslation.z")));
	
	
						// windshield load
						if (leftHandSideSteering){
							modelPathWindshield = sim.getDrivingTask().getSceneLoader().getWindschieldLeft();
							highPolyWindshield = (Node) sim.getSceneNode().getChild("shadowGlassRainLayer_left");
						} else {
							modelPathWindshield = sim.getDrivingTask().getSceneLoader().getWindschieldRight();
							highPolyWindshield = (Node) sim.getSceneNode().getChild("shadowGlassRainLayer_right");
						}
	
						System.out.println("Model loaded = " + highPolyWindshield.equals(null));
						
						String propertiesPathWindshield = modelPathWindshield.replace(".scene", ".properties");
						if (!propertiesPathWindshield.equals(null)) {
							propertiesPathWindshield = modelPathWindshield.replace(".j3o", ".properties");
						}
						
						Properties propertiesWindshield = (Properties) sim.getAssetManager().loadAsset(propertiesPathWindshield);
						Vector3f NewWindshieldScale = new Vector3f(getVector3f(propertiesWindshield, "windshieldScale", 1));
	
						// scale chassis
						highPolyWindshield.scale(Float.parseFloat(propertiesWindshield.getProperty("windshieldScale.x")),
								Float.parseFloat(propertiesWindshield.getProperty("windshieldScale.y")),
								Float.parseFloat(propertiesWindshield.getProperty("windshieldScale.z")));
						// rotate chassis
						highPolyWindshield.rotate(
								Float.parseFloat(propertiesWindshield.getProperty("windshieldRotation.x")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWindshield.getProperty("windshieldRotation.y")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWindshield.getProperty("windshieldRotation.z")) * FastMath.DEG_TO_RAD);
						// location chassis -- not required at the moment due to the
						// design
						highPolyWindshield.setLocalTranslation(
								Float.parseFloat(propertiesWindshield.getProperty("windshieldTranslation.x")),
								Float.parseFloat(propertiesWindshield.getProperty("windshieldTranslation.y")),
								Float.parseFloat(propertiesWindshield.getProperty("windshieldTranslation.z")));
	
						carNode_extCar.attachChild(highPolyWindshield);
					
						
						System.out.println("Tree of the car"+Util.printTree(sim.getSceneNode()));
						try {
							if (rainSettings.getStatus() == true) {
		
								System.out.println(rainSettings.getStatus());
								System.out.println(rainSettings.getRainLayerName());
								System.out.println(rainSettings.getFrontLayerName());
								System.out.println(rainSettings.getFPS());
								System.out.println(rainSettings.getMode());
								System.out.println(rainSettings.getPath());
								System.out.println(Util.printTree(highPolyWindshield));
								
								
								
								glassFrontRainLayer = highPolyWindshield.getChild(rainSettings.getRainLayerName());
								glassFrontRainLayer.setName("windowglassFrontRainLayer");
		
								glassFront = highPolyChassis.getChild(rainSettings.getFrontLayerName());
								glassFront.setName("windowglassFront");
		
								glassSideL = highPolyChassis.getChild(rainSettings.getLeftName());
								glassSideL.setName("windowglasssideL");
		
								glassSideR = highPolyChassis.getChild(rainSettings.getRightName());
								glassSideR.setName("windowglasssideR");
		
								((Geometry) glassFrontRainLayer).getMesh().scaleTextureCoordinates(new Vector2f(rainSettings.getScaleX()+6f, rainSettings.getScaleY()+6f));
								((Geometry) glassSideL).getMesh().scaleTextureCoordinates(
										new Vector2f(rainSettings.getScaleX(), rainSettings.getScaleY()));
								((Geometry) glassSideR).getMesh().scaleTextureCoordinates(
										new Vector2f(rainSettings.getScaleX(), rainSettings.getScaleY()));
		
								Material matGlassRainLayer = new Material(sim.getAssetManager(), "Materials/Unshaded.j3md");
		
								matGlassRainLayer.setColor("Color", new ColorRGBA(0.8f, 0.8f, 0.8f, 0.2f));
								matGlassRainLayer.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
								matGlassRainLayer.getAdditionalRenderState().setDepthWrite(false);
		
								Material matGlass = new Material(sim.getAssetManager(), "Materials/Unshaded.j3md");
		
								matGlass.setColor("Color", new ColorRGBA(0.8f, 0.8f, 0.8f, 0.2f));
								matGlass.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
								matGlass.getAdditionalRenderState().setDepthWrite(false);
		
								//((Geometry) glassFrontRainLayer).setMaterial(matGlassRainLayer);
								glassFrontRainLayer.setMaterial(matGlassRainLayer);
								glassFront.setMaterial(matGlass);
								glassSideL.setMaterial(matGlass);
								glassSideR.setMaterial(matGlass);
								
								//((Geometry) glassFront).setMaterial(matGlass);
								//((Geometry) glassSideL).setMaterial(matGlass);
								//((Geometry) glassSideR).setMaterial(matGlass);
								
							}
						} catch (NullPointerException e) {
							System.out.println(e.getMessage());
						}
	
						carNode_extCar.attachChild(highPolyChassis);
	
						if (leftHandSideSteering){
							modelPathSteeringWheel = sim.getDrivingTask().getSceneLoader().getSteeringWheelLeft();
						} else {
							modelPathSteeringWheel = sim.getDrivingTask().getSceneLoader().getSteeringWheelRight();
						}
						
	
						highPolySteering = (Node) sim.getAssetManager().loadModel(modelPathSteeringWheel);
						if (leftHandSideSteering){
							highPolySteering = (Node) sim.getSceneNode().getChild("shadowSteeringWheel_left");
						} else {
							highPolySteering = (Node) sim.getSceneNode().getChild("shadowSteeringWheel_right");
						}						
						highPolySteering.center();
	
						
						String propertiesPathSteeringWheel = modelPathSteeringWheel.replace(".scene", ".properties");
						if (!propertiesPathSteeringWheel.equals(null)) {
							propertiesPathSteeringWheel = modelPathSteeringWheel.replace(".j3o", ".properties");
						}
						
						Properties propertiesSteeringWheel = (Properties) sim.getAssetManager()
								.loadAsset(propertiesPathSteeringWheel);
						Vector3f newSteeringWheelScale = new Vector3f(
								getVector3f(propertiesSteeringWheel, "steeringWheelScale", 1));
	
						// scale steering wheel
						highPolySteering.scale(
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelScale.x")),
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelScale.y")),
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelScale.z")));
						// rotate steering wheel
						highPolySteering.rotate(
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelRotation.x"))
										* FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelRotation.y"))
										* FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelRotation.z"))
										* FastMath.DEG_TO_RAD);
						// location steering -- not required at the moment due to
						// the design
						highPolySteering.setLocalTranslation(
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelTranslation.x")),
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelTranslation.y")),
								Float.parseFloat(propertiesSteeringWheel.getProperty("steeringWheelTranslation.z")));
	
						// carNode.attachChild(highPolySteering);
						carNode_extCar.attachChild(highPolySteering);
	
						if (leftHandSideSteering){
							modelPathInstrumentCluster = sim.getDrivingTask().getSceneLoader().getInstrumnetClusterLeft();
							instrumnetCluster = (Node) sim.getSceneNode().getChild("shadowInstrumentCluster_left");
						} else {
							modelPathInstrumentCluster = sim.getDrivingTask().getSceneLoader().getInstrumnetClusterRight();
							instrumnetCluster = (Node) sim.getSceneNode().getChild("shadowInstrumentCluster_right");
						}
						
						String propertiesPathInstrumentCluster = modelPathInstrumentCluster.replace(".scene",
								".properties");
						if (!propertiesPathInstrumentCluster.equals(null)) {
							propertiesPathInstrumentCluster = modelPathInstrumentCluster.replace(".j3o", ".properties");
						}
						
						Properties propertiesInstrumentCluster = (Properties) sim.getAssetManager()
								.loadAsset(propertiesPathInstrumentCluster);
	
						instrumnetCluster.scale(
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterScale.x")),
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterScale.y")),
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterScale.z")));
						// rotate steering wheel
						instrumnetCluster.rotate(
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterRotation.x"))
										* FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterRotation.y"))
										* FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterRotation.z"))
										* FastMath.DEG_TO_RAD);
						// location steering -- not required at the moment due to
						// the design
						instrumnetCluster.setLocalTranslation(
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterTranslation.x")),
								Float.parseFloat(propertiesInstrumentCluster.getProperty("InstrumentClusterTranslation.y")),
								Float.parseFloat(
										propertiesInstrumentCluster.getProperty("InstrumentClusterTranslation.z")));
	
						// carNode.attachChild(instrumnetCluster);
						carNode_extCar.attachChild(instrumnetCluster);
	
						try {
							if (leftHandSideSteering){
								modelPathWhipersLeft = sim.getDrivingTask().getSceneLoader().getWhipersLeftLeft();
								whipersLeft = (Node) sim.getSceneNode().getChild("shadowWhipersLeft_left");
							}
							else {
								modelPathWhipersLeft = sim.getDrivingTask().getSceneLoader().getWhipersLeftRight();
								whipersLeft = (Node) sim.getSceneNode().getChild("shadowWhipersLeft_right");
							}
								
								whipersLeft.setName("whipersLeft");
		
								String propertiesPathWhipersLeft = modelPathWhipersLeft.replace(".scene", ".properties");
								if (!propertiesPathWhipersLeft.equals(null)) {
									propertiesPathWhipersLeft = modelPathWhipersLeft.replace(".j3o", ".properties");
								}
						
								System.out.println("Take in action" + propertiesPathWhipersLeft);
								Properties propertiesWhipersLeft = (Properties) sim.getAssetManager().loadAsset(propertiesPathWhipersLeft);
		
								whipersLeft.scale(Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftScale.x")),
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftScale.y")),
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftScale.z")));
								// whipersLeft.rotate(Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.x"))*FastMath.DEG_TO_RAD,
								// Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.y"))*FastMath.DEG_TO_RAD,
								// Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.z"))*FastMath.DEG_TO_RAD);
								Quaternion quaL = new Quaternion(
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.x"))
												* FastMath.DEG_TO_RAD,
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.y"))
												* FastMath.DEG_TO_RAD,
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.z"))
												* FastMath.DEG_TO_RAD,
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.w")));
								whipersLeft.setLocalRotation(quaL);
								whipersLeft.setLocalTranslation(
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftTranslation.x")),
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftTranslation.y")),
										Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftTranslation.z")));
	
							carNode_extCar.attachChild(whipersLeft);
	
							// wipers right
							if (leftHandSideSteering){
								modelPathWhipersRight = sim.getDrivingTask().getSceneLoader().getWhipersRightLeft();
								whipersRight = (Node) sim.getSceneNode().getChild("shadowWhipersRight_left");
							} else {
								modelPathWhipersRight = sim.getDrivingTask().getSceneLoader().getWhipersRightRight();
								whipersRight = (Node) sim.getSceneNode().getChild("shadowWhipersRight_right");	
							}
								
								whipersRight.setName("whipersRight");
		
								String propertiesPathWhipersRight = modelPathWhipersRight.replace(".scene", ".properties");
								if (!propertiesPathWhipersRight.equals(null)) {
									propertiesPathWhipersRight = modelPathWhipersRight.replace(".j3o", ".properties");
								}
						
								Properties propertiesWhipersRight = (Properties) sim.getAssetManager()
										.loadAsset(propertiesPathWhipersRight);
		
								whipersRight.scale(Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightScale.x")),
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightScale.y")),
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightScale.z")));
								// whipersLeft.rotate(Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.x"))*FastMath.DEG_TO_RAD,
								// Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.y"))*FastMath.DEG_TO_RAD,
								// Float.parseFloat(propertiesWhipersLeft.getProperty("WhipersLeftRotation.z"))*FastMath.DEG_TO_RAD);
								Quaternion quaR = new Quaternion(
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightRotation.x"))
												* FastMath.DEG_TO_RAD,
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightRotation.y"))
												* FastMath.DEG_TO_RAD,
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightRotation.z"))
												* FastMath.DEG_TO_RAD,
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightRotation.w")));
								whipersRight.setLocalRotation(quaR);
								whipersRight.setLocalTranslation(
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightTranslation.x")),
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightTranslation.y")),
										Float.parseFloat(propertiesWhipersRight.getProperty("WhipersRightTranslation.z")));
		
								carNode_extCar.attachChild(whipersRight);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							System.out.println("No whipers specified in car model");
						}
	
	
						highPolyWheelFrontLeft = (Node) sim.getSceneNode().getChild("shadowWheelFrontLeft");
						highPolyWheelFrontRight = (Node) sim.getSceneNode().getChild("shadowWheelFrontRight");
						highPolyWheelBackLeft = (Node) sim.getSceneNode().getChild("shadowWheelBackLeft");
						highPolyWheelBackRight = (Node) sim.getSceneNode().getChild("shadowWheelBackRight");
					} catch (AssetNotFoundException e) {
						e.printStackTrace();
					}
	
				}
			}

			if (properties.getProperty("useBoxCollisionShape") != null 	&& Boolean.parseBoolean(properties.getProperty("useBoxCollisionShape")) == true)
				//carHull = CollisionShapeFactory.createBoxShape(largestSpatial);
				carHull = CollisionShapeFactory.createBoxShape(largestSpatial);
			else
				carHull = CollisionShapeFactory.createDynamicMeshShape(largestSpatial);
	
			// add collision shape to compound collision shape in order to
			// apply chassis's translation and rotation to collision shape
			CompoundCollisionShape compoundShape = new CompoundCollisionShape();
			Vector3f location = chassis.getWorldTranslation();
			Matrix3f rotation = (new Matrix3f()).set(chassis.getWorldRotation());
			Vector3f offset = getCollisionShapeOffset(properties).mult(chassisScale);
			compoundShape.addChildShape(carHull, location.add(offset), rotation);
	
			// create a vehicle control
			VehicleControl bulletVehicleControl = new VehicleControl(compoundShape, mass);
			carControl = new CarControl(bulletVehicleControl);
			carNode.addControl(bulletVehicleControl);
	
			// set values for suspension
			bulletVehicleControl.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
			bulletVehicleControl.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
			bulletVehicleControl.setSuspensionStiffness(stiffness);
			bulletVehicleControl.setMaxSuspensionForce(10000);
	
			// create four wheels and add them at their locations
			// note that the car actually goes backwards
			Vector3f wheelDirection = new Vector3f(0, -1, 0);
			Vector3f wheelAxle = new Vector3f(-1, 0, 0);
	
			// add front right wheel
			Geometry geom_wheel_fr = Util.findGeom(carNode_basicCar, "WheelFrontRight");
			geom_wheel_fr.setLocalScale(wheelScale);
			geom_wheel_fr.center();
			BoundingBox box = (BoundingBox) geom_wheel_fr.getModelBound();
			float wheelRadius = wheelScale * box.getYExtent();
			VehicleWheel wheel_fr = bulletVehicleControl.addWheel(geom_wheel_fr.getParent(),
					new Vector3f(rightWheelsPos, frontAxleHeight, frontAxlePos), wheelDirection, wheelAxle,
					suspensionLenght, wheelRadius, true);
			wheel_fr.setFrictionSlip(frictionSlip); // apply friction slip
													// (likelihood of breakaway)
	
			// add front left wheel
			Geometry geom_wheel_fl = Util.findGeom(carNode_basicCar, "WheelFrontLeft");
			geom_wheel_fl.setLocalScale(wheelScale);
			geom_wheel_fl.center();
			box = (BoundingBox) geom_wheel_fl.getModelBound();
			wheelRadius = wheelScale * box.getYExtent();
			VehicleWheel wheel_fl = bulletVehicleControl.addWheel(geom_wheel_fl.getParent(),
					new Vector3f(leftWheelsPos, frontAxleHeight, frontAxlePos), wheelDirection, wheelAxle, suspensionLenght,
					wheelRadius, true);
			wheel_fl.setFrictionSlip(frictionSlip); // apply friction slip
													// (likelihood of breakaway)
	
			// add back right wheel
			Geometry geom_wheel_br = Util.findGeom(carNode_basicCar, "WheelBackRight");
			geom_wheel_br.setLocalScale(wheelScale);
			geom_wheel_br.center();
			box = (BoundingBox) geom_wheel_br.getModelBound();
			wheelRadius = wheelScale * box.getYExtent();
			VehicleWheel wheel_br = bulletVehicleControl.addWheel(geom_wheel_br.getParent(),
					new Vector3f(rightWheelsPos, backAxleHeight, backAxlePos), wheelDirection, wheelAxle, suspensionLenght,
					wheelRadius, false);
			wheel_br.setFrictionSlip(frictionSlip); // apply friction slip
													// (likelihood of breakaway)
	
			// add back left wheel
			Geometry geom_wheel_bl = Util.findGeom(carNode_basicCar, "WheelBackLeft");
	
			geom_wheel_bl.setLocalScale(wheelScale);
			geom_wheel_bl.center();
			box = (BoundingBox) geom_wheel_bl.getModelBound();
			wheelRadius = wheelScale * box.getYExtent();
			VehicleWheel wheel_bl = bulletVehicleControl.addWheel(geom_wheel_bl.getParent(),
					new Vector3f(leftWheelsPos, backAxleHeight, backAxlePos), wheelDirection, wheelAxle, suspensionLenght,
					wheelRadius, false);
			wheel_bl.setFrictionSlip(frictionSlip); // apply friction slip
													// (likelihood of breakaway)
	
			if (car instanceof SteeringCar) {
				if (shadowModelActive) {
					try {
						float wheelScaleExtended = 0f;
						String wheelScaleExtendedString = properties.getProperty("wheelScaleExtended");
						if (wheelScaleExtendedString != null)
							wheelScaleExtended = Float.parseFloat(wheelScaleExtendedString);
	
						// front right wheel
						Node node_wheel_fr = Util.findNode(carNode_basicCar, "front_right");
						node_wheel_fr.attachChild(highPolyWheelFrontRight);
						String modelPathWheelFR = sim.getDrivingTask().getSceneLoader().getWheelFrontRight();
						String propertiesPathWheelFR = modelPathWheelFR.replace(".scene", ".properties");
						if (!propertiesPathWheelFR.equals(null)) {
							propertiesPathWheelFR = modelPathWheelFR.replace(".j3o", ".properties");
						}
						Properties propertiesWheelFR = (Properties) sim.getAssetManager().loadAsset(propertiesPathWheelFR);
	
						highPolyWheelFrontRight.scale(Float.parseFloat(propertiesWheelFR.getProperty("WheelFRScale.x")),
								Float.parseFloat(propertiesWheelFR.getProperty("WheelFRScale.y")),
								Float.parseFloat(propertiesWheelFR.getProperty("WheelFRScale.z")));
						highPolyWheelFrontRight.rotate(
								Float.parseFloat(propertiesWheelFR.getProperty("WheelFRRotation.x")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelFR.getProperty("WheelFRRotation.y")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelFR.getProperty("WheelFRRotation.z")) * FastMath.DEG_TO_RAD);
	
						System.out.println("Container = " + Util.printTree(highPolyWheelFrontRight));
						Geometry geom_wheel_fr_v2 = Util.findGeom(highPolyWheelFrontRight, "wheels-BR");
						geom_wheel_fr_v2.center();
						box = (BoundingBox) geom_wheel_fr_v2.getModelBound();
						float wheelRadius_v3 = wheelScaleExtended * box.getYExtent();
						VehicleWheel wheel_fr_v2 = bulletVehicleControl.addWheel(geom_wheel_fr_v2.getParent(),
								new Vector3f(rightWheelsPos, frontAxleHeight, frontAxlePos - 0.14f), wheelDirection,
								wheelAxle, suspensionLenght, wheelRadius_v3, true);
						wheel_fr_v2.setFrictionSlip(frictionSlip);
	
						// front left wheel
						Node node_wheel_fl = Util.findNode(carNode_basicCar, "front_left");
						node_wheel_fl.attachChild(highPolyWheelFrontLeft);
						String modelPathWheelFL = sim.getDrivingTask().getSceneLoader().getWheelFrontLeft();
	
						String propertiesPathWheelFL = modelPathWheelFL.replace(".scene", ".properties");
						if (!propertiesPathWheelFL.equals(null)) {
							propertiesPathWheelFL = modelPathWheelFL.replace(".j3o", ".properties");
						}
						Properties propertiesWheelFL = (Properties) sim.getAssetManager().loadAsset(propertiesPathWheelFL);
	
						highPolyWheelFrontLeft.scale(Float.parseFloat(propertiesWheelFL.getProperty("WheelFLScale.x")),
								Float.parseFloat(propertiesWheelFL.getProperty("WheelFLScale.y")),
								Float.parseFloat(propertiesWheelFL.getProperty("WheelFLScale.z")));
						highPolyWheelFrontLeft.rotate(
								Float.parseFloat(propertiesWheelFL.getProperty("WheelFLRotation.x")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelFL.getProperty("WheelFLRotation.y")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelFL.getProperty("WheelFLRotation.z")) * FastMath.DEG_TO_RAD);
	
						Geometry geom_wheel_fl_v2 = Util.findGeom(highPolyWheelFrontLeft, "wheels-LF");
						geom_wheel_fl_v2.center();
						box = (BoundingBox) geom_wheel_fl_v2.getModelBound();
						float wheelRadius_v2 = wheelScaleExtended * box.getYExtent();
						VehicleWheel wheel_fl_v2 = bulletVehicleControl.addWheel(geom_wheel_fl_v2.getParent(),
								new Vector3f(leftWheelsPos, frontAxleHeight, frontAxlePos - 0.14f), wheelDirection,
								wheelAxle, suspensionLenght, wheelRadius_v2, true);
						wheel_fl_v2.setFrictionSlip(frictionSlip);
	
						// back right wheel
						Node node_wheel_br = Util.findNode(carNode_basicCar, "back_right");
						node_wheel_br.attachChild(highPolyWheelBackRight);
						String modelPathWheelBR = sim.getDrivingTask().getSceneLoader().getWheelBackRight();
	
						String propertiesPathWheelBR = modelPathWheelBR.replace(".scene", ".properties");
						if (!propertiesPathWheelBR.equals(null)) {
							propertiesPathWheelBR = modelPathWheelBR.replace(".j3o", ".properties");
						}
						Properties propertiesWheelBR = (Properties) sim.getAssetManager().loadAsset(propertiesPathWheelBR);
	
						highPolyWheelBackRight.scale(Float.parseFloat(propertiesWheelBR.getProperty("WheelBRScale.x")),
								Float.parseFloat(propertiesWheelBR.getProperty("WheelBRScale.y")),
								Float.parseFloat(propertiesWheelBR.getProperty("WheelBRScale.z")));
						highPolyWheelBackRight.rotate(
								Float.parseFloat(propertiesWheelBR.getProperty("WheelBRRotation.x")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelBR.getProperty("WheelBRRotation.y")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelBR.getProperty("WheelBRRotation.z")) * FastMath.DEG_TO_RAD);
						Geometry geom_wheel_br_v2 = Util.findGeom(highPolyWheelBackRight, "wheels-BR");
						geom_wheel_br_v2.center();
						box = (BoundingBox) geom_wheel_br_v2.getModelBound();
						float wheelRadius_v5 = wheelScaleExtended * box.getYExtent();
						VehicleWheel wheel_br_v2 = bulletVehicleControl.addWheel(geom_wheel_br_v2.getParent(),
								new Vector3f(rightWheelsPos, backAxleHeight, backAxlePos + 0.06f), wheelDirection,
								wheelAxle, suspensionLenght, wheelRadius_v5, false);
						wheel_br_v2.setFrictionSlip(frictionSlip);
	
						// back left wheel
						Node node_wheel_bl = Util.findNode(carNode_basicCar, "back_left");
						node_wheel_bl.attachChild(highPolyWheelBackLeft);
						String modelPathWheelBL = sim.getDrivingTask().getSceneLoader().getWheelBackLeft();
	
						String propertiesPathWheelBL = modelPathWheelBL.replace(".scene", ".properties");
						if (!propertiesPathWheelBL.equals(null)) {
							propertiesPathWheelBL = modelPathWheelBL.replace(".j3o", ".properties");
						}
						Properties propertiesWheelBL = (Properties) sim.getAssetManager().loadAsset(propertiesPathWheelBL);
	
						highPolyWheelBackLeft.scale(Float.parseFloat(propertiesWheelBL.getProperty("WheelBLScale.x")),
								Float.parseFloat(propertiesWheelBL.getProperty("WheelBLScale.y")),
								Float.parseFloat(propertiesWheelBL.getProperty("WheelBLScale.z")));
						highPolyWheelBackLeft.rotate(
								Float.parseFloat(propertiesWheelBL.getProperty("WheelBLRotation.x")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelBL.getProperty("WheelBLRotation.y")) * FastMath.DEG_TO_RAD,
								Float.parseFloat(propertiesWheelBL.getProperty("WheelBLRotation.z")) * FastMath.DEG_TO_RAD);
						Geometry geom_wheel_bl_v2 = Util.findGeom(highPolyWheelBackLeft, "wheels-LF");
						geom_wheel_bl_v2.center();
						box = (BoundingBox) geom_wheel_bl_v2.getModelBound();
						float wheelRadius_v4 = wheelScaleExtended * box.getYExtent();
						VehicleWheel wheel_bl_v2 = bulletVehicleControl.addWheel(geom_wheel_bl_v2.getParent(),
								new Vector3f(leftWheelsPos, backAxleHeight, backAxlePos + 0.06f), wheelDirection, wheelAxle,
								suspensionLenght, wheelRadius_v4, false);
						wheel_bl_v2.setFrictionSlip(frictionSlip);
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
	
				}
			}

			if (properties.getProperty("thirdAxlePos") != null && properties.getProperty("thirdAxleHeight") != null) {
				float thirdAxlePos = chassisScale.z * Float.parseFloat(properties.getProperty("thirdAxlePos"))
						- centerOfMass.z;
				float thirdAxleHeight = chassisScale.y * Float.parseFloat(properties.getProperty("thirdAxleHeight"))
						- centerOfMass.y;
	
				// add back right wheel 2
				Geometry geom_wheel_br2 = Util.findGeom(carNode_basicCar, "WheelBackRight2");
				geom_wheel_br2.setLocalScale(wheelScale);
				geom_wheel_br2.center();
				box = (BoundingBox) geom_wheel_br2.getModelBound();
				wheelRadius = wheelScale * box.getYExtent();
				VehicleWheel wheel_br2 = bulletVehicleControl.addWheel(geom_wheel_br2.getParent(),
						new Vector3f(rightWheelsPos, thirdAxleHeight, thirdAxlePos), wheelDirection, wheelAxle,
						suspensionLenght, wheelRadius, false);
				wheel_br2.setFrictionSlip(frictionSlip); // apply friction slip
															// (likelihood of
															// breakaway)
	
				// add back left wheel 2
				Geometry geom_wheel_bl2 = Util.findGeom(carNode_basicCar, "WheelBackLeft2");
				geom_wheel_bl2.setLocalScale(wheelScale);
				geom_wheel_bl2.center();
				box = (BoundingBox) geom_wheel_bl2.getModelBound();
				wheelRadius = wheelScale * box.getYExtent();
				VehicleWheel wheel_bl2 = bulletVehicleControl.addWheel(geom_wheel_bl2.getParent(),
						new Vector3f(leftWheelsPos, thirdAxleHeight, thirdAxlePos), wheelDirection, wheelAxle,
						suspensionLenght, wheelRadius, false);
				wheel_bl2.setFrictionSlip(frictionSlip); // apply friction slip
															// (likelihood of
															// breakaway)
			}
        }
        else
        {
    		// Chrono parameters
    		String vehicleFile = properties.getProperty("vehicleFile");
    		String tireFile = properties.getProperty("tireFile");
    		String powertrainFile = properties.getProperty("powertrainFile");
    		String terrainFile = "terrain/RigidMesh2.json";  //FIXME

    		
			// new Chrono vehicle
        	ChronoVehicleControl chronoVehicleControl = new ChronoVehicleControl(sim, initialPos, initialRot, 
        			vehicleFile, tireFile, powertrainFile, terrainFile);
        	carControl = new CarControl(chronoVehicleControl);
        	
        	Node chronoVehicleNode = new Node("ChronoVehicle");
        	
			// load car parts
        	Node loadNode = (Node)sim.getAssetManager().loadModel(modelPath);
        	
	        // get chassis
        	Geometry chassisSpatial = Util.findGeom(loadNode, "Chassis");
			chassisSpatial.setLocalRotation((new Quaternion()).fromAngles(0, 270*FastMath.DEG_TO_RAD, 0));
			chassisSpatial.setLocalTranslation(0.25f,0,0);
			Node chassis = new Node("chassis");
			chassis.attachChild(chassisSpatial);
			chronoVehicleNode.attachChild(chassis);
			
			// create car node (e.g. for camera movement)
        	carNode = new Node("carNode");
        	carNode.setLocalRotation((new Quaternion()).fromAngles(0, 270*FastMath.DEG_TO_RAD, 0));
        	carNode.setLocalTranslation(0.25f,0,0);
			chassis.attachChild(carNode);

			// add node representing position of left front wheel
			Node leftFrontWheelSpatial = Util.findNode(loadNode, "front_left");
			leftFrontWheelSpatial.setLocalRotation((new Quaternion()).fromAngles(0, 270*FastMath.DEG_TO_RAD, 0));
			Node leftFrontWheel = new Node("leftFrontWheel");
			leftFrontWheel.attachChild(leftFrontWheelSpatial);
			chronoVehicleNode.attachChild(leftFrontWheel);
			
			// add node representing position of right front wheel
			Node rightFrontWheelSpatial = Util.findNode(loadNode, "front_right");
			rightFrontWheelSpatial.setLocalRotation((new Quaternion()).fromAngles(0, 270*FastMath.DEG_TO_RAD, 0));
			Node rightFrontWheel = new Node("rightFrontWheel");
			rightFrontWheel.attachChild(rightFrontWheelSpatial);
			chronoVehicleNode.attachChild(rightFrontWheel);
			
			// add node representing position of left back wheel		
			Node leftBackWheelSpatial = Util.findNode(loadNode, "back_left");
			leftBackWheelSpatial.setLocalRotation((new Quaternion()).fromAngles(0, 270*FastMath.DEG_TO_RAD, 0));
			Node leftBackWheel = new Node("leftBackWheel");
			leftBackWheel.attachChild(leftBackWheelSpatial);
			chronoVehicleNode.attachChild(leftBackWheel);
			
			// add node representing position of right back wheel
			Node rightBackWheelSpatial = Util.findNode(loadNode, "back_right");
			rightBackWheelSpatial.setLocalRotation((new Quaternion()).fromAngles(0, 270*FastMath.DEG_TO_RAD, 0));
			Node rightBackWheel = new Node("rightBackWheel");
			rightBackWheel.attachChild(rightBackWheelSpatial);
			chronoVehicleNode.attachChild(rightBackWheel);
			
			sim.getSceneNode().attachChild(chronoVehicleNode);
        }
        

        // setup position and direction of head lights
        setupHeadLight(sim, properties);
        
        // setup reference points
        setupReferencePoints();
        
		// adding car interior if available
		if (car instanceof SteeringCar) {
			setupInterior(sim, properties);
		}
	}

	private Vector3f getCollisionShapeOffset(Properties properties) {
		float offsetX = 0;
		float offsetY = 0;
		float offsetZ = 0;

		if (properties.getProperty("collisionShapePos.x") != null)
			offsetX = Float.parseFloat(properties.getProperty("collisionShapePos.x"));

		if (properties.getProperty("collisionShapePos.y") != null)
			offsetY = Float.parseFloat(properties.getProperty("collisionShapePos.y"));

		if (properties.getProperty("collisionShapePos.z") != null)
			offsetZ = Float.parseFloat(properties.getProperty("collisionShapePos.z"));

		return new Vector3f(offsetX, offsetY, offsetZ);
	}

	@SuppressWarnings("unused")
	private Spatial findLargestSpatial(Node chassisNode) {
		// if no child larger than chassisNode available, return chassisNode
		Spatial largestSpatial = chassisNode;
		int vertexCount = 0;

		for (Spatial n : chassisNode.getChildren()) {
			if (n.getVertexCount() > vertexCount) {
				largestSpatial = n;
				vertexCount = n.getVertexCount();
			}
		}

		return largestSpatial;
	}

	private void setupHeadLight(Simulator sim, Properties properties) {
		// add node representing position of left head light
		Box leftLightBox = new Box(0.01f, 0.01f, 0.01f);
		leftLightSource = new Geometry("leftLightBox", leftLightBox);
		leftLightSource.setLocalTranslation(getVector3f(properties, "leftHeadlightPos", 0));
		Material leftMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		leftMaterial.setColor("Color", ColorRGBA.Red);
		leftLightSource.setMaterial(leftMaterial);
		Node leftNode = new Node();
		leftNode.attachChild(leftLightSource);
		leftNode.setCullHint(showHeadLightDebugBoxes);
		carNode_basicCar.attachChild(leftNode);

		// add node representing target position of left head light
		Box leftLightTargetBox = new Box(0.01f, 0.01f, 0.01f);
		leftLightTarget = new Geometry("leftLightTargetBox", leftLightTargetBox);
		leftLightTarget.setLocalTranslation(getVector3f(properties, "leftHeadlightTarget", 0));
		Material leftTargetMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		leftTargetMaterial.setColor("Color", ColorRGBA.Red);
		leftLightTarget.setMaterial(leftTargetMaterial);
		Node leftTargetNode = new Node();
		leftTargetNode.attachChild(leftLightTarget);
		leftTargetNode.setCullHint(showHeadLightDebugBoxes);
		carNode_basicCar.attachChild(leftTargetNode);

		// add node representing position of right head light
		Box rightLightBox = new Box(0.01f, 0.01f, 0.01f);
		rightLightSource = new Geometry("rightLightBox", rightLightBox);
		rightLightSource.setLocalTranslation(getVector3f(properties, "rightHeadlightPos", 0));
		Material rightMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		rightMaterial.setColor("Color", ColorRGBA.Green);
		rightLightSource.setMaterial(rightMaterial);
		Node rightNode = new Node();
		rightNode.attachChild(rightLightSource);
		rightNode.setCullHint(showHeadLightDebugBoxes);
		carNode_basicCar.attachChild(rightNode);

		// add node representing target position of right head light
		Box rightLightTargetBox = new Box(0.01f, 0.01f, 0.01f);
		rightLightTarget = new Geometry("rightLightTargetBox", rightLightTargetBox);
		rightLightTarget.setLocalTranslation(getVector3f(properties, "rightHeadlightTarget", 0));
		Material rightTargetMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		rightTargetMaterial.setColor("Color", ColorRGBA.Green);
		rightLightTarget.setMaterial(rightTargetMaterial);
		Node rightTargetNode = new Node();
		rightTargetNode.attachChild(rightLightTarget);
		rightTargetNode.setCullHint(showHeadLightDebugBoxes);
		carNode_basicCar.attachChild(rightTargetNode);
	}

	private void setupReferencePoints() {
		Node leftPoint = new Node("leftPoint");
		leftPoint.setLocalTranslation(-1, 1, 0);
		carNode_basicCar.attachChild(leftPoint);

		Node rightPoint = new Node("rightPoint");
		rightPoint.setLocalTranslation(1, 1, 0);
		carNode_basicCar.attachChild(rightPoint);

		Node frontPoint = new Node("frontPoint");
		frontPoint.setLocalTranslation(0, 1, -2);
		carNode_basicCar.attachChild(frontPoint);

		Node backPoint = new Node("backPoint");
		backPoint.setLocalTranslation(0, 1, 2);
		carNode_basicCar.attachChild(backPoint);
	}

	private void setupInterior(Simulator sim, Properties properties)
	{
		String  interiorPath = properties.getProperty("interiorPath");
		if(interiorPath != null)
		{
			// get values of interior
			Vector3f interiorScale = new Vector3f(getVector3f(properties, "interiorScale", 1));
			Vector3f interiorRotation = new Vector3f(getVector3f(properties, "interiorRotation", 0));
			Vector3f interiorTranslation = new Vector3f(getVector3f(properties, "interiorTranslation", 0));
			
			try{
				
				// load interior model
				Spatial interior = sim.getAssetManager().loadModel(interiorPath);
				
				// set name of interior spatial to "interior" (for culling in class SimulatorCam)
				interior.setName("interior");
				
				// add properties to interior model
				interior.setLocalScale(interiorScale);
				Quaternion quaternion = new Quaternion();
				quaternion.fromAngles(interiorRotation.x * FastMath.DEG_TO_RAD, 
						interiorRotation.y * FastMath.DEG_TO_RAD, interiorRotation.z * FastMath.DEG_TO_RAD);
				interior.setLocalRotation(quaternion);
				interior.setLocalTranslation(interiorTranslation);
				
				// add interior spatial to car node
				carNode.attachChild(interior);
			
			} catch (Exception ex) {
				System.err.println("Car interior '" + interiorPath + "' could not be loaded");
				ex.printStackTrace();
			}
			
		}
	}
	private Vector3f getVector3f(Properties properties, String key, float defaultValue) {
		float x = defaultValue;
		float y = defaultValue;
		float z = defaultValue;

		String xValue = properties.getProperty(key + ".x");
		if (xValue != null)
			x = Float.parseFloat(xValue);

		String yValue = properties.getProperty(key + ".y");
		if (yValue != null)
			y = Float.parseFloat(yValue);

		String zValue = properties.getProperty(key + ".z");
		if (zValue != null)
			z = Float.parseFloat(zValue);

		return new Vector3f(x, y, z);
	}

}
