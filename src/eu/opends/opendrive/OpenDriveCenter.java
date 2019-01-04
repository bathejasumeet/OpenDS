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

package eu.opends.opendrive;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;

import eu.opends.codriver.ScenarioMessage;
import eu.opends.main.Simulator;
import eu.opends.opendrive.data.OpenDRIVE;
import eu.opends.opendrive.data.OpenDRIVE.Junction;
import eu.opends.opendrive.data.OpenDRIVE.Road;
import eu.opends.opendrive.processed.ODLane;
import eu.opends.opendrive.processed.ODPoint;
import eu.opends.opendrive.processed.ODRoad;
import eu.opends.opendrive.util.ODVisualizer;


public class OpenDriveCenter
{
	private static String schemaFile = "assets/DrivingTasks/Schema/OpenDRIVE_1.4H.xsd";
	private boolean drawCompass = false;
	private boolean drawMarker = true;
	private boolean textureProjectionEnabled = false;
	private float projectionOffset = 0.1f;
	
	private Simulator sim;
	private OpenDRIVE od;
	private Unmarshaller unmarshaller;
	private List<Junction> junctionList = new ArrayList<Junction>();
	private HashMap<String,ODRoad> roadMap = new HashMap<String, ODRoad>();
	private ODVisualizer visualizer;
	private ScenarioMessage scenarioMessage = null;
	private boolean enabled = false;

	
	public OpenDriveCenter(Simulator sim)
	{
		this.sim = sim;
		visualizer = new ODVisualizer(sim, drawCompass, drawMarker);
		
		try {
			
			od = new OpenDRIVE();
			JAXBContext context = JAXBContext.newInstance(od.getClass());
			unmarshaller = context.createUnmarshaller();
		
			Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File(schemaFile));
			unmarshaller.setSchema(schema);
		
		} catch (javax.xml.bind.UnmarshalException e){
			
			System.err.println(e.getLinkedException().toString());
			
		} catch (JAXBException e){
		
			e.printStackTrace();
			
		} catch (SAXException e){
		
			e.printStackTrace();
		}
	}


	public void update(float tpf)
	{
		if(enabled && scenarioMessage != null)
			scenarioMessage.update(tpf);
	}
	
	
	public ODVisualizer getVisualizer() 
	{
		return visualizer;
	}
	
	
	public List<Junction> getJunctionList()
	{
		return junctionList;
	}
	

	public HashMap<String,ODRoad> getRoadMap()
	{
		return roadMap;
	}
	
	
	public void processOpenDrive(String openDriveFile)
	{	
		try{
				
			OpenDRIVE openDrive = od.getClass().cast(unmarshaller.unmarshal(new File(openDriveFile)));

			// make junction list available
			junctionList = openDrive.getJunction();
			
			// process roads
			for(Road road : openDrive.getRoad())
				roadMap.put(road.getId(), new ODRoad(sim, road));
	
			for(ODRoad road : roadMap.values())
			{
				// init predecessors and successors
				road.initLinks();
				
				Material mat = visualizer.getRandomMaterial(false);
				
				for(ODPoint point : road.getRoadReferencePointlist())
				{
					//visualizer.drawBox(point.getID(), point.getPosition(), mat, 0.03f);
					//visualizer.drawOrthogonal(point.getID()+"_ortho", point, mat, 2, 0.03f, false);
//					System.out.println(point.getID() + "\t\t" + point.getS());
				}
	
//				visualizer.drawConnector(road.getID(), road.getRoadReferencePointlist(), mat, true);	
				
				/*
				visualizer.drawArea(road.getID()+"_area3", road.getPointlist(), mat, 0.4f, 1.1f);
				visualizer.drawArea(road.getID()+"_area2", road.getPointlist(), mat, 0.4f, 0.6f);	
				visualizer.drawArea(road.getID()+"_area1", road.getPointlist(), mat, 0.4f, 0.1f);	
				
				visualizer.drawArea(road.getID()+"_area-1", road.getPointlist(), mat, -0.4f, -0.1f);
				visualizer.drawArea(road.getID()+"_area-2", road.getPointlist(), mat, -0.4f, -0.6f);	
				visualizer.drawArea(road.getID()+"_area-3", road.getPointlist(), mat, -0.4f, -1.1f);
				*/	
					
//				System.out.println();
				
			}
			
			/*
	    	for(Geometry geometry : Util.getAllGeometries(rootNode))
	    		geometry.getMaterial().getAdditionalRenderState().setWireframe(true);
	    	*/
			
			
			if(!(sim instanceof OpenDRIVELoader))
				scenarioMessage = new ScenarioMessage(sim, visualizer, roadMap);
			
			enabled = true;
			
			
		} catch (JAXBException e){
			
			e.printStackTrace();
		}
	}


	public ODLane getMostProbableLane(Vector3f carPos)
	{
		// reset collision results list
		CollisionResults results = new CollisionResults();
				
		// downward direction
		Vector3f direction = new Vector3f(0,-1,0);
				
		// aim a ray from the car's position towards the target
		Ray ray = new Ray(new Vector3f(carPos.x, 10000, carPos.z), direction);

		// collect intersections between ray and scene elements in results list.
		sim.getOpenDriveNode().collideWith(ray, results);
				
		
		float minAbsHdgDiff = 181;
		ODLane mostProbableLane = null;

		for(int i=0; i<results.size(); i++)
		{
			String geometryName = results.getCollision(i).getGeometry().getName();
		
			// the closest collision point is what was truly hit
			//CollisionResult closest = results.getClosestCollision();
			//String geometryName = closest.getGeometry().getName();
			//System.out.println(geometryName);
			
			String[] array = geometryName.split("_");
			if(array.length == 3 && "ODarea".equals(array[0]) && roadMap.containsKey(array[1]))
			{
				String roadID = array[1];
				ODRoad road = roadMap.get(roadID);

				HashMap<Integer,ODLane> laneMap = road.getLaneInformationAtPosition(carPos);
				if(laneMap!=null)
				{
					int laneID = Integer.parseInt(array[2]);
					ODLane lane = laneMap.get(laneID);

					if(lane != null)
					{
						float absHdgDiff = FastMath.abs(lane.getHeadingDiff(sim.getCar().getHeadingDegree()));
						
						if(absHdgDiff < minAbsHdgDiff)
						{
							minAbsHdgDiff = absHdgDiff;
							mostProbableLane = lane;
						}
					}
					else
						System.err.println("Geometry '" + geometryName + "' does not exist");
				}				
			}
		}
		
		return mostProbableLane;
	}

	
	public double getHeightAt(double x, double z)
	{
		if(textureProjectionEnabled)
		{
			Vector3f origin = new Vector3f((float)x, 10000, (float)z);
			
			// reset collision results list
			CollisionResults results = new CollisionResults();
					
			// downward direction
			Vector3f direction = new Vector3f(0,-1,0);
					
			// aim a ray from the car's position towards the target
			Ray ray = new Ray(origin, direction);
	
			// collect intersections between ray and scene elements in results list.
			sim.getSceneNode().collideWith(ray, results);				
	
			for(int i=0; i<results.size(); i++)
			{
				String geometryName = results.getCollision(i).getGeometry().getName();
			
				if(!geometryName.startsWith("x-") && !geometryName.startsWith("y-") 
						&& !geometryName.startsWith("z-") && !geometryName.startsWith("center")
						&& !geometryName.startsWith("Sky") && !geometryName.startsWith("ODarea"))
				{
					//System.out.println(geometryName);
					return results.getCollision(i).getContactPoint().getY() + projectionOffset;
				}
			}
		}
		
		return 0;
	}

}
