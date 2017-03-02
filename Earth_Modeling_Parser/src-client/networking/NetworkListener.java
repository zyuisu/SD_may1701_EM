/*
 * 
 * Copyright (C) 2017 Anish Kunduru
 * 
 * This file is part the Visual Earth Modeling System (VEMS).
 * 
 * VEMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * VEMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with VEMS. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Anish Kunduru
 * 
 *         This class listens for an incoming message and sends it to the appropriate location.
 */

package networking;

import java.io.IOException;
import java.io.ObjectInputStream;

import framework.AbstractNetworkedScreenController;
import networking.StringMessage.Type;
import singleton.MainModel;

public class NetworkListener extends Thread {

	private boolean run;
	private ObjectInputStream input;

	/**
	 * Constructor that creates a listener thread to wait for input from the server.
	 * 
	 * @param input
	 *           The input stream of the socket that the server is connected to.
	 */
	public NetworkListener(ObjectInputStream input) {
		this.input = input;
	}

	/**
	 * Starts the listener thread, which waits for input from the server and injects it onto the proper screen. The proper screen is tightly linked, so it might be a good idea to decouple the way I'm going about this.
	 */
	@Override
	public void run() {
		run = true;

		// Give NetworkHandler enough time to initalize stuff.
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("There was an issue putting the listener thread to sleep.");
			e.printStackTrace();
		}

		while (run)
			try {
				StringMessage msg = (StringMessage) input.readObject();
				AbstractNetworkedScreenController controller = MainModel.getModel().getControllerData().getCurrentController();

				if (msg == null)
					controller.errorAlert("Communication Error", "There was an issue talking to the server.", "The server passed an invalid or incomplete message.");
				else if (msg.getMessageType() == Type.ERROR_MESSAGE)
					controller.errorAlert("Server Error", msg.getMsgHeader(), msg.getMsgContent());
				else if (msg.getMessageType() == Type.WARNING_MESSAGE)
					controller.warningAlert("Server Warning", msg.getMsgHeader(), msg.getMsgContent());
				else
					controller.informationAlert("Server Message", msg.getMsgHeader(), msg.getMsgContent());
			} catch (IOException ioe) {
				System.out.println("The connection to the server has been terminated.");
				ioe.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				System.out.println("The object sent could not be parsed.");
				cnfe.printStackTrace();
			}
	}

	/**
	 * Handles ending the listener by tripping the flag.
	 */
	public void end() {
		run = false;
	}
}
