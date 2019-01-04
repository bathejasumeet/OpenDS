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

package eu.opends.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jme3.input.ChaseCamera;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.ui.Picture;

import eu.opends.basics.SimulationBasics;
import eu.opends.drivingTask.settings.SettingsLoader;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;
import eu.opends.oculusRift.StereoCamAppState;

import com.jme3.post.FilterPostProcessor;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.post.filters.ColorOverlayFilter;

/**
 * 
 * @author Rafael Math
 */
public abstract class CameraFactory {
	protected SimulationBasics sim;
	protected SettingsLoader settingsLoader;
	protected CameraMode camMode = CameraMode.EGO;
	protected boolean topViewEnabled = false;
	protected MirrorMode mirrorMode = MirrorMode.OFF;
	protected ChaseCamera chaseCam;
	protected CameraNode topViewCamNode;
	protected CameraNode mainCameraNode = new CameraNode();
	protected CameraNode frontCameraNode = new CameraNode();

	protected Node targetNode;
	protected Camera cam;
	protected static ViewPort topViewPort;
	protected static ViewPort backViewPort;
	protected static ViewPort leftBackViewPort;
	protected static ViewPort rightBackViewPort;
	protected Picture topViewFrame;
	protected Picture backMirrorFrame;
	protected Picture leftMirrorFrame;
	protected Picture rightMirrorFrame;
	protected float topViewVerticalDistance;
	protected float topViewcarOffset;
	protected CameraNode centerCamNode;

	// if false: TOP camera will always show north at the top of the screen
	// if true: car is pointing to the top of the screen (moving map)
	protected boolean isCarPointingUp = true;

	protected Vector3f outsideCamPos = new Vector3f(-558f, 22f, -668f);

	private float angleBetweenAdjacentCameras;

	private int width;
	private int height;
	private float aspectRatio;
	private float frustumNear;
	private float frustumFar;

	protected SimulationBasics backgroundSim;
	protected SettingsLoader backgroundSettingsLoader;
	protected Node backgroundTargetNode;
	protected Camera backgroundCam;
	protected static ViewPort backgroundViewPort;
	private CameraNode backgroundCameraNode = new CameraNode();
	protected CameraNode backgroundCenterCamNode;
	protected Vector3f backgroundOutsideCamPos = new Vector3f(-558f, 22f, -668f);
	protected MirrorMode backgroundMirrorMode = MirrorMode.OFF;
	
	protected static ViewPort radarViewPort;
	protected static ViewPort rgbViewPort;
	
	protected ScreenshotAppState screenshotAppState = new ScreenshotAppState("", "LiDAR_", 0);
	protected ScreenshotAppState screenshotAppState_rgb = new ScreenshotAppState("", "RGB_", 0);
	
	protected static ViewPort backgroundHighViewPort;
	protected CameraNode backgroundHighCameraNode = new CameraNode();
	protected Camera backgroundHighCam;
	protected Node backgroundHighTargetNode;
	public static float bgHighCamXMid = 0f;
	public static float bgHighCamYMid = 0f;
	public static String lastScreenshotPath = "";

	private static ArrayList<ViewPort> viewPortList = new ArrayList<ViewPort>();
	
	protected CameraNode radarCamNode;
	protected CameraNode rgbCamNode;

	public static ArrayList<ViewPort> getViewPortList() {
		return viewPortList;
	}

	public static ViewPort getTopViewPort() {
		return topViewPort;
	}

	public static ViewPort getBackViewPort() {
		return backViewPort;
	}
	
	public Node getBackgroundCameraNode() {
		return backgroundCameraNode;
	}

	public static ViewPort getLeftBackViewPort() {
		return leftBackViewPort;
	}

	public static ViewPort getRightBackViewPort() {
		return rightBackViewPort;
	}

	public static ViewPort getBackgroundViewPort() {
		return backgroundViewPort;
	}
	
	public static ViewPort getBackgroundHighViewPort() {
		return backgroundHighViewPort;
	}

