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


package eu.opends.opendrive;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;

import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;
import eu.opends.main.StartPropertiesReader;
import eu.opends.opendrive.processed.ODPoint;
import eu.opends.opendrive.processed.ODRoad;
import eu.opends.opendrive.util.OpenDRIVELoaderAnalogListener;
import eu.opends.tools.Util;


public class OpenDRIVELoader extends Simulator 
{	
	private OpenDriveCenter openDriveCenter;
	public OpenDriveCenter getOpenDriveCenter() 
	{
		return openDriveCenter;
	}

	
    public static void main(String[] args) 
    {
    	java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.SEVERE);

    	OpenDRIVELoader app = new OpenDRIVELoader();
    	
    	StartPropertiesReader startPropertiesReader = new StartPropertiesReader();
		app.setSettings(startPropertiesReader.getSettings());
		app.setShowSettings(startPropertiesReader.showSettingsScreen());
		
        app.start();
    }
       
    
    public void simpleInitApp() 
    {
    	String path = "Scenes/grassPlane/Scene.j3o";
    	
    	assetManager.registerLocator("assets", FileLocator.class);
        
        //the actual model would be attached to this node
        Spatial model = (Spatial) assetManager.loadModel(path);        
        rootNode.attachChild(model);
        
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.7f));
        rootNode.addLight(al);
        
        Spatial sky = SkyFactory.createSky(assetManager, SimulationDefaults.skyTexture, EnvMapType.CubeMap);
        rootNode.attachChild(sky);

        float initialFrustumSize = 100;
        float aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setParallelProjection(true);
        cam.setFrustum(0, 5000, -aspect * initialFrustumSize, aspect * initialFrustumSize, initialFrustumSize, -initialFrustumSize);
        
        cam.setLocation(new Vector3f(0,100,0));
        cam.setRotation((new Quaternion()).fromAngles(FastMath.HALF_PI, FastMath.PI, 0));
        //cam.lookAt(new Vector3f(0,0,0), new Vector3f(0,1,0));
        new OpenDRIVELoaderAnalogListener(this, aspect, initialFrustumSize);
        
        flyCam.setMoveSpeed(100);

        openDriveNode = new Node("openDriveNode");
		rootNode.attachChild(openDriveNode);
        
        // OpenDRIVE content
        openDriveCenter = new OpenDriveCenter((Simulator)this);
		//openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/Crossing8Course.xodr");
		//openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/CulDeSac.xodr");
		//openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/KA-Suedtangente-Vires.xodr");
		//openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/2017-04-04_Testfeld_A9_Nord.xodr");
		//openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/2017-04-04_Testfeld_A9_Sued.xodr");
		//openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/CrossingComplex8Course.xodr");
        //openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/Roundabout8Course.xodr");
		openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/sample1.1.xodr");
        //openDriveCenter.processOpenDrive("assets/DrivingTasks/Projects/grassPlane/openDRIVE.xodr");
        //openDriveCenter.processOpenDrive("openDRIVEData/curve.xodr");
		
		// output a list for every ODRoad (to a separate text file)
		//for(Map.Entry<String,ODRoad> set : openDriveCenter.getRoadMap().entrySet())
			//writePointList("openDRIVEData/" + set.getKey(), set.getValue(), true);
    }

    
    private String newLine = System.getProperty("line.separator");
	public void writePointList(String outputFolder, ODRoad odRoad, boolean forward)
	{
		Util.makeDirectory(outputFolder);

		File OpenDRIVEFile = new File(outputFolder + "/pointList.txt");

		
		if (OpenDRIVEFile.getAbsolutePath() == null) 
		{
			System.err.println("Parameter not accepted at method writePointList.");
			return;
		}
		
		File outFile = new File(OpenDRIVEFile.getAbsolutePath());
		
		try {
			
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
			
			ArrayList<ODPoint> list = odRoad.getRoadReferencePointlist();

			// write data
			if(forward)
			{
				for(ODPoint point: odRoad.getRoadReferencePointlist())
					out.write(point.getPosition().getX() + ";" + point.getPosition().getZ() + newLine);
			}
			else
			{
				for(int i=list.size()-1; i>=0; i--)
					out.write(list.get(i).getPosition().getX() + ";" + list.get(i).getPosition().getZ() + newLine);
			}
			
			// close output file
			if (out != null)
				out.close();
	
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

    @Override
    public void simpleUpdate(float tpf) 
    {
    }
}

