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
import framework.IMessageReceivable;
import javafx.application.Platform;
import networking.StringMessage.Type;
import singleton.MainModel;

public class NetworkListener extends Thread {

	private boolean run;
	private ObjectInputStream input;
	private NetworkHandler handler;

	/**
	 * Constructor that creates a listener thread to wait for input from the server.
	 * 
	 * @param input
	 *           The input stream of the socket that the server is connected to.
	 */
	public NetworkListener(ObjectInputStream input, NetworkHandler handler) {
		this.input = input;
		this.handler = handler;
	}

	/**
	 * Starts the listener thread, which waits for input from the server and injects it onto the proper screen. The proper screen is tightly linked, so it might be a good idea to decouple the way I'm going about this.
	 */
	@Override
	public void run() {
		run = false;
		// Check if login was successful.
		try {
			ConnectionMessage cm = (ConnectionMessage) input.readObject();

			if (cm.getMessageType() == ConnectionMessage.Type.UNSUCCESSFUL_CONNECTION)
				handler.setLogin(false);
			else if (cm.getMessageType() == ConnectionMessage.Type.SUCCESSFUL_CONNECTION)
				handler.setLogin(true);

		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Initial login failure.");
			e.printStackTrace();
		}
		run = true;

		while (run) {
			try {
				Object msg = input.readObject();

				// Check to see if it is an alive ping from the server, as those are always ints (Integer with autoboxing).
				if (!(msg instanceof Integer)) {

					AbstractNetworkedScreenController controller = MainModel.getModel().getControllerData().getCurrentController();

					if (msg == null)
						Platform.runLater(() -> {
							controller.errorAlert("Communication Error", "There was an issue talking to the server.", "The server passed an invalid or incomplete message.");
						});

					if (controller instanceof IMessageReceivable) {
						IMessageReceivable smController = (IMessageReceivable) controller;
						Platform.runLater(() -> {
							smController.outputMessage(msg);
						});
					} else {
						StringMessage sm = (StringMessage) msg;

						if (sm.getMessageType() == Type.ERROR_MESSAGE)
							Platform.runLater(() -> {
								controller.errorAlert("Server Error", sm.getMsgHeader(), sm.getMsgContent());
							});
						else if (sm.getMessageType() == Type.WARNING_MESSAGE)
							Platform.runLater(() -> {
								controller.warningAlert("Server Warning", sm.getMsgHeader(), sm.getMsgContent());
							});
						else
							Platform.runLater(() -> {
								controller.informationAlert("Server Message", sm.getMsgHeader(), sm.getMsgContent());
							});
					}
				}
			} catch (IOException ioe) {
				System.out.println("The connection to the server has been terminated.");
				ioe.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				System.out.println("The object sent could not be parsed, because the class doesn't exist.");
				cnfe.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handles ending the listener by tripping the flag.
	 */
	public void end() {
		run = false;
	}

	/**
	 * Let's the caller know if the NetworkListener has successfully authenticated with the server and is running.
	 * 
	 * @return true if listener is waiting for incoming messages; false if it still initializing or if the run flag was manually tripped by calling end().
	 */
	public boolean isRunning() {
		return run;
	}
}
