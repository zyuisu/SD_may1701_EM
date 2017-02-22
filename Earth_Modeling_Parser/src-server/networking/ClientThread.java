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
 *         Server-side that parses handles the streams between server and client.
 */

package networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import org.pmw.tinylog.Logger;

public class ClientThread extends Thread {

	private SSLSocket socket;
	private String username;
	private ClientServer server;

	private ObjectInputStream input;
	private ObjectOutputStream output;

	private boolean run;

	/**
	 * Constructor creates a new ClientThread to manage I/O streams with a client connection.
	 * 
	 * @param socket
	 *           The socket that the client is operating on.
	 * @param server
	 *           A reference to the parent ClientServer that is managing this client.
	 */
	public ClientThread(SSLSocket socket, ClientServer server) {
		this.socket = socket;
		this.server = server;
		
		try {
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException ioe) {
			Logger.error("Exception creating I/O streams: {}", ioe);
		}
	}

	/**
	 * Waits for and checks a ConnectionMessage for validity.
	 * 
	 * @return true if the user was validated; false if they were not.
	 */
	private boolean initializeUser() {
		try {
			Object obj = input.readObject();

			if (!(obj instanceof ConnectionMessage))
				return false;

			ConnectionMessage cm = (ConnectionMessage) obj;

			if (cm.getMessageType() != ConnectionMessage.Type.CONNECT)
				return false;

			if (!server.validateUser(cm.getUsername(), cm.getPassword(), socket)) {
				bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "Invalid username or password!"));
				return false;
			}

			username = cm.getUsername();
			return true;

		} catch (IOException ioe) {
			Logger.error("Exception parsing I/O sream: {}", ioe);
		} catch (ClassNotFoundException cnfe) {
			Logger.error("Couldn't parse with a defined class. Check src-shared.networking? {}", cnfe);
		} catch (IllegalAccessException iae) {
			Logger.error("Error message was defined with incorrect parameters: {}", iae);
		}

		return false;
	}

	/**
	 * Starts the thread. Runs until the client passes a ConnectionMessage of ConnectionMessage.Type.DISCONNECT. The run flag will also be tripped by bufferMessage() if there is a timeout event on the socket that causes sending a message over output to fail. An error will be logged if this method is given an invalid object from the input stream.
	 */
	public void run() {
		run = true;

		// Validate user.
		while (!initializeUser()) {
		}

		Logger.info("{} sucessfully connected to server", username);

		// Parse objects sent by the user.
		while (run)
			try {
				Object obj = input.readObject();

				if (obj instanceof AsciiFileMessage)
					server.parseAsciiFileMessage((AsciiFileMessage) obj, this);
				else if (obj instanceof ConnectionMessage) {
					ConnectionMessage cm = (ConnectionMessage) obj;

					if (cm.getMessageType() == ConnectionMessage.Type.DISCONNECT)
						run = false;
				} else
					bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "The input object passed is not a value message class defined in src-shared.networking."));
			} catch (IOException ioe) {
				Logger.error("Exception parsing I/O sream: {}", ioe);
				break; // //////////////////// So we don't lock up the thread. ////////////////////////////
			} catch (ClassNotFoundException cnfe) {
				Logger.error("Couldn't parse with a defined class. Check src-shared.networking? {}", cnfe);
				break; // //////////////////// So we don't lock up the thread. ////////////////////////////
			} catch (IllegalAccessException iae) {
				Logger.error("Error message was defined with incorrect parameters: {}", iae);
				break; // //////////////////// So we don't lock up the thread. ////////////////////////////
			}

		server.removeClient(this);
		end();
	}

	/**
	 * Helper method to assist in the closing of streams. Unexpected behavior may occur if called outside of this class.
	 */
	protected void end() {
		try {
			output.close();
			input.close();
			socket.close();
			run = false;
		} catch (IOException ioe) {
			Logger.error("There was a issue trying to close the socket or I/O streams: {}", ioe);
		}

		Logger.info("{} sucessfully disconnected from the server", username);
	}

	/**
	 * Writes a message to the output socket.
	 * 
	 * @param sm
	 *           The message formatted as a src-shared.networking.StringMessage object.
	 * @return true if the message was successfully set, false if an error occurred and the client was somehow disconnected.
	 */
	public boolean bufferMessage(StringMessage sm) {
		if (!socket.isConnected()) {
			end();
			return false;
		}

		try {
			output.writeObject(sm);
		} catch (IOException ioe) {
			Logger.error("{} had an error when attempting to write to the output stream: {}", username, ioe);
		}
		return true;
	}

	/**
	 * Accessor for this client's username.
	 * 
	 * @return A String representing the client.
	 */
	public String getUsername() {
		return username;
	}
}
