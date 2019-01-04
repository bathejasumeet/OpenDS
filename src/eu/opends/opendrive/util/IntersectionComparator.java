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

import eu.opends.opendrive.processed.Intersection;


/**
 * This class compares instances from class Intersection by the distance value 
 * increasing or decreasing.
 * 
 * @author Rafael Math
 */
public class IntersectionComparator implements Comparator<Intersection> 
{
	private boolean increasing;
	
	
	/**
	 * Creates a new comparator, defining whether the sort function 
	 * will sort increasing or decreasing.
	 * 
	 * @param increasing
	 * 			If true, sort function will sort increasing.
	 */
	public IntersectionComparator(boolean increasing)
	{
		this.increasing = increasing;
	}
	
	
	/**
	 * Compares two Intersection instances
	 */
	@Override
	public int compare(Intersection arg0, Intersection arg1) 
	{
		if(increasing)
			return Double.compare(arg0.getDistance(), arg1.getDistance());
		else
			return -Double.compare(arg0.getDistance(), arg1.getDistance());
	}

}

