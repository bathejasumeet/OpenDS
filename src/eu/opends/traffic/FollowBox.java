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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Spline.SplineType;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;

import eu.opends.car.SteeringCar;
import eu.opends.infrastructure.Segment;
import eu.opends.infrastructure.Waypoint;
import eu.opends.main.Simulator;
import eu.opends.tools.Util;

/**
 * 
 * @author Rafael Math
 */
public class FollowBox 
{
    // Distance where traffic objects of this FollowBox (ownSafetyDistanceToIntersection) 
    // will start slowing down to full stop if priority road ahead with traffic in less 
    // than their safety distance (othersSafetyDistanceToIntersection).
    private float ownSafetyDistanceToIntersection = 15; 
    private float othersSafetyDistanceToIntersection = 15;

    // Minimum time clearance between two traffic objects meeting at the same intersection.
    // If minIntersectionClearance = 5, a traffic object must wait for 5 seconds to enter 
    // an intersection which has been entered by a higher prioritized traffic object before.
    private float minIntersectionClearance = 5;
    
    
	private Simulator sim;
	private TrafficObject trafficObject;
	private FollowBoxSettings settings;
	private boolean trafficObjectInitialized = false;
    private Geometry followBox;
    private Waypoint currentFromWaypoint;
    private Segment currentSegment = null;
    private Segment nextSegment = null;
    private float traveledDistance = 0;
    private Waypoint resetWaypoint = null;
    private ArrayList<Segment> preferredSegments = new ArrayList<Segment>();
    private float distanceToNextWP = Float.MAX_VALUE;
    private int helperSegmentCounter = 1;
    private boolean obstacleInTheWay = false;
	private List<Waypoint> waypointList;
	private float minDistance;
	private float maxDistance;
    private MotionPath motionPath;
    private MotionEvent motionControl;
    //private Spatial followBox;
	private float carSpeed = 0;
	private int previousWayPointIndex = 0;
	private int targetWayPointIndex = 0;
	private boolean isTargetWayPointAvailable = false;
	private boolean waitForNextUpdate = true;
	private String name;

	
	public FollowBox(Simulator sim, TrafficObject trafficObject, FollowBoxSettings settings, boolean setToStartWayPoint, boolean activateOld)
	{
		this.sim = sim;
		this.trafficObject = trafficObject;
		this.settings = settings;
		this.trafficObject = trafficObject;
		
		name = trafficObject.getName();
		waypointList = settings.getWayPoints();
		minDistance = settings.getMinDistance();
		maxDistance = settings.getMaxDistance();
		
		motionPath = new MotionPath();

		motionPath.setCycle(settings.isPathCyclic());
		
		for(Waypoint wayPoint : waypointList)
			motionPath.addWayPoint(wayPoint.getPosition());

	    motionPath.setPathSplineType(SplineType.CatmullRom); // --> default: CatmullRom
	    motionPath.setCurveTension(settings.getCurveTension());
	    
	    if(settings.isPathVisible())
	    	motionPath.enableDebugShape(sim.getAssetManager(), sim.getSceneNode());

/*
		// does not trigger every way point reliably !!!!!
		// implemented own MotionPath listener in method "checkIfWayPointReached()"
        motionPath.addListener(new MotionPathListener() 
        {
            public void onWayPointReach(MotionEvent control, int wayPointIndex) 
            {
            	// set speed limit for next way point
            	int index = wayPointIndex % waypointList.size();
            	float speed = waypointList.get(index).getSpeed();
            	setSpeed(speed);
            	
            	// if last way point reached
                if (motionPath.getNbWayPoints() == wayPointIndex + 1) 
                {
                	// reset traffic object to first way point if not cyclic
                	if(!motionPath.isCycle())
                	{
                		setToWayPoint(0);
                		System.err.print(", reset");
                	}
                }
                
            }
        });
*/
	    
	    followBox = createFollowBox() ;
	    motionControl = new MotionEvent(followBox,motionPath);
	    
	    // get start way point
	    int startWayPointIndex = settings.getStartWayPointIndex(activateOld);
	    if(setToStartWayPoint)
	    	setToWayPoint(startWayPointIndex);	    
        
        // set start speed
	    float initialSpeed = waypointList.get(startWayPointIndex).getSpeed();
	    setSpeed(initialSpeed);

	    // move object along path considering rotation
        motionControl.setDirectionType(MotionEvent.Direction.PathAndRotation);
        
        // loop movement of object
        motionControl.setLoopMode(LoopMode.Loop);
        
        // rotate moving object
        //motionControl.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
        
        // set moving object to position "20 seconds"
        //motionPath.interpolatePath(20, motionControl, tpf);

        // start movement
        motionControl.play(); // already contained in update method
	}

