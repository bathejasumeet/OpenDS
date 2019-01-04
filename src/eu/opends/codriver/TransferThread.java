package eu.opends.codriver;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import eu.opends.codriver.util.DataStructures.Input_data_str;
import eu.opends.codriver.util.DataStructures.Output_data_str;

public class TransferThread extends Thread
{
	private CodriverLibrary codriver;
	private int message_id;
	private Input_data_str scenario_msg;
	private CodriverConnector codriverConnector;
	

	public TransferThread(CodriverLibrary codriver, int message_id, Input_data_str scenario_msg, 
			CodriverConnector codriverConnector)
	{
		this.codriver = codriver;
		this.message_id = message_id;
		this.scenario_msg = scenario_msg;
		this.codriverConnector = codriverConnector;
	}

	
	@Override
	public void run()
	{
		//long start = System.currentTimeMillis();
		
		long start_time = codriver.client_get_time_ms();	

		// Send and receive operations
		if(codriver.client_send(1, message_id, scenario_msg) == -1)
		{		
			System.err.println("Warning! Couldn't send the message with ID " + message_id);
			return;
		}
		else 
		{
			final Pointer server_run_ptr = new Memory(Native.getNativeSize(Integer.TYPE));
			server_run_ptr.setInt(0, 1);
			
			final Pointer message_id_ptr = new Memory(Native.getNativeSize(Integer.TYPE));
			message_id_ptr.setInt(0, message_id);
			
			Output_data_str manoeuvre_msg = new Output_data_str();
			
			if (codriver.client_receive(server_run_ptr, message_id_ptr, manoeuvre_msg, start_time) == -1)
			{
				System.err.println("Output message: manoeuvre not calculated");
				return;
			}	
			else
			{
				if(manoeuvre_msg.Status == 0)
				{
					int messageID = message_id_ptr.getInt(0);
					codriverConnector.setManoeuvreMsg(messageID, manoeuvre_msg);
				}
				else
				{
					System.err.println("Output message: manoeuvre not calculated (Status != 0)");
					return;
				}
			}
		}
		
		//long end = System.currentTimeMillis();
		//System.err.println("diff: " + (end - start));
	}
	
	
	public int closeServer()
	{
		return codriver.client_send(0, message_id, scenario_msg);
	}
	
}