	/**
	 * Get main camera node which contains all scene cameras.
	 * 
	 * @return Node containing all scene cameras.
	 */
	public CameraNode getMainCameraNode() {
		return mainCameraNode;
	}

	public CameraNode getCenterCamNode() {
		return centerCamNode;
	}

	/**
	 * Camera views that can be activated while driving
	 */
	public enum CameraMode {
		CHASE, TOP, EGO, STATIC_BACK, OUTSIDE, OFF
	}

	/**
	 * Camera views that can be activated while driving
	 */
	public enum MirrorMode {
		OFF, BACK_ONLY, ALL, SIDE_ONLY
	}

	/**
	 * Get the current camera view.
	 * 
	 * @return Current camera view.
	 */
	public CameraMode getCamMode() {
		return camMode;
	}

	public boolean isTopViewEnabled() {
		return topViewEnabled;
	}

	public void setTopViewEnabled(boolean enabled) {
		topViewEnabled = enabled;
	}

	/**
	 * Get the current mirror mode.
	 * 
	 * @return Current mirror mode.
	 */
	public MirrorMode getMirrorMode() {
		return mirrorMode;
	}

	/**
	 * Set which mirror is visible or not
	 * 
	 * @param mode
	 *            parameter indicating visibility of rear and side view mirrors
	 */
	public void setMirrorMode(MirrorMode mode) {
		// user may only change mirror mode in ego camera mode
		if (camMode == CameraMode.EGO)
			mirrorMode = mode;
	}

	/**
	 * Setup all scene cameras.
	 * 
	 * @param sim
	 *            Simulator or Analyzer
	 *
	 * @param targetNode
	 *            target node the camera is pointing towards (Analyzer only!)
	 */
	public void initCamera(SimulationBasics sim, Node targetNode) {
		this.sim = sim;
		this.targetNode = targetNode;
		this.cam = sim.getCamera();
		this.settingsLoader = SimulationBasics.getSettingsLoader();

		mainCameraNode.attachChild(frontCameraNode);

		isCarPointingUp = settingsLoader.getSetting(Setting.General_topView_carPointingUp, true);

		float outsideCamPosX = settingsLoader.getSetting(Setting.General_outsideCamPosition_x, -558f);
		float outsideCamPosY = settingsLoader.getSetting(Setting.General_outsideCamPosition_y, 22f);
		float outsideCamPosZ = settingsLoader.getSetting(Setting.General_outsideCamPosition_z, -668f);
		outsideCamPos = new Vector3f(outsideCamPosX, outsideCamPosY, outsideCamPosZ);

		this.frustumNear = settingsLoader.getSetting(Setting.General_frustumNear, 0.2f);
		// changed from 1.0f
		this.frustumFar = settingsLoader.getSetting(Setting.General_frustumFar, 2000f);

		// only render scene node (child of root node)
		// as root node contains for instance the map marker
		sim.getViewPort().detachScene(sim.getRootNode());
		sim.getViewPort().attachScene(sim.getSceneNode());

		this.width = sim.getSettings().getWidth();
		this.height = sim.getSettings().getHeight();
		this.aspectRatio = (float) width / (float) height;

		// set initial mirror state
		String mirrorModeString = settingsLoader.getSetting(Setting.General_mirrorMode, "off");
		if (mirrorModeString.isEmpty())
			mirrorModeString = "off";
		this.mirrorMode = MirrorMode.valueOf(mirrorModeString.toUpperCase());

		angleBetweenAdjacentCameras = settingsLoader.getSetting(Setting.General_angleBetweenAdjacentCameras, 40);
		if (angleBetweenAdjacentCameras > 90 || angleBetweenAdjacentCameras < 0) {
			System.err.println(
					"Angle between adjacent cameras must be within 0 to 90 degrees. Set to default: 40 degrees.");
			angleBetweenAdjacentCameras = 40;
		}

		int numberOfScreens = sim.getNumberOfScreens();
		if (numberOfScreens > 1) {
			// clear default cam
			sim.getRenderManager().getMainView("Default").clearScenes();

			// add one camera for each screen
			for (int i = 1; i <= numberOfScreens; i++)
				setupCamera(i, numberOfScreens);
		} else
			setupCenterCamera();

		if (sim instanceof Simulator) {
			setupTopView();
			setupBackCamera();
			setupLeftBackCamera();
			setupRightBackCamera();
			setupRadarCamera();
			setupRGBCamera();
		}

		setupChaseCamera();
		
		screenshotAppState.initialize(sim.getRenderManager(), getViewPortList().get(0));
		sim.getStateManager().attach(screenshotAppState);
		
		screenshotAppState_rgb.initialize(sim.getRenderManager(), getViewPortList().get(0));
		sim.getStateManager().attach(screenshotAppState_rgb);
	}
	