	public FollowBox(Simulator sim, TrafficObject trafficObject, FollowBoxSettings settings, boolean setToStartWayPoint)
	{		
		this.sim = sim;
		this.settings = settings;
		this.trafficObject = trafficObject;
		preferredSegments = settings.getPreferredSegments();
		ownSafetyDistanceToIntersection = settings.getGiveWayDistance();
		othersSafetyDistanceToIntersection = settings.getIntersectionObservationDistance();
		minIntersectionClearance = settings.getMinIntersectionClearance();

        createFollowBoxGeometry();
		
        String startWaypointID = settings.getStartWayPointID();
        Waypoint startWaypoint = sim.getRoadNetwork().getWaypoint(startWaypointID);
        
        // check if startWaypointID exists
        if(startWaypoint != null)
        {
        	boolean success = setFollowBoxToWP(startWaypoint);

	        if(success && setToStartWayPoint)
	        	setToWayPoint(startWaypointID);
        }
        else
        	System.err.println("Could not set " + trafficObject.getName() + " to non-existing way point " + startWaypointID);
	}
	

	




	int counter = 0;
	private void createFollowBoxGeometry()
	{
		Material materialGreen = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        materialGreen.getAdditionalRenderState().setWireframe(true);
        materialGreen.setColor("Color", ColorRGBA.Green);
        
        followBox = new Geometry("followBox", new Box(0.3f, 0.3f, 0.3f));
        followBox.setMaterial(materialGreen);
        sim.getRoadNetwork().getDebugNode().attachChild(followBox);
	}

		public void update(float tpf, Vector3f trafficObjectCenterPos)
	{
		// skip update during pause
		if(sim.isPause() || currentSegment==null)
			return;

		computeDistanceToNextWP();
		
		// if traffic object has crashed / got stuck (i.e. not moved significantly for a longer time)
		if(hasCrashed())
			setToWayPoint(settings.getStartWayPointID());
		
		// if new WP to set traffic object available
		if(resetWaypoint != null)
		{
			//System.err.println("RESET");
			
			// set traffic object to new position
	        performWayPointChange(resetWaypoint);
	        resetWaypoint = null;
		}
		
		// wait one frame for traffic object to be placed, then start follow box
		if(!trafficObjectInitialized)
		{
			trafficObjectInitialized = true;
			return;
		}
	

		stopTrafficObject = false;
		float minDistance = settings.getMinDistance();
		float maxDistance = settings.getMaxDistance();
		float currentDistance = getCurrentDistance(trafficObjectCenterPos);
		boolean hasLostSteeringCar = hasLostSteeringCar(currentDistance);
		
		if(hasLostSteeringCar)
		{
			// move followBox not more than "maxDistance" meters when SteeringCar is lost in order to 
			// prevent missing the steering car (auto-pilot only!)
			traveledDistance += maxDistance;
		}
		else
		{
			// set limits
			currentDistance = Math.max(Math.min(maxDistance, currentDistance), minDistance);

			//maxDistance --> 0.0
			//minDistance --> 2.0 (speed of follow box may be up to two times faster than speed of car)
			float factor = (1.0f - ((currentDistance-minDistance)/(maxDistance-minDistance)))*2.0f;
			
			float speed = currentSegment.getSpeed()/3.6f;
			traveledDistance += speed*tpf*factor;
		}
		
		float progress = Math.max(0, Math.min(1, traveledDistance/currentSegment.getLength()));
		
		/*
		if(nextSegment != null)
		{
			Spline spline = new Spline();

			spline.addControlPoint(currentSegment.getFromWaypoint().getPosition());
			spline.addControlPoint(currentSegment.getToWaypoint().getPosition());
			spline.addControlPoint(nextSegment.getToWaypoint().getPosition());
			
			spline.setType(SplineType.CatmullRom);
			spline.setCurveTension(0.05f);
			
			followBox.setLocalTranslation(spline.interpolate(progress,0,null));
			
	        Material materialBlue = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	        materialBlue.getAdditionalRenderState().setWireframe(true);
	        materialBlue.setColor("Color", ColorRGBA.Blue);
	     
			Node debugNode = sim.getRoadNetwork().getDebugNode();
			
			Curve curve = new Curve(spline, 10);
	        Geometry lineGeometry = new Geometry("segment_" + currentSegment.getName(), curve);
	        		
	        lineGeometry.setMaterial(materialBlue);
			debugNode.attachChild(lineGeometry);
		}
		else*/
			followBox.setLocalTranslation(currentSegment.interpolate(progress));


		boolean isRightHandTraffic = sim.getRoadNetwork().isRightHandTraffic();

		// if obstacle in the segment (lane) try to change to passing segment (lane)
		Segment passingSegment = getPassingSegment(isRightHandTraffic);
		if(obstacleInTheWay && passingSegment!=null && passingSegment.isClear(trafficObject, traveledDistance))
			changeToNeighborSegment(progress, passingSegment);
		
		// if regular segment (lane) is clear (again) try to change to regular segment (lane)
		Segment regularSegment = getRegularSegment(isRightHandTraffic);
		if(regularSegment!=null && regularSegment.isClear(trafficObject, traveledDistance))
			changeToNeighborSegment(progress, regularSegment);

		
		// way point reached
		if(progress >= 1.0f)
		{
			if(hasLostSteeringCar)
			{
				// keep follow box close to steering car when auto-pilot is switched off
				currentFromWaypoint = sim.getRoadNetwork().getRandomNearbyWaypoint(trafficObject.getPosition());
				currentSegment = currentFromWaypoint.getNextSegment(preferredSegments);
				nextSegment = currentSegment.getToWaypoint().getNextSegment(preferredSegments);
				traveledDistance = 0;
			}
			else
			{
				// usual handling when way point reached
				Waypoint currentToWaypoint = currentSegment.getToWaypoint();
				if(waitAtWP(currentToWaypoint))
				{
					stopTrafficObject = true;
				}
				else
				{
					//System.err.println("finished: " + currentSegment.getFromWaypointString() + 
					//		" --(" + currentSegment.getName() + ")--> " + currentSegment.getToWaypointString());
					
					if(nextSegment == null)
					{
						// stop traffic object at current position forever
						stopTrafficObject = true;
					}
					else
					{
						if(nextSegment.isJump())
						{
							//System.err.println("finished: " + nextSegment.getFromWaypointString() + 
							//		" --(" + nextSegment.getName() + ")--> " + nextSegment.getToWaypointString() + " (jump)");
							
							performWayPointChange(nextSegment.getToWaypoint());
						}
						else
						{
							// last WP not yet reached --> move on to next WP
							currentFromWaypoint = currentToWaypoint;
							currentSegment = nextSegment;
							nextSegment = nextSegment.getToWaypoint().getNextSegment(preferredSegments);
							
							// usually 0 - unless a segment is begun somewhere between FromWaypoint and 
							// ToWaypoint (e.g. changeToNeighborSegment)
							traveledDistance = currentSegment.getTraveledDistance(trafficObject);
						}
					}
				}
			}
		}
	}

	private Segment getRegularSegment(boolean rightHandTraffic)
	{
		Segment regularSegment;
		if(rightHandTraffic)
			regularSegment = currentSegment.getRightNeighbor();
		else
			regularSegment = currentSegment.getLeftNeighbor();
		return regularSegment;
	}


	private Segment getPassingSegment(boolean isRightHandTraffic)
	{
		Segment passingSegment;
		if(isRightHandTraffic)
			passingSegment = currentSegment.getLeftNeighbor();
		else
			passingSegment = currentSegment.getRightNeighbor();
		return passingSegment;
	}

