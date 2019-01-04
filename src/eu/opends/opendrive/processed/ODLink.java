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
import java.util.List;

import eu.opends.main.Simulator;
import eu.opends.opendrive.data.ContactPoint;
import eu.opends.opendrive.data.OpenDRIVE.Junction;
import eu.opends.opendrive.data.OpenDRIVE.Junction.Connection;
import eu.opends.opendrive.data.OpenDRIVE.Junction.Connection.LaneLink;

public class ODLink
{
	private Simulator sim;
	private boolean isJunction = false;
	private String junctionID = "";
	private ArrayList<ODLane> laneList = new ArrayList<ODLane>();
	private ArrayList<ContactPoint> contactPointList = new ArrayList<ContactPoint>();
	
	
	public ODLink(Simulator sim, ODLane linkedLane, ContactPoint contactPoint)
	{
		this.sim = sim;

		laneList.add(linkedLane);
		contactPointList.add(contactPoint);
	}

	
	public ODLink(Simulator sim, String roadID, Integer laneID, ContactPoint contactPoint) 
	{
		this.sim = sim;
		
		ODLane lane = getLane(roadID, laneID, contactPoint);
		
		if(lane != null)
		{
			laneList.add(lane);
			contactPointList.add(contactPoint);
		}
	}

	
	public ODLink(Simulator sim, List<Junction> junctionList, String junctionID, String incomingRoadID, int fromLaneID)
	{
		this.sim = sim;
		this.isJunction = true;
		this.junctionID = junctionID;
		
		for(Junction junction : junctionList)
		{
			if(junction.getId().equals(junctionID))
			{
				for(Connection connection : junction.getConnection())
				{
					if(connection.getIncomingRoad().equals(incomingRoadID))
					{
						String connectingRoadID = connection.getConnectingRoad();
						ContactPoint contactPoint = connection.getContactPoint();
						
						for(LaneLink laneLink : connection.getLaneLink())
						{
							if(laneLink.getFrom().equals(fromLaneID))
							{
								// lane with roadID "connectingRoadID" and laneID "toLane"
								ODLane toLane = getLane(connectingRoadID, laneLink.getTo(), contactPoint);
								
								// add lane and contact point to result list
								if(toLane != null)
								{
									laneList.add(toLane);
									contactPointList.add(contactPoint);
									
									/*
									System.err.println("ADD: junctionID: " + junctionID + 
											", from: " + incomingRoadID + " (lane: " + fromLaneID + ") " +
											", to: " + connectingRoadID + " (lane: " + toLane.getID() + ")");
									*/
								}
							} 
						}
					}
				}
			}
		}
	}



	private ODLane getLane(String roadID, Integer laneID, ContactPoint contactPoint)
	{
		if(roadID != null && !roadID.isEmpty() && laneID != null)
		{
			ODRoad road = sim.getOpenDriveCenter().getRoadMap().get(roadID);
			
			if(road != null)
			{
				ArrayList<ODLaneSection> laneSectionList = road.getLaneSectionList();
				if(laneSectionList.size() > 0)
				{
					int item = 0;
					if(contactPoint == ContactPoint.END)
						item = laneSectionList.size()-1;

					ODLaneSection laneSection = laneSectionList.get(item);
					if(laneSection != null)
						return laneSection.getLane(laneID);
				}
			}
		}
		
		return null;
	}


	public ODLane getLane()
	{
		if(laneList.size() > 0)
			return laneList.get(0);
		
		return null;
	}
	
	
	public ContactPoint getContactPoint()
	{
		if(contactPointList.size() > 0)
			return contactPointList.get(0);
		
		return null;
	}

	
	public boolean isJunction()
	{
		return isJunction;
	}

	
	public String getJunctionID()
	{
		return junctionID;
	}


	public int getNrOfLanes() 
	{
		return laneList.size();
	}
}