	/**
	 * Setup background scene camera.
	 * 
	 * @param sim
	 *            Simulator or Analyzer
	 *
	 * @param targetNode
	 *            target node the camera is pointing towards (Analyzer only!)
	 */
	public void initBackgroundHighCamera(SimulationBasics sim) {
		this.backgroundSim = sim;
		this.backgroundHighCam = sim.getCamera();
		this.backgroundSettingsLoader = SimulationBasics.getSettingsLoader();

		//mainCameraNode.attachChild(backgroundHighCameraNode);

		// only render scene node (child of root node)
		// as root node contains for instance the map marker
		sim.getViewPort().detachScene(sim.getRootNode());
		sim.getViewPort().attachScene(sim.getSceneNode());

		setupHighBackgroundCamera();
	}

	public float getAngleBetweenAdjacentCameras() {
		return angleBetweenAdjacentCameras;
	}

	public void setAngleBetweenAdjacentCameras(float angle) {
		System.err.println("Angle between adjacent cameras: " + angle);

		angleBetweenAdjacentCameras = angle;

		int numberOfScreens = sim.getNumberOfScreens();
		float width = FastMath.tan(angle * FastMath.DEG_TO_RAD * .5f) * frustumNear;
		float height = width * numberOfScreens / aspectRatio;

		for (int i = 0; i < frontCameraNode.getChildren().size(); i++) {
			Spatial cam_i = frontCameraNode.getChildren().get(i);
			float localAngle = (((numberOfScreens + 1) / 2) - (i + 1)) * angle;
			((CameraNode) cam_i)
					.setLocalRotation(new Quaternion().fromAngles(0, (180 + localAngle) * FastMath.DEG_TO_RAD, 0));
			((CameraNode) cam_i).getCamera().setFrustum(frustumNear, frustumFar, -width, width, height, -height);
		}
	}

	public abstract void setCamMode(CameraMode mode);

	public abstract void changeCamera();

	public abstract void updateCamera();

	private void setupCamera(int index, int totalInt) {
		float total = totalInt;
		float additionalPixel = 1f / width;
		float viewPortLeft = (index - 1) / total;
		float viewPortRight = (index) / total + additionalPixel;
		float angle = (((totalInt + 1) / 2) - index) * angleBetweenAdjacentCameras;

		// setup camera
		Camera cam_i;
		if (index == 1)
			cam_i = cam;
		else
			cam_i = new Camera(width, height);

		// setup frustum according to number and angle of different cameras
		float width = FastMath.tan(angleBetweenAdjacentCameras * FastMath.DEG_TO_RAD * .5f) * frustumNear;
		float height = width * total / aspectRatio;
		cam_i.setFrustum(frustumNear, frustumFar, -width, width, height, -height);
		cam_i.setParallelProjection(false);
		cam_i.setViewPort(viewPortLeft, viewPortRight, 0f, 1f);

		// setup camera node and add it to main camera node
		CameraNode camNode = new CameraNode("CamNode" + index, cam_i);
		camNode.setControlDir(ControlDirection.SpatialToCamera);
		camNode.setLocalTranslation(new Vector3f(0, 0, 0));
		camNode.setLocalRotation(new Quaternion().fromAngles(0, (180 + angle) * FastMath.DEG_TO_RAD, 0));
		frontCameraNode.attachChild(camNode);

		// setup view port
		ViewPort viewPort = sim.getRenderManager().createMainView("View" + index, cam_i);
		viewPort.setClearFlags(true, true, true);
		viewPort.attachScene(sim.getSceneNode());
		viewPortList.add(viewPort);
	}