	private void changeToNeighborSegment(float progress, Segment neighborSegment)
	{
		float progressOnNeighborSegment = progress + (30/neighborSegment.getLength());
		if(neighborSegment.getLength()>30 && progressOnNeighborSegment<=1.0)
		{
			// generate names for new way points and segment
			String helperStartWPName = trafficObject.getName() + "_helperStartWP" + helperSegmentCounter;
			String helperSegmentName = trafficObject.getName() + "_helperSegment" + helperSegmentCounter;
			String helperTargetWPName = trafficObject.getName() + "_helperTargetWP" + helperSegmentCounter;
			helperSegmentCounter++;
			
			// helper target way point (on neighbor segment)
			// needs to be added before helperStartWP as it is referenced by the outgoing segment of helperStartWP
			Vector3f targetPos = neighborSegment.interpolate(progressOnNeighborSegment);
			ArrayList<Segment> segmentList2 = new ArrayList<Segment>();
			segmentList2.add(neighborSegment);
			Waypoint helperTargetWP = new Waypoint(helperTargetWPName, targetPos, null, null, null, null, null, segmentList2);
			sim.getRoadNetwork().addWaypoint(helperTargetWP);
			
			// helper segment connecting start and target segment	
			Segment helperSegment = new Segment(helperSegmentName, helperStartWPName, new ArrayList<String>(), 
					helperTargetWPName, null, null, neighborSegment.getSpeed(), false, 1, new ArrayList<String>(), 0.05f);
			
			// helper start way point (= current position)
			Vector3f curPos = followBox.getLocalTranslation();
			ArrayList<Segment> segmentList1 = new ArrayList<Segment>();
			segmentList1.add(helperSegment);
			Waypoint helperStartWP = new Waypoint(helperStartWPName, curPos, null, null, null, null, null, segmentList1);
			sim.getRoadNetwork().addWaypoint(helperStartWP);
	
			// make a note of the starting position ("traveledDistance") of the follow box when reaching the neighbor segment
			neighborSegment.setTraveledDistance(progressOnNeighborSegment*neighborSegment.getLength(), trafficObject);
			
			// update follow box values
			currentFromWaypoint = helperStartWP;				
			currentSegment = helperSegment;
			nextSegment = neighborSegment;
			traveledDistance = 0;
			
			//System.err.println(trafficObject.getName() + " changed");
		}
		//else
		//	System.err.println("Segment '" + neighborSegment.getName() + "' is too short (<30 meters)");
	}


	public void update(Vector3f trafficObjectPos)
	{
		// pause movement of follower box if traffic object's distance
		// has exceeded maximum
		/*
		if(maxDistanceExceeded(trafficObjectPos) || sim.isPause())
			//motionControl.setSpeed(0f);
			motionControl.pause();
		else
			//motionControl.setSpeed(0.01f);
			motionControl.play();
		*/
		
		// skip "else"-part during initialization (first 3 update loops)
		if(sim.isPause() || counter<3)
		{
			motionControl.setSpeed(0f);
			counter++;
		}
		else
		{
			float currentDistance = getCurrentDistance(trafficObjectPos);
			
			//if(trafficObject.getName().equals("car1"))
			//	System.err.println(currentDistance);
			
			// set limits
			currentDistance = Math.max(Math.min(maxDistance, currentDistance), minDistance);

			//maxDistance --> 0
			//minDistance --> 1
			float factor = 1.0f - ((currentDistance-minDistance)/(maxDistance-minDistance));
			motionControl.setSpeed(factor);
		}
		
		// if new WP to set traffic object available, wait for NEXT update and set
		if(isTargetWayPointAvailable && (waitForNextUpdate = !waitForNextUpdate))
		{
			// set traffic object to new position
	        performWayPointChange(targetWayPointIndex);
	        isTargetWayPointAvailable = false;
		}
		
		checkIfWayPointReached();
	}


	private boolean hasLostSteeringCar(float currentDistance) 
	{
		if(trafficObject instanceof SteeringCar)
		{
			// true if follow box is behind traffic object (angle > 270 degrees)
			Vector3f carFrontPos = ((SteeringCar)trafficObject).getFrontGeometry().getWorldTranslation();
			Vector3f carCenterPos = ((SteeringCar)trafficObject).getCenterGeometry().getWorldTranslation();
			Vector3f followBoxPos = followBox.getWorldTranslation();
			float angle = Util.getAngleBetweenPoints(carFrontPos, carCenterPos, followBoxPos, true);
			//System.err.println("carFrontPos: " + carFrontPos + ", carCenterPos: " + carCenterPos + 
			//		", followBoxPos: " + followBoxPos + ", angle: " + angle*FastMath.RAD_TO_DEG);
			boolean boxBehindCar = FastMath.abs(angle)>(0.75f*FastMath.PI);
			
			// true if follow box is more than maxDistance+1 ahead
			boolean tooFarAhead = !boxBehindCar && currentDistance > settings.getMaxDistance() + 1;
			
			return boxBehindCar || tooFarAhead;
		}
		else
			return false;
	}

