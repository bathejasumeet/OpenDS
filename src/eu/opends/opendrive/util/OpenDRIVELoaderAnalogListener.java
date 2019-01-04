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

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.renderer.Camera;

import eu.opends.main.Simulator;


public class OpenDRIVELoaderAnalogListener implements AnalogListener
{
	private Simulator sim;
	private float aspect;
	private float frustumSize;
	
	
	public OpenDRIVELoaderAnalogListener(Simulator sim, float aspect, float frustumSize) 
	{
		this.sim = sim;
		this.aspect = aspect;
		this.frustumSize = frustumSize;
		sim.getInputManager().addMapping("Left",   new KeyTrigger(KeyInput.KEY_LEFT));
		sim.getInputManager().addMapping("Right",  new KeyTrigger(KeyInput.KEY_RIGHT));
		sim.getInputManager().addMapping("In", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false), new KeyTrigger(KeyInput.KEY_PGDN));
		sim.getInputManager().addMapping("Out", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true), new KeyTrigger(KeyInput.KEY_PGUP));
		sim.getInputManager().addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
		sim.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
		sim.getInputManager().addListener(this, "Left", "Right", "In", "Out", "Up", "Down");
	}

	
	@Override
    public void onAnalog(String name, float value, float tpf) 
    {
		Camera cam = sim.getCamera();
		
        if (name.equals("Right"))
    		cam.setLocation(cam.getLocation().add(value*frustumSize, 0, 0));
        
        
        if (name.equals("Left"))
    		cam.setLocation(cam.getLocation().subtract(value*frustumSize, 0, 0));
        
        
        if (name.equals("Down"))
        	cam.setLocation(cam.getLocation().add(0, 0, value*frustumSize));
        
        
        if (name.equals("Up"))
        	cam.setLocation(cam.getLocation().subtract(0, 0, value*frustumSize));
        
        
        if (name.equals("In"))
        {
            frustumSize = Math.max(frustumSize-5, 1f);
            cam.setFrustum(0, 5000, -aspect*frustumSize, aspect*frustumSize, frustumSize, -frustumSize);
        }
        
        if (name.equals("Out"))
        {
            frustumSize += 5;
            cam.setFrustum(0, 5000, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
        }
    }

}
