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

import eu.opends.opendrive.data.OpenDRIVE.Road.PlanView.Geometry;
import eu.opends.opendrive.processed.ODLane.LaneSide;
import eu.opends.tools.Vector3d;

public class ODPoint
{
	private String ID;
    private double s;
    private Vector3d position;
    private double ortho;
    private Geometry geometry;
    private GeometryType geometryType;
    private ODLane parentLane;
    
    
    public enum GeometryType
    {
    	Line, Arc, Spiral, Poly3, ParamPoly3
    }
    
    
    public ODPoint(String ID, double s, Vector3d position, double ortho, Geometry geometry, ODLane parentLane)
    {
    	this.ID = ID;
    	this.s = s;
    	this.position = position;
    	this.ortho = ortho;
    	this.geometry = geometry;
    	this.parentLane = parentLane;
    	
		if(geometry.getLine() != null)
			geometryType = GeometryType.Line;
		else if(geometry.getArc() != null)
			geometryType = GeometryType.Arc;
		else if(geometry.getSpiral() != null)
			geometryType = GeometryType.Spiral;
		else if(geometry.getPoly3() != null)
			geometryType = GeometryType.Poly3;
		else if(geometry.getParamPoly3() != null)
			geometryType = GeometryType.ParamPoly3;	
	}

    
    public String getID()
    {
		return ID;
	}

    
	public double getS()
	{
		return s;
	}
	
	
	public Vector3d getPosition()
	{
		return position;
	}


	public double getOrtho()
	{
		return ortho;
	}
	
	
	public Geometry getGeometry()
	{
		return geometry;
	}

	
	public GeometryType getGeometryType()
	{
		return geometryType;
	}

	
	public ODLane getParentLane()
	{
		return parentLane;
	}
	
	
	public String toString()
	{
        return ID + " (" + position.x + ", " + position.y + ", " + position.z + ")";
    }

	
	public Double getLaneCurvature()
	{
		if(parentLane != null)
		{
			Double curv = getGeometryCurvature();
			
			if(parentLane.getLaneSide() == LaneSide.LEFT)
				curv = -curv;
			
			return curv;
		}
		else
		{
			System.err.println("Point " + ID + " has no parent lane --> cannot calculate lane curvature");
			return null;
		}
	}


	public Double getGeometryCurvature()
	{
		Double curv = null;
		
		if(geometryType == GeometryType.Line)
			curv = 0.0;
		else if(geometryType == GeometryType.Arc)
			curv = geometry.getArc().getCurvature();
		else if(geometryType == GeometryType.Spiral)
		{
			double geomS = geometry.getS();
			double length = geometry.getLength();
			double percentage = (s-geomS)/length;
			
			// interpolate linearly curv value for current position
			double curvStart = geometry.getSpiral().getCurvStart();
			double curvEnd = geometry.getSpiral().getCurvEnd();
			curv = curvStart + percentage*(curvEnd-curvStart);
		}
		else if(geometryType == GeometryType.Poly3)
			curv = 0.0; //TODO compute curvature
		else if(geometryType == GeometryType.ParamPoly3)
			curv = 0.0; //TODO compute curvature
		
		return curv;
	}


}