	/**
	 * Setup center camera (always on)
	 */
	private void setupCenterCamera() {
		// OpenDS-Rift
		Spatial obs = null;
		if (Simulator.oculusRiftAttached) {
			obs = sim.getObserver();
			StereoCamAppState scas = sim.getStereoCamAppState();
			obs.addControl(scas.getCameraControl());
		}

		// add center camera to main camera node
		centerCamNode = new CameraNode("CamNode1", cam);
		centerCamNode.setControlDir(ControlDirection.SpatialToCamera);
		frontCameraNode.attachChild(centerCamNode);
		centerCamNode.setLocalTranslation(new Vector3f(0, 0, 0));
		centerCamNode.setLocalRotation(new Quaternion().fromAngles(0, 180 * FastMath.DEG_TO_RAD, 0));

		// frustumNear = 0.2f used for internal car environment
		centerCamNode.getCamera().setFrustumPerspective(30.5f, aspectRatio, frustumNear, frustumFar);

		// OpenDS-Rift
		if (Simulator.oculusRiftAttached) {
			centerCamNode.attachChild(obs);
		}

		viewPortList.add(sim.getViewPort());
	}
	
	/**
	 * Setup second background camera (always on)
	 */
	private void setupHighBackgroundCamera() {
		Camera bgCam = backgroundHighCam.clone();
		bgCam.setViewPort(0f, 1f, 0f, 1f);

		backgroundHighViewPort = backgroundSim.getRenderManager().createPreView("BackgroundHighView", bgCam);
		backgroundHighViewPort.setClearFlags(true, true, true);
		backgroundHighViewPort.attachScene(backgroundSim.getSceneNode());
		backgroundHighViewPort.setEnabled(false);
		viewPortList.add(backgroundHighViewPort);

		// add background camera to main camera node
		CameraNode backgroundHighCamNode = new CameraNode("BackgroundHighCamNode", bgCam);
		backgroundHighCamNode.setControlDir(ControlDirection.SpatialToCamera);
		backgroundHighCamNode
				.setLocalRotation(new Quaternion().fromAngles(90 * FastMath.DEG_TO_RAD, 90 * FastMath.DEG_TO_RAD, 0));
		backgroundHighCamNode.setLocalTranslation(new Vector3f(0, 10, 0));
		try {
			List<Float> xCoords = new ArrayList<>();
			List<Float> yCoords = new ArrayList<>();
			for (Spatial sp : backgroundSim.getSceneNode().getChildren()) {
				if (sp.getName() != null){
					if (sp.getName().contains("TrafficCone")) {
						xCoords.add(sp.getLocalTranslation().x);
						yCoords.add(sp.getLocalTranslation().z);
					}
				}
			}
			float xMax = Collections.max(xCoords);
			float yMax = Collections.max(yCoords);
			float xMin = Collections.min(xCoords);
			float yMin = Collections.min(yCoords);
			bgHighCamXMid = xMin + ((xMax - xMin) / 2);
			bgHighCamYMid = yMin + ((yMax - yMin) / 2);
			Node highbox = (Node) backgroundSim.getSceneNode().getChild("highbox");
			highbox.setLocalTranslation(new Vector3f(bgHighCamXMid, 10f, bgHighCamYMid));
			highbox.attachChild(backgroundHighCamNode);
		} catch (Exception e) {
			System.out.println("There is either no highbox or no traffic cones to attach high cam to");
		}		
	}

