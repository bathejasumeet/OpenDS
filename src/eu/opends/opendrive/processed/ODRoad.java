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
import java.util.HashMap;
import java.util.List;

import com.jme3.math.Vector3f;

import eu.opends.main.Simulator;
import eu.opends.opendrive.data.ContactPoint;
import eu.opends.opendrive.data.ElementType;
import eu.opends.opendrive.data.OpenDRIVE.Junction;
import eu.opends.opendrive.data.OpenDRIVE.Road;
import eu.opends.opendrive.data.OpenDRIVE.Road.Lanes.LaneOffset;
import eu.opends.opendrive.data.OpenDRIVE.Road.Lanes.LaneSection;
import eu.opends.opendrive.data.OpenDRIVE.Road.Link.Predecessor;
import eu.opends.opendrive.data.OpenDRIVE.Road.Link.Successor;
import eu.opends.opendrive.data.OpenDRIVE.Road.PlanView.Geometry;
import eu.opends.opendrive.data.PRange;
import eu.opends.opendrive.processed.ODPoint.GeometryType;
import eu.opends.opendrive.util.Spiral;
import eu.opends.opendrive.util.CurvePoint;
import eu.opends.opendrive.util.ODVisualizer;
import eu.opends.tools.Vector3d;


public class ODRoad 
{
	private boolean vizLanes = true;
	private boolean vizLine = false;
	private boolean vizArc = false;
	private boolean vizSpiral = false;
	private boolean vizPoly3 = false;
	private boolean vizParamPoly3 = false;
	private boolean vizStartBox = false;
	private boolean vizTargetBox = false;
	private boolean vizIntermediateBox = false;
	private boolean vizOrtho = false;
	private boolean vizArrows = false;
	private boolean vizOrthoArrows = false;
	private double interpolationStep = 1;
	
	private Simulator sim;
	private ODVisualizer visualizer;
	private Road road;
	private ArrayList<ODPoint> roadReferencePointlist = new ArrayList<ODPoint>();
	private ArrayList<ODLaneSection> ODLaneSectionList = new ArrayList<ODLaneSection>();
	private int pointCounter = 1;
	
	
	public ODRoad(Simulator sim, Road road) 
	{
		this.sim = sim;
		this.visualizer = sim.getOpenDriveCenter().getVisualizer();
		this.road = road;
		
		int segmentCounter = 1;
		String roadID = road.getId();
		
		// extract points of reference line from plan view geometries
		for(Geometry geometry : road.getPlanView().getGeometry())
		{
			String ID = roadID + "_" + segmentCounter;
			segmentCounter++;
			
			if(geometry.getLine() != null)
				roadReferencePointlist.addAll(extractPointsFromLine(geometry, ID));
			else if(geometry.getArc() != null)
				roadReferencePointlist.addAll(extractPointsFromArc(geometry, ID));
			else if(geometry.getSpiral() != null)
				roadReferencePointlist.addAll(extractPointsFromSpiral(geometry, ID));
			else if(geometry.getPoly3() != null)
				roadReferencePointlist.addAll(extractPointsFromPoly3(geometry, ID));
			else if(geometry.getParamPoly3() != null)
				roadReferencePointlist.addAll(extractPointsFromParamPoly3(geometry, ID));
		}

		
		List<LaneSection> laneSectionList = road.getLanes().getLaneSection();
		for(int i=0; i<laneSectionList.size(); i++)
		{
			LaneSection ls = laneSectionList.get(i);
			double startS = ls.getS();
			
			double endS = road.getLength();
			if(i+1 < laneSectionList.size())
				endS = laneSectionList.get(i+1).getS();
		
			ArrayList<ODPoint> sectionReferencePointList = getReferencePointsOnSection(startS, endS);
			//System.err.println(road.getId() + "->" + sectionReferencePointList.size());				
			ODLaneSection laneSection = new ODLaneSection(sim, this, sectionReferencePointList, ls, endS);
			laneSection.initLanes(vizLanes);
			ODLaneSectionList.add(laneSection);
		}
	}
		
	
	// Constructor only used by GeometryGenerator
	public ODRoad(Simulator sim, ArrayList<Geometry> geometryList)
	{
		this.sim = sim;
		this.visualizer = sim.getOpenDriveCenter().getVisualizer();

		
		vizLanes = false;
		vizLine = true;
		vizArc = true;
		vizSpiral = true;
		vizPoly3 = false;
		vizParamPoly3 = false;
		vizStartBox = false;
		vizTargetBox = false;
		vizIntermediateBox = true;
		vizOrtho = true;
		vizArrows = true;
		vizOrthoArrows = false;
		interpolationStep = 0.5;
		
		
		int segmentCounter = 1;
		
		// extract points of reference line from plan view geometries
		for(int i=0; i<geometryList.size(); i++)
		{
			ArrayList<ODPoint> pointList = new ArrayList<ODPoint>();
			Geometry geometry = geometryList.get(i);
			
			String ID = "roadPart" + "_" + segmentCounter;
			segmentCounter++;
			
			if(geometry.getLine() != null)
				pointList = extractPointsFromLine(geometry, ID);
			else if(geometry.getArc() != null)
				pointList = extractPointsFromArc(geometry, ID);
			else if(geometry.getSpiral() != null)
				pointList = extractPointsFromSpiral(geometry, ID);
			else if(geometry.getPoly3() != null)
				pointList = extractPointsFromPoly3(geometry, ID);
			else if(geometry.getParamPoly3() != null)
				pointList = extractPointsFromParamPoly3(geometry, ID);
			
			if(pointList != null && !pointList.isEmpty() && geometryList.size()>i+1)
			{
				ODPoint lastPoint = pointList.get(pointList.size()-1);
				Geometry nextGeometry = geometryList.get(i+1);
				
				nextGeometry.setX(lastPoint.getPosition().getX());
				nextGeometry.setY(-lastPoint.getPosition().getZ());
				nextGeometry.setS(lastPoint.getS());
				nextGeometry.setHdg(lastPoint.getOrtho());
			}
			
			roadReferencePointlist.addAll(pointList);
		}
	}


