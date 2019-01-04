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

import java.util.ArrayList;
import java.util.List;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.math.Spline.SplineType;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Curve;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

import eu.opends.main.Simulator;
import eu.opends.opendrive.processed.ODLane;
import eu.opends.opendrive.processed.ODPoint;
import eu.opends.tools.Vector3d;

public class ODVisualizer 
{
	private Simulator sim;
	private boolean drawMarker;
	public Material redMaterial, greenMaterial, blueMaterial, yellowMaterial, blackMaterial, whiteMaterial;
	public Material redWireMaterial, greenWireMaterial, blueWireMaterial, yellowWireMaterial, blackWireMaterial, whiteWireMaterial;
	public Material roadSolidLineLaneTextureMaterial, roadBrokenLineLaneTextureMaterial, roadNoLineLaneTextureMaterial, 
				 	roadSolidLineTextureMaterial, roadBrokenLineTextureMaterial, roadNoLineTextureMaterial, 
					roadParkingParallelTextureMaterial, curbTextureMaterial, sidewalkTextureMaterial, 
					shoulderTextureMaterial, restrictedTextureMaterial;
	
	public ODVisualizer(Simulator sim, boolean drawCompass, boolean drawMarker)
	{
		this.sim = sim;
		this.drawMarker = drawMarker;
		
		defineMaterials();
		
		if(drawCompass)
			drawCompass();
	}
	
	
    private void defineMaterials()
    {
		redMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		redMaterial.getAdditionalRenderState().setWireframe(false);
		redMaterial.setColor("Color", ColorRGBA.Red);
		
		redWireMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		redWireMaterial.getAdditionalRenderState().setWireframe(true);
		redWireMaterial.setColor("Color", ColorRGBA.Red);
        
		greenMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		greenMaterial.getAdditionalRenderState().setWireframe(false);
		greenMaterial.setColor("Color", ColorRGBA.Green);
		
		greenWireMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		greenWireMaterial.getAdditionalRenderState().setWireframe(true);
		greenWireMaterial.setColor("Color", ColorRGBA.Green);
		
		blueMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        blueMaterial.getAdditionalRenderState().setWireframe(false);
        blueMaterial.setColor("Color", ColorRGBA.Blue);
        
		blueWireMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        blueWireMaterial.getAdditionalRenderState().setWireframe(true);
        blueWireMaterial.setColor("Color", ColorRGBA.Blue);
        
        yellowMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        yellowMaterial.getAdditionalRenderState().setWireframe(false);
        yellowMaterial.setColor("Color", ColorRGBA.Yellow);
        
        yellowWireMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        yellowWireMaterial.getAdditionalRenderState().setWireframe(true);
        yellowWireMaterial.setColor("Color", ColorRGBA.Yellow);
        
        blackMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        blackMaterial.getAdditionalRenderState().setWireframe(false);
        blackMaterial.setColor("Color", ColorRGBA.Black);
        
        blackWireMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        blackWireMaterial.getAdditionalRenderState().setWireframe(true);
        blackWireMaterial.setColor("Color", ColorRGBA.Black);
        
        whiteMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        whiteMaterial.getAdditionalRenderState().setWireframe(false);
        whiteMaterial.setColor("Color", ColorRGBA.White);
        
        whiteWireMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        whiteWireMaterial.getAdditionalRenderState().setWireframe(true);
        whiteWireMaterial.setColor("Color", ColorRGBA.White);
        
	    roadSolidLineLaneTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture roadSolidLineLaneTexture = sim.getAssetManager().loadTexture("Textures/Road/road_basic_solid.png");
	    roadSolidLineLaneTexture.setAnisotropicFilter(32);
	    roadSolidLineLaneTextureMaterial.setTexture("ColorMap", roadSolidLineLaneTexture);
	    
	    roadBrokenLineLaneTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture roadBrokenLineLaneTexture = sim.getAssetManager().loadTexture("Textures/Road/road_basic_broken.png");
	    roadBrokenLineLaneTexture.setAnisotropicFilter(32);
	    roadBrokenLineLaneTextureMaterial.setTexture("ColorMap", roadBrokenLineLaneTexture);
	    
	    roadNoLineLaneTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture roadNoLineLaneTexture = sim.getAssetManager().loadTexture("Textures/Road/road_basic_noline.png");
	    roadNoLineLaneTexture.setAnisotropicFilter(32);
	    roadNoLineLaneTextureMaterial.setTexture("ColorMap", roadNoLineLaneTexture);
	    
	    roadSolidLineTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture roadSolidLineTexture = sim.getAssetManager().loadTexture("Textures/Road/solidline.png");
	    roadSolidLineTexture.setAnisotropicFilter(32);
	    roadSolidLineTextureMaterial.setTexture("ColorMap", roadSolidLineTexture);
	    
	    roadBrokenLineTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture roadBrokenLineTexture = sim.getAssetManager().loadTexture("Textures/Road/brokenline.png");
	    roadBrokenLineTexture.setAnisotropicFilter(32);
	    roadBrokenLineTextureMaterial.setTexture("ColorMap", roadBrokenLineTexture);
	    
	    roadNoLineTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture roadNoLineTexture = sim.getAssetManager().loadTexture("Textures/Road/noline.png");
	    roadNoLineTexture.setAnisotropicFilter(32);
	    roadNoLineTextureMaterial.setTexture("ColorMap", roadNoLineTexture);
	    
	    roadParkingParallelTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture ParkingParallelTexture = sim.getAssetManager().loadTexture("Textures/Road/road_basic_parking_parallel.png");
	    ParkingParallelTexture.setAnisotropicFilter(32);
	    roadParkingParallelTextureMaterial.setTexture("ColorMap", ParkingParallelTexture);	    
	    
	    curbTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture curbTexture = sim.getAssetManager().loadTexture("Textures/Road/curb.png");
	    curbTexture.setAnisotropicFilter(32);
	    curbTextureMaterial.setTexture("ColorMap", curbTexture);
	    
	    sidewalkTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture sidewalkTexture = sim.getAssetManager().loadTexture("Textures/Road/sidewalk.jpg");
	    sidewalkTexture.setAnisotropicFilter(32);
	    sidewalkTextureMaterial.setTexture("ColorMap", sidewalkTexture);
	    
	    shoulderTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture shoulderTexture = sim.getAssetManager().loadTexture("Textures/Road/shoulder.jpg");
	    shoulderTexture.setAnisotropicFilter(32);
	    shoulderTextureMaterial.setTexture("ColorMap", shoulderTexture);
	    
	    restrictedTextureMaterial = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
	    Texture restrictedTexture = sim.getAssetManager().loadTexture("Textures/Road/restricted.jpg");
	    restrictedTexture.setAnisotropicFilter(32);
	    restrictedTextureMaterial.setTexture("ColorMap", restrictedTexture);
    }
    
    
    public Material getRandomMaterial(boolean wireFrame)
    {
		Material material = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		material.getAdditionalRenderState().setWireframe(wireFrame);
        material.setColor("Color", ColorRGBA.randomColor());
        return material;
    }
    
    
	private void drawCompass() 
	{
        drawBox("north", new Vector3f(0, 0, -10), blueMaterial, 0.5f);
        drawBox("south", new Vector3f(0, 0, 10), yellowMaterial, 0.5f);
        drawBox("west", new Vector3f(-10, 0, 0), greenMaterial, 0.5f);
        drawBox("east", new Vector3f(10, 0, 0), redMaterial, 0.5f);
	}

	
	public void drawBox(String ID, Vector3f position, Material material, float size) 
	{
        com.jme3.scene.Geometry box = new com.jme3.scene.Geometry(ID, new Box(size, size, size));
        box.setMaterial(material);
        box.setLocalTranslation(position);
        
        sim.getOpenDriveNode().attachChild(box);
	}
	
	
	public void drawSphere(String ID, Vector3f position, Material material, float size) 
	{
        com.jme3.scene.Geometry sphere = new com.jme3.scene.Geometry(ID, new Sphere(10, 10, 0.5f*size));
        sphere.setMaterial(material);
        sphere.setLocalTranslation(position);
        
        sim.getOpenDriveNode().attachChild(sphere);
	}
	
	
	public void drawConnector(String ID, ArrayList<?> pointList, Material material, boolean vizArrows)
	{
        Spline spline = new Spline();
        
		// add points
        for(Object point : pointList)
        {
        	if(point instanceof ODPoint)
        		spline.addControlPoint(((ODPoint)point).getPosition().toVector3f());
        	else if(point instanceof Vector3f)
        		spline.addControlPoint((Vector3f)point);
        }
        
		spline.setType(SplineType.Linear);
		
		Vector3f[] interpolationResult = interpolate(spline, 0.3f);
		Vector3f cylinderPos = interpolationResult[0];
		Vector3f cylinderOrigin = interpolationResult[1];
		Vector3f cylinderTarget = interpolationResult[2];
		Vector3f t = cylinderTarget.subtract(cylinderOrigin);
		float cylinderRot = FastMath.atan2(t.x,t.z) + FastMath.PI;
		
		Node curveGeometry = new Node(ID);
		com.jme3.scene.Geometry curve = new com.jme3.scene.Geometry(ID + "_curve", new Curve(spline, 0));
		curveGeometry.attachChild(curve);
		
		if(vizArrows)
		{
			Cylinder cyl = new Cylinder(5, 20, 0.5f, 0, 1.5f, true, false);
			com.jme3.scene.Geometry cylinder = new com.jme3.scene.Geometry(ID + "_cylinder", cyl);
			cylinder.scale(0.5f);
			cylinder.setLocalTranslation(cylinderPos);
			cylinder.setLocalRotation((new Quaternion()).fromAngles(0, cylinderRot, 0));
			curveGeometry.attachChild(cylinder);
		}
		
		curveGeometry.setMaterial(material);

		sim.getOpenDriveNode().attachChild(curveGeometry);
	}
	
	
    private Vector3f[] interpolate(Spline spline, float progress)
    {
    	float sum = 0;
    	float currentPos = progress * spline.getTotalLength();
    	
    	List<Float> segmentsLengthList = spline.getSegmentsLength();
    	for(int i=0; i<segmentsLengthList.size(); i++)
    	{
    		float segmentLength = segmentsLengthList.get(i);
    		if(sum + segmentLength >= currentPos)
    		{
    			float p = (currentPos - sum)/segmentLength;
    			Vector3f targetPos = spline.interpolate(p, i, null);
    			Vector3f currentControlPoint = spline.getControlPoints().get(i);
    			Vector3f nextControlPoint = spline.getControlPoints().get(i+1);
    			return new Vector3f[] {targetPos, currentControlPoint, nextControlPoint};
    		}
    		sum += segmentLength;
    	}
    	
    	// if progress > 1.0
    	return null;
    }
    