	/**
	 * Setup top view
	 */
	private void setupTopView() {
		Camera topViewCam = cam.clone();

		float left = settingsLoader.getSetting(Setting.General_topView_viewPortLeft, 0.05f);
		float right = settingsLoader.getSetting(Setting.General_topView_viewPortRight, 0.45f);
		float bottom = settingsLoader.getSetting(Setting.General_topView_viewPortBottom, 0.58f);
		float top = settingsLoader.getSetting(Setting.General_topView_viewPortTop, 0.98f);
		topViewVerticalDistance = settingsLoader.getSetting(Setting.General_topView_verticalDistance, 200f);
		topViewcarOffset = settingsLoader.getSetting(Setting.General_topView_carOffset, 40f);

		/*
		 * if(sim.getNumberOfScreens() > 1) { left = 0.15f; right = 0.35f;
		 * bottom = 0.78f; top = 0.98f; }
		 */

		topViewFrame = createMirrorFrame("topViewFrame", left, right, bottom, top);
		sim.getGuiNode().attachChild(topViewFrame);

		float aspect = ((right - left) * width) / ((top - bottom) * height);

		topViewCam.setFrustumPerspective(30.0f, aspect, frustumNear, frustumFar);
		// topViewCam.setFrustum(1, 2000, -20, 20, 20, -20);
		topViewCam.setViewPort(left, right, bottom, top);
		// topViewCam.setParallelProjection(true);

		// set view port (needed to show/hide top view)
		topViewPort = sim.getRenderManager().createMainView("TopView", topViewCam);
		topViewPort.setClearFlags(true, true, true);
		topViewPort.attachScene(sim.getRootNode()); // use root node to
													// visualize map marker
		topViewPort.setEnabled(false);
		viewPortList.add(topViewPort);

		// add top view camera to main camera node
		topViewCamNode = new CameraNode("topViewCamNode", topViewCam);
		sim.getRootNode().attachChild(topViewCamNode);
	}

	/**
	 * Setup rear view mirror
	 */
	private void setupBackCamera() {
		Camera backCam = cam.clone();

		float left = settingsLoader.getSetting(Setting.General_rearviewMirror_viewPortLeft, 0.3f);
		float right = settingsLoader.getSetting(Setting.General_rearviewMirror_viewPortRight, 0.7f);
		float bottom = settingsLoader.getSetting(Setting.General_rearviewMirror_viewPortBottom, 0.78f);
		float top = settingsLoader.getSetting(Setting.General_rearviewMirror_viewPortTop, 0.98f);
		float horizontalAngle = settingsLoader.getSetting(Setting.General_rearviewMirror_horizontalAngle, 0f);
		float verticalAngle = settingsLoader.getSetting(Setting.General_rearviewMirror_verticalAngle, 0f);

		/*
		 * if(sim.getNumberOfScreens() > 1) { left = 0.4f; right = 0.6f; bottom
		 * = 0.78f; top = 0.98f; }
		 */

		backMirrorFrame = createMirrorFrame("backViewFrame", left, right, bottom, top);
		sim.getGuiNode().attachChild(backMirrorFrame);

		float aspect = ((right - left) * width) / ((top - bottom) * height);

		backCam.setFrustumPerspective(30.0f, aspect, frustumNear, frustumFar);
		backCam.setViewPort(left, right, bottom, top);

		// inverse back view cam (=> back view mirror)
		Matrix4f matrix = backCam.getProjectionMatrix().clone();
		matrix.m00 = -matrix.m00;
		backCam.setProjectionMatrix(matrix);

		// set view port (needed to show/hide mirror)
		backViewPort = sim.getRenderManager().createMainView("BackView", backCam);
		backViewPort.setClearFlags(true, true, true);
		backViewPort.attachScene(sim.getSceneNode());
		backViewPort.setEnabled(false);
		viewPortList.add(backViewPort);

		// add back camera to main camera node
		CameraNode backCamNode = new CameraNode("BackCamNode", backCam);
		backCamNode.setControlDir(ControlDirection.SpatialToCamera);
		backCamNode.setLocalRotation(new Quaternion().fromAngles(verticalAngle * FastMath.DEG_TO_RAD,
				horizontalAngle * FastMath.DEG_TO_RAD, 0));
		Vector3f centerMirrorPos = ((Simulator) sim).getCar().getCarModel().getCenterMirrorPos();
		backCamNode.setLocalTranslation(new Vector3f(centerMirrorPos));
		mainCameraNode.attachChild(backCamNode);
	}

