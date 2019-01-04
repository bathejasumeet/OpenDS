package eu.opends.effects;

public class RainSettings {

	Boolean isEnabled;
	float scale2D_X;
	float scale2D_Y;
	String mode;
	String path;
	int fps;
	String frontRainSurface;
	String front;
	String sideLeft;
	String sideRight;
	String other;
	
	public RainSettings(Boolean isEnabled, float scaleX, float scaleY, String mode, int fps, String path, String frontRainSurface, String front, String sideLeft, String sideRight, String other){
		
		this.isEnabled = isEnabled;
		this.scale2D_X = scaleX;
		this.scale2D_Y = scaleY;
		this.mode = mode;
		this.path = path;
		this.fps = fps;
		this.frontRainSurface = frontRainSurface;
		this.front = front;
		this.sideLeft = sideLeft;
		this.sideRight = sideRight;
		this.other = other;
	}
	
	public Boolean getStatus(){
		return isEnabled;
	}
	
	public float getScaleX(){
		return scale2D_X;
	}
	
	public float getScaleY(){
		return scale2D_Y;
	}
	
	public String getMode(){
		return mode;
	}
	
	public String getPath(){
		return path;
	}
	
	public int getFPS(){
		return fps;
	}
	
	public String getRainLayerName(){
		return frontRainSurface;
	}
	
	public String getFrontLayerName(){
		return front;
	}
	
	public String getLeftName(){
		return sideLeft;
	}
	
	public String getRightName(){
		return sideRight;
	}
	
	public String getOtherName(){
		return other;
	}
	
}
