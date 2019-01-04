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

package eu.opends.niftyGui;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import eu.opends.drivingTask.DrivingTask;
import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;

/**
 * 
 * @author Till Maurer
 */
public class JakartaGUIController implements ScreenController
{
	private Simulator sim;
	private Nifty nifty;
	private String currentPath = "./assets/DrivingTasks/Projects";
	private de.lessvoid.nifty.elements.Element errorPopup;
	
	private String driverName = null;
	private String carName = null;
	
	private String carPic = null;
	
	private String steeringSide = null;
	private String taskName = null;
	private String drivingTaskFolderName = null;
	private String drivingTaskFileName = null;
	private String daytime = null;
	private String rainStrength = null;
	private Boolean recVideo = null;
	private Boolean takeScreen = null;
	
	public JakartaGUIController(Simulator sim, Nifty nifty)
	{
		this.sim = sim;
		this.nifty = nifty;
		
		AssetManager assetManager = sim.getAssetManager();
		assetManager.registerLocator("assets", FileLocator.class);
	}
	
	
	// happens before GUI moves in
	@Override
    public void bind(Nifty nifty, Screen screen)
	{
		init();
	}
	
	// happens after GUI moved in
	@Override
    public void onStartScreen() 
	{
		initScreen();
    }

	@Override
    public void onEndScreen() {
        sim.simpleInitDrivingTask(this.drivingTaskFileName, this.driverName);
    }
    
	@SuppressWarnings("unchecked")
	public void init() 
	{
		@SuppressWarnings("rawtypes")
		DropDown carCarDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_car_car", DropDown.class);
		if (carCarDropDown != null) {
			carCarDropDown.addItem("BMW");
			carCarDropDown.addItem("Avanza");
			carCarDropDown.addItem("Innova");
			carCarDropDown.addItem("Yaris");
			
			carCarDropDown.selectItemByIndex(0);
			this.carName = "BMW";
			NiftyImage newImage = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/BMW_1_2.png", false);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_car_car").getRenderer(ImageRenderer.class).setImage(newImage);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_overview_car").getRenderer(ImageRenderer.class).setImage(newImage);
			nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_car").getRenderer(TextRenderer.class).setText("BMW");
			((Slider) nifty.getCurrentScreen().findNiftyControl("GHorizontalSlider_jgui_car_pic", Slider.class)).setValue(2f);
			this.carPic = "2";
		}
		
		
		@SuppressWarnings("rawtypes")
		DropDown carSteeringDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_car_steering", DropDown.class);
		if (carCarDropDown != null) {
			carSteeringDropDown.addItem("Left-Handed");
			carSteeringDropDown.addItem("Right-Handed");
			
			carSteeringDropDown.selectItemByIndex(0);
			this.steeringSide = new String("Left-Handed");
		}
		
		//nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_task").getRenderer(TextRenderer.class).setText(SimulationDefaults.drivingTaskFileName);
		
