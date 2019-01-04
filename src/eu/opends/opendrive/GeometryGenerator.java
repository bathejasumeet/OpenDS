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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext.Type;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;

import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;
import eu.opends.main.StartPropertiesReader;
import eu.opends.opendrive.data.OpenDRIVE.Road.PlanView.Geometry;
import eu.opends.opendrive.data.OpenDRIVE.Road.PlanView.Geometry.*;
import eu.opends.opendrive.geometryGenerator.ArcType;
import eu.opends.opendrive.geometryGenerator.LineType;
import eu.opends.opendrive.geometryGenerator.Road;
import eu.opends.opendrive.geometryGenerator.RoadDescription;
import eu.opends.opendrive.geometryGenerator.SpiralType;
import eu.opends.opendrive.processed.ODPoint;
import eu.opends.opendrive.processed.ODRoad;
import eu.opends.opendrive.util.OpenDRIVELoaderAnalogListener;
import eu.opends.tools.Util;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;


public class GeometryGenerator extends Simulator 
{
	private static Type contextType = Type.Display;
	private static String ScenePath = "Scenes/grassPlane/Scene.j3o";
	private static String schemaFile = "openDriveExtras/roadDescription.xsd";
	private static String descriptionFile = "openDriveExtras/roadDescription-1.xml";
	private static String geometriesFile = null;
	
	
	private String newLine = System.getProperty("line.separator");
	private RoadDescription rd;
	private Unmarshaller unmarshaller;
	private double initialX = 0;
	private double initialY = 0;
	private double initialHdg = 0;
	
	
	private OpenDriveCenter openDriveCenter;
	public OpenDriveCenter getOpenDriveCenter() 
	{
		return openDriveCenter;
	}

	
    public static void main(String[] args) 
    {
    	if(args.length > 0)
    	{
    		File roadDescriptionFile = new File(args[0]);
    		if (roadDescriptionFile.getAbsolutePath() != null && roadDescriptionFile.exists())
    			descriptionFile = args[0];
    		else
    			System.err.println("File '" + args[0] + "' does not exist. Using '" + descriptionFile + "' instead.");
    	}
    	
    	if(args.length > 1)
    		geometriesFile = args[1];
    	
    	if(args.length > 2 && args[2].equalsIgnoreCase("headless"))
    		contextType = Type.Headless;
    	
    	java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.SEVERE);

    	GeometryGenerator app = new GeometryGenerator();
    	
    	StartPropertiesReader startPropertiesReader = new StartPropertiesReader();
		app.setSettings(startPropertiesReader.getSettings());
		app.setShowSettings(startPropertiesReader.showSettingsScreen());