	/**
	 * Setup left rear view mirror
	 */
	private void setupLeftBackCamera() {
		float left = settingsLoader.getSetting(Setting.General_leftMirror_viewPortLeft, 0.02f);
		float right = settingsLoader.getSetting(Setting.General_leftMirror_viewPortRight, 0.2f);
		float bottom = settingsLoader.getSetting(Setting.General_leftMirror_viewPortBottom, 0.3f);
		float top = settingsLoader.getSetting(Setting.General_leftMirror_viewPortTop, 0.6f);
		float horizontalAngle = settingsLoader.getSetting(Setting.General_leftMirror_horizontalAngle, -45f);
		float verticalAngle = settingsLoader.getSetting(Setting.General_leftMirror_verticalAngle, 10f);

		Camera leftBackCam = cam.clone();

		leftMirrorFrame = createMirrorFrame("leftBackViewFrame", left, right, bottom, top);
		sim.getGuiNode().attachChild(leftMirrorFrame);

		float aspect = ((right - left) * width) / ((top - bottom) * height);

		leftBackCam.setFrustumPerspective(45.0f, aspect, frustumNear, frustumFar);
		leftBackCam.setViewPort(left, right, bottom, top);

		// inverse left back view cam (=> left back view mirror)
		Matrix4f matrix = leftBackCam.getProjectionMatrix().clone();
		matrix.m00 = -matrix.m00;
		leftBackCam.setProjectionMatrix(matrix);

		// set view port (needed to show/hide mirror)
		leftBackViewPort = sim.getRenderManager().createMainView("LeftBackView", leftBackCam);
		leftBackViewPort.setClearFlags(true, true, true);
		leftBackViewPort.attachScene(sim.getSceneNode());
		leftBackViewPort.setEnabled(false);
		viewPortList.add(leftBackViewPort);

		// add left back camera to main camera node
		CameraNode leftBackCamNode = new CameraNode("LeftBackCamNode", leftBackCam);
		leftBackCamNode.setControlDir(ControlDirection.SpatialToCamera);

		leftBackCamNode.setLocalRotation(new Quaternion().fromAngles(verticalAngle * FastMath.DEG_TO_RAD,
				horizontalAngle * FastMath.DEG_TO_RAD, 0));
		Vector3f leftMirrorPos = ((Simulator) sim).getCar().getCarModel().getLeftMirrorPos();
		leftBackCamNode.setLocalTranslation(new Vector3f(leftMirrorPos));
		mainCameraNode.attachChild(leftBackCamNode);
	}