		@SuppressWarnings("rawtypes")
		DropDown envDaytimeDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_env_daytime", DropDown.class);
		if (envDaytimeDropDown != null) {
			envDaytimeDropDown.addItem("Day");
			envDaytimeDropDown.addItem("Night");
			
			envDaytimeDropDown.selectItemByIndex(0);
			
			daytime = new String("Day");
			NiftyImage newImage = null;
			newImage = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/weather_daytime_day.png", false);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_weather_daytime").getRenderer(ImageRenderer.class).setImage(newImage);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_env_daytime").getRenderer(ImageRenderer.class).setImage(newImage);
		}
		
		rainStrength = new String("0");
		NiftyImage newImage2 = null;
		newImage2 = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/weather_rain_0.png", false);
		nifty.getCurrentScreen().findElementByName("GImage_jgui_weather_rain").getRenderer(ImageRenderer.class).setImage(newImage2);
		nifty.getCurrentScreen().findElementByName("GImage_jgui_env_rain").getRenderer(ImageRenderer.class).setImage(newImage2);
				
		@SuppressWarnings("rawtypes")
		DropDown sceneTaskDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_scene_task", DropDown.class);
		if (sceneTaskDropDown != null) {

			sceneTaskDropDown.addItem("Task 1 - Free roam");
			sceneTaskDropDown.addItem("Task 2 - Crank");
			sceneTaskDropDown.addItem("Task 3 - Maneuver");
			sceneTaskDropDown.addItem("Task 4 - Ramp");
			/*sceneTaskDropDown.addItem("Task 5 - Zig-Zag");
			sceneTaskDropDown.addItem("Task 6 - Weather");
			sceneTaskDropDown.addItem("Task 7 - Sequence");
			sceneTaskDropDown.addItem("Task 8 - Cross");
			sceneTaskDropDown.addItem("Task 9 - Lead Man");*/
			
			sceneTaskDropDown.selectItemByIndex(0);
			this.taskName = "Task 1 - Free roam";
			nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_task").getRenderer(TextRenderer.class).setText("Task 1 - Free roam");
			
			this.taskName = "Task 1 - Free roam";
			SimulationDefaults.drivingTaskFileName = this.taskName;
		}
		
		nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_driver").getRenderer(TextRenderer.class).setText(SimulationDefaults.driverName);
		driverName = new String(SimulationDefaults.driverName);
		
		recVideo = false;
		CheckBox reportsRecvideoCheckBox = (CheckBox) nifty.getCurrentScreen().findNiftyControl("GCheckbox_jgui_reports_recvideo", CheckBox.class);
		reportsRecvideoCheckBox.setChecked(false);//.check();
		
		takeScreen = true;
		//CheckBox reportsTakescreenCheckBox = (CheckBox) nifty.getCurrentScreen().findNiftyControl("GCheckbox_jgui_reports_takescreen", CheckBox.class);
		//reportsTakescreenCheckBox.check();
		
    }
	
	public void initScreen() 
	{
		
		nifty.getCurrentScreen().findElementByName("tab1_jgui_overview").setVisible(true);		
		nifty.getCurrentScreen().findElementByName("tab2_jgui_car").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab3_jgui_scene").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab4_jgui_environment").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab5_jgui_reports").setVisible(false);
		
    }
	
	public void clickStartButton() throws TransformerException 
    {
		
		System.out.println(this.carName 
				+ " : "	+ this.steeringSide 
				+ " : " + this.taskName 
				+ " : " + this.daytime 
				+ " : " + this.rainStrength 
				+ " : "	+ this.driverName 
				+ " : "	+ this.recVideo 
				+ " : "	+ this.takeScreen
				);
		
		String taskNumber = null; 
		Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(this.taskName);
        while ( m.find() ) {
        	taskNumber = this.taskName.substring(m.start(), m.end());
        }
                
		drivingTaskFolderName = currentPath
				+ "/Task_" 
				+ taskNumber;
		
		drivingTaskFileName = drivingTaskFolderName
				+ "/Task_"
				+ taskNumber
				+ ".xml";
		
		writeCarModelToXml();
		writeSteeringSideToXml();
		writeDaytimeToXml();
		writeRainStrengtToXml();
		writeDriverNameToXml();
		writeRecVideoToXml();
		
		//(nifty.getCurrentScreen().findNiftyControl("GLabel_jgui_overview_task", Label.class)).getText();
    	File drivingTaskFile = new File(drivingTaskFileName);
    	        
    	if(drivingTaskFile.isFile() && DrivingTask.isValidDrivingTask(drivingTaskFile))
    	{
    		//System.out.println(drivingTaskFileName);
    		//sim.simpleInitDrivingTask(this.drivingTaskFileName, this.driverName);
    		sim.closeDrivingTaskSelectionGUI();
    	}
    	else
    	{
    		// show error message when invalid DT selected
    		errorPopup = nifty.createPopup("errorPopup");
    		nifty.showPopup(nifty.getCurrentScreen(), errorPopup.getId(), null);
    	}
    }
    
	public void writeDaytimeToXml() throws TransformerException
	{
    	Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(drivingTaskFolderName + "/scene.xml");

            Element doc = dom.getDocumentElement();
            //System.out.println(dom.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName("skyTexture");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				if (this.daytime.equals("Day")) {
    					eElement.setTextContent("Textures/Sky/BrightV5/clouds.properties");
    				}
    				else {
    					eElement.setTextContent("Textures/Sky/FullMoon/clouds.properties");
    				}
    				//System.out.println(eElement.getTextContent());
            	}
            }
            nList = doc.getElementsByTagName("directionalLight");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				NodeList inList = eElement.getElementsByTagName("color");
    				for (int itemp = 0; itemp < inList.getLength(); itemp++) {

    	        		Node iinNode = inList.item(itemp);
    	            
    	        		if (iinNode.getNodeType() == Node.ELEMENT_NODE) {
    	    				Element ieElement = (Element) iinNode;
    	    				NodeList iinList = ieElement.getElementsByTagName("entry");
    	    				for (int iitemp = 0; iitemp < iinList.getLength() - 1; iitemp++) {

    	    	        		Node iiinNode = iinList.item(iitemp);
    	    	            
    	    	        		if (iiinNode.getNodeType() == Node.ELEMENT_NODE) {
    	    	    				Element iieElement = (Element) iiinNode;
    	    	    				if (this.daytime.equals("Day")) {
    	    	    					iieElement.setTextContent("0.7");
    	    	    				}
    	    	    				else {
    	    	    					iieElement.setTextContent("0.05");
    	    	    				}
    	    	    				//System.out.println(iieElement.getTextContent());
    	    	        		}
    	    				}
    	        		}
    				}
            	}
            }
            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        Source source = new DOMSource(dom);
        Result result = new StreamResult(drivingTaskFolderName + "/scene.xml");
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
	}
	
	public void writeRainStrengtToXml() throws TransformerException
	{
    	Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(drivingTaskFolderName + "/scenario.xml");

            Element doc = dom.getDocumentElement();
            //System.out.println(dom.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName("rainingPercentage");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				eElement.setTextContent("" + (Integer.parseInt(rainStrength) * 25));
    				//System.out.println(eElement.getTextContent());
            	}
            }
            nList = doc.getElementsByTagName("fogPercentage");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				eElement.setTextContent("" + (Integer.parseInt(rainStrength) * 10));
    				//System.out.println(eElement.getTextContent());
            	}
            }
            nList = doc.getElementsByTagName("frictionMap");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				NodeList inList = eElement.getElementsByTagName("frictionItem");
    				for (int itemp = 0; itemp < inList.getLength(); itemp++) {

    	        		Node iinNode = inList.item(itemp);
    	            
    	        		if (iinNode.getNodeType() == Node.ELEMENT_NODE) {
    	    				Element ieElement = (Element) iinNode;
    	    				ieElement.setAttribute("value", "" + (1 - (Integer.parseInt(rainStrength) * 0.1)));
    	    				//System.out.println(ieElement.getAttribute("value"));
    	        		}
    				}
        		}
            }
            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        Source source = new DOMSource(dom);
        Result result = new StreamResult(drivingTaskFolderName + "/scenario.xml");
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
	}
	
	public void writeCarModelToXml() throws TransformerException
	{
    	Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(drivingTaskFolderName + "/scene.xml");

            Element doc = dom.getDocumentElement();
            //System.out.println(dom.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName("model");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				if (eElement.getAttribute("id").startsWith("shadow")) {
    					
    				      //if (eElement.getAttribute("id").startsWith("shadow") && eElement.getAttribute("key").contains("Toyota")  ) {
    						if ( eElement.getAttribute("id").startsWith("shadow") && (eElement.getAttribute("key").contains("Toyota")  )) {
    	                        if (this.carName.equals("Avanza")) {
    	                            eElement.setAttribute("key",
    	                                    eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("Toyota"))
    	                                    + "ToyotaAvanza"
    	                                    + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("Toyota")), eElement.getAttribute("key").length())
    	                                    );
    	                        }
    	                        else if (this.carName.equals("Innova")) {
    	                            eElement.setAttribute("key",
    	                                    eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("Toyota"))
    	                                    + "ToyotaInnova"
    	                                    + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("Toyota")), eElement.getAttribute("key").length())
    	                                    );
    	                        }
    	                        else if (this.carName.equals("Yaris")) {
    	                            eElement.setAttribute("key",
    	                                    eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("Toyota"))
    	                                    + "ToyotaYaris"
    	                                    + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("Toyota")), eElement.getAttribute("key").length())
    	                                    );
    	                        }
    	                        else if (this.carName.equals("BMW")) {
    			                    eElement.setAttribute("key", eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("Toyota"))
    			                            + "ToyotaBMW"
    			                            + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("Toyota")), eElement.getAttribute("key").length())
    			                            );
    			                }
    	                        
    	                        //System.out.println(eElement.getAttribute("key"));
    	                    }
    					
    					//System.out.println(eElement.getAttribute("key"));
    				}
    				//if (eElement.getAttribute("id").startsWith("driverCar")) {
    					/*if (this.carName.equals("Avanza")) {
    						eElement.setAttribute("key", eElement.getAttribute("key").replace("AudiS6_shadowFor*", "AudiS6_shadowForAvanza/"));
    					} 
    					else if (this.carName.equals("Innova")) {
    						eElement.setAttribute("key", eElement.getAttribute("key").replace("AudiS6_shadowFor*", "AudiS6_shadowForInnova/"));
    					} 
    					else if (this.carName.equals("Yaris")) {
    						eElement.setAttribute("key", eElement.getAttribute("key").replace("AudiS6_shadowFor*", "AudiS6_shadowForYaris/"));
    					} */
    					
    				      if (eElement.getAttribute("id").startsWith("driverCar") && eElement.getAttribute("key").contains("AudiS6_")) {
    	                        if (this.carName.equals("Avanza")) {
    	                            eElement.setAttribute("key",
    	                                    eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("AudiS6_"))
    	                                    + "AudiS6_shadowForAvanza"
    	                                    + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("AudiS6_")), eElement.getAttribute("key").length())
    	                                    );
    	                        }
    	                        else if (this.carName.equals("Innova")) {
    	                            eElement.setAttribute("key",
    	                                    eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("AudiS6_"))
    	                                    + "AudiS6_shadowForInnova"
    	                                    + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("AudiS6_")), eElement.getAttribute("key").length())
    	                                    );
    	                        }
    	                        else if (this.carName.equals("Yaris")) {
    	                            eElement.setAttribute("key",
    	                                    eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("AudiS6_"))
    	                                    + "AudiS6_shadowForYaris"
    	                                    + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("AudiS6_")), eElement.getAttribute("key").length())
    	                                    );
    	                        }
    	                        else if (this.carName.equals("BMW")) {
    	                            eElement.setAttribute("key",
    	                                    eElement.getAttribute("key").substring(0, eElement.getAttribute("key").indexOf("AudiS6_"))
    	                                    + "AudiS6_shadowForBMW"
    	                                    + eElement.getAttribute("key").substring(eElement.getAttribute("key").indexOf("/", eElement.getAttribute("key").indexOf("AudiS6_")), eElement.getAttribute("key").length())
    	                                    );
    	                        }
    	                        //System.out.println(eElement.getAttribute("key"));
    	                    }
    					
    				//}
            	}
            }
            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        Source source = new DOMSource(dom);
        Result result = new StreamResult(drivingTaskFolderName + "/scene.xml");
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
	}
	
	public void writeSteeringSideToXml() throws TransformerException
	{
    	Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(drivingTaskFolderName + "/scenario.xml");

            Element doc = dom.getDocumentElement();
            //System.out.println(dom.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName("left");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				if (this.steeringSide.equals("Left-Handed")) {
    					eElement.setTextContent("true");
    				}
    				else {
    					eElement.setTextContent("false");
    				}
    				//System.out.println(eElement.getTextContent());
            	}
            }
            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        Source source = new DOMSource(dom);
        Result result = new StreamResult(drivingTaskFolderName + "/scenario.xml");
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
	}
	
	public void writeDriverNameToXml() throws TransformerException
	{
		Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(drivingTaskFolderName + "/settings.xml");

            Element doc = dom.getDocumentElement();
            //System.out.println(dom.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName("driverName");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				eElement.setTextContent(this.driverName);
    				//System.out.println(eElement.getTextContent());
            	}
            }
            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        Source source = new DOMSource(dom);
        Result result = new StreamResult(drivingTaskFolderName + "/settings.xml");
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
	}
	
	public void writeRecVideoToXml() throws TransformerException
	{
    	Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(drivingTaskFolderName + "/settings.xml");

            Element doc = dom.getDocumentElement();
            //System.out.println(dom.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName("captureVideo");
            for (int temp = 0; temp < nList.getLength(); temp++) {

        		Node nNode = nList.item(temp);
            
        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				if (this.recVideo) {
    					//eElement.setTextContent(this.taskName + "_" + new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
    					//eElement.setTextContent(this.taskName + "_" + new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()) + ".avi");
                        eElement.setTextContent((
                                (this.taskName).replaceAll("\\s+","")
                                + "_"
                                + new java.sql.Timestamp(Calendar.getInstance().getTime().getTime())
                                ).replaceAll("\\W","_")
                                + ".avi"
                                );
    				}
    				else {
    					eElement.setTextContent("_.avi");
    				}
    				//System.out.println(eElement.getTextContent());
            	}
            }
            
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        Source source = new DOMSource(dom);
        Result result = new StreamResult(drivingTaskFolderName + "/settings.xml");
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
	}
	
	public void clickCloseButton()
	{
		nifty.closePopup(errorPopup.getId());
	}
	
	public void clickOverviewButton()
	{
		nifty.getCurrentScreen().findElementByName("tab1_jgui_overview").setVisible(true);		
		nifty.getCurrentScreen().findElementByName("tab2_jgui_car").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab3_jgui_scene").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab4_jgui_environment").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab5_jgui_reports").setVisible(false);
	}
	
	public void clickCarButton()
	{
		nifty.getCurrentScreen().findElementByName("tab1_jgui_overview").setVisible(false);		
		nifty.getCurrentScreen().findElementByName("tab2_jgui_car").setVisible(true);
		nifty.getCurrentScreen().findElementByName("tab3_jgui_scene").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab4_jgui_environment").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab5_jgui_reports").setVisible(false);
	}
	
	public void clickSceneButton()
	{
		nifty.getCurrentScreen().findElementByName("tab1_jgui_overview").setVisible(false);		
		nifty.getCurrentScreen().findElementByName("tab2_jgui_car").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab3_jgui_scene").setVisible(true);
		nifty.getCurrentScreen().findElementByName("tab4_jgui_environment").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab5_jgui_reports").setVisible(false);
	}
	
	public void clickEnvButton()
	{
		nifty.getCurrentScreen().findElementByName("tab1_jgui_overview").setVisible(false);		
		nifty.getCurrentScreen().findElementByName("tab2_jgui_car").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab3_jgui_scene").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab4_jgui_environment").setVisible(true);
		nifty.getCurrentScreen().findElementByName("tab5_jgui_reports").setVisible(false);
	}
	
	public void clickReportsButton()
	{
		nifty.getCurrentScreen().findElementByName("tab1_jgui_overview").setVisible(false);		
		nifty.getCurrentScreen().findElementByName("tab2_jgui_car").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab3_jgui_scene").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab4_jgui_environment").setVisible(false);
		nifty.getCurrentScreen().findElementByName("tab5_jgui_reports").setVisible(true);
	}
	
	@NiftyEventSubscriber(id="GDropDown_jgui_car_car")
	public void onCarCarDropDownSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event)
	{
		@SuppressWarnings("rawtypes")
		DropDown carCarDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_car_car", DropDown.class);
		if (carCarDropDown != null) {
			//System.out.println(carCarDropDown.getSelection().toString());
			NiftyImage newImage = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/" + carCarDropDown.getSelection().toString() + "_1_2.png", false);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_car_car").getRenderer(ImageRenderer.class).setImage(newImage);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_overview_car").getRenderer(ImageRenderer.class).setImage(newImage);
			nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_car").getRenderer(TextRenderer.class).setText(carCarDropDown.getSelection().toString());
			
			this.carName = carCarDropDown.getSelection().toString();
			
			((Slider) nifty.getCurrentScreen().findNiftyControl("GHorizontalSlider_jgui_car_pic", Slider.class)).setValue(2f);
			this.carPic = "2";			
		}
		
	}
	
	@NiftyEventSubscriber(id="GHorizontalSlider_jgui_car_pic")
	public void onCarPicSliderChangedEvent(final String id, final SliderChangedEvent event)
	{
		Slider carPicSlider = (Slider) nifty.getCurrentScreen().findNiftyControl("GHorizontalSlider_jgui_car_pic", Slider.class);
		if (carPicSlider != null) {
			//System.out.println(Math.round(carPicSlider.getValue()));
			NiftyImage newImage = null;
			newImage = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/" + this.carName + "_1_" + Integer.toString(Math.round(carPicSlider.getValue())) + ".png", false);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_car_car").getRenderer(ImageRenderer.class).setImage(newImage);
			
			this.carPic = Integer.toString(Math.round(carPicSlider.getValue()));
		}
		
	}
	
	@NiftyEventSubscriber(id="GDropDown_jgui_car_steering")
	public void onCarSteeringDropDownSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event)
	{
		@SuppressWarnings("rawtypes")
		DropDown carSteeringDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_car_steering", DropDown.class);
		if (carSteeringDropDown != null) {
			//System.out.println(carSteeringDropDown.getSelection().toString());
			nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_steering").getRenderer(TextRenderer.class).setText(carSteeringDropDown.getSelection().toString());
			
			this.steeringSide = carSteeringDropDown.getSelection().toString();
		}
		
	}
	
	@NiftyEventSubscriber(id="GDropDown_jgui_scene_task")
	public void onSceneTaskDropDownSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event)
	{
		@SuppressWarnings("rawtypes")
		DropDown SceneTaskDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_scene_task", DropDown.class);
		if (SceneTaskDropDown != null) {
			//System.out.println(SceneTaskDropDown.getSelection().toString());
			nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_task").getRenderer(TextRenderer.class).setText(SceneTaskDropDown.getSelection().toString());
			
			this.taskName = SceneTaskDropDown.getSelection().toString();
			SimulationDefaults.drivingTaskFileName = this.taskName;
		}
		
	}
	
	@NiftyEventSubscriber(id="GDropDown_jgui_env_daytime")
	public void onEnvDaytimeDropDownSelectionChanged(final String id, final DropDownSelectionChangedEvent<String> event)
	{
		@SuppressWarnings("rawtypes")
		DropDown envDaytimeDropDown = (DropDown) nifty.getCurrentScreen().findNiftyControl("GDropDown_jgui_env_daytime", DropDown.class);
		if (envDaytimeDropDown != null) {
			//System.out.println(envDaytimeDropDown.getSelection().toString());
			NiftyImage newImage = null;
			if (envDaytimeDropDown.getSelection().toString().equals("Day")) {
				newImage = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/weather_daytime_day.png", false);
			}
			else {
				newImage = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/weather_daytime_night.png", false);
			}
			nifty.getCurrentScreen().findElementByName("GImage_jgui_weather_daytime").getRenderer(ImageRenderer.class).setImage(newImage);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_env_daytime").getRenderer(ImageRenderer.class).setImage(newImage);
			
			this.daytime = envDaytimeDropDown.getSelection().toString();
		}
		
	}
	
	@NiftyEventSubscriber(id="GHorizontalSlider_jgui_env_rain")
	public void onEnvRainSliderChangedEvent(final String id, final SliderChangedEvent event)
	{
		Slider envRainSlider = (Slider) nifty.getCurrentScreen().findNiftyControl("GHorizontalSlider_jgui_env_rain", Slider.class);
		if (envRainSlider != null) {
			//System.out.println(Math.round(envRainSlider.getValue()));
			NiftyImage newImage = null;
			newImage = nifty.getRenderEngine().createImage(nifty.getCurrentScreen(), "Interface/j_gui_pics/weather_rain_" + Math.round(envRainSlider.getValue()) + ".png", false);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_weather_rain").getRenderer(ImageRenderer.class).setImage(newImage);
			nifty.getCurrentScreen().findElementByName("GImage_jgui_env_rain").getRenderer(ImageRenderer.class).setImage(newImage);
			
			this.rainStrength = Integer.toString(Math.round(envRainSlider.getValue()));
		}
		
	}
	
	@NiftyEventSubscriber(id="GTextfield_jgui_reports_driver")
	public void onReportsDriverTextfieldInputEvent(final String id, final NiftyInputEvent event) 
	{
		if (NiftyInputEvent.SubmitText.equals(event)) 
		{
			//System.out.println(getTextFromTextfield("GTextfield_jgui_reports_driver"));
			nifty.getCurrentScreen().findElementByName("GLabel_jgui_overview_driver").getRenderer(TextRenderer.class).setText(getTextFromTextfield("GTextfield_jgui_reports_driver"));
			
			this.driverName = getTextFromTextfield("GTextfield_jgui_reports_driver");
		}
	}
	
	@NiftyEventSubscriber(id="GCheckbox_jgui_reports_recvideo")
	public void onReportsRecvideoCheckBoxStateChangedEvent(final String id, final CheckBoxStateChangedEvent event) 
	{
		CheckBox reportsRecvideoCheckBox = (CheckBox) nifty.getCurrentScreen().findNiftyControl("GCheckbox_jgui_reports_recvideo", CheckBox.class);
		//reportsRecvideoCheckBox.setChecked(false);
		//reportsRecvideoCheckBox.setChecked(arg0);
		if (reportsRecvideoCheckBox != null) {
			//System.out.println(reportsRecvideoCheckBox.isChecked());
			
			this.recVideo = reportsRecvideoCheckBox.isChecked();
		}
		else {
			
			this.recVideo = false;
			
		}
	}
	
	@NiftyEventSubscriber(id="GCheckbox_jgui_reports_takescreen")
	public void onReportsTakescreenCheckBoxStateChangedEvent(final String id, final CheckBoxStateChangedEvent event) 
	{
		CheckBox reportsTakescreenCheckBox = (CheckBox) nifty.getCurrentScreen().findNiftyControl("GCheckbox_jgui_reports_takescreen", CheckBox.class);
		if (reportsTakescreenCheckBox != null) {
			//System.out.println(reportsTakescreenCheckBox.isChecked());
			
			this.takeScreen = reportsTakescreenCheckBox.isChecked();
		}
	}
	
    public void setTextToElement(String element, String text) 
    {
    	getElementByName(element).getRenderer(TextRenderer.class).setText(text);
    }
    
    
    public de.lessvoid.nifty.elements.Element getElementByName(String element)
    {
    	return nifty.getCurrentScreen().findElementByName(element);
    }
    
    
    public String getTextFromTextfield(String element)
    {
    	return nifty.getCurrentScreen().findNiftyControl(element, TextField.class).getRealText();
    }
    
    
    public void setTextToTextfield(String element, String text) 
    {
    	nifty.getCurrentScreen().findNiftyControl(element, TextField.class).setText(text);
    }
        
	
}
