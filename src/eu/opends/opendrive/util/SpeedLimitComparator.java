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

package eu.opends.opendrive.util;

import java.util.Comparator;

import eu.opends.opendrive.processed.SpeedLimit;


/**
 * This class compares instances from class SpeedLimit by the distance value 
 * increasing or decreasing.
 * 
 * @author Rafael Math
 */
public class SpeedLimitComparator implements Comparator<SpeedLimit> 
{
	private boolean increasing;
	
	
	/**
	 * Creates a new comparator, defining whether the sort function 
	 * will sort increasing or decreasing.
	 * 
	 * @param increasing
	 * 			If true, sort function will sort increasing.
	 */
	public SpeedLimitComparator(boolean increasing)
	{
		this.increasing = increasing;
	}
	
	
	/**
	 * Compares two SpeedLimit instances
	 */
	@Override
	public int compare(SpeedLimit arg0, SpeedLimit arg1) 
	{
		if(increasing)
			return Double.compare(arg0.getDistance(), arg1.getDistance());
		else
			return -Double.compare(arg0.getDistance(), arg1.getDistance());
	}

}