	/**
	 * Setup right rear view mirror
	 */
	private void setupRightBackCamera() {
		float left = settingsLoader.getSetting(Setting.General_rightMirror_viewPortLeft, 0.8f);
		float right = settingsLoader.getSetting(Setting.General_rightMirror_viewPortRight, 0.98f);
		float bottom = settingsLoader.getSetting(Setting.General_rightMirror_viewPortBottom, 0.3f);
		float top = settingsLoader.getSetting(Setting.General_rightMirror_viewPortTop, 0.6f);
		float horizontalAngle = settingsLoader.getSetting(Setting.General_rightMirror_horizontalAngle, 45f);
		float verticalAngle = settingsLoader.getSetting(Setting.General_rightMirror_verticalAngle, 10f);

		Camera rightBackCam = cam.clone();

		rightMirrorFrame = createMirrorFrame("rightBackViewFrame", left, right, bottom, top);
		sim.getGuiNode().attachChild(rightMirrorFrame);

		float aspect = ((right - left) * width) / ((top - bottom) * height);

		rightBackCam.setFrustumPerspective(45.0f, aspect, frustumNear, frustumFar);
		rightBackCam.setViewPort(left, right, bottom, top);

		// inverse right back view cam (=> right back view mirror)
		Matrix4f matrix = rightBackCam.getProjectionMatrix().clone();
		matrix.m00 = -matrix.m00;
		rightBackCam.setProjectionMatrix(matrix);

		// set view port (needed to show/hide mirror)
		rightBackViewPort = sim.getRenderManager().createMainView("RightBackView", rightBackCam);
		rightBackViewPort.setClearFlags(true, true, true);
		rightBackViewPort.attachScene(sim.getSceneNode());
		rightBackViewPort.setEnabled(false);
		viewPortList.add(rightBackViewPort);

		// add right back camera to main camera node
		CameraNode rightBackCamNode = new CameraNode("RightBackCamNode", rightBackCam);
		rightBackCamNode.setControlDir(ControlDirection.SpatialToCamera);

		rightBackCamNode.setLocalRotation(new Quaternion().fromAngles(verticalAngle * FastMath.DEG_TO_RAD,
				horizontalAngle * FastMath.DEG_TO_RAD, 0));
		Vector3f rightMirrorPos = ((Simulator) sim).getCar().getCarModel().getRightMirrorPos();
		rightBackCamNode.setLocalTranslation(new Vector3f(rightMirrorPos));
		mainCameraNode.attachChild(rightBackCamNode);
	}

	private Picture createMirrorFrame(String name, float left, float right, float bottom, float top) {
		Picture mirrorImage = new Picture(name);
		mirrorImage.setImage(sim.getAssetManager(), "Textures/Misc/mirrorFrame.png", true);

		float imageWidth = (right - left) * width * 1.15f;
		mirrorImage.setWidth(imageWidth);

		float imageHeight = (top - bottom) * height * 1.15f;
		mirrorImage.setHeight(imageHeight);

		float x = left * width - (imageWidth * 0.055f);
		float y = bottom * height - (imageHeight * 0.055f);
		mirrorImage.setPosition(x, y);

		return mirrorImage;
	}

	/**
	 * Setup free camera (can be controlled with mouse)
	 */
	private void setupChaseCamera() {
		chaseCam = new ChaseCamera(cam, targetNode, sim.getInputManager());
		chaseCam.setUpVector(new Vector3f(0, 1, 0));
		chaseCam.setEnabled(false);

		// set visual parameters
		float minDistance = settingsLoader.getSetting(Setting.Mouse_minScrollZoom, 1f);
		chaseCam.setMinDistance(minDistance);

		float maxDistance = settingsLoader.getSetting(Setting.Mouse_maxScrollZoom, 40f);
		chaseCam.setMaxDistance(maxDistance);

		float zoomSensitivity = settingsLoader.getSetting(Setting.Mouse_scrollSensitivityFactor, 5f);
		chaseCam.setZoomSensitivity(zoomSensitivity);
	}
	
