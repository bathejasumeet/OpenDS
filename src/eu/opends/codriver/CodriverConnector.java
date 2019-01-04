package eu.opends.codriver;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.jna.Native;

import eu.opends.codriver.util.DataStructures.Input_data_str;
import eu.opends.codriver.util.DataStructures.Output_data_str;
import eu.opends.main.Simulator;


public class CodriverConnector
{
	//TODO CHANGED sumeet
	private boolean startConnection = false; //TODO
	
	private String DEFAULT_SERVER_IP = "127.0.0.1";
	private int SERVER_PORT = 30000;
	
	private CodriverLibrary codriver;
	private boolean enabled = false;
	
	private Output_data_str manoeuvre_msg = null;
	private long manoeuvre_msg_timestamp = 0;
	//private ArrayList<TransferThread> threadList = new ArrayList<TransferThread>();
	
	public CodriverConnector(Simulator sim)
	{
		if(startConnection)//TODO
		{
			System.setProperty("jna.library.path", "lib");
			
			codriver = (CodriverLibrary)Native.loadLibrary(("codriver_client_functions"), CodriverLibrary.class);
		
			// Init Client
			if(codriver.client_init(DEFAULT_SERVER_IP, SERVER_PORT) == -1)
				System.err.println("Could not initialize client");
			else
				enabled = true;
		}
	}
	
	
	private int message_id = 0;
	public void sendScenarioMsg(Input_data_str scenario_msg)
	{
		if(enabled)
		{
			message_id++;
			
			TransferThread transferThread = new TransferThread(codriver, message_id, scenario_msg, this);
			transferThread.start();
			
			/*
			threadList.add(transferThread);
			
			Iterator<TransferThread> it = threadList.iterator();
			while(it.hasNext())
			{
				TransferThread thread = it.next();
				
				if(!thread.isAlive())
					it.remove();
			}
			
			System.err.println("Nr. of active threads: " + threadList.size());
			*/
		}
	}
	
	
	private int previousMessageID = 0;
	public synchronized void setManoeuvreMsg(int messageID, Output_data_str manoeuvre_msg)
	{
		if(messageID > previousMessageID)
		{
			//System.err.println(messageID);
			
			this.manoeuvre_msg = manoeuvre_msg;
			this.manoeuvre_msg_timestamp = System.currentTimeMillis();
					
			previousMessageID = messageID;
		}
	}
	
	
	public Output_data_str getLatestManoeuvreMsg()
	{
		return manoeuvre_msg;
	}
	
	
	public long getLatestManoeuvreMsgTimestamp()
	{
		return manoeuvre_msg_timestamp;
	}
	
	
	public int getLatestManoeuvreMsgID()
	{
		return previousMessageID;
	}
	
	
	public void close()
	{
		if(enabled)
		{
			// Close codriver server
			message_id++;
			TransferThread transferThread = new TransferThread(codriver, message_id, new Input_data_str(), this);
			if(transferThread.closeServer() == -1)
				System.err.println("CodriverConnection: server could not be closed");
			
			// Close socket
			if(codriver.client_close() == -1)
				System.err.println("CodriverConnection: socket could not be closed");
		}
	}
}