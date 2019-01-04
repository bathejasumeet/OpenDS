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

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.scene.Spatial;

import eu.opends.basics.SimulationBasics;
import eu.opends.jakarta.Task4;
import eu.opends.tools.Util;

/**
 * Jakarta Task 4 trigger
 * 
 * @author Konstantin Poddubnyy
 */
public class JakartaTask4 extends TriggerAction {
	private SimulationBasics sim;
	private String objectID;


	/**
	 * Creates a new JakartaTask4 trigger action instance,
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
	public JakartaTask4(SimulationBasics sim, float delay, int maxRepeat, String objectID) {
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
				
				/*System.out.println("Child details " + Util.printTree(sim.getSceneNode()));
				for (Spatial sp: sim.getSceneNode().getChildren()) {
					CollisionResults cr = new CollisionResults(); 
					int i = object.getWorldBound().collideWith(sp, cr);
					System.out.println(cr.getClosestCollision());
				}*/
				if (objectName.contains("Entrance") && !Task4.isWatching) {
					Task4.startWatching();
				} else if (objectName.contains("Exit") && Task4.isWatching) {
					Task4.stopWatching();
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not start watching for roll back '" + objectID + "' Maybe it does not exist.");
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