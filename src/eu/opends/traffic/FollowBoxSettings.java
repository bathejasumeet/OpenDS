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
import java.util.List;

import eu.opends.infrastructure.Segment;
import eu.opends.infrastructure.Waypoint;

/**
 * 
 * @author Rafael Math
 */
public class FollowBoxSettings 
{
	private List<Waypoint> wayPoints;
	private float minDistance;
	private float maxDistance;
	private float curveTension;
	private boolean pathCyclic;
	private boolean pathVisible;
	private String startWayPointID;
	private float giveWayDistance;
	private float intersectionObservationDistance;
	private float minIntersectionClearance;
	private float maxSpeed;
	private ArrayList<Segment> preferredSegments;
	private ArrayList<String> preferredSegmentsStringList = new ArrayList<String>();
	
	
	
	public FollowBoxSettings(List<Waypoint> wayPoints, float minDistance, float maxDistance, float curveTension,
			boolean pathCyclic, boolean pathVisible, String startWayPointID) 
	{
		this.wayPoints = wayPoints;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.curveTension = curveTension;
		this.pathCyclic = pathCyclic;
		this.pathVisible = pathVisible;
		this.startWayPointID = startWayPointID;
	}

	public FollowBoxSettings(ArrayList<Segment> preferredSegments, float minDistance, float maxDistance, 
			float maxSpeed, String startWayPointID, float giveWayDistance, float intersectionObservationDistance,
			float minIntersectionClearance) 
	{
		this.preferredSegments = preferredSegments;
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.maxSpeed = maxSpeed;
		this.startWayPointID = startWayPointID;
		this.giveWayDistance = giveWayDistance;
		this.intersectionObservationDistance = intersectionObservationDistance;
		this.minIntersectionClearance = minIntersectionClearance;
		
		for(Segment segment : preferredSegments)
			preferredSegmentsStringList.add(segment.getName());
	}


	/**
	 * @return the wayPoints
	 */
	public List<Waypoint> getWayPoints() 
	{
		return wayPoints;
	}

	/**
	 * @return the preferred segments of this follow box
	 */
	public ArrayList<Segment> getPreferredSegments() 
	{
		return preferredSegments;
	}


	public void setStartWayPointID(String startWayPointID)
	{
		this.startWayPointID = startWayPointID;
	}
	

	/**
	 * @return the curveTension
	 */
	public float getCurveTension()
	{
		return curveTension;
	}


	/**
	 * @return the pathCyclic
	 */
	public boolean isPathCyclic() 
	{
		return pathCyclic;
	}


	/**
	 * @return the pathVisible
	 */
	public boolean isPathVisible() 
	{
		return pathVisible;
	}

	public ArrayList<String> getPreferredSegmentsStringList() 
	{
		return preferredSegmentsStringList;
	}
	
		


	public String getStartWayPointID() 
	{
		return startWayPointID;
	}
	

	public int getStartWayPointIndex(boolean oldFunctionality) 
	{
		if (oldFunctionality) {
			for(int i=0; i<wayPoints.size(); i++)
				if(wayPoints.get(i).getName().equals(startWayPointID))
					return i;
			
			return -1;
		}
		else {
			for(int i=0; i<preferredSegments.size(); i++)
				if(preferredSegments.get(i).getName().equals(startWayPointID))
					return i;
			
			return -1;
		}
	}


	public float getMinDistance() 
	{
		return minDistance;
	}
	

	public void setMinDistance(float minDistance)
	{
		this.minDistance = minDistance;
	}
	


	public float getMaxDistance()
	{
		return maxDistance;
	}

	public void setMaxDistance(float maxDistance)
	{
		this.maxDistance = maxDistance;
	}
	
	public float getMaxSpeed()
	{
		return maxSpeed;
	}
	
	
	public void setMaxSpeed(float maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}
	public float getGiveWayDistance() 
	{
		return giveWayDistance;
	}

	
	public void setGiveWayDistance(float giveWayDistance)
	{
		this.giveWayDistance = giveWayDistance;
	}
	

	public float getIntersectionObservationDistance()
	{
		return intersectionObservationDistance;
	}

	
	public void setIntersectionObservationDistance(float intersectionObservationDistance)
	{
		this.intersectionObservationDistance = intersectionObservationDistance;
	}
	

	public float getMinIntersectionClearance()
	{
		return minIntersectionClearance;
	}
	

	public void setMinIntersectionClearance(float minIntersectionClearance)
	{
		this.minIntersectionClearance = minIntersectionClearance;
	}

}
