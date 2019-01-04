package eu.opends.codriver;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

import eu.opends.codriver.util.DataStructures.Input_data_str;
import eu.opends.codriver.util.DataStructures.Output_data_str;



public interface CodriverLibrary extends Library
{
	// Client init
	int client_init(String server_ip, int server_port);

	// Client send
	int client_send(int server_run, int message_id, Input_data_str scenario_msg);

	// Client receive
	int client_receive(Pointer server_run, Pointer message_id, Output_data_str manoeuvre_msg, long start_time);

	// Close socket
	int client_close();

	// Client Get time
	long client_get_time_ms();
}    