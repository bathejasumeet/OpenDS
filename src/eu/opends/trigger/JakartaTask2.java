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
import eu.opends.jakarta.Task2;
import eu.opends.tools.Util;

/**
 * Jakarta Task 2 trigger
 * 
 * @author Konstantin Poddubnyy
 */
public class JakartaTask2 extends TriggerAction {
	private SimulationBasics sim;
	private String objectID;


	/**
	 * Creates a new JakartaTask2 trigger action instance,
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
	public JakartaTask2(SimulationBasics sim, float delay, int maxRepeat, String objectID) {
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
				
				if (objectName.contains("yellowStripe") && objectName.contains("2cm") && !Task2.isSideLessThan2cm()) {
					Task2.setSideLessThan2cm(true);
				}
				if (objectName.contains("yellowStripe") && objectName.contains("5cm") && !Task2.isSide2to5cm()) {
					Task2.setSide2to5cm(true);
				}
				if (objectName.contains("Destination") && objectName.contains("30cm") && !Task2.isFrontLessThan30cm()) {
					Task2.setFrontLessThan30cm(true);
				}
				if (objectName.contains("Destination") && objectName.contains("35cm") && !Task2.isFront30to35cm()) {
					Task2.setFront30to35cm(true);
				}
				if (objectName.contains("Destination") && objectName.contains("50cm") && !Task2.isFront35to50cm()) {
					Task2.setFront35to50cm(true);
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
		return "Task 2 scored: " + objectID + "";
	}

}
