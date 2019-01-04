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

import java.util.ArrayList;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import eu.opends.main.Simulator;

public class RadarSensor 
{
	private static final boolean enabled = false;
	private static final boolean debug = true;
	private static final float maxRange = 10.0f;
	private Node raySourceNode = new Node();
	private ArrayList<RadarRay> radarRayList = new ArrayList<RadarRay>();
	
	
	public RadarSensor(Simulator sim, Node carNode)
	{		
		if(enabled)
		{
			// place ray source node in front of car
			raySourceNode.setLocalTranslation(new Vector3f(0, 0.25f, -1.6f));
			carNode.attachChild(raySourceNode);
			
			// create rays
			for(float x=-0.8f; x<=0.8f; x+=0.1f)
			{
				for(float y=-0.12f; y<=0.12f; y+=0.04f)
				{
					//Vector3f direction = new Vector3f(-0.3f,0.2f,-0.8f);
					Vector3f direction = new Vector3f(x,y,-0.8f);
					RadarRay ray = new RadarRay(sim, raySourceNode, "ray_"+x+"_"+y, direction, maxRange, debug);
					radarRayList.add(ray);
				}
			}
		}
	}


	public void update()
	{
		if(enabled)
		{
			for(RadarRay rr : radarRayList)
				System.err.println("Obstacle in: " + rr.getDistanceToObstacle() + " m");
		}
	}
}