	/**
	 *	Setup radar view
	 */
	@SuppressWarnings("unused")
	private void setupRadarCamera() 
	{
		boolean enabled = settingsLoader.getSetting(Setting.General_radarCamera_enabled, false);
		
		if(enabled)
		{
			// show preview map view port during runtime
			boolean debug = settingsLoader.getSetting(Setting.General_radarCamera_debug, false);
			
			// resolution of frame buffer (=output resolution)
			int width = settingsLoader.getSetting(Setting.General_radarCamera_width, 400);
			int height = settingsLoader.getSetting(Setting.General_radarCamera_height, 400);
			
			Camera radarCam;
			
			if(debug)
				radarCam = cam.clone();
			else
				radarCam = new Camera(width, height);
			
			radarCam.setFrustumPerspective(20f, width/height, 1, 2000);
			if(debug)
				radarCam.setViewPort(0, 0.5f, 0, 0.5f);
	
			// set view port (needed to show/hide top view)
			radarViewPort = sim.getRenderManager().createMainView("RadarView", radarCam);
			radarViewPort.setClearFlags(true, true, true);
			radarViewPort.attachScene(sim.getSceneNode());
 			radarViewPort.setEnabled(true);
			
			if(!debug)
			{
				FrameBuffer fb = new FrameBuffer(width, height, 24);
				fb.setColorBuffer(Format.RGBA8);
				radarViewPort.setOutputFrameBuffer(fb);
			}
			
			// add (any) filter: required to "activate" view port
			FilterPostProcessor processor = new FilterPostProcessor(sim.getAssetManager());
	    	processor.addFilter(new ColorOverlayFilter());
	    	radarViewPort.addProcessor(processor);
			
		    viewPortList.add(radarViewPort);   
		    
		    // add radar view camera to main camera node
	    	radarCamNode = new CameraNode("radarCamNode", radarCam);
	    	radarCamNode.setControlDir(ControlDirection.SpatialToCamera);
	    	
	    	float verticalAngle = 0;
	    	float horizontalAngle = 180;
	    	radarCamNode.setLocalRotation(new Quaternion().fromAngles(verticalAngle*FastMath.DEG_TO_RAD, horizontalAngle*FastMath.DEG_TO_RAD, 0));
			
	    	Vector3f radarCamPos = new Vector3f(0,0.9f,0);
			radarCamNode.setLocalTranslation(radarCamPos);
	    	
			mainCameraNode.attachChild(radarCamNode);
		}
	}

	/**
	 *	Setup radar view
	 */
	@SuppressWarnings("unused")
	private void setupRGBCamera() 
	{
		boolean enabled = settingsLoader.getSetting(Setting.General_radarCamera_enabled, false);
		
		if(enabled)
		{
			// show preview map view port during runtime
			//boolean debug = settingsLoader.getSetting(Setting.General_radarCamera_debug, false);
			
			// resolution of frame buffer (=output resolution)
			int width = settingsLoader.getSetting(Setting.General_radarCamera_width, 400);
			int height = settingsLoader.getSetting(Setting.General_radarCamera_height, 400);
			
			Camera rgbCam;
			
			rgbCam = new Camera(width, height);
			
			rgbCam.setFrustumPerspective(20f, width/height, 1, 2000);
	
			// set view port (needed to show/hide top view)
			rgbViewPort = sim.getRenderManager().createMainView("RGBView", rgbCam);
			rgbViewPort.setClearFlags(true, true, true);
			rgbViewPort.attachScene(sim.getSceneNode());
			rgbViewPort.setEnabled(true);
			
			FrameBuffer fb = new FrameBuffer(width, height, 24);
			fb.setColorBuffer(Format.RGBA8);
			rgbViewPort.setOutputFrameBuffer(fb);
			
			// add (any) filter: required to "activate" view port
			FilterPostProcessor processor = new FilterPostProcessor(sim.getAssetManager());
	    	processor.addFilter(new ColorOverlayFilter());
	    	rgbViewPort.addProcessor(processor);
			
		    viewPortList.add(rgbViewPort);   
		    
		    // add radar view camera to main camera node
	    	rgbCamNode = new CameraNode("rgbCamNode", rgbCam);
	    	rgbCamNode.setControlDir(ControlDirection.SpatialToCamera);
	    	
	    	float verticalAngle = 0;
	    	float horizontalAngle = 180;
	    	rgbCamNode.setLocalRotation(new Quaternion().fromAngles(verticalAngle*FastMath.DEG_TO_RAD, horizontalAngle*FastMath.DEG_TO_RAD, 0));
			
	    	Vector3f rgbCamPos = new Vector3f(0,0.9f,0);
			rgbCamNode.setLocalTranslation(rgbCamPos);
	    	
			mainCameraNode.attachChild(rgbCamNode);
		}
	}
	
	
}