	public void drawOrthogonal(String ID, ODPoint point, Material material, float length, float endBoxSize, boolean vizArrows)
	{
		double s = point.getS();
		Vector3d centerPos = point.getPosition();
		double ortho = point.getOrtho();
		
		ArrayList<ODPoint> pointList = new ArrayList<ODPoint>();
		
		Vector3d leftPos = centerPos.add(new Vector3d(-length*Math.sin(ortho), 0, -length*Math.cos(ortho)));
		pointList.add(new ODPoint(ID + "_left", s, leftPos, ortho + Math.PI, point.getGeometry(), point.getParentLane()));
		drawBox(ID + "_left", leftPos.toVector3f(), material, endBoxSize);

		Vector3d rightPos = centerPos.add(new Vector3d(length*Math.sin(ortho), 0, length*Math.cos(ortho)));
		pointList.add(new ODPoint(ID + "_right", s, rightPos, ortho + Math.PI, point.getGeometry(), point.getParentLane()));
		drawBox(ID + "_right", rightPos.toVector3f(), material, endBoxSize);
		
		drawConnector(ID + "_connector", pointList, material, vizArrows);
	}


	public void createMarker(String ID, Vector3f initialPosition, Vector3f vehiclePosition, Material material, float size, boolean drawConnector)
	{
		if(drawMarker)
		{
			drawSphere(ID, initialPosition, material, size);
			
			ArrayList<Vector3f> pointList = new ArrayList<Vector3f>();
			pointList.add(vehiclePosition.setY(0));
			pointList.add(initialPosition);
			
			if(drawConnector)
				drawConnector(ID + "_connector", pointList, material, false);
		}
	}
	
	
	public void setMarkerPosition(String ID, Vector3f position, Vector3f vehiclePosition, Material material, boolean drawConnector)
	{
		if(drawMarker)
		{
			Spatial marker = sim.getOpenDriveNode().getChild(ID);
			marker.setLocalTranslation(position);
			marker.setCullHint(CullHint.Dynamic);
			
			Spatial connector = sim.getOpenDriveNode().getChild(ID + "_connector");
			if(connector != null)
				sim.getOpenDriveNode().detachChild(connector);
			
			ArrayList<Vector3f> pointList = new ArrayList<Vector3f>();
			pointList.add(vehiclePosition.setY(0));
			pointList.add(position);
			
			if(drawConnector)
				drawConnector(ID + "_connector", pointList, material, false);
		}
	}


	public void hideMarker(String ID)
	{
		if(drawMarker)
		{
			Spatial marker = sim.getOpenDriveNode().getChild(ID);
			marker.setCullHint(CullHint.Always);
			
			Spatial connector = sim.getOpenDriveNode().getChild(ID + "_connector");
			if(connector != null)
				sim.getOpenDriveNode().detachChild(connector);
		}
	}
	
}