        app.start(contextType);
    }
       
    
    public void simpleInitApp() 
    {
    	assetManager.registerLocator("assets", FileLocator.class);
        
        //the actual model would be attached to this node
        Spatial model = (Spatial) assetManager.loadModel(ScenePath);        
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
        
                
        
        /*
        initRoad(1.0, 10.0, 1.2);
        geometryList.add(arc(50, 0.1));
        geometryList.add(spiral(15, 0.1, 0.0));
        geometryList.add(spiral(15, 0.0, -0.1));
        geometryList.add(arc(50, -0.1));
        */
        
        /*
        geometryList.add(line(120));
        geometryList.add(spiral(20, 0.0, 0.02));
        geometryList.add(arc(30, 0.02));
        geometryList.add(spiral(20, 0.02, 0.00));
        geometryList.add(line(30));
        geometryList.add(spiral(100, 0.0, -0.01));
        geometryList.add(arc(80, -0.01));
        geometryList.add(spiral(100, -0.01, 0.0));
        geometryList.add(line(40));
        geometryList.add(spiral(40, 0.0, -0.005));
        geometryList.add(arc(100, -0.005));
        geometryList.add(spiral(40, -0.005, 0.0));
        geometryList.add(line(100));
        geometryList.add(spiral(10, 0.0, 0.01));
        geometryList.add(arc(20, 0.01));
        geometryList.add(spiral(10, 0.01, 0.0));
        geometryList.add(line(100));
         */
        

		RoadDescription roadDescription = getRoadDescription(schemaFile, descriptionFile);
		if(roadDescription != null)
		{			
			Road road = roadDescription.getRoad();
			
			// init start settings (start position and direction of construction)
			initStartSettings(road);
			
			// read geometry list
			ArrayList<Geometry> geometryList = getGeometries(road);
		
	        // visualize road
			ODRoad odRoad = new ODRoad(this, geometryList);
			
			// make openDRIVEData folder if not exists
			Util.makeDirectory("openDRIVEData");
			
			// write OpenDRIVE file
			String creationDate = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
			writeXODR("openDRIVEData/" + creationDate, "openDrive.xodr", creationDate, geometryList, road);
			
			// write copy of OpenDRIVE file to local folder
			if(geometriesFile != null)
				writeXODR(".", geometriesFile, creationDate, geometryList, road);
			
			// print road center point list
			writePointList("openDRIVEData/" + creationDate, odRoad); 
		}
		else
			System.err.println("No road description found!");
		
		if(contextType == Type.Headless)
			stop();
    }
    

	public ArrayList<Geometry> getGeometries(Road road)
	{
		ArrayList<Geometry> geometryList = new ArrayList<Geometry>();
		List<Object> list = road.getGeometries().getLineOrSpiralOrArc();
		for(Object item : list)
		{
			if(item instanceof LineType)
			{
				double length = ((LineType) item).getLength();
				geometryList.add(line(length));
				//System.err.println("line: length: " + length);
			}
			else if(item instanceof SpiralType)
			{
				double length = ((SpiralType) item).getLength();
				double curvStart = ((SpiralType) item).getCurvStart();
				double curvEnd = ((SpiralType) item).getCurvEnd();
				geometryList.add(spiral(length, curvStart, curvEnd));
				//System.err.println("spiral: length: " + length + "; curvStart: " + curvStart + "; curvEnd: " + curvEnd);
			}
			else if(item instanceof ArcType)
			{
				double length = ((ArcType) item).getLength();
				double curvature = ((ArcType) item).getCurvature();
				geometryList.add(arc(length, curvature));
				//System.err.println("arc: length: " + length + "; curvature: " + curvature);
			}
		}
		return geometryList;
	}


	public RoadDescription getRoadDescription(String schemaFile, String descriptionFile)
	{
		RoadDescription roadDescription = null;
		
		try {
			
			rd = new RoadDescription();
			JAXBContext context = JAXBContext.newInstance(rd.getClass());
			unmarshaller = context.createUnmarshaller();
	
			Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File(schemaFile));
			unmarshaller.setSchema(schema);
			
			roadDescription = rd.getClass().cast(unmarshaller.unmarshal(new File(descriptionFile)));
		
		} catch (javax.xml.bind.UnmarshalException e){
			
			System.err.println(e.getLinkedException().toString());
			
		} catch (JAXBException e){
		
			e.printStackTrace();
			
		} catch (SAXException e){
		
			e.printStackTrace();
		}
		
		return roadDescription;
	}


	public void writeXODR(String outputFolder, String fileName, String creationDate, ArrayList<Geometry> geometryList, Road road)
	{
		String roadID = road.getId();
		int noOfLanes = road.getNoOfLanes();
		double roadWidth = road.getWidth();
		double speedLimit = road.getSpeedLimit();
		
		Util.makeDirectory(outputFolder);

		File OpenDRIVEFile = new File(outputFolder + "/" + fileName);

		
		if (OpenDRIVEFile.getAbsolutePath() == null) 
		{
			System.err.println("Parameter not accepted at method initWriter.");
			return;
		}
		
		File outFile = new File(OpenDRIVEFile.getAbsolutePath());
		
		
		try {
        
			// Create your Configuration instance, and specify if up to what FreeMarker
			// version (here 2.3.27) do you want to apply the fixes that are not 100%
			// backward-compatible. See the Configuration JavaDoc for details.
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
	
			// Specify the source where the template files come from. Here I set a
			// plain directory for it, but non-file-system sources are possible too:
			cfg.setDirectoryForTemplateLoading(new File("./assets/OpenDRIVE/templates"));
	
			// Set the preferred charset template files are stored in. UTF-8 is
			// a good choice in most applications:
			cfg.setDefaultEncoding("UTF-8");
	
			// Sets how errors will appear.
			// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	
			// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
			cfg.setLogTemplateExceptions(false);
	
			// Wrap unchecked exceptions thrown during template processing into TemplateException-s.
			cfg.setWrapUncheckedExceptions(true);
			
			// Create the root hash. We use a Map here, but it could be a JavaBean too.
			Map<String, String> root = new HashMap<>();
	
			// Put data into the root
			root.put("creationDateTime", creationDate);
			root.put("roadName", roadID);
			root.put("roadLength", "" + getRoadLength(geometryList));
			root.put("geometries", getGeometryString(geometryList, roadID));
			root.put("laneWidth", "" + (roadWidth/noOfLanes));
			root.put("speedLimit", "" + speedLimit);
			
			// load template
			Template temp;
			
			if(noOfLanes == 1)
				temp = cfg.getTemplate("oneLaneRoad.ftlh");
			else if(noOfLanes == 4)
				temp = cfg.getTemplate("fourLaneRoad.ftlh");
			else
				temp = cfg.getTemplate("twoLaneRoad.ftlh");

			// write data
			//Writer out = new OutputStreamWriter(System.out);
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
			temp.process(root, out);
			
			// close output file
			if (out != null)
				out.close();

		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	
	public void writePointList(String outputFolder, ODRoad odRoad)
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
			
			// write data
			for(ODPoint point: odRoad.getRoadReferencePointlist())
				out.write(point.getPosition().getX() + ";" + point.getPosition().getZ() + newLine);
			
			// close output file
			if (out != null)
				out.close();
	
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}
	
    
	private void initStartSettings(Road road)
    {
		if(road.getStart() != null)
		{
			if(road.getStart().getX() != null)
				initialX = road.getStart().getX();
	
			if(road.getStart().getY() != null)
				initialY = road.getStart().getY();
			
			if(road.getStart().getHdg() != null)
				initialHdg = road.getStart().getHdg();
		}
	}


	private Geometry line(double length)
    {
        Geometry geometry = new Geometry();
        
        geometry.setLength(length);
        geometry.setHdg(initialHdg);
        geometry.setS(0.0);
        geometry.setX(initialX);
        geometry.setY(initialY);
        Line l = new Line();
        geometry.setLine(l);
        
		return geometry;
	}


    private Geometry spiral(double length, double curvStart, double curvEnd)
    {
        Geometry geometry = new Geometry();
        
        geometry.setLength(length);
        geometry.setHdg(initialHdg);
        geometry.setS(0.0);
        geometry.setX(initialX);
        geometry.setY(initialY);
        Spiral s = new Spiral();
        s.setCurvStart(curvStart);
        s.setCurvEnd(curvEnd);
        geometry.setSpiral(s);

		return geometry;
	}
    
    
    private Geometry arc(double length, double curvature)
    {
        Geometry geometry = new Geometry();
        
        geometry.setLength(length);
        geometry.setHdg(initialHdg);
        geometry.setS(0.0);
        geometry.setX(initialX);
        geometry.setY(initialY);
        Arc a = new Arc();
        a.setCurvature(curvature);
        geometry.setArc(a);

		return geometry;
	}


	private String getGeometryString(ArrayList<Geometry> geometryList, String roadID)
    {    	
    	String output = "";
		for(int i=0; i<geometryList.size(); i++)
		{
			Geometry geometry = geometryList.get(i);
			
			double s = geometry.getS();
			double x = geometry.getX();
			double y = geometry.getY();
			double hdg = geometry.getHdg();
			
			while(hdg > 2*Math.PI)
				hdg -= 2*Math.PI;
			
			while(hdg < 0)
				hdg += 2*Math.PI;
			
			double length = geometry.getLength();
			output += "\t\t\t<geometry s=\"" + s + "\" x=\"" + x + "\" y=\"" + y + "\" hdg=\"" + hdg + "\" length=\"" + length + "\">" + newLine; 
			
			if(geometry.getLine() != null)
				output += "\t\t\t\t<line/>" + newLine;
			else if(geometry.getArc() != null)
				output += "\t\t\t\t<arc curvature=\"" + geometry.getArc().getCurvature() + "\"/>" + newLine;
			else if(geometry.getSpiral() != null)
				output += "\t\t\t\t<spiral curvStart=\"" + geometry.getSpiral().getCurvStart() + "\" curvEnd=\"" + geometry.getSpiral().getCurvEnd() + "\"/>" + newLine;
			else if(geometry.getPoly3() != null)
				output += "\t\t\t\t<poly3/>" + newLine;
			else if(geometry.getParamPoly3() != null)
				output += "\t\t\t\t<paramPoly3/>" + newLine;
					
				output += "\t\t\t</geometry>" + newLine;
		}
		
		return output;
	}
	
	
	private double getRoadLength(ArrayList<Geometry> geometryList)
    {
    	double totalLength = 0.0;
    	
    	if(geometryList.size()>0)
    	{
    		Geometry lastGeometry = geometryList.get(geometryList.size()-1);
    		totalLength = lastGeometry.getS() + lastGeometry.getLength();
    	}
		
		return totalLength;
	}


	@Override
    public void simpleUpdate(float tpf) 
    {
    }
}

