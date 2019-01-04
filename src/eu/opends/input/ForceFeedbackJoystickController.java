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


package eu.opends.input;

import java.util.ArrayList;

import at.wisch.joystick.*;
import at.wisch.joystick.event.*;
import at.wisch.joystick.exception.*;
import at.wisch.joystick.ffeffect.*;
import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.Simulator;


/**
 * 
 * @author Rafael Math
 */
public class ForceFeedbackJoystickController implements FeatureNotSupportedEventListener 
{
	private static final int EFFECT_INERTIA = 0;
	private Simulator sim;
	private boolean enabled = false;
	private FFJoystick primaryJoystick;
	private SpringEffect springEffect;
	private DamperEffect damperEffect;
	
	
	private SineEffect sineEffect;
	private SquareEffect squareEffect;
	private TriangleEffect triangleEffect;
	private SawtoothUpEffect sawtoothUpEffect;
	private SawtoothDownEffect sawtoothDownEffect;
	private RampEffect rampEffect;
	private InertiaEffect inertiaEffect;
	private FrictionEffect frictionEffect;
	private ConstantEffect constantEffect;
	
	private float sineForceFactor = 1.0f;
	private float squareForceFactor = 1.0f;
	private float triangleForceFactor = 1.0f;
	private float sawtoothUpForceFactor = 1.0f;
	private float sawtoothDownForceFactor = 1.0f;
	private float rampForceFactor = 1.0f;
	private float inertiaForceFactor = 10.0f;
	private float frictionForceFactor = 13.0f;
	private float constantForceFactor = 1.0f;
	
	
	private float springForceFactor = 1.0f;
	private float damperForceFactor = 1.0f;


	public ForceFeedbackJoystickController(Simulator sim)
	{
		enabled = Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableForceFeedback, false);
		springForceFactor = Simulator.getSettingsLoader().getSetting(Setting.Joystick_springForce, 1.0f);
		damperForceFactor = Simulator.getSettingsLoader().getSetting(Setting.Joystick_damperForce, 1.0f);
		sineForceFactor = Simulator.getSettingsLoader().getSetting(Setting.Joystick_sineForce, 1.0f);
		
