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
import java.util.HashMap;
import java.util.List;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import eu.opends.main.Simulator;
import eu.opends.opendrive.OpenDRIVELoader;
import eu.opends.opendrive.data.CenterLane;
import eu.opends.opendrive.data.Lane;
import eu.opends.opendrive.data.RoadmarkType;
import eu.opends.opendrive.data.Lane.RoadMark;
import eu.opends.opendrive.processed.ODLane.LaneSide;
import eu.opends.opendrive.util.ODPointComparator;
import eu.opends.tools.Vector3d;
import eu.opends.opendrive.data.OpenDRIVE.Road.Lanes.LaneSection;
import eu.opends.opendrive.data.OpenDRIVE.Road.Lanes.LaneSection.Center;
import eu.opends.opendrive.data.OpenDRIVE.Road.Lanes.LaneSection.Left;
import eu.opends.opendrive.data.OpenDRIVE.Road.Lanes.LaneSection.Right;
import eu.opends.opendrive.data.OpenDRIVE.Road.PlanView.Geometry;


public class ODLaneSection
{
	private Simulator sim;
	private ODRoad road;
	private LaneSection laneSection;
	private double endS;
	private ArrayList<ODPoint> laneSectionReferencePointlist;
	private ArrayList<ODPoint> firstLaneReferencePointlist = new ArrayList<ODPoint>();
	private HashMap<Integer, ODLane> laneMap = new HashMap<Integer, ODLane>();
	private HashMap<Integer, ODLane> leftODLaneMap = new HashMap<Integer, ODLane>();
	private HashMap<Integer, ODLane> rightODLaneMap = new HashMap<Integer, ODLane>();
	private List<eu.opends.opendrive.data.CenterLane.RoadMark> centerLaneRoadMarkList = 
			new ArrayList<eu.opends.opendrive.data.CenterLane.RoadMark>();

	
	public ODLaneSection(Simulator sim, ODRoad road, ArrayList<ODPoint> laneSectionReferencePointlist, 
			LaneSection laneSection, double endS) 
	{
		this.sim = sim;
		this.road = road;
		this.laneSection = laneSection;
		this.endS = endS;
		this.laneSectionReferencePointlist = laneSectionReferencePointlist;
		
		Left left = laneSection.getLeft();
		if(left != null)
		{
			List<Lane> leftLaneList = left.getLane();
			for(Lane lane : leftLaneList)
			{
				// insert ODPoints at positions where lanes will be split (due to road marker changes)
				List<RoadMark> roadMarkList = lane.getRoadMark();
				for(int i=roadMarkList.size()-1; i>=0; i--)
				{
					double s = laneSection.getS() + roadMarkList.get(i).getSOffset();
					ODPoint resultingPoint = road.getPointOnReferenceLine(s, "roadMarkSeparation_" + s);
					if(resultingPoint != null)
						laneSectionReferencePointlist.add(resultingPoint);
				}
				
				ODLane l = new ODLane(sim, road, this, lane, LaneSide.LEFT);
				laneMap.put(lane.getId(), l);
				leftODLaneMap.put(lane.getId(), l);
			}
		}
		
		Center center = laneSection.getCenter();
		if(center != null)
		{
			CenterLane centerLane = center.getLane();
			//int ID = centerLane.getId();
			//LaneType type = centerLane.getType();
			//SingleSide level = centerLane.getLevel();
			centerLaneRoadMarkList = centerLane.getRoadMark();
			for(int i=centerLaneRoadMarkList.size()-1; i>=0; i--)
			{
				double s = laneSection.getS() + centerLaneRoadMarkList.get(i).getSOffset();
				ODPoint resultingPoint = road.getPointOnReferenceLine(s, "roadMarkSeparation_" + s);
				if(resultingPoint != null)
					laneSectionReferencePointlist.add(resultingPoint);
			}
						
			/*
			eu.opends.opendrive.data.CenterLane.Link link = centerLane.getLink();
			if(link != null)
			{
				Integer predecessorID = null;
				eu.opends.opendrive.data.CenterLane.Link.Predecessor predecessor = link.getPredecessor();
				if(predecessor != null)
					predecessorID = predecessor.getId();
				
				Integer successorID = null;
				eu.opends.opendrive.data.CenterLane.Link.Successor successor = link.getSuccessor();
				if(successor != null)
					successorID = successor.getId();
			}		
			*/			
		}
		
		Right right = laneSection.getRight();
		if(right != null)
		{
			List<Lane> rightLaneList = right.getLane();
			for(Lane lane : rightLaneList)
			{
				// insert ODPoints at positions where lanes will be split (due to road marker changes)
				List<RoadMark> roadMarkList = lane.getRoadMark();
				for(int i=roadMarkList.size()-1; i>=0; i--)
				{
					double s = laneSection.getS() + roadMarkList.get(i).getSOffset();
					ODPoint resultingPoint = road.getPointOnReferenceLine(s, "roadMarkSeparation_" + s);
					if(resultingPoint != null)
						laneSectionReferencePointlist.add(resultingPoint);
				}	
				
				ODLane l = new ODLane(sim, road, this, lane, LaneSide.RIGHT);
				laneMap.put(lane.getId(), l);
				rightODLaneMap.put(lane.getId(), l);
			}
		}
		
		// sort list of reference points by ascending s (order disrupted due to insertion of additional points)
		Collections.sort(laneSectionReferencePointlist, new ODPointComparator(true));
	}

	
	public List<eu.opends.opendrive.data.CenterLane.RoadMark> getCenterLaneRoadMarkList()
	{
		return centerLaneRoadMarkList;
	}
	
	
	public ODPoint applyLaneOffset(ODPoint point)
	{
		String ID = point.getID() + "_laneRef";
		double s = point.getS();
		Vector3d position = point.getPosition();
		double ortho = point.getOrtho();
		Geometry geometry = point.getGeometry();
			
		// apply lane offset
		double laneOffset = -road.getLaneOffset(s);
		
		Vector3d resultPos = position.add(new Vector3d((laneOffset)*Math.sin(ortho), 0, (laneOffset)*Math.cos(ortho)));		
		return new ODPoint(ID, s, resultPos, ortho, geometry, null);
	}

	
	public ODRoad getODRoad() 
	{
		return road;
	}

	
	public void initLanes(boolean visualize)
	{
		for(ODPoint point : laneSectionReferencePointlist)
			firstLaneReferencePointlist.add(applyLaneOffset(point));
		
		// visualize left lanes 1, 2, 3, ...
		for(int i=1; i<=leftODLaneMap.size(); i++)
		{			
			if(i==1)
				leftODLaneMap.get(i).initODLane(firstLaneReferencePointlist, visualize);
			else
			{
				ArrayList<ODPoint> borderPoints = leftODLaneMap.get(i-1).getBorderPoints();
				leftODLaneMap.get(i).initODLane(borderPoints, visualize);
			}
		}

		// visualize right lanes -1, -2, -3, ...
		for(int i=-1; Math.abs(i)<=rightODLaneMap.size(); i--)
		{			
			if(i==-1)
				rightODLaneMap.get(i).initODLane(firstLaneReferencePointlist, visualize);
			else
			{
				ArrayList<ODPoint> borderPoints = rightODLaneMap.get(i+1).getBorderPoints();
				rightODLaneMap.get(i).initODLane(borderPoints, visualize);
			}
		}	
		

		// visualize center line
		for(int i=centerLaneRoadMarkList.size()-1; i>=0; i--)
		{				
			RoadmarkType roadmarkType = centerLaneRoadMarkList.get(i).getType();
			ArrayList<ODPoint> pointlist2 = new ArrayList<ODPoint>();
			
			eu.opends.opendrive.data.CenterLane.RoadMark roadMark = centerLaneRoadMarkList.get(i);
			double startS =  laneSection.getS() + roadMark.getSOffset();
			
			if(i+1 < centerLaneRoadMarkList.size())
				endS =  laneSection.getS() + centerLaneRoadMarkList.get(i+1).getSOffset();
			
			for(ODPoint point : firstLaneReferencePointlist)
				if(startS <= point.getS() && point.getS() <= endS)
					pointlist2.add(point);
			
			drawCenterLineSegment(pointlist2, visualize, roadmarkType);
		}
	}


