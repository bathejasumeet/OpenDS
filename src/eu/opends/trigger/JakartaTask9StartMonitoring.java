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
import eu.opends.jakarta.Task8;
import eu.opends.jakarta.Task9;
import eu.opends.main.Simulator;
import eu.opends.tools.Util;

/**
 * Jakarta Task 9 trigger
 * 
 */
public class JakartaTask9StartMonitoring extends TriggerAction {
	private SimulationBasics sim;
	private boolean leadmanOn;


	/**
	 * Creates a new JakartaTask9 trigger action instance,
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
	public JakartaTask9StartMonitoring(float delay, int maxRepeat, Simulator sim, boolean leadmanOn) {
		super(delay, maxRepeat);
		this.sim = sim;
		this.leadmanOn = leadmanOn;
	}

	/**
	 * Manipulates the given object by applying a translation, rotation, scaling
	 * or visibility change.
	 */
	@Override
	protected void execute() {
		if (!isExceeded()) {

			try {
				if (!Task9.isWatching){
					Task9.startWatching();
					System.out.println("Monitoring started");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			updateCounter();
		}
	}

	/**
	 * Returns a String of the object that will be manipulated.
	 */
	@Override
	public String toString() {
		return "Task 9 scored: "  + "";
	}

}