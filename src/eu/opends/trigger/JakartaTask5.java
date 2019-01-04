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

package eu.opends.trigger;

import com.jme3.scene.Spatial;

import eu.opends.basics.SimulationBasics;
import eu.opends.jakarta.Task5;
import eu.opends.tools.Util;

/**
 * This class represents the following set of actions: make object visible,
 * switch camera to the top view, pause the simulator, make a screenshot, switch
 * camera to a normal view again and unpause the simulator.
 * 
 * @author Konstantin Poddubnyy
 */
public class JakartaTask5 extends TriggerAction {
	private SimulationBasics sim;
	private String objectID;


	/**
	 * Creates a new HighlightAndMakeScreenshot trigger action instance,
	 * providing maximum number of repetitions and the object to manipulate.
	 * 
	 * @param sim
	 *            Simulator
	 * 
	 * @param delay
	 *            Amount of seconds (float) to wait before the TriggerAction
	 *            will be executed.
	 * 
	 * @param maxRepeat
	 *            Maximum number how often the trigger can be hit (0 =
	 *            infinite).
	 * 
	 * @param objectID
	 *            ID of the object to manipulate.
	 */
	public JakartaTask5(SimulationBasics sim, float delay, int maxRepeat, String objectID) {
		super(delay, maxRepeat);
		this.sim = sim;
		this.objectID = objectID;
	}

	/**
	 * Manipulates the given object by applying a translation, rotation, scaling
	 * or visibility change.
	 */
	@Override
	protected void execute() {
		if (!isExceeded()) {

			try {

				// get "visual" or "physical" spatial
				// search in all sub-nodes of root node (scene node, trigger
				// node, ...)
				
				Spatial object = Util.findNode(sim.getRootNode(), objectID);
				String objectName = object.getName();
				
				if (objectName.contains("Sphere") && !Task5.isWithin20cm()) {
					Task5.setWithin20cm(true);
				}
				if (objectName.contains("TrafficCone") && !Task5.isCollisionWithCones()) {
					Task5.setCollisionWithCones(true);
				}
				if (objectName.contains("Destination") && !Task5.isDestination()) {
					Task5.setDestination(true);
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not score the object '" + objectID + "' Maybe it does not exist.");
			}

			updateCounter();
		}
	}

	/**
	 * Returns a String of the object that will be manipulated.
	 */
	@Override
	public String toString() {
		return "Task 5 scored: " + objectID + "";
	}

}
