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

package eu.opends.opendrive.processed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import eu.opends.main.Simulator;
import eu.opends.opendrive.processed.SpeedLimit;
import eu.opends.opendrive.OpenDRIVELoader;
import eu.opends.opendrive.data.Color;
import eu.opends.opendrive.data.ContactPoint;
import eu.opends.opendrive.data.Lane;
import eu.opends.opendrive.data.LaneType;
import eu.opends.opendrive.data.RoadmarkType;
import eu.opends.opendrive.data.SingleSide;
import eu.opends.opendrive.data.Weight;
import eu.opends.opendrive.util.IntersectionComparator;
import eu.opends.opendrive.util.SpeedLimitComparator;
import eu.opends.opendrive.data.Lane.Link;
import eu.opends.opendrive.data.Lane.Width;
import eu.opends.opendrive.data.LaneChange;
import eu.opends.opendrive.data.Lane.Link.Predecessor;
import eu.opends.opendrive.data.Lane.Link.Successor;
import eu.opends.opendrive.data.Lane.RoadMark;
import eu.opends.opendrive.data.Lane.Speed;
import eu.opends.tools.Vector3d;

public class ODLane
{
	private boolean printDebugMsg = false;		
		
	private Simulator sim;
	private ODRoad road;
	private ODLaneSection laneSection;
	private double laneStartS;
	private double laneEndS;
	private Lane lane;
	private int laneID;
	private LaneSide laneSide;
	private LaneType type;
	private SingleSide level;
	private Integer predecessorID = null;
	private Integer successorID = null;
	private ArrayList<ODPoint> borderPointList = new ArrayList<ODPoint>();
	private ODPoint innerPoint = null;
	private ODPoint outerPoint = null;
	private ODLink successorLink = null;
	private ODLink predecessorLink = null;
	
	
	public enum LaneSide
	{
		LEFT, RIGHT;
	}
	
	
	public ODLane(Simulator sim, ODRoad road, ODLaneSection laneSection, Lane lane, LaneSide laneSide)
	{
		this.sim = sim;
		this.road = road;
		this.laneSection = laneSection;
		this.laneStartS = laneSection.getS();
		this.laneEndS = laneSection.getEndS();
		this.laneSide = laneSide;
		this.lane = lane;
		laneID = lane.getId();
		type = lane.getType();
		level = lane.getLevel();

		Link link = lane.getLink();
		if(link != null)
		{
			Predecessor predecessor = link.getPredecessor();
			if(predecessor != null)
				predecessorID = predecessor.getId();
			
			Successor successor = link.getSuccessor();
			if(successor != null)
				successorID = successor.getId();
		}
	}


	public ODRoad getODRoad() 
	{
		return road;
	}
	
	
	public double getStartS() 
	{
		return laneStartS;
	}
	
	
	public double getEndS() 
	{
		return laneEndS;
	}
	
	
	public LaneSide getLaneSide() 
	{
		return laneSide;
	}
	
	
	public double getWidth(double s)
	{
		double laneSection_s = laneSection.getS();
		List<Width> widthList = lane.getWidth();
		
		for(int i=widthList.size()-1; i>=0; i--)
		{
			double offset = laneSection_s + widthList.get(i).getSOffset();
			if(s >= offset)
			{
				double a = widthList.get(i).getA();
				double b = widthList.get(i).getB();
				double c = widthList.get(i).getC();
				double d = widthList.get(i).getD();
				
				double ds = s - offset;
				
				return a + b*ds + c*ds*ds + d*ds*ds*ds;
			}
		}
		return 0;
	}

	/*
	public Double getSpeedLimitKmh(double s)
	{
		double laneSection_s = laneSection.getS();
		List<Speed> speedList = lane.getSpeed();
		
		for(int i=speedList.size()-1; i>=0; i--)
		{
			double offset = laneSection_s + speedList.get(i).getSOffset();
			if(s >= offset)
			{
				double maxSpeed = speedList.get(i).getMax();
				//String unit = speedList.get(i).getUnit();

				return maxSpeed;
			}
		}
		return null;
	}
	*/
	