	/**
	 * Checks whether the traffic object gets stuck for at least 30 seconds.
	 * If movement is less than 1 meter for the last 30 seconds, true will
	 * be returned. Check not applied to traffic objects of type SteeringCar.
	 * 
	 * @return
	 * 			true, if traffic object stuck.
	 */
	private boolean hasCrashed()
	{
		// exclude steering car from this check (e.g. auto-pilot) 
		if(trafficObject instanceof SteeringCar)
			return false;
		
		// check every 3 seconds
		if(System.currentTimeMillis()-lastCrashCheck > 3000)
		{
			lastCrashCheck = System.currentTimeMillis();
			
			//add traveled distance on current segment to storage
			distanceStorage.addLast(traveledDistance);
			
			// if maximum size (10) has been reached...
			if(distanceStorage.size() > 10)
			{
				// ...remove oldest value
	        	distanceStorage.removeFirst();
				
				// ...compute total distance the traffic object moved during the last 30 seconds
				float totalDist = 0;
		        for (int i=0; i<distanceStorage.size()-1; i++)
		        	totalDist += FastMath.abs(distanceStorage.get(i+1) - distanceStorage.get(i));
		                
		        // if total distance less than one meter --> crash
				if(totalDist < 1.0f)
				{
					//System.err.println("RESET: " + totalDist);
					return true;
				}
			}
		}

		return false;
	}
	private long lastCrashCheck = 0;
	private LinkedList<Float> distanceStorage = new LinkedList<Float>();


	private void checkIfWayPointReached() 
	{
		int currentWayPointIndex = motionControl.getCurrentWayPoint() % waypointList.size();
		if(currentWayPointIndex != previousWayPointIndex)
		{
			if(waitAtWP(currentWayPointIndex))
				return;
				
        	// set speed limit for next way point
        	float speed = waypointList.get(currentWayPointIndex).getSpeed();
        	setSpeed(speed);
        	
        	// if last WP reached and path not cyclic --> reset traffic object to first WP
            if (currentWayPointIndex == 0 && !motionPath.isCycle())
            	performWayPointChange(0);
            
            previousWayPointIndex = currentWayPointIndex;
            
            //System.err.println("WP " + currentWayPointIndex + ": " + speed);
		}
	}

	
	//boolean isSetWaitTimer = false;
	//long waitTimer = 0;
	private boolean waitAtWP(int currentWayPointIndex)
	{
		// get waiting time at upcoming way point (if available)
		Integer waitingTime = waypointList.get(currentWayPointIndex).getWaitingTime();
		
		if(waitingTime == null || waitingTime <= 0)
		{
			// no (or invalid waiting time) --> do not wait
			return false;
		}
		else
		{
			// valid waiting time available
			if(!isSetWaitTimer)
			{
				// waiting timer not yet set --> set timer to current time stamp and wait
				waitTimer = System.currentTimeMillis();
				isSetWaitTimer = true;
				
				motionControl.pause();
				//System.err.println("WAIT");
				
				return true;
			}
			else
			{
				// waiting timer already set --> check if elapsed
				if(System.currentTimeMillis()-waitTimer > waitingTime)
				{
					// waiting timer elapsed --> stop waiting and resume motion
					motionControl.play();
					//System.err.println("RESUME");
					
					isSetWaitTimer = false;
					
					return false;
				}
				else 
				{
					// waiting timer not elapsed --> wait
					return true;
				}
			}
		}
	}

	boolean isSetWaitTimer = false;
	long waitTimer = 0;
	private boolean waitAtWP(Waypoint wayPoint)
	{
		// get waiting time at way point (if available)
		Integer waitingTime = wayPoint.getWaitingTime();
		
		if(waitingTime == null || waitingTime <= 0)
		{
			// no (or invalid waiting time) --> do not wait
			return false;
		}
		else
		{
			// valid waiting time available
			if(!isSetWaitTimer)
			{
				// waiting timer not yet set --> set timer to current time stamp and wait
				waitTimer = System.currentTimeMillis();
				isSetWaitTimer = true;
				return true;
			}
			else
			{
				// waiting timer already set --> check if elapsed
				if(System.currentTimeMillis()-waitTimer > waitingTime)
				{
					// waiting timer elapsed --> stop waiting and resume motion					
					isSetWaitTimer = false;
					return false;
				}
				else 
				{
					// waiting timer not elapsed --> wait
					return true;
				}
			}
		}
	}


	public void setToWayPoint(int index)
	{
		if(0 <= index && index < waypointList.size())
		{
			targetWayPointIndex = index;
			isTargetWayPointAvailable = true;
		}
		else
			System.err.println("Way point " + index + " does not exist");
	}