	/**
	 * Sets up successor and predecessor lanes.
	 * Needs to be called after all ODRoads have been created (c.f. constructor).
	 */
	public void initLinks() 
	{
		for(int i=0; i<ODLaneSectionList.size(); i++)
		{
			ODLaneSection laneSection = ODLaneSectionList.get(i);
			
			for(ODLane lane : laneSection.getLaneMap().values())
			{
				// Successor
				// ---------
				Integer laneSuccessorID = lane.getSuccessorID();
				
				// no junction
				if(laneSuccessorID != null)
				{
					if(i+1 < ODLaneSectionList.size())
					{
						// successor is on same road in next lane section
						ODLane successorLane = ODLaneSectionList.get(i+1).getLane(laneSuccessorID);
						lane.setSuccessor(new ODLink(sim, successorLane, ContactPoint.START));
					}
					else
					{
						// successor is on next road
						Successor roadSuccessor = road.getLink().getSuccessor();
						if(roadSuccessor != null)
						{
							String roadSuccessorID = roadSuccessor.getElementId();
							if(roadSuccessor.getElementType() == ElementType.ROAD && roadSuccessorID != null)
							{
								ContactPoint roadSuccessorContactPoint = roadSuccessor.getContactPoint();
								lane.setSuccessor(new ODLink(sim, roadSuccessorID, laneSuccessorID, roadSuccessorContactPoint));
							}
						}
					}
				}
				
				// junction
				else
				{
					Successor roadSuccessor = road.getLink().getSuccessor();
					if(roadSuccessor != null)
					{
						String roadSuccessorID = roadSuccessor.getElementId();
						if(roadSuccessor.getElementType() == ElementType.JUNCTION && roadSuccessorID != null)
						{
							List<Junction> junctionList = sim.getOpenDriveCenter().getJunctionList();
							lane.setSuccessor(new ODLink(sim, junctionList, roadSuccessorID, road.getId(), lane.getID()));
						}
					}
				}	
				
				// Predecessor
				// -----------
				Integer lanePredecessorID = lane.getPredecessorID();
				if(lanePredecessorID != null)
				{
					if(i-1 >= 0)
					{
						// predecessor is on same road in previous lane section
						ODLane predecessorLane = ODLaneSectionList.get(i-1).getLane(lanePredecessorID);
						lane.setPredecessor(new ODLink(sim, predecessorLane, ContactPoint.END));
					}
					else
					{
						// predecessor is on previous road
						Predecessor predecessor = road.getLink().getPredecessor();
						if(predecessor != null)
						{
							String roadPredecessorID = predecessor.getElementId();
							if(predecessor.getElementType() == ElementType.ROAD && roadPredecessorID != null)
							{
								ContactPoint roadPredecessorContactPoint = predecessor.getContactPoint();
								lane.setPredecessor(new ODLink(sim, roadPredecessorID, lanePredecessorID, roadPredecessorContactPoint));
							}
						}
					}
				}
				
				// junction
				else
				{
					Predecessor predecessor = road.getLink().getPredecessor();
					if(predecessor != null)
					{
						String roadPredecessorID = predecessor.getElementId();
						if(predecessor.getElementType() == ElementType.JUNCTION && roadPredecessorID != null)
						{
							List<Junction> junctionList = sim.getOpenDriveCenter().getJunctionList();
							lane.setPredecessor(new ODLink(sim, junctionList, roadPredecessorID, road.getId(), lane.getID()));
						}
					}
				}
			}
		}	
	}