	public void initODLane(ArrayList<ODPoint> pointlist, boolean visualize)
	{
		initBorderPoints(pointlist);
		
		// get lane segments for texturing road markers
		double laneSection_s = laneSection.getS();
		List<RoadMark> roadMarkList = lane.getRoadMark();
		
		// if RoadMark information is missing (optional!) use default settings
		if(roadMarkList.size() == 0)
		{
			RoadMark defaultRoadMark = new RoadMark();
			defaultRoadMark.setType(RoadmarkType.NONE);
			defaultRoadMark.setSOffset(0.0);
			defaultRoadMark.setWeight(Weight.STANDARD);
			defaultRoadMark.setColor(Color.STANDARD);
			defaultRoadMark.setWidth(0.13);
			defaultRoadMark.setLaneChange(LaneChange.BOTH);
			roadMarkList.add(defaultRoadMark);
		}

		
		for(int i=0; i<roadMarkList.size(); i++)
		{
			/*
			System.err.println("Road: " + road.getID() + ", LaneSection: " + laneSection_s + ", Lane: " + lane.getId() + 
					", roadMark: " + roadMarkList.get(i).getType().toString());
			*/
			
			RoadmarkType roadmarkType = roadMarkList.get(i).getType();
			ArrayList<ODPoint> pointlist2 = new ArrayList<ODPoint>();
			
			RoadMark roadMark = roadMarkList.get(i);
			double startS = laneSection_s + roadMark.getSOffset();
			
			double endS = laneSection.getEndS();
			if(i+1 < roadMarkList.size())
				endS = laneSection_s + roadMarkList.get(i+1).getSOffset();
			
			for(ODPoint point : pointlist)
				if(startS <= point.getS() && point.getS() <= endS)
					pointlist2.add(point);
			
			drawLaneSegment(pointlist2, visualize, roadmarkType);
			//System.err.println("startS: " + startS + ", EndS: " + endS);
			//System.err.println("Size: " + pointlist2.size());
		}


	}

	
	private void initBorderPoints(ArrayList<ODPoint> pointlist)
	{
		for(int i=0; i<pointlist.size(); i++)
		{
			// get point parameters
			ODPoint point = pointlist.get(i);
			String pointID = point.getID();
			double s = point.getS();
			Vector3d position = point.getPosition();
			double ortho = point.getOrtho();


			// calculate lane width at current s
			double width = getWidth(s);
			
			if(laneSide == LaneSide.LEFT)
				width = -width;
			
			Vector3d borderPos = position.add(new Vector3d((width)*Math.sin(ortho), 0, (width)*Math.cos(ortho)));

			ODPoint borderPoint = new ODPoint(pointID+"_"+laneID, s, borderPos, ortho, point.getGeometry(), this);
			borderPointList.add(borderPoint);
		}
	}
	

	private void drawLaneSegment(ArrayList<ODPoint> pointlist, boolean visualize, RoadmarkType roadmarkType)
	{
		if(pointlist.size()<2)
		{
			if(printDebugMsg)
				System.out.println("Pointlist too small");
			return;
		}
		
		Vector3f[] vertices = new Vector3f[2*pointlist.size()];
		Vector2f[] texCoord = new Vector2f[2*pointlist.size()];
		int [] indexes = new int[6*(pointlist.size()-1)];
		
		for(int i=0; i<pointlist.size(); i++)
		{
			// get point parameters
			ODPoint point = pointlist.get(i);
			double s = point.getS();
			Vector3d position = point.getPosition();
			double ortho = point.getOrtho();

			
			double textureOffset = 0;
			
			if(laneID == -1 || laneID == 1)
			{
				eu.opends.opendrive.data.CenterLane.RoadMark centerLineRoadMark = laneSection.getCenterLineRoadMarkAtPos(s);
				if(centerLineRoadMark != null)
				{
					if(centerLineRoadMark.getWidth() != null)	
					{
						if(laneSide == LaneSide.LEFT)
							textureOffset = -0.5f * centerLineRoadMark.getWidth();
						else
							textureOffset = 0.5f * centerLineRoadMark.getWidth();
					}
					else
						System.err.println("WARNING: Road: " + getODRoad().getID() + ", Lane: " + laneID + "; centerLineRoadMark width == null (ODLane)");
				}
			}

			Vector3d textureStartPos = position.add(new Vector3d((textureOffset)*Math.sin(ortho), 0, (textureOffset)*Math.cos(ortho)));
			
			// place vertex on top of underlying surface (if enabled)
			textureStartPos.setY(sim.getOpenDriveCenter().getHeightAt(textureStartPos.getX(), textureStartPos.getZ()));
			
			vertices[2*i] = textureStartPos.toVector3f();
			
			// calculate lane width at current s
			double width = getWidth(s);
			
			if(laneSide == LaneSide.LEFT)
				width = -width;
			
			Vector3d borderPos = position.add(new Vector3d((width)*Math.sin(ortho), 0, (width)*Math.cos(ortho)));
			
			// place vertex on top of underlying surface (if enabled)
			borderPos.setY(sim.getOpenDriveCenter().getHeightAt(borderPos.getX(), borderPos.getZ()));
			
			vertices[2*i+1] = borderPos.toVector3f();
			

			if(i%2==0)
			{
				texCoord[2*i] = new Vector2f(0,0);
				texCoord[2*i+1] = new Vector2f(1,0);
			}			
			else
			{
				texCoord[2*i] = new Vector2f(0,1);
				texCoord[2*i+1] = new Vector2f(1,1);
			}

			
			if(i<pointlist.size()-1)
			{
				indexes[6*i+0] = 2*i+0;
				indexes[6*i+1] = 2*i+1;
				indexes[6*i+2] = 2*i+3;
				indexes[6*i+3] = 2*i+3;
				indexes[6*i+4] = 2*i+2;
				indexes[6*i+5] = 2*i+0;
			}
		}


		Mesh mesh = new Mesh();

		mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		mesh.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexes));
		mesh.updateBound();
		
		Geometry geo = new Geometry("ODarea_" + road.getID() + "_" + laneID, mesh);
		geo.setMaterial(getMaterial(roadmarkType));
		
		if(!visualize || type == LaneType.NONE)
			geo.setCullHint(CullHint.Always);
		
		if(type == LaneType.DRIVING)
			mesh.scaleTextureCoordinates(new Vector2f(1f,1f));
		else if(type == LaneType.SIDEWALK)
			mesh.scaleTextureCoordinates(new Vector2f(1f,1f));
		else if(type == LaneType.BORDER)
			mesh.scaleTextureCoordinates(new Vector2f(1f,3f));
		else if(type == LaneType.SHOULDER)
			mesh.scaleTextureCoordinates(new Vector2f(1f,0.5f));
		else if(type == LaneType.RESTRICTED)
			mesh.scaleTextureCoordinates(new Vector2f(1f,0.08f));
		else if(type == LaneType.PARKING)
			mesh.scaleTextureCoordinates(new Vector2f(1f,1f));
		
		sim.getOpenDriveNode().attachChild(geo);
		
		if(!(sim instanceof OpenDRIVELoader))
			laneSection.addToBulletPhysicsSpace(geo);
		
		//System.err.println(roadID + "_" + laneID);
	}

	
	private Material getMaterial(RoadmarkType roadmarkType)
	{
		switch(type)
		{
			case DRIVING:
				{
					if(roadmarkType == RoadmarkType.SOLID)
						return sim.getOpenDriveCenter().getVisualizer().roadSolidLineLaneTextureMaterial;
					else if(roadmarkType == RoadmarkType.BROKEN)
						return sim.getOpenDriveCenter().getVisualizer().roadBrokenLineLaneTextureMaterial;
					else
						return sim.getOpenDriveCenter().getVisualizer().roadNoLineLaneTextureMaterial;
				}
			case SIDEWALK:return sim.getOpenDriveCenter().getVisualizer().sidewalkTextureMaterial; 
			case BORDER:return sim.getOpenDriveCenter().getVisualizer().curbTextureMaterial; 
			case SHOULDER:return sim.getOpenDriveCenter().getVisualizer().shoulderTextureMaterial; 
			case RESTRICTED:return sim.getOpenDriveCenter().getVisualizer().restrictedTextureMaterial;
			case PARKING:return sim.getOpenDriveCenter().getVisualizer().roadParkingParallelTextureMaterial;
			default: return sim.getOpenDriveCenter().getVisualizer().getRandomMaterial(false); 
		}
	}


	public int getID()
	{
		return laneID;
	}


	public LaneType getType()
	{
		return type;
	}


	public SingleSide getLevel()
	{
		return level;
	}


	public Integer getPredecessorID()
	{
		return predecessorID;
	}


	public Integer getSuccessorID()
	{
		return successorID;
	}


	public ArrayList<ODPoint> getBorderPoints()
	{
		return borderPointList;		
	}


	public void setCurrentBorderPoints(ODPoint innerPoint) 
	{
		this.innerPoint = innerPoint;
		
		// get point parameters
		String pointID = innerPoint.getID();
		double s = innerPoint.getS();
		Vector3d position = innerPoint.getPosition();
		double ortho = innerPoint.getOrtho();

		// calculate lane width at current s
		double width = getWidth(s);
		
		if(laneSide == LaneSide.LEFT)
			width = -width;
		
		Vector3d outerPos = position.add(new Vector3d((width)*Math.sin(ortho), 0, (width)*Math.cos(ortho)));
		outerPoint = new ODPoint(pointID+"_"+laneID, s, outerPos, ortho, innerPoint.getGeometry(), this);
	}
	
	
	public ODPoint getCurrentInnerBorderPoint()
	{
		return innerPoint;
	}

	
	public ODPoint getCurrentOuterBorderPoint()
	{
		return outerPoint;
	}

	
	public double getCurrentWidth()
	{
		return outerPoint.getPosition().distance(innerPoint.getPosition());
	}
	

	public float getHeadingDiff(float carHdg)
	{
		// compute angle difference between car and lane orientation
		double ortho = innerPoint.getOrtho();
		float laneHdg = FastMath.RAD_TO_DEG * (FastMath.HALF_PI - ((float) ortho));
		
		float leftValue = 0;
		if(laneSide == LaneSide.LEFT)
			leftValue = 180;
		
		float diff = (carHdg - laneHdg + leftValue)%360;
		
		if(diff>180)
			diff -= 360;
		
		return diff;
	}
	
	
	public float getLaneHeading()
	{
		// compute angle difference between car and lane orientation
		double ortho = innerPoint.getOrtho();
		float laneHdg = FastMath.RAD_TO_DEG * (FastMath.HALF_PI - ((float) ortho));
		
		float leftValue = 0;
		if(laneSide == LaneSide.LEFT)
			leftValue = 180;
		
		float diff = (-laneHdg + leftValue)%360;
		
		if(diff>180)
			diff -= 360;
		
		return diff;
	}

	
	public enum AdasisLaneType
	{
		Unknown, EmergencyLane, SingleLaneRoad, LeftMostLane, RightMostLane, MiddleLane
	}
	

	public AdasisLaneType getAdasisLaneType(boolean isWrongWay)
	{	
		if(type == LaneType.DRIVING)
		{
			boolean hasInnerNeighbor = hasInnerNeighbor();
			boolean hasOuterNeighbor = hasOuterNeighbor();
			
			if(hasInnerNeighbor && hasOuterNeighbor)
				return AdasisLaneType.MiddleLane;
			else if(hasInnerNeighbor)
			{
				if(isWrongWay)
					return AdasisLaneType.LeftMostLane;
				else
					return AdasisLaneType.RightMostLane;
			}
			else if(hasOuterNeighbor)
			{
				if(isWrongWay)
					return AdasisLaneType.RightMostLane;
				else
					return AdasisLaneType.LeftMostLane;
			}
			else
				return AdasisLaneType.SingleLaneRoad;			
		}
		else if(type == LaneType.SHOULDER)
			return AdasisLaneType.EmergencyLane;
		else 
			return AdasisLaneType.Unknown;
	}


	/**
	 * Returns true, if this lane has a neighbor lane of type "DRIVING" which is closer 
	 * to the center than this lane (if this lane is next to the center, it will be checked 
	 * whether there is a "DIVING" lane on the other side of the center line.
	 * 
	 * @return
	 * 			true, if neighbor exists
	 */
	private boolean hasInnerNeighbor()
	{
		if(laneSide == LaneSide.LEFT)
		{
			if(laneSection.getLeftLaneMap().containsKey(laneID-1))
				return (laneSection.getLeftLaneMap().get(laneID-1).getType() == LaneType.DRIVING);
			/**/
			else if(laneID == 1 && laneSection.getRightLaneMap().containsKey(-1))
				return (laneSection.getRightLaneMap().get(-1).getType() == LaneType.DRIVING);
			/**/
		}
		else
		{
			if(laneSection.getRightLaneMap().containsKey(laneID+1))
				return (laneSection.getRightLaneMap().get(laneID+1).getType() == LaneType.DRIVING);
			/**/
			else if(laneID == -1 && laneSection.getLeftLaneMap().containsKey(1))
				return (laneSection.getLeftLaneMap().get(1).getType() == LaneType.DRIVING);
			/**/
		}
		
		return false;
	}
	

	/**
	 * Returns true, if this lane has a neighbor lane of type "DRIVING" which is farther away 
	 * from the center than this lane.
	 * 
	 * @return
	 * 			true, if neighbor exists
	 */
	private boolean hasOuterNeighbor()
	{		
		if(laneSide == LaneSide.LEFT)
		{
			if(laneSection.getLeftLaneMap().containsKey(laneID+1))
				return (laneSection.getLeftLaneMap().get(laneID+1).getType() == LaneType.DRIVING);
		}
		else
		{
			if(laneSection.getRightLaneMap().containsKey(laneID-1))
				return (laneSection.getRightLaneMap().get(laneID-1).getType() == LaneType.DRIVING);
		}
		
		return false;
	}


	public Double getCurrentCurvature()
	{
		Double curv = innerPoint.getGeometryCurvature();
		
		if(laneSide == LaneSide.LEFT)
			curv = -curv;

		return curv;		
	}


	public void setSuccessor(ODLink successorLink)
	{
		this.successorLink = successorLink;		
	}

	
	public void setPredecessor(ODLink predecessorLink)
	{
		this.predecessorLink = predecessorLink;		
	}
	
	
	public ODPoint getLaneCenterPointBack(boolean isWrongWay, double s, double range)
	{
		if(laneID>0)
			return getLaneCenterPointAhead(s, range, !isWrongWay);//true
		else if(laneID<0)
			return getLaneCenterPointAhead(s, range, isWrongWay);//false
		else
			return null;
	}
	
	
	public ODPoint getLaneCenterPointAhead(boolean isWrongWay, double s, double range)
	{
		if(laneID>0)
			return getLaneCenterPointAhead(s, range, isWrongWay);//false
		else if(laneID<0)
			return getLaneCenterPointAhead(s, range, !isWrongWay);//true
		else
			return null;
	}


	private ODPoint getLaneCenterPointAhead(double s, double range, boolean increasingS)
	{
		if(laneStartS > s || s > laneEndS)
		{
			if(printDebugMsg)
				System.err.println("s=" + s + " is out of lane " + laneID + " (road: " + road.getID() + ", getLaneCenterPointAhead)");
			return null;
		}
			
		if(increasingS)
		{
			if(s+range > laneEndS)
			{
				// search successor
				double distToEnd = laneEndS - s;
				
				if(successorLink != null && successorLink.getLane() != null)
				{
					ODLane successorLane = successorLink.getLane();
					double successorS = successorLane.getStartS();
					
					if(successorLink.getContactPoint() == ContactPoint.END)
					{
						increasingS = !increasingS;
						successorS = successorLane.getEndS();
					}
					
					double remainingRange = range - distToEnd;
					return successorLane.getLaneCenterPointAhead(successorS, remainingRange, increasingS);
				}
				else if(printDebugMsg)
					System.err.println("no successor available (lane: " + laneID + "; road: " + road.getID() + "; getLaneCenterPointAhead)");
			}
			else
				return laneSection.getLaneCenterPointAt(this, s+range);
		}
		else
		{
			if(s-range < laneStartS)
			{
				// search predecessor
				double distToStart = s - laneStartS;
				
				if(predecessorLink != null && predecessorLink.getLane() != null)
				{
					ODLane predecessorLane = predecessorLink.getLane();
					double predecessorS = predecessorLane.getEndS();
					
					if(predecessorLink.getContactPoint() == ContactPoint.START)
					{
						increasingS = !increasingS;
						predecessorS = predecessorLane.getStartS();
					}
					
					double remainingRange = range - distToStart;
					return predecessorLane.getLaneCenterPointAhead(predecessorS, remainingRange, increasingS);
				}
				else if(printDebugMsg)
					System.err.println("no predecessor available (lane: " + laneID + "; road: " + road.getID() + "; getLaneCenterPointAhead)");
			}
			else
				return laneSection.getLaneCenterPointAt(this, s-range);
		}
		
		return null;
	}


	public ArrayList<SpeedLimit> getSpeedLimitListAhead(boolean isWrongWay, double s, int range)
	{
		ArrayList<SpeedLimit> speedLimitList;
		
		if(laneID>0)
			speedLimitList = getSpeedLimitListAhead(0, s, range, isWrongWay);//false
		else if(laneID<0)
			speedLimitList = getSpeedLimitListAhead(0, s, range, !isWrongWay);//true
		else
			return new ArrayList<SpeedLimit>();
		
		// sort speed limit list ascending by distance
		Collections.sort(speedLimitList, new SpeedLimitComparator(true));
		
		Double previousSpeedValue = -1.0;
		
		// remove speed limits which are out of range (<= 0 or > range) 
		Iterator<SpeedLimit> it = speedLimitList.iterator();
		while(it.hasNext())
		{
			SpeedLimit speedLimit = it.next();
			if(speedLimit.getDistance() <= 0)
			{
				previousSpeedValue = speedLimit.getSpeed();
				it.remove();
			}
			
			if(speedLimit.getDistance() > range)
				it.remove();
		}
		
		// remove entries repeating speed limits
		Iterator<SpeedLimit> it2 = speedLimitList.iterator();
		while(it2.hasNext())
		{
			SpeedLimit speedLimit = it2.next();
			Double speedValue = speedLimit.getSpeed();
			
			if(speedValue != null)
			{
				if(speedValue.equals(previousSpeedValue))
					it2.remove();
			}
			else
			{
				if(previousSpeedValue == null)
					it2.remove();
			}
			
			previousSpeedValue = speedValue;
		}
		
		return speedLimitList;
	}
	
	
	private ArrayList<SpeedLimit> getSpeedLimitListAhead(double traveledDistance, double s, double range, boolean increasingS)
	{
		if(laneStartS > s || s > laneEndS)
		{
			if(printDebugMsg)
				System.err.println("s=" + s + " is out of lane " + laneID + " (road: " + road.getID() + ", getSpeedLimitListAhead)");
			return new ArrayList<SpeedLimit>();
		}
		
		ArrayList<SpeedLimit> list = getSpeedLimitList(traveledDistance, s, increasingS);
			
		if(increasingS)
		{			
			if(s+range > laneEndS)
			{
				// search successor
				double distToEnd = laneEndS - s;
				
				if(successorLink != null && successorLink.getLane() != null)
				{
					ODLane successorLane = successorLink.getLane();
					double successorS = successorLane.getStartS();
					
					if(successorLink.getContactPoint() == ContactPoint.END)
					{
						increasingS = !increasingS;
						successorS = successorLane.getEndS();
					}
					
					double traveledDist = traveledDistance + distToEnd;
					double remainingRange = range - distToEnd;
					list.addAll(successorLane.getSpeedLimitListAhead(traveledDist, successorS, remainingRange, increasingS));
				}
				else if(printDebugMsg)
					System.err.println("no successor available (lane: " + laneID + "; road: " + road.getID() + "; getSpeedLimitListAhead)");
			}
		}
		else
		{			
			if(s-range < laneStartS)
			{
				// search predecessor
				double distToStart = s - laneStartS;
				
				if(predecessorLink != null && predecessorLink.getLane() != null)
				{
					ODLane predecessorLane = predecessorLink.getLane();
					double predecessorS = predecessorLane.getEndS();
					
					if(predecessorLink.getContactPoint() == ContactPoint.START)
					{
						increasingS = !increasingS;
						predecessorS = predecessorLane.getStartS();
					}
					
					double traveledDist = traveledDistance + distToStart;
					double remainingRange = range - distToStart;
					list.addAll(predecessorLane.getSpeedLimitListAhead(traveledDist, predecessorS, remainingRange, increasingS));
				}
				else if(printDebugMsg)
					System.err.println("no predecessor available (lane: " + laneID + "; road: " + road.getID() + "; getSpeedLimitListAhead)");
			}
		}
		
		return list;
	}


	private ArrayList<SpeedLimit> getSpeedLimitList(double traveledDistance, double s, boolean increasingS)
	{
		ArrayList<SpeedLimit> list = new ArrayList<SpeedLimit>();
		
		List<Speed> speedList = lane.getSpeed();
		
		if(increasingS)
		{
			for(int i=0; i<speedList.size(); i++)
			{
				double distance = traveledDistance + speedList.get(i).getSOffset() + laneStartS - s;
				double maxSpeed = speedList.get(i).getMax();
				
				SpeedLimit speedLimit = new SpeedLimit(distance, maxSpeed);
				list.add(speedLimit);
			}
			
			// insert "unlimited" before, if initial offset is present 
			if(speedList.size()>0 && speedList.get(0).getSOffset() != 0)
				list.add(new SpeedLimit(traveledDistance + laneStartS - s, null));
		}
		else
		{
			double lengthOfFollowingSpeedLimitSegments = 0;

			for(int i=speedList.size()-1; i>=0; i--)
			{
				double distance = traveledDistance + lengthOfFollowingSpeedLimitSegments + s - laneEndS;
				lengthOfFollowingSpeedLimitSegments = laneEndS - laneStartS - speedList.get(i).getSOffset();
				
				double maxSpeed = speedList.get(i).getMax();

				SpeedLimit speedLimit = new SpeedLimit(distance, maxSpeed);
				list.add(speedLimit);

			}
			
			// insert "unlimited" before, if initial offset is present 
			if(speedList.size()>0 && speedList.get(0).getSOffset() != 0)
				list.add(new SpeedLimit(traveledDistance + s - laneStartS - speedList.get(0).getSOffset(), null));
		}
		
		
		// if no speed limit available
		if(speedList.size() == 0)
		{
			SpeedLimit speedLimit = new SpeedLimit(traveledDistance, null);
			list.add(speedLimit);
		}
		
		return list;
	}


	public ArrayList<Intersection> getIntersectionAhead(boolean isWrongWay, double s, double range)
	{
		ArrayList<Intersection> speedLimitList;
		
		if(laneID>0)
			speedLimitList = getIntersectionAhead(0, s, range, isWrongWay);//false
		else if(laneID<0)
			speedLimitList = getIntersectionAhead(0, s, range, !isWrongWay);//true
		else
			return new ArrayList<Intersection>();		
		
		// sort intersection list ascending by distance
		Collections.sort(speedLimitList, new IntersectionComparator(true));

		// remove intersections which are out of range (<= 0 or > range) 
		Iterator<Intersection> it = speedLimitList.iterator();
		while(it.hasNext())
		{
			Intersection speedLimit = it.next();
			if(speedLimit.getDistance() <= 0 || speedLimit.getDistance() > range)
				it.remove();
		}

		return speedLimitList;
	}
	
	
	private ArrayList<Intersection> getIntersectionAhead(double traveledDistance, double s, double range, boolean increasingS)
	{
		if(laneStartS > s || s > laneEndS)
		{
			if(printDebugMsg)
				System.err.println("s=" + s + " is out of lane " + laneID + " (road: " + road.getID() + ", getIntersectionAhead)");
			return new ArrayList<Intersection>();
		}
		
		ArrayList<Intersection> list = getIntersectionList(traveledDistance, s, increasingS);
			
		if(increasingS)
		{
			if(s+range > laneEndS)
			{
				// search successor
				double distToEnd = laneEndS - s;
				
				if(successorLink != null && successorLink.getLane() != null)
				{
					ODLane successorLane = successorLink.getLane();
					double successorS = successorLane.getStartS();
					
					if(successorLink.getContactPoint() == ContactPoint.END)
					{
						increasingS = !increasingS;
						successorS = successorLane.getEndS();
					}
					
					double traveledDist = traveledDistance + distToEnd;
					double remainingRange = range - distToEnd;
					list.addAll(successorLane.getIntersectionAhead(traveledDist, successorS, remainingRange, increasingS));
				}
				else if(printDebugMsg)
					System.err.println("no successor available (lane: " + laneID + "; road: " + road.getID() + "; getIntersectionAhead)");
			}
		}
		else
		{
			if(s-range < laneStartS)
			{
				// search predecessor
				double distToStart = s - laneStartS;
				
				if(predecessorLink != null && predecessorLink.getLane() != null)
				{
					ODLane predecessorLane = predecessorLink.getLane();
					double predecessorS = predecessorLane.getEndS();
					
					if(predecessorLink.getContactPoint() == ContactPoint.START)
					{
						increasingS = !increasingS;
						predecessorS = predecessorLane.getStartS();
					}
					
					double traveledDist = traveledDistance + distToStart;
					double remainingRange = range - distToStart;
					list.addAll(predecessorLane.getIntersectionAhead(traveledDist, predecessorS, remainingRange, increasingS));
				}
				else if(printDebugMsg)
					System.err.println("no predecessor available (lane: " + laneID + "; road: " + road.getID() + "; getIntersectionAhead)");
			}
		}
		
		return list;
	}
	
	
	private ArrayList<Intersection> getIntersectionList(double traveledDistance, double s, boolean increasingS)
	{
		ArrayList<Intersection> list = new ArrayList<Intersection>();
		
		if(increasingS)
		{
			if(successorLink != null && successorLink.getNrOfLanes()>1)
			{
				double distance = traveledDistance + laneEndS - s;
				String junctionID = successorLink.getJunctionID();
				
				Intersection intersection = new Intersection(distance, junctionID);
				list.add(intersection);
			}
		}
		else
		{
			if(predecessorLink != null && predecessorLink.getNrOfLanes()>1)
			{
				double distance = traveledDistance + s - laneStartS;
				String junctionID = predecessorLink.getJunctionID();
				
				Intersection intersection = new Intersection(distance, junctionID);
				list.add(intersection);
			}
		}

		return list;
	}


	public double getSpeedLimit(double s)
	{
		double laneSection_s = laneSection.getS();
		List<Speed> widthList = lane.getSpeed();
		
		for(int i=widthList.size()-1; i>=0; i--)
		{
			double offset = laneSection_s + widthList.get(i).getSOffset();
			if(s >= offset)
				return widthList.get(i).getMax();
		}
		
		// return -1 if speed limit not set
		return -1;
	}

}