	public void setToWayPoint(String name)
	{
		if(sim.getRoadNetwork().getWaypoint(name) != null)
			resetWaypoint = sim.getRoadNetwork().getWaypoint(name);
		else
			System.err.println("Way point " + name + " does not exist");
	}

	
	
	private void performWayPointChange(int index)
	{
		// set follow box to WP
		float traveledDistance = 0;
        for (int i=0; i<index;i++)
        	traveledDistance += motionPath.getSpline().getSegmentsLength().get(i);
        float traveledTime = (traveledDistance/motionPath.getLength()) * motionControl.getInitialDuration();
        motionControl.setTime(traveledTime);
        
        //System.err.println("SET: dist " + traveledDistance + ", time: " + traveledTime + ", index: " + index);
		
		// set position to traffic object
		Vector3f position = waypointList.get(index).getPosition();
		trafficObject.setPosition(position);
		
		// set heading to traffic object
		float heading = getHeadingAtWP(index);
		Quaternion quaternion = new Quaternion().fromAngles(0, heading, 0);
		trafficObject.setRotation(quaternion);
	}
	
	private void performWayPointChange(Waypoint waypoint)
	{
		// set parameters of follow box
    	boolean success = setFollowBoxToWP(waypoint);
		
		// set position and rotation to traffic object
		// do not set position and heading to SteeringCar with auto-pilot switched off
		if(success && (!(trafficObject instanceof SteeringCar) || ((SteeringCar)trafficObject).isAutoPilot()))
		{	
			// set position to traffic object
			trafficObject.setPosition(waypoint.getPosition());
			
			// set heading to traffic object
			if(currentSegment != null)
			{
				float heading = currentSegment.getHeading(traveledDistance);
				Quaternion quaternion = new Quaternion().fromAngles(0, heading, 0);
				trafficObject.setRotation(quaternion);
			}
		}
	}

	
	public int getIndexOfWP(String wayPointID) 
	{
		for(int i=0; i<waypointList.size(); i++)
			if(waypointList.get(i).getName().equals(wayPointID))
				return i;
		return -1;
	}

	
	public float getHeadingAtWP(int index) 
	{
		float heading = 0;
		Waypoint nextWayPoint = getNextWayPoint(index);
		
		// if next way point available, compute heading towards it
		if(nextWayPoint != null)
		{
			// compute driving direction by looking at next way point from current position 
			Vector3f targetPosition = nextWayPoint.getPosition().clone();
			targetPosition.setY(0);
			
			Vector3f currentPosition = waypointList.get(index).getPosition().clone();
			currentPosition.setY(0);
			
			Vector3f drivingDirection = targetPosition.subtract(currentPosition).normalize();

			// compute heading (orientation) from driving direction vector for
			// angle between driving direction and heading "0"
			float angle0  = drivingDirection.angleBetween(new Vector3f(0,0,-1));
			// angle between driving direction and heading "90"
			float angle90 = drivingDirection.angleBetween(new Vector3f(1,0,0));
			
			// get all candidates for heading
			// find the value from {heading1,heading2} which matches with one of {heading3,heading4}
			float heading1 = (2.0f * FastMath.PI + angle0)  % FastMath.TWO_PI;
			float heading2 = (2.0f * FastMath.PI - angle0)  % FastMath.TWO_PI;
			float heading3 = (2.5f * FastMath.PI + angle90) % FastMath.TWO_PI;
			float heading4 = (2.5f * FastMath.PI - angle90) % FastMath.TWO_PI;
			
			float diff_1_3 = FastMath.abs(heading1-heading3);
			float diff_1_4 = FastMath.abs(heading1-heading4);
			float diff_2_3 = FastMath.abs(heading2-heading3);
			float diff_2_4 = FastMath.abs(heading2-heading4);
			
			if((diff_1_3 < diff_1_4 && diff_1_3 < diff_2_3 && diff_1_3 < diff_2_4) ||
				(diff_1_4 < diff_1_3 && diff_1_4 < diff_2_3 && diff_1_4 < diff_2_4))
			{
				// if diff_1_3 or diff_1_4 are smallest --> the correct heading is heading1
				heading = heading1;
			}
			else
			{
				// if diff_2_3 or diff_2_4 are smallest --> the correct heading is heading2
				heading = heading2;
			}
		}
		return heading;
	}
	
	
	public Waypoint getPreviousWayPoint() 
	{
		int currentIndex = motionControl.getCurrentWayPoint();
		return getPreviousWayPoint(currentIndex);
	}
	

	public Waypoint getCurrentWayPoint() 
	{
		int currentIndex = motionControl.getCurrentWayPoint();
		return waypointList.get(currentIndex);
	}


	public Waypoint getNextWayPoint() 
	{
		int currentIndex = motionControl.getCurrentWayPoint();
		return getNextWayPoint(currentIndex);
	}
	
	private boolean setFollowBoxToWP(Waypoint waypoint)
	{
		if(!waypoint.isEndPoint())
    	{
    		currentSegment = waypoint.getNextSegment(preferredSegments);
    		currentFromWaypoint = waypoint;
    		traveledDistance = 0;
    	}
    	else if(waypoint.isViaWP())
    	{
    		// in case of the way point has no outgoing segment, however, is a viaWayPoint, 
    		// then select outgoing segment of its parent instead
    		currentSegment = waypoint.getRandomParentSegment();
    		currentFromWaypoint = currentSegment.getFromWaypoint();
    		traveledDistance = currentSegment.getViaWPPosition(waypoint);
    	}
    	else
    	{
    		System.err.println("Could not set " + trafficObject.getName() + " to way point " + waypoint.getName() + " (end point)");
    		return false;
    	}
		
		nextSegment = currentSegment.getToWaypoint().getNextSegment(preferredSegments);
		return true;
	}


	public Waypoint getPreviousWayPoint(int index) 
	{
		Waypoint previousWayPoint = null;
		
		if(motionPath.isCycle())
		{
			// if path is cyclic, the predecessor of the first WP will be the last WP
			previousWayPoint = waypointList.get((index-1+waypointList.size()) % waypointList.size());
		}
		else if(motionPath.getNbWayPoints() > index-1 && index-1 >= 0)
		{
			// if not cyclic, only predecessors for way points 1 .. n exist
			previousWayPoint = waypointList.get(index-1);
		}
		
		return previousWayPoint;
	}
	
	
	public Waypoint getNextWayPoint(int index) 
	{
		Waypoint nextWayPoint = null;
		
		if(motionPath.isCycle())
		{
			// if path is cyclic, the successor of the last WP will be the first WP
			nextWayPoint = waypointList.get((index+1) % waypointList.size());
		}
		else if(motionPath.getNbWayPoints() > index+1 && index+1 >= 0)
		{
			// if not cyclic, only successors for way points 0 .. n-1 exist
			nextWayPoint = waypointList.get(index+1);
		}
		
		return nextWayPoint;
	}
    
	
    public float getSpeed()
    {
    	return carSpeed;
    }

    
    public void setSpeed(float speedKmh)
    {
    	carSpeed = speedKmh;
    	
    	if(getFollowBoxSpeed() < speedKmh + 10)
    		setFollowBoxSpeed(speedKmh + 10);
    }

    
    private float getFollowBoxSpeed()
    {
    	float duration = motionControl.getInitialDuration();
    	float distanceMeters = motionPath.getLength();
    	float speed = distanceMeters / duration;
    	return (3.6f * speed);
    }
    
    
    private void setFollowBoxSpeed(float speedKmh)
    {
    	float distanceMeters = motionPath.getLength();
        float speed = speedKmh / 3.6f;
        float duration = distanceMeters / speed;
        motionControl.setInitialDuration(duration);
    }
    
    
	public Vector3f getPosition() 
	{
		return followBox.getWorldTranslation();
	}


	public MotionEvent getMotionControl() 
	{
		return motionControl;
	}

    
    private Geometry createFollowBox() 
    {
		// add spatial representing the position the driving car is steering towards
		Box box = new Box(1f, 1f, 1f);
		Geometry followBox = new Geometry("followBox"+name, box);
		followBox.setLocalTranslation(0, 0, 0);
		Material followBoxMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		followBoxMaterial.setColor("Color", ColorRGBA.Green);
		followBox.setMaterial(followBoxMaterial);
        followBox.setLocalScale(0.4f);
        followBox.setName("followBox"+name);
        sim.getSceneNode().attachChild(followBox);
        System.out.println("Follow box added =" + followBox.getName());
        
        if(!settings.isPathVisible())
        	followBox.setCullHint(CullHint.Always);
        	
        return followBox;
    }
    