		if(enabled)
		{
			this.sim = sim;
			
			ArrayList<FFJoystick> joysticks;
			
			// init and get-joystick methods have to be done within a try-catch-block
			// (these are fatal errors and we need to deal with them)
			try {
				JoystickManager.init();
				joysticks = JoystickManager.getAllFFJoysticks();
				
				if(!joysticks.isEmpty())
				{
					int steeringControllerID = Simulator.getSettingsLoader().getSetting(Setting.Joystick_steeringControllerID, 0);
					if(0 <= steeringControllerID && steeringControllerID < joysticks.size())
						primaryJoystick = joysticks.get(steeringControllerID);
					else
						primaryJoystick = joysticks.get(0);
				}
				
			} catch (FFJoystickException e) {
				e.printErrorMessage();
			}
			
			FeatureNotSupportedEventManager.addFeatureNotSupportedEventListener(this);
			
			
			if(primaryJoystick != null)
			{
				System.out.println("Supported effects of " + primaryJoystick.getName() + ": " 
								+ primaryJoystick.getSupportedEffects());
		
				//System.out.println(" creating effects ...");
				springEffect = new SpringEffect();
				springEffect.setEffectLength(10000); // 10 seconds
				
				damperEffect = new DamperEffect();
				damperEffect.setEffectLength(10000); // 10 second
				
				sineEffect = new SineEffect();
				sineEffect.setEffectLength(10000); // 10 second

				squareEffect = new SquareEffect();
				squareEffect.setEffectLength(10000); // 10 second
				
				//inertiaEffect = new InertiaEffect();
				//inertiaEffect.setEffectLength(EFFECT_INERTIA);
				
				//inertiaEffect.EFFECT_INERTIA
				
				System.out.println(" Attack kength" + squareEffect.getAttackLength());
				System.out.println(" Attack level" + squareEffect.getAttackLevel());
				System.out.println(" Effect delay" + squareEffect.getEffectDelay());
				System.out.println(" Effect length" + squareEffect.getEffectLength());
				System.out.println(" Magnitude" + squareEffect.getMagnitude());
				System.out.println(" Strength" + squareEffect.getStrength());
				System.out.println(" Get Direction" + squareEffect.getDirection());
				System.out.println(" Get Default direction" + squareEffect.getDefaultDirection());
				

				triangleEffect = new TriangleEffect();
				triangleEffect.setEffectLength(10000); // 10 second

				sawtoothUpEffect = new SawtoothUpEffect();
				sawtoothUpEffect.setEffectLength(10000); // 10 second

				sawtoothDownEffect = new SawtoothDownEffect();
				sawtoothDownEffect.setEffectLength(10000); // 10 second

				rampEffect = new RampEffect();
				rampEffect.setEffectLength(10000); // 10 second

				inertiaEffect = new InertiaEffect();
				inertiaEffect.setEffectLength(10000000); // 10 second		
				
				frictionEffect = new FrictionEffect();
				frictionEffect.setEffectLength(10000); // 10 second
				
				constantEffect = new ConstantEffect();
				constantEffect.setEffectLength(10000); // 10 second
				
				//System.out.println(" uploading effects ...");
				//upload the effects to the joystick
				
				boolean springEnabled = Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableSpringForce, false);
				if (springEnabled)
					primaryJoystick.newEffect(springEffect);
				
				boolean damperEnabled = Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableDamperForce, false);
				if (damperEnabled)
					primaryJoystick.newEffect(damperEffect);
				
