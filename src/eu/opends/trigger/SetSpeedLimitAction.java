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

import eu.opends.tools.SpeedControlCenter;

/**
 * 
 * @author Rafael Math
 */
public class SetSpeedLimitAction extends TriggerAction 
{
	private int speedLimit;
	private boolean currentSpeedLimit;
	
	
	public SetSpeedLimitAction(float delay, int maxRepeat, int speedLimit, boolean currentSpeedLimit)
	{
		super(delay, maxRepeat);
		this.speedLimit = speedLimit;
		this.currentSpeedLimit = currentSpeedLimit;
	}

	
	@Override
	protected void execute() 
	{
		if(!isExceeded())
		{
			if(currentSpeedLimit){
				//System.out.println("New speed limit set to =" + speedLimit);
				//System.out.println("Current max speed =" +SpeedControlCenter.getCurrentSpeedlimit() + "km/h");
				SpeedControlCenter.setCurrentSpeedlimit(speedLimit);
				//System.out.println("New max speed =" +SpeedControlCenter.getCurrentSpeedlimit()+ "km/h");
			}
			else
				SpeedControlCenter.setUpcomingSpeedlimit(speedLimit);

			updateCounter();
		}
	}

}