	private void drawCenterLineSegment(ArrayList<ODPoint> pointlist, boolean visualize, RoadmarkType roadmarkType)
	{
		if(pointlist.size()<2)
		{
			System.out.println("Pointlist (for center line) too small");
			return;
		}
		
		Vector3f[] verticesLeft = new Vector3f[2*pointlist.size()];
		Vector3f[] verticesRight = new Vector3f[2*pointlist.size()];
		Vector2f[] texCoordLeft = new Vector2f[2*pointlist.size()];
		Vector2f[] texCoordRight = new Vector2f[2*pointlist.size()];
		int [] indexesLeft = new int[6*(pointlist.size()-1)];
		int [] indexesRight = new int[6*(pointlist.size()-1)];
		
		for(int i=0; i<pointlist.size(); i++)
		{
			// get point parameters
			ODPoint point = pointlist.get(i);
			double s = point.getS();
			Vector3d position = point.getPosition();
			double ortho = point.getOrtho();

			
			double textureOffset = 0.5f;
			
			eu.opends.opendrive.data.CenterLane.RoadMark roadMark = getCenterLineRoadMarkAtPos(s);
			if(roadMark.getWidth() != null)
				textureOffset *= roadMark.getWidth();
			else	
				System.err.println("WARNING: Road: " + getODRoad().getID() + "; centerLineRoadMark width == null (ODLaneSection)");
			
			Vector3d textureLeftPos = position.subtract(new Vector3d((textureOffset)*Math.sin(ortho), 0, (textureOffset)*Math.cos(ortho)));
			
			// place vertex on top of underlying surface (if enabled)
			textureLeftPos.setY(sim.getOpenDriveCenter().getHeightAt(textureLeftPos.getX(), textureLeftPos.getZ()));
			
			verticesLeft[2*i] = textureLeftPos.toVector3f();
			
			Vector3d textureMiddlePos = position;

			// place vertex on top of underlying surface (if enabled)
			textureMiddlePos.setY(sim.getOpenDriveCenter().getHeightAt(textureMiddlePos.getX(), textureMiddlePos.getZ()));
			
			verticesLeft[2*i+1] = textureMiddlePos.toVector3f();
			
			verticesRight[2*i] = textureMiddlePos.toVector3f();
			
			Vector3d textureRightPos = position.add(new Vector3d((textureOffset)*Math.sin(ortho), 0, (textureOffset)*Math.cos(ortho)));

			// place vertex on top of underlying surface (if enabled)
			textureRightPos.setY(sim.getOpenDriveCenter().getHeightAt(textureRightPos.getX(), textureRightPos.getZ()));
			
			verticesRight[2*i+1] = textureRightPos.toVector3f();
			

			if(i%2==0)
			{
				texCoordLeft[2*i] = new Vector2f(0,0);
				texCoordLeft[2*i+1] = new Vector2f(1,0);
				texCoordRight[2*i] = new Vector2f(0,0);
				texCoordRight[2*i+1] = new Vector2f(1,0);
			}			
			else
			{
				texCoordLeft[2*i] = new Vector2f(0,1);
				texCoordLeft[2*i+1] = new Vector2f(1,1);
				texCoordRight[2*i] = new Vector2f(0,1);
				texCoordRight[2*i+1] = new Vector2f(1,1);
			}

			
			if(i<pointlist.size()-1)
			{
				indexesLeft[6*i+0] = 2*i+0;
				indexesLeft[6*i+1] = 2*i+1;
				indexesLeft[6*i+2] = 2*i+3;
				indexesLeft[6*i+3] = 2*i+3;
				indexesLeft[6*i+4] = 2*i+2;
				indexesLeft[6*i+5] = 2*i+0;
				
				indexesRight[6*i+0] = 2*i+0;
				indexesRight[6*i+1] = 2*i+1;
				indexesRight[6*i+2] = 2*i+3;
				indexesRight[6*i+3] = 2*i+3;
				indexesRight[6*i+4] = 2*i+2;
				indexesRight[6*i+5] = 2*i+0;
			}
		}

		Material material;
		if(roadmarkType == RoadmarkType.SOLID)
			material = sim.getOpenDriveCenter().getVisualizer().roadSolidLineTextureMaterial;
		else if(roadmarkType == RoadmarkType.BROKEN)
			material = sim.getOpenDriveCenter().getVisualizer().roadBrokenLineTextureMaterial;
		else
			material = sim.getOpenDriveCenter().getVisualizer().roadNoLineTextureMaterial;	
		

		Mesh meshLeft = new Mesh();
		meshLeft.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(verticesLeft));
		meshLeft.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoordLeft));
		meshLeft.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexesLeft));
		meshLeft.scaleTextureCoordinates(new Vector2f(1f,0.5f));
		meshLeft.updateBound();
		
		Mesh meshRight = new Mesh();
		meshRight.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(verticesRight));
		meshRight.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoordRight));
		meshRight.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexesRight));
		meshRight.scaleTextureCoordinates(new Vector2f(1f,0.5f));
		meshRight.updateBound();
		
		com.jme3.scene.Geometry geoLeft = new com.jme3.scene.Geometry("ODarea_" + road.getID() + "_1", meshLeft);
		geoLeft.setMaterial(material);

		com.jme3.scene.Geometry geoRight = new com.jme3.scene.Geometry("ODarea_" + road.getID() + "_-1", meshRight);
		geoRight.setMaterial(material);
		
		if(!visualize)
		{
			geoLeft.setCullHint(CullHint.Always);
			geoRight.setCullHint(CullHint.Always);
		}
		
		if(leftODLaneMap.containsKey(1))
		{
			sim.getOpenDriveNode().attachChild(geoLeft);	
			
			if(!(sim instanceof OpenDRIVELoader))
				addToBulletPhysicsSpace(geoLeft);
		}
		
		if(rightODLaneMap.containsKey(-1))
		{
			sim.getOpenDriveNode().attachChild(geoRight);
			
			if(!(sim instanceof OpenDRIVELoader))
				addToBulletPhysicsSpace(geoRight);
		}
		
		//System.err.println(roadID + "_" + laneID);
	}


	public void addToBulletPhysicsSpace(Spatial spatial)
	{
		CollisionShape collisionShape = CollisionShapeFactory.createMeshShape(spatial);		        
        RigidBodyControl physicsControl = new RigidBodyControl(collisionShape, 0);
        spatial.addControl(physicsControl);
		sim.getBulletPhysicsSpace().add(physicsControl);
	}
	
	
	public eu.opends.opendrive.data.CenterLane.RoadMark getCenterLineRoadMarkAtPos(double s) 
	{
		for(int i=centerLaneRoadMarkList.size()-1; i>=0; i--)
		{							
			eu.opends.opendrive.data.CenterLane.RoadMark roadMark = centerLaneRoadMarkList.get(i);
			
			double _startS =  laneSection.getS() + roadMark.getSOffset();
			double _endS = endS;
			
			if(i+1 < centerLaneRoadMarkList.size())
				_endS = laneSection.getS() + centerLaneRoadMarkList.get(i+1).getSOffset();
			
			if(_startS <= s && s <= _endS)
				return roadMark;
		}
		
		return null;
	}


	public double getS()
	{
		return laneSection.getS();
	}
	
	
	public double getEndS()
	{
		return endS;
	}


	public void setCurrentLaneBorders(ODPoint roadReferencePoint)
	{
		ODPoint laneReferencePoint = applyLaneOffset(roadReferencePoint);
		
		// set lane borders of left lanes 1, 2, 3, ... for given point
		for(int i=1; i<=leftODLaneMap.size(); i++)
		{			
			if(i==1)
				leftODLaneMap.get(i).setCurrentBorderPoints(laneReferencePoint);
			else
			{
				ODPoint borderPoint = leftODLaneMap.get(i-1).getCurrentOuterBorderPoint();
				leftODLaneMap.get(i).setCurrentBorderPoints(borderPoint);
			}
		}

		// set lane borders of right lanes -1, -2, -3, ... for given point
		for(int i=-1; Math.abs(i)<=rightODLaneMap.size(); i--)
		{			
			if(i==-1)
				rightODLaneMap.get(i).setCurrentBorderPoints(laneReferencePoint);
			else
			{
				ODPoint borderPoint = rightODLaneMap.get(i+1).getCurrentOuterBorderPoint();
				rightODLaneMap.get(i).setCurrentBorderPoints(borderPoint);
			}
		}
	}


	public HashMap<Integer, ODLane> getLaneMap()
	{
		return laneMap;
	}


	public HashMap<Integer, ODLane> getLeftLaneMap() 
	{
		return leftODLaneMap;
		
	}
	
	
	public HashMap<Integer, ODLane> getRightLaneMap() 
	{
		return rightODLaneMap;
		
	}


	public ODLane getLane(Integer ID)
	{
		return laneMap.get(ID);
	}


	public ODPoint getLaneCenterPointAt(ODLane lane, double s) 
	{
		ODPoint roadReferencePoint = road.getPointOnReferenceLine(s, "point_s_" + s);
		ODPoint laneReferencePoint = applyLaneOffset(roadReferencePoint);
		
		int laneID = lane.getID();
		double width = 0;
		
		if(0 < laneID && laneID <= leftODLaneMap.size())
		{
			// get accumulated lane width of left lanes -1, -2, -3, ... until given lane
			for(int i=1; i<=laneID; i++)
			{
				if(i==laneID)
					width -= 0.5*leftODLaneMap.get(i).getWidth(s);
				else
					width -= leftODLaneMap.get(i).getWidth(s);
			}
		}
		
		else if(-rightODLaneMap.size() <= laneID && laneID < 0)
		{
			// get accumulated lane width of right lanes -1, -2, -3, ... until given lane
			for(int i=-1; i>=laneID; i--)
			{			
				if(i==laneID)
					width += 0.5*rightODLaneMap.get(i).getWidth(s);
				else
					width += rightODLaneMap.get(i).getWidth(s);
			}
		}
		
		// get point parameters
		String pointID = laneReferencePoint.getID();
		double ortho = laneReferencePoint.getOrtho();
		Vector3d position = laneReferencePoint.getPosition();
		Vector3d centerPos = position.add(new Vector3d((width)*Math.sin(ortho), 0, (width)*Math.cos(ortho)));
		Geometry geometry = laneReferencePoint.getGeometry();

		return new ODPoint(pointID+"_"+laneID, s, centerPos, ortho, geometry, lane);
	}
}