	public double getLaneOffset(double s)
	{
		List<LaneOffset> laneOffsetList = road.getLanes().getLaneOffset();
		for(int i=laneOffsetList.size()-1; i>=0; i--)
		{
			double sOffset = laneOffsetList.get(i).getS();
			if(s >= sOffset)
			{
				double a = laneOffsetList.get(i).getA();
				double b = laneOffsetList.get(i).getB();
				double c = laneOffsetList.get(i).getC();
				double d = laneOffsetList.get(i).getD();
				
				double ds = s - sOffset;
				
				return a + b*ds + c*ds*ds + d*ds*ds*ds;
			}
		}
		return 0;
	}


	private ArrayList<ODPoint> getReferencePointsOnSection(double startS, double endS)
	{
		ArrayList<ODPoint> refPointList = new ArrayList<ODPoint>();

		// add reference point at start of section (in case there is no reference point in the transition area) 
		ODPoint startPoint = getPointOnReferenceLine(startS, "LaneSection_" + startS + "_startPoint"); // startS == LS-ID
		if(startPoint != null)
			refPointList.add(startPoint);
		
		for(ODPoint point : roadReferencePointlist)
			if(startS < point.getS() && point.getS() < endS)
				refPointList.add(point);
		
		// add reference point at end of section (in case there is no reference point in the transition area) 
		ODPoint endPoint = getPointOnReferenceLine(endS, "LaneSection_" + startS + "_endPoint"); // startS == LS-ID
		if(endPoint != null)
			refPointList.add(endPoint);

		return refPointList;
	}


	public ODPoint getPointOnReferenceLine(double s, String pointID)
	{
		Geometry geometry = getGeometryAtS(s);
		if(geometry != null)
		{
			double ds = s - geometry.getS();
			
			if(geometry.getLine() != null)
				return getPointOnLine(ds, geometry, pointID);
			else if(geometry.getArc() != null)
				return getPointOnArc(ds, geometry, pointID);
			else if(geometry.getSpiral() != null)
				return getPointOnSpiral(ds, geometry, pointID);
			else if(geometry.getPoly3() != null)
				return getPointOnPoly3(ds, geometry, pointID);
			else if(geometry.getParamPoly3() != null)
				return getPointOnParamPoly3(ds, geometry, pointID);
		}
		return null;
	}


	public Geometry getGeometryAtS(double s)
	{
		for(int i=road.getPlanView().getGeometry().size()-1; i>=0; i--)
		{
			Geometry geometry = road.getPlanView().getGeometry().get(i);
			//System.err.println("GS: " + geometry.getS() + ", s: " + s + ", GE: " + (geometry.getS() + geometry.getLength()));
			if(geometry.getS() <= s && s <= (geometry.getS() + geometry.getLength()))
				return geometry;
		}
		return null;
	}
	
/*
	public Geometry getGeometryAtS(double s)
	{
		for(Geometry geometry : road.getPlanView().getGeometry())
		{
			//System.err.println("GS: " + geometry.getS() + ", s: " + s + ", GE: " + (geometry.getS() + geometry.getLength()));
			if(geometry.getS() <= s && s <= (geometry.getS() + geometry.getLength()))
				return geometry;
		}
		return null;
	}
*/
	
	public ArrayList<ODPoint> getRoadReferencePointlist()
	{
		return roadReferencePointlist;
	}
	
	
	public String getID()
	{
		return road.getId();
	}
	

