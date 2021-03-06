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

package eu.opends.traffic;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * 
 * @author Rafael Math
 */
public class AnimatedRoadSignData 
{
	private String name;
	private float mass;
	private String animationBlink;
	private float localScale;
	private Vector3f localTranslation;
	private Quaternion localRotation;
	private String modelPath;	
	
	public AnimatedRoadSignData(String name, float mass, String animationBlink, 
			float localScale, Vector3f localTranslation, Quaternion localRotation, String modelPath) 
	{
		this.name = name;
		this.mass = mass;
		this.animationBlink = animationBlink;
		this.localScale = localScale;
		this.localTranslation = localTranslation;
		this.localRotation = localRotation;
		this.modelPath = modelPath;
	}


	public String getName() {
		return name;
	}	

	public float getMass() {
		return mass;
	}


	public String getAnimationBlink() {
		return animationBlink;
	}


	public float getLocalScale() {
		return localScale;
	}
	
	
	public Vector3f getLocalTranslation() {
		return localTranslation;
	}

	
	public Quaternion getLocalRotation() {
		return localRotation;
	}


	public String getModelPath() {
		return modelPath;
	}
}