	private float getCurrentDistance(Vector3f trafficObjectPos) 
	{
		// get box's position on xz-plane (ignore y component)
		Vector3f followBoxPosition = getPosition();
		followBoxPosition.setY(0);
		
		// get traffic object's position on xz-plane (ignore y component)
		Vector3f trafficObjectPosition = trafficObjectPos;
		trafficObjectPosition.setY(0);
		
		// distance between box and trafficObject
		float currentDistance = followBoxPosition.distance(trafficObjectPosition);
		return currentDistance;
	}
	
	private boolean stopTrafficObject = false;
    /*
	private boolean maxDistanceExceeded(Vector3f trafficObjectPos) 
	{
		float currentDistance = getCurrentDistance(trafficObjectPos);
		
		// report whether maximum distance is exceeded 
		return currentDistance > maxDistance;
	}
     */

	
	public float getReducedSpeed()
	{
		// return a temporarily reduced speed for the traffic car
		// in order to reach next (lower) speed limit in time
		float reducedSpeedInKmh = Float.POSITIVE_INFINITY;
		
		// if next way point with lower speed comes closer --> reduce speed
		int currentIndex = motionControl.getCurrentWayPoint();
		Waypoint nextWP = getNextWayPoint(currentIndex);
		if(nextWP != null)
		{
			// current way point (already passed)
			Waypoint curentWP = waypointList.get(currentIndex);
			
			// speed at current way point
			float currentSpeedInKmh = curentWP.getSpeed();
			float currentSpeed = currentSpeedInKmh / 3.6f;
			
			// speed at next way point
			float targetSpeedInKmh = nextWP.getSpeed();
			float targetSpeed = targetSpeedInKmh / 3.6f;
			
			// if speed at the next WP is lower than at the current WP --> brake traffic object
			if(targetSpeed < currentSpeed)
			{
				// % of traveled distance between current and next way point
				float wayPercentage = motionControl.getCurrentValue();
				
				// distance between current and next way point
				Vector3f currentPos = curentWP.getPosition().clone();
				currentPos.setY(0);
				Vector3f nextPos = nextWP.getPosition().clone();
				nextPos.setY(0);
				float distance = currentPos.distance(nextPos);
				
				// distance (in meters) between follow box and next way point
				float distanceToNextWP = (1 - wayPercentage) * distance;
			
				// speed difference in m/s between current WP's speed and next WP's speed
				float speedDifference = currentSpeed - targetSpeed;
				
				// compute the distance in front of the next WP at what the traffic object has to start 
				// braking with 50% brake force in order to reach the next WP's (lower) speed in time.
				float deceleration50Percent = 50f * trafficObject.getMaxBrakeForce()/trafficObject.getMass();
				
				// time in seconds needed for braking process
				float time = speedDifference / deceleration50Percent;
				
				// distance covered during braking process
				float coveredDistance = 0.5f * -deceleration50Percent * time * time + currentSpeed * time;

				// start braking in x meters
				float distanceToBrakingPoint = distanceToNextWP - coveredDistance;
				
				if(distanceToBrakingPoint < 0)
				{
					// reduce speed linearly beginning from braking point
					
					// % of traveled distance between braking point and next way point
					float speedPercentage = -distanceToBrakingPoint/coveredDistance;
					
					//   0% traveled: reduced speed = currentSpeed
					//  50% traveled: reduced speed = (currentSpeed+targetSpeed)/2
					// 100% traveled: reduced speed = targetSpeed
					float reducedSpeed = currentSpeed - (speedPercentage * speedDifference);
					reducedSpeedInKmh = reducedSpeed * 3.6f;
					
					/*
					if(trafficObject.getName().equals("car1"))
					{
						float trafficObjectSpeedInKmh = trafficObject.getLinearSpeedInKmh();
						System.out.println(curentWP.getName() + " : " + speedPercentage + " : " + 
								reducedSpeedInKmh + " : " + trafficObjectSpeedInKmh + " : " + targetSpeedInKmh);
					}
					*/
				}
			}
		}
		return reducedSpeedInKmh;
	}
	private void computeDistanceToNextWP() 
	{
		// distance between followBox and next way point
		Vector3f followBoxPos = followBox.getWorldTranslation().clone();
		followBoxPos.setY(0);
		Vector3f nextPos = currentSegment.getToWaypoint().getPosition().clone();
		nextPos.setY(0);
		distanceToNextWP = followBoxPos.distance(nextPos);
	}
	public float getDistanceToNextWP() 
	{
		return distanceToNextWP;
	}

	public Segment getCurrentSegment()
	{
		return currentSegment;
	}


	public float getTraveledDistance()
	{
		return traveledDistance;
	}
}