	public HashMap<Integer,ODLane> getLaneInformationAtPosition(Vector3f pos)
	{
		ODPoint point = getNearestPointOnReferenceLine(pos);
		return getLaneInformationAtODPoint(point);
	}
	
	
	public HashMap<Integer,ODLane> getLaneInformationAtODPoint(ODPoint point)
	{
		if(point != null)
		{
			double s = point.getS();
			for(int i=ODLaneSectionList.size()-1; i>=0; i--)
			{
				ODLaneSection laneSection = ODLaneSectionList.get(i);
				if(s >= laneSection.getS())
				{
					laneSection.setCurrentLaneBorders(point);
					return laneSection.getLaneMap();
				}
			}
		}
		return null;
	}

	
	public ODPoint getNearestPointOnReferenceLine(Vector3f pos)
	{
		return getNearestPointOnReferenceLine(new Vector3d(pos.x, pos.y, pos.z)); 
	}
	
	
	public ODPoint getNearestPointOnReferenceLine(Vector3d P)
	{
		ODPoint referencePointA = null;
		ODPoint referencePointB = null;
		Vector3d nearestPointOnLineAB = null;
		double nearestDistance = Double.MAX_VALUE;
		double nearestPercentage = -1;
		
		// iterate over all pairs of successive points of reference point list
		for(int i=0; i<roadReferencePointlist.size()-1; i++)
		{
			Vector3d A = roadReferencePointlist.get(i).getPosition();
			Vector3d B = roadReferencePointlist.get(i+1).getPosition();		

			Vector3d AP = P.subtract(A);
			Vector3d AB = B.subtract(A);
			
			if(AB.equals(Vector3d.ZERO))
				continue;
			
			double magAB2 = AB.x*AB.x + AB.z*AB.z; 
			double ABdotAP = AB.dot(AP);
			double percentage = ABdotAP / magAB2;
			
			Vector3d pointOnLineAB;
			if ( percentage < 0)
				pointOnLineAB = A;
			else if (percentage > 1)
				pointOnLineAB = B;
			else
				pointOnLineAB = A.add(AB.mult(percentage));

			double distance = P.distance(pointOnLineAB);
			
			if(distance < nearestDistance)
			{
				referencePointA = roadReferencePointlist.get(i);
				referencePointB = roadReferencePointlist.get(i+1);
				nearestPointOnLineAB = pointOnLineAB;
				nearestDistance = distance;
				nearestPercentage = percentage;
			}
		}
		
		if(nearestPointOnLineAB != null)
		{
			GeometryType geometryType = referencePointA.getGeometryType();
			Geometry geometry = referencePointA.getGeometry();
			
			// calculate global s value of nearest point (C) on line AB
			double pointA_s = referencePointA.getS();
			double pointB_s = referencePointB.getS();
			double pointC_s = pointA_s + nearestPercentage * (pointB_s - pointA_s);
			
			// calculate ds (relative s value for given geometry)
			double geometry_s = referencePointA.getGeometry().getS();
			double ds = pointC_s - geometry_s;

			String pointID = "intermediateReferencePoint" + pointCounter;
			pointCounter++;
			
			if(geometryType == GeometryType.Line)
				return getPointOnLine(ds, geometry, pointID);
			else if(geometry.getArc() != null)
				return getPointOnArc(ds, geometry, pointID);
			else if(geometry.getSpiral() != null)
				return getPointOnSpiral(ds, geometry, pointID);
			else if(geometry.getPoly3() != null)
				return getPointOnPoly3(ds, geometry, pointID);
			else if(geometry.getParamPoly3() != null)
				return getPointOnParamPoly3(ds, geometry, pointID);
		}

		return null;
	}

	
	private ArrayList<ODPoint> extractPointsFromLine(Geometry geometry, String ID) 
	{
		//System.out.println("Generating line");
		
		ArrayList<ODPoint> pointList = new ArrayList<ODPoint>();

		String lineID = ID + "_line";
		
		double length = geometry.getLength();
		double ortho = geometry.getHdg();
		double startPointS = geometry.getS();
		
		// extract start point
		String startPointID = lineID + "_startPoint";
		Vector3d startPos = getStartPos(geometry);
		ODPoint startPoint = new ODPoint(startPointID, startPointS, startPos, ortho, geometry, null);
		//pointList.add(startPoint); // already added in loop below
		if(vizLine && vizStartBox)
		{
			visualizer.drawBox(startPointID, startPos.toVector3f(), visualizer.redWireMaterial, 0.3f);

	        if(vizOrtho)
	        	visualizer.drawOrthogonal(startPointID + "_ortho", startPoint, 
	        			visualizer.blueMaterial, 3, 0.03f, vizOrthoArrows);
		}
		
		// extract intermediate points
		int pointCounter = 1;
	    for (double ds = 0.0; ds < length; ds += interpolationStep)
	    {
	    	String intermediatePointID = lineID + "_intermediatePoint_" + pointCounter;
	    	ODPoint intermediatePoint = getPointOnLine(ds, geometry, intermediatePointID);
	        pointList.add(intermediatePoint);
	        if(vizLine && vizIntermediateBox)
	        {
	        	visualizer.drawBox(intermediatePointID, intermediatePoint.getPosition().toVector3f(),
	        			visualizer.blueMaterial, 0.03f);
	        	
		        if(vizOrtho)
		        	visualizer.drawOrthogonal(intermediatePointID + "_ortho", intermediatePoint, 
		        			visualizer.blueMaterial, 3, 0.03f, vizOrthoArrows);
	        }
	        pointCounter++;
	    }
		
		// extract target point
		String targetPointID = lineID + "_targetPoint";
		ODPoint targetPoint = getPointOnLine(length, geometry, targetPointID);
		pointList.add(targetPoint);
		if(vizLine && vizTargetBox)
		{
			visualizer.drawBox(targetPointID, targetPoint.getPosition().toVector3f(), visualizer.greenWireMaterial, 0.2f);
			
	        if(vizOrtho)
	        	visualizer.drawOrthogonal(targetPointID + "_ortho", targetPoint, 
	        			visualizer.blueMaterial, 3, 0.03f, vizOrthoArrows);
		}
		
		if(vizLine)
			visualizer.drawConnector(lineID, pointList, visualizer.blueMaterial, vizArrows);
		
        return pointList;
	}    
	
	
	private ODPoint getPointOnLine(double ds, Geometry geometry, String pointID)
	{
		double startPointS = geometry.getS();
    	Vector3d pos = getPositionOnLine(geometry, ds);
    	double ortho = geometry.getHdg();
        return new ODPoint(pointID, startPointS + ds, pos, ortho, geometry, null);
	}
	
	
	private Vector3d getStartPos(Geometry geometry)
	{
		double x = geometry.getX();
		double y = geometry.getY();
		
		// convert coordinate systems
		return new Vector3d(x, 0, -y);
	}

	
	private Vector3d getPositionOnLine(Geometry geometry, double ds)
	{
		double hdg = geometry.getHdg();
		
		double targetX = geometry.getX() + Math.cos(hdg) * ds;
		double targetY = geometry.getY() + Math.sin(hdg) * ds;

		// convert coordinate systems
		return new Vector3d(targetX, 0, -targetY);
	}


