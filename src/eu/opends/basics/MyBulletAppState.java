package eu.opends.basics;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;

public class MyBulletAppState extends BulletAppState
{
	float bulletElapsedSeconds = 0;
	
	
	public void physicsTick(PhysicsSpace space, float tpf)
	{
		bulletElapsedSeconds += tpf;
	}
	
	
	public float getElapsedSecondsSinceStart()
	{
		return bulletElapsedSeconds;
	}
}
