package eu.opends.car;

import java.util.ArrayList;
import java.util.LinkedList;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

import eu.opends.main.Simulator;


public class TexturedMovie {
	
	private Simulator sim;
	private Spatial model;
	private LinkedList<Texture> textureArray;
	
	private int imageArrayCount = 0;
	
	//@SuppressWarnings({ "static-access", "deprecation" })
	public TexturedMovie(Simulator sim, Spatial model, LinkedList<Texture> textureArray){
		this.sim = sim;
		this.model = model;
		this.textureArray = textureArray;
	}		
	
	public TexturedMovie(){}
	
	public void increaseCounter(){
		imageArrayCount++;
	}
	
	public int getCounter(){
		return imageArrayCount;
	}
	
	public void setCounter(int value){
		imageArrayCount = value;
	}
	
	public void applyTexture(){
		//System.out.println("Enter loop");
		
		if (getCounter() < textureArray.size()){ 		
    				
    		Geometry geo = (Geometry)model;
    				
    		Material mat = new Material(sim.getAssetManager(), "Materials/Unshaded.j3md");
    		Texture tex = textureArray.get(imageArrayCount);

    		mat.setTexture("ColorMap", tex);
    		mat.setColor("Color",  new ColorRGBA(0.8f, 0.8f, 0.8f, 0.2f));
    		mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    		mat.getAdditionalRenderState().setDepthWrite(false);
    		
    		
    		geo.setQueueBucket(Bucket.Transparent);
    		
    		
    		model.setMaterial(mat);
    		
		
    		increaseCounter();
    		
    		//System.out.println("Array Counter = " + getCounter());
		}
		if (getCounter() == textureArray.size()){
			setCounter(0);
		}
	}
	
}