	private ArrayList<ODPoint> extractPointsFromArc(Geometry geometry, String ID)
	{
		//System.out.println("Generating arc");
		
		String arcID = ID + "_arc";
		
		ArrayList<ODPoint> pointList = new ArrayList<ODPoint>();

		double length = geometry.getLength();

		// extract start point
		String startPointID = arcID + "_startPoint";
		Vector3d startPos = getStartPos(geometry);
		//double ortho_s = geometry.getHdg();
		//pointList.add(new ODPoint(startPointID, startPointS, startPos, ortho_s)); // already added in loop below
		if(vizArc && vizStartBox)
			visualizer.drawBox(startPointID, startPos.toVector3f(), visualizer.redWireMaterial, 0.3f);
        
		// extract intermediate points
		int pointCounter = 1;
	    for (double ds = 0.0; ds < length; ds += interpolationStep)
	    {
	    	String intermediatePointID = arcID + "_intermediatePoint_" + pointCounter;
	    	ODPoint intermediatePoint = getPointOnArc(ds, geometry, intermediatePointID);
	        pointList.add(intermediatePoint);
	        if(vizArc && vizIntermediateBox)
	        {
	        	visualizer.drawBox(intermediatePointID, intermediatePoint.getPosition().toVector3f(), 
	        			visualizer.greenMaterial, 0.03f);
	        	
		        if(vizOrtho)
		        	visualizer.drawOrthogonal(intermediatePointID + "_ortho", intermediatePoint, 
		        			visualizer.greenMaterial, 3, 0.03f, vizOrthoArrows);
	        }
	        pointCounter++;
	    }
	    
	    // extract target point
	    String targetPointID = arcID + "_targetPoint";	
	    ODPoint targetPoint = getPointOnArc(length, geometry, targetPointID);
        pointList.add(targetPoint);
        if(vizArc && vizTargetBox)
        {
        	visualizer.drawBox(targetPointID, targetPoint.getPosition().toVector3f(), visualizer.greenWireMaterial, 0.2f);
	        
	        if(vizOrtho)
	        	visualizer.drawOrthogonal(targetPointID + "_ortho", targetPoint, 
	        			visualizer.greenMaterial, 3, 0.03f, vizOrthoArrows);
        }
        
        if(vizArc)
        	visualizer.drawConnector(arcID, pointList, visualizer.greenMaterial, vizArrows);
        
        return pointList;
	}
	
	
	private ODPoint getPointOnArc(double ds, Geometry geometry, String pointID)
	{
		double hdg = geometry.getHdg();
		double curvature = geometry.getArc().getCurvature();
		double startPointS = geometry.getS();
		Vector3d startPos = getStartPos(geometry);
		
	    CurvePoint curvePoint = ordArc(ds, -curvature);
    	Vector3d pos = curvePoint.getCoordinates();
        Vector3d resultingPos = translate(rotate(pos, hdg), startPos);
        double ortho = 2*Math.PI-curvePoint.getOrtho() + hdg;		
        return new ODPoint(pointID, startPointS + ds, resultingPos, ortho, geometry, null);
	}


