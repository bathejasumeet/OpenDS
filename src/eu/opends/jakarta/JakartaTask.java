package eu.opends.jakarta;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import eu.opends.main.Simulator;
import eu.opends.taskDescription.contreTask.ContreReport;
import eu.opends.taskDescription.contreTask.PrimaryTask;
import eu.opends.taskDescription.contreTask.SecondaryTask;
import eu.opends.taskDescription.contreTask.SteeringTaskSettings;
import eu.opends.taskDescription.contreTask.SteeringTask.SteeringTaskType;
import eu.opends.tools.Util;

public class JakartaTask {
	private Simulator sim;
	private String driverName;
	private Vector3f startPointLogging;
	private Vector3f ptStartPoint;
	private Vector3f stStartPoint;
	private Vector3f endPointLogging;
	private Vector3f ptEndPoint;
	private Vector3f stEndPoint;
	private SteeringTaskType steeringTaskType;
	private Long conditionNumber;
	private PrimaryTask primaryTask;
	private SecondaryTask secondaryTask;
	private Random randomizer = new Random();
	private float steeringIntensity = 0;
	private float steeringPosChange = 0;
	private Spatial trafficLightObject = null;
	private Spatial targetObject = null;
	private Spatial steeringObject = null;
	private Float distanceToObjects;
	private Float objectOffset;
	private Float heightOffset;
	private Float maxLeftTargetObject;
	private Float maxRightTargetObject;
	private Float maxLeftSteeringObject;
	private Float maxRightSteeringObject;
	private float steeringObjectPos = 0;
	private float currentPos = 0;
	private float targetPos = 0;
	private Float lateralSpeedOfTargetObject;
	private Float lateralSpeedOfSteeringObject;
	private float pauseCounter = 0;
	private Integer msToPause;
	private Integer targetObjectBlinkingInterval;
	private boolean isBlinking = false;
	private long timestampOfLastBlinkStateChange = new GregorianCalendar().getTimeInMillis();
	private ContreReport report;
	private boolean logIsRunning = false;
	private Boolean createReport;
	private boolean blinkStateOn = false;
	private Material steeringObjectMaterial;
	private long startTime;
	private String markerID = null;
	private boolean isPausedJakartaTask = false;
	
	// TODO use global action listener
	private ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {
			if (name.equals("pauseJakartaTask") && !keyPressed) {
				isPausedJakartaTask = !isPausedJakartaTask;
			}
		}
	};
	
	public JakartaTask(Simulator sim, String driverName)
	{
		this.sim = sim;
		this.driverName = driverName;
		
	    // Map input to named action and add it to the action listener // TODO get from global action listener
	    sim.getInputManager().addMapping("pauseSteeringTask",  new KeyTrigger(KeyInput.KEY_H));
	    sim.getInputManager().addListener(actionListener, "pauseSteeringTask");
	    
		// load settings from driving task
		SteeringTaskSettings steeringTaskSettings = Simulator.getDrivingTask().
				getTaskLoader().getSteeringTaskSettings();
		
		startPointLogging = steeringTaskSettings.getStartPointLogging();
		ptStartPoint = steeringTaskSettings.getPtStartPoint();
		stStartPoint = steeringTaskSettings.getStStartPoint();
		endPointLogging = steeringTaskSettings.getEndPointLogging();
		ptEndPoint = steeringTaskSettings.getPtEndPoint();
		stEndPoint = steeringTaskSettings.getStEndPoint();
		
		String reportTemplate;
		
		if(steeringTaskSettings.getSteeringTaskType().equalsIgnoreCase("passenger"))
		{
			steeringTaskType = SteeringTaskType.PASSENGER;
			reportTemplate = "assets/JasperReports/templates/passenger.jasper"; //TODO
		}
		else
		{
			steeringTaskType = SteeringTaskType.DRIVER;
			reportTemplate = "assets/JasperReports/templates/driver.jasper"; //TODO
		}
		
		conditionNumber = steeringTaskSettings.getConditionNumber();
		distanceToObjects = steeringTaskSettings.getDistanceToObjects();
		objectOffset = steeringTaskSettings.getObjectOffset();
		heightOffset = steeringTaskSettings.getHeightOffset();
		maxLeftTargetObject = steeringTaskSettings.getTargetObjectMaxLeft();
		maxRightTargetObject = steeringTaskSettings.getTargetObjectMaxRight();
		maxLeftSteeringObject = steeringTaskSettings.getSteeringObjectMaxLeft();
		maxRightSteeringObject = steeringTaskSettings.getSteeringObjectMaxRight();
		lateralSpeedOfTargetObject = steeringTaskSettings.getTargetObjectSpeed();
		lateralSpeedOfSteeringObject = steeringTaskSettings.getSteeringObjectSpeed();
		msToPause = steeringTaskSettings.getPauseAfterTargetSet();
		targetObjectBlinkingInterval = steeringTaskSettings.getBlinkingInterval();
		createReport = steeringTaskSettings.getCreateReport();
		
		String trafficLightObjectId = steeringTaskSettings.getTrafficLightObjectId();
		if(!"".equals(trafficLightObjectId))
			trafficLightObject = sim.getSceneNode().getChild(trafficLightObjectId);
		
		String targetObjectId = steeringTaskSettings.getTargetObjectId();
		if(!"".equals(targetObjectId))
				targetObject = sim.getSceneNode().getChild(targetObjectId);
		
		String steeringObjectId = steeringTaskSettings.getSteeringObjectId();
		if(!"".equals(steeringObjectId))
				steeringObject = sim.getSceneNode().getChild(steeringObjectId);
		
		// save original material of steeringObject for restoring after blinking
		List<Geometry> list = Util.getAllGeometries(steeringObject);
		if(!list.isEmpty())
			steeringObjectMaterial = (list.get(0)).getMaterial();

		if(createReport)
		{
            // load database settings from driving task
    		String url = steeringTaskSettings.getDatabaseUrl();
    		String user = steeringTaskSettings.getDatabaseUser();
    		String pass = steeringTaskSettings.getDatabasePassword();
    		String table = steeringTaskSettings.getDatabaseTable();
    		
    		/*boolean useAdditionalTable = true;
    		if(steeringTaskSettings.getUseAdditionalTable() != null)
				useAdditionalTable = steeringTaskSettings.getUseAdditionalTable();
    		
    		float maxDeviation = 1;
    		if(steeringTaskSettings.getMaxDeviation() != null)
    			maxDeviation = steeringTaskSettings.getMaxDeviation();
    		
    		report = new ContreReport(reportTemplate, url, user, pass, table, useAdditionalTable, maxDeviation);*/
		}
	}
}