				boolean sineEnabled = Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableSineForce, false);
				if (sineEnabled)
					primaryJoystick.newEffect(sineEffect);
				
				
				boolean squareEnabled = Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableSquareForce, false);
				if (squareEnabled)
					primaryJoystick.newEffect(squareEffect);
				
				
				//primaryJoystick.newEffect(triangleEffect);
				//primaryJoystick.newEffect(sawtoothUpEffect);
				//primaryJoystick.newEffect(sawtoothDownEffect);
				//primaryJoystick.newEffect(rampEffect);
				primaryJoystick.newEffect(inertiaEffect);
				//primaryJoystick.newEffect(frictionEffect);
				//primaryJoystick.newEffect(constantEffect);
				
				//System.out.println(" playing effects ...");
				//play the effect infinite times
				if (springEnabled)
					primaryJoystick.playEffect(springEffect, FFJoystick.INFINITE_TIMES);
				if (damperEnabled)
					primaryJoystick.playEffect(damperEffect, FFJoystick.INFINITE_TIMES);
				if (sineEnabled)
					primaryJoystick.playEffect(sineEffect, FFJoystick.INFINITE_TIMES);
				if (squareEnabled)
					primaryJoystick.playEffect(squareEffect, FFJoystick.INFINITE_TIMES);
				
				primaryJoystick.playEffect(inertiaEffect, FFJoystick.POV_UP);
				
				//primaryJoystick.playEffect(frictionEffect, FFJoystick.INFINITE_TIMES);
				
				
				//primaryJoystick.playEffect(squareEffect, FFJoystick.INFINITE_TIMES);
				//primaryJoystick.playEffect(triangleEffect, FFJoystick.INFINITE_TIMES);
				//primaryJoystick.playEffect(sawtoothUpEffect, FFJoystick.INFINITE_TIMES);
				//primaryJoystick.playEffect(sawtoothDownEffect, FFJoystick.INFINITE_TIMES);
				//primaryJoystick.playEffect(rampEffect, FFJoystick.INFINITE_TIMES);
				//primaryJoystick.playEffect(inertiaEffect, FFJoystick.INFINITE_TIMES);
				//primaryJoystick.playEffect(constantEffect, FFJoystick.INFINITE_TIMES);
			}	
		}
	}
	
	
	public void update(float tpf)
	{
		if(enabled && primaryJoystick != null)
		{
			float speed = sim.getCar().getCurrentSpeedKmh();
			
			//update spring effect
			//float springForce = Math.min(Math.max(speed/200f, 0), 0.15f);
			if (Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableSpringForce, false)){
				float springForce = Math.max(speed/200f, 0);
				//System.out.println("Spring force: " + springForce);
				springEffect.setStrength((int) (Effect.MAX_LEVEL * springForce * springForceFactor)); // % of full strength
				primaryJoystick.updateEffect(springEffect);
			}
			//update damper effect
			if (Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableDamperForce, false)){
				float damperForce = 0.01f/speed;
				//System.out.println("Damper force: " + damperForce);
				damperEffect.setStrength((int) (Effect.MAX_LEVEL * damperForce * damperForceFactor)); // % of full strength
				primaryJoystick.updateEffect(damperEffect);
			}
			if (Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableSineForce, false)){
				float sineForce = speed / 100f;
				sineEffect.setStrength((int) (Effect.MAX_LEVEL * sineForce * sineForceFactor)); // % of full strength
				primaryJoystick.updateEffect(sineEffect);
			}
			float inertiaForce = speed;
			inertiaEffect.setStrength((int) (Effect.MAX_LEVEL * inertiaForceFactor * inertiaForce)); // % of full strength
			primaryJoystick.updateEffect(inertiaEffect);
			
			//float frictionForce = speed;
			//frictionEffect.setStrength((int) (Effect.MAX_LEVEL * frictionForceFactor)); // % of full strength
			//primaryJoystick.updateEffect(frictionEffect);
			
			//float constantForce = speed / 1000f;
			//constantEffect.setStrength((int) (Effect.MAX_LEVEL * constantForce * constantForceFactor)); // % of full strength
			//primaryJoystick.updateEffect(constantEffect);
			if (Simulator.getSettingsLoader().getSetting(Setting.Joystick_enableSquareForce, false)){
				float squareForce = speed / 100f;
				squareEffect.setStrength((int) (Effect.MAX_LEVEL * squareForce * squareForceFactor)); // % of full strength
				primaryJoystick.updateEffect(squareEffect);
			}
			/*
			float triangleForce = speed;
			triangleEffect.setStrength((int) (Effect.MAX_LEVEL * triangleForce * triangleForceFactor)); // % of full strength
			primaryJoystick.updateEffect(triangleEffect);

			float sawtoothUpForce = speed;
			sawtoothUpEffect.setStrength((int) (Effect.MAX_LEVEL * sawtoothUpForce * sawtoothUpForceFactor)); // % of full strength
			primaryJoystick.updateEffect(sawtoothUpEffect);

			float sawtoothDownForce = speed;
			sawtoothDownEffect.setStrength((int) (Effect.MAX_LEVEL * sawtoothDownForce * sawtoothDownForceFactor)); // % of full strength
			primaryJoystick.updateEffect(sawtoothDownEffect);

			float rampForce = speed;
			rampEffect.setStrength((int) (Effect.MAX_LEVEL * rampForce * rampForceFactor)); // % of full strength
			primaryJoystick.updateEffect(rampEffect);

			*/
		}
	}
	
	
	public void close() 
	{
		if(enabled)
		{
			if(primaryJoystick != null)
				primaryJoystick.stopAll();
			
			JoystickManager.close();
		}
	}

	
	/* output errors that are not fatal. if these happen e.g. during a game, there
	 is no need for output or stopping. but during development we should always
	 be aware that something went wrong! */ 
	@Override
	public void featureNotSupportedEventOccured(FeatureNotSupportedEvent event) 
	{
		//System.out.println(event);
	}

}