	private CurvePoint ordArc(double ds, double curvature)
	{
		double radius = 1/curvature;
		double angle = ds/radius;
		
		double x = radius * Math.sin(angle);	
		double z = radius * (1-Math.cos(angle));		
		
		return new CurvePoint(new Vector3d(x, 0, z), angle);
	}


	private ArrayList<ODPoint> extractPointsFromSpiral(Geometry geometry, String ID)
	{
		//System.out.println("Generating spiral");
		
		ArrayList<ODPoint> pointList = new ArrayList<ODPoint>();

    	String spiralID = ID + "_spiral";

		double length = geometry.getLength();
		
		// extract start point
		String startPointID = spiralID + "_startPoint";
		Vector3d startPos = getStartPos(geometry);
		//double ortho_s = geometry.getHdg();
		//pointList.add(new ODPoint(startPointID, startPointS, startPos, ortho_s)); // already added in loop below
        if(vizSpiral && vizStartBox)
        	visualizer.drawBox(startPointID, startPos.toVector3f(), visualizer.redWireMaterial, 0.3f);

		
		int pointCounter = 1;
	    for (double ds = 0; ds < length; ds += interpolationStep)
	    {
	    	String pointID = spiralID + "_intermediatePoint_" + pointCounter;
	    	ODPoint intermediatePoint = getPointOnSpiral(ds, geometry, pointID);
	        pointList.add(intermediatePoint);
	        if(vizSpiral && vizIntermediateBox)
	        {
	        	visualizer.drawBox(pointID, intermediatePoint.getPosition().toVector3f(), visualizer.yellowMaterial, 0.03f);
	        	
		        if(vizOrtho)
		        	visualizer.drawOrthogonal(pointID + "_ortho", intermediatePoint, 
		        			visualizer.yellowMaterial, 3, 0.03f, vizOrthoArrows);
	        }
	        pointCounter++;
	    }
	    
	    // extract target point
	    String targetPointID = spiralID + "_targetPoint";
        ODPoint targetPoint = getPointOnSpiral(length, geometry, targetPointID);
        pointList.add(targetPoint);
        
        if(vizSpiral && vizTargetBox)
        {
        	visualizer.drawBox(targetPointID, targetPoint.getPosition().toVector3f(), visualizer.greenWireMaterial, 0.2f);        
	       
        	if(vizOrtho)
        		visualizer.drawOrthogonal(targetPointID + "_ortho", targetPoint, 
        				visualizer.yellowMaterial, 3, 0.03f, vizOrthoArrows);
        }
        
        if(vizSpiral)
        	visualizer.drawConnector(spiralID, pointList, visualizer.yellowMaterial, vizArrows);
        
		return pointList;
	}


