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

package eu.opends.effects;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterBoxShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
//import com.jme3.effect.shapes.*;

public class WindshieldRainParticleEmitter extends ParticleEmitter {

	private AssetManager assetManager;
	private float percentage;
	private ColorRGBA color;

	public WindshieldRainParticleEmitter(AssetManager assetManager, float percentage, ColorRGBA color) {
			super("Emitter", ParticleMesh.Type.Triangle,
					10000 /* (int) (50 * percentage) */);
			this.assetManager = assetManager;
			this.percentage = percentage;
			this.color = color;
			setupMaterial();
		}

	private void setupMaterial() {
		Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
		mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Weather/windshield_drop.png"));
		this.setMaterial(mat_red);
		this.setParticlesPerSec(40 * percentage);
		this.setImagesX(2);
		this.setImagesY(2); // 2x2 texture animation
		this.setStartColor(color);
		this.setEndColor(color);
		this.setStartSize(0.005f);
		this.setEndSize(0.005f);
		this.setGravity(0, 0.01f, 0);
		this.setLowLife(1f);
		this.setHighLife(1f);
		this.getParticleInfluencer().setInitialVelocity(new Vector3f(0, -1f, 0));
		this.getParticleInfluencer().setVelocityVariation(0f);
		this.setShape(new EmitterBoxShape(new Vector3f(-1.5f, 0f, 3f), new Vector3f(1.5f, 3f, -3f)));
	}

	public void setPercentage(float percentage) {
		// super.setNumParticles((int) (50 * percentage));
		this.setParticlesPerSec(100 * percentage);
	}
}