	private ODPoint getPointOnSpiral(double ds, Geometry geometry, String pointID)
	{
		// geometry parameters
		double startPointS = geometry.getS();
		Vector3d startPos = getStartPos(geometry);
		double hdg = geometry.getHdg();	
		double length = geometry.getLength();
		double curvStart = geometry.getSpiral().getCurvStart();
		double curvEnd = geometry.getSpiral().getCurvEnd();

		// derived parameters
		double cDot = (curvStart-curvEnd)/length;
		double runFrom = -curvStart/cDot;

		// compute pivot point for translation and rotation
		CurvePoint pivotPoint = Spiral.odrSpiral(runFrom, cDot);
        Vector3d pivotPos = pivotPoint.getCoordinates();
		double angle = hdg + pivotPoint.getOrtho();

		// compute point on curve
    	CurvePoint curvePoint = Spiral.odrSpiral(runFrom + ds, cDot);
    	Vector3d oos = curvePoint.getCoordinates().subtract(pivotPos);
        Vector3d resultingPos = translate(rotate(oos, angle), startPos);
        double ortho = 2*Math.PI - curvePoint.getOrtho() + angle;
        double pointS = startPointS + ds;
		return new ODPoint(pointID, pointS, resultingPos, ortho, geometry, null);
	}
	
	
	private ArrayList<ODPoint> extractPointsFromPoly3(Geometry geometry, String ID)
	{
		//System.out.println("Generating poly3");
		
		String poly3ID = ID + "_poly3";
		
		ArrayList<ODPoint> pointList = new ArrayList<ODPoint>();

		double length = geometry.getLength();

		// extract start point
		String startPointID = poly3ID + "_startPoint";
		Vector3d startPos = getStartPos(geometry);
		//double ortho_s = geometry.getHdg();
		//pointList.add(new ODPoint(startPointID, startPointS, startPos, ortho_s)); // already added in loop below
		if(vizPoly3 && vizStartBox)
			visualizer.drawBox(startPointID, startPos.toVector3f(), visualizer.redWireMaterial, 0.3f);
        
		// extract intermediate points
		int pointCounter = 1;
	    for (double ds = 0.0; ds < length; ds += interpolationStep)
	    {
	    	String intermediatePointID = poly3ID + "_intermediatePoint_" + pointCounter;
	    	ODPoint intermediatePoint = getPointOnPoly3(ds, geometry, intermediatePointID);
	        pointList.add(intermediatePoint);
	        if(vizPoly3 && vizIntermediateBox)
	        {
	        	visualizer.drawBox(intermediatePointID, intermediatePoint.getPosition().toVector3f(), 
	        			visualizer.whiteMaterial, 0.03f);
	        	
		        if(vizOrtho)
		        	visualizer.drawOrthogonal(intermediatePointID + "_ortho", intermediatePoint, 
		        			visualizer.whiteMaterial, 3, 0.03f, vizOrthoArrows);
	        }
	        pointCounter++;
	    }
	    
	    // extract target point
	    String targetPointID = poly3ID + "_targetPoint";	
	    ODPoint targetPoint = getPointOnPoly3(length, geometry, targetPointID);
        pointList.add(targetPoint);
        if(vizPoly3 && vizTargetBox)
        {
        	visualizer.drawBox(targetPointID, targetPoint.getPosition().toVector3f(), visualizer.greenWireMaterial, 0.2f);
	        
	        if(vizOrtho)
	        	visualizer.drawOrthogonal(targetPointID + "_ortho", targetPoint, 
	        			visualizer.greenMaterial, 3, 0.03f, vizOrthoArrows);
        }
        
        if(vizPoly3)
        	visualizer.drawConnector(poly3ID, pointList, visualizer.whiteMaterial, vizArrows);
        
        return pointList;
	}
	
	
	private ODPoint getPointOnPoly3(double ds, Geometry geometry, String pointID)
	{
		double hdg = geometry.getHdg();
		double startPointS = geometry.getS();
		Vector3d startPos = getStartPos(geometry);
		
	    CurvePoint curvePoint = ordPoly3(ds, geometry);
    	Vector3d pos = curvePoint.getCoordinates();
        Vector3d resultingPos = translate(rotate(pos, hdg), startPos);
        double ortho = 2*Math.PI-curvePoint.getOrtho() + hdg;		
        return new ODPoint(pointID, startPointS + ds, resultingPos, ortho, geometry, null);
	}


	private CurvePoint ordPoly3(double ds, Geometry geometry)
	{
		double a = geometry.getPoly3().getA();
		double b = geometry.getPoly3().getB();
		double c = geometry.getPoly3().getC();
		double d = geometry.getPoly3().getD();
		
		double v = a + b*ds + c*ds*ds + d*ds*ds*ds;
		return new CurvePoint(new Vector3d(ds, 0, -v), 0);  //TODO check if correct & calculate ortho
	}

	
	private ArrayList<ODPoint> extractPointsFromParamPoly3(Geometry geometry, String ID)
	{
		//System.out.println("Generating paramPoly3");
		
		String paramPoly3ID = ID + "_paramPoly3";
		
		ArrayList<ODPoint> pointList = new ArrayList<ODPoint>();

		double length = geometry.getLength();

		// extract start point
		String startPointID = paramPoly3ID + "_startPoint";
		Vector3d startPos = getStartPos(geometry);
		//double ortho_s = geometry.getHdg();
		//pointList.add(new ODPoint(startPointID, startPointS, startPos, ortho_s)); // already added in loop below
		if(vizParamPoly3 && vizStartBox)
			visualizer.drawBox(startPointID, startPos.toVector3f(), visualizer.redWireMaterial, 0.3f);
        
		// extract intermediate points
		int pointCounter = 1;
	    for (double ds = 0.0; ds < length; ds += interpolationStep)
	    {
	    	String intermediatePointID = paramPoly3ID + "_intermediatePoint_" + pointCounter;
	    	ODPoint intermediatePoint = getPointOnParamPoly3(ds, geometry, intermediatePointID);
	        pointList.add(intermediatePoint);
	        if(vizParamPoly3 && vizIntermediateBox)
	        {
	        	visualizer.drawBox(intermediatePointID, intermediatePoint.getPosition().toVector3f(), 
	        			visualizer.blackMaterial, 0.03f);
	        	
		        if(vizOrtho)
		        	visualizer.drawOrthogonal(intermediatePointID + "_ortho", intermediatePoint, 
		        			visualizer.blackMaterial, 3, 0.03f, vizOrthoArrows);
	        }
	        pointCounter++;
	    }
	    
	    // extract target point
	    String targetPointID = paramPoly3ID + "_targetPoint";	
	    ODPoint targetPoint = getPointOnParamPoly3(length, geometry, targetPointID);
        pointList.add(targetPoint);
        if(vizParamPoly3 && vizTargetBox)
        {
        	visualizer.drawBox(targetPointID, targetPoint.getPosition().toVector3f(), visualizer.greenWireMaterial, 0.2f);
	        
	        if(vizOrtho)
	        	visualizer.drawOrthogonal(targetPointID + "_ortho", targetPoint, 
	        			visualizer.greenMaterial, 3, 0.03f, vizOrthoArrows);
        }
        
        if(vizParamPoly3)
        	visualizer.drawConnector(paramPoly3ID, pointList, visualizer.blackMaterial, vizArrows);
        
        return pointList;
	}
	
	
	private ODPoint getPointOnParamPoly3(double ds, Geometry geometry, String pointID)
	{
		double hdg = geometry.getHdg();
		double startPointS = geometry.getS();
		Vector3d startPos = getStartPos(geometry);
		
	    CurvePoint curvePoint = ordParamPoly3(ds, geometry);
    	Vector3d pos = curvePoint.getCoordinates();
        Vector3d resultingPos = translate(rotate(pos, hdg), startPos);
        double ortho = 2*Math.PI-curvePoint.getOrtho() + hdg;		
        return new ODPoint(pointID, startPointS + ds, resultingPos, ortho, geometry, null);
	}


	private CurvePoint ordParamPoly3(double ds, Geometry geometry)
	{
		// previous position
		Vector3d previousPos = getPos(ds-interpolationStep, geometry);
		
		// current position
		Vector3d currentPos = getPos(ds, geometry);
		
		// next position
		Vector3d nextPos = getPos(ds+interpolationStep, geometry);

		// compute angle that is orthogonal to segment between previousPos and currentPos
		Vector3d A = currentPos.subtract(previousPos);
		double orthoA = Math.atan(A.z/A.x);
		
		// compute angle that is orthogonal to segment between currentPos and nextPos
		Vector3d B = nextPos.subtract(currentPos);
		double orthoB = Math.atan(B.z/B.x);
		
		// compute mean between both values (= ortho value in point currentPos)
		double ortho = 0.5 * (orthoA + orthoB);
		
		return new CurvePoint(currentPos, ortho);
	}


	private Vector3d getPos(double ds, Geometry geometry)
	{
		double p = ds;
		
		PRange pRange= geometry.getParamPoly3().getPRange();
		if(pRange == null || pRange == PRange.NORMALIZED)
			p /= geometry.getLength();
		
		double au = geometry.getParamPoly3().getAU();
		double bu = geometry.getParamPoly3().getBU();
		double cu = geometry.getParamPoly3().getCU();
		double du = geometry.getParamPoly3().getDU();
		
		double av = geometry.getParamPoly3().getAV();
		double bv = geometry.getParamPoly3().getBV();
		double cv = geometry.getParamPoly3().getCV();
		double dv = geometry.getParamPoly3().getDV();

		double u = au + bu*p + cu*p*p + du*p*p*p;
		double v = av + bv*p + cv*p*p + dv*p*p*p;

		return new Vector3d(u, 0, -v);
	}
	
	
	private Vector3d translate(Vector3d vector, Vector3d translation) 
	{
		return new Vector3d(translation.x + vector.x, 0, translation.z - vector.z);
	}


	private Vector3d rotate(Vector3d vector, double angle)
	{
		double x = (vector.x * Math.cos(angle)) + (vector.z * Math.sin(angle));
		double z = (vector.x * Math.sin(angle)) - (vector.z * Math.cos(angle));
		return new Vector3d(x, 0, z);
	}


	public ArrayList<ODLaneSection> getLaneSectionList()
	{
		return ODLaneSectionList;
	}


}
