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
		} catch (Exception e) {
			Logger.error(e);
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

			if (!(obj == null || obj instanceof ConnectionMessage))
				return false;

			ConnectionMessage cm = (ConnectionMessage) obj;

			if (cm.getMessageType() != ConnectionMessage.Type.CONNECT)
				return false;

			if (!server.validateUser(cm.getUsername(), cm.getPassword(), socket)) {
				bufferMessage(new ConnectionMessage(ConnectionMessage.Type.UNSUCCESSFUL_CONNECTION, cm.getUsername(), cm.getPassword()));
				return false;
			}

			username = cm.getUsername();
			bufferMessage(new ConnectionMessage(ConnectionMessage.Type.SUCCESSFUL_CONNECTION, cm.getUsername(), cm.getPassword()));
			return true;

		} catch (IOException ioe) {
			Logger.error("Exception parsing I/O stream: {}", ioe);
		} catch (ClassNotFoundException cnfe) {
			Logger.error("Couldn't parse with a defined class. Check src-shared.networking? {}", cnfe);
		} catch (IllegalAccessException iae) {
			Logger.error("Error message was defined with incorrect parameters: {}", iae);
		} catch (Exception e) {
			Logger.error(e);
		}

		return false;
	}

	/**
	 * Starts the thread. Runs until the client passes a ConnectionMessage of ConnectionMessage.Type.DISCONNECT. The run flag will also be tripped by bufferMessage() if there is a timeout event on the socket that causes sending a message over output to fail. An error will be logged if this method is given an invalid object from the input stream.
	 */
	@Override
	public void run() {
		run = true;

		int validateCount = 0;
		// Validate user.
		while (!initializeUser()) {
			if (validateCount > 5) {
				Logger.info("{} failed attempted connection too many times.", username);
				end();
				break;
			}
			validateCount++;
		}

		Logger.info("{} sucessfully connected to server", username);

		// Parse objects sent by the user.
		while (run)
			try {
				Object obj = input.readObject();

				if (obj == null)
					bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "Communication Error", "A null value was passed to the server."));
				else if (obj instanceof AsciiFileMessage) {
					StringMessage sm = server.parseAsciiFileMessage((AsciiFileMessage) obj);
					if (sm == null)
						bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "An unknown error occured while parsing the ascii file message.", "This shouldn't happen."));
					else
						bufferMessage(sm);
				} else if (obj instanceof ConnectionMessage) {
					ConnectionMessage cm = (ConnectionMessage) obj;

					if (cm.getMessageType() == ConnectionMessage.Type.DISCONNECT)
						run = false;
				} else if (obj instanceof DeleteMapMessage) {
					StringMessage sm = server.parseDeleteMapMessage((DeleteMapMessage) obj);

					if (sm == null)
						bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "An unknown error occured while parsing the delete map message.", "This shouldn't happen."));
					else
						bufferMessage(sm);
				} else if (obj instanceof LogMessage) {
					LogMessage lm = (LogMessage) obj;
					if (!lm.isRequest())
						bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "LogMessage error", "The server was passed a log message that wasn't a type of log request."));

					LogMessage responseMsg = server.parseLogMessage(lm);
					if (responseMsg == null)
						bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "An unknown error occured while parsing the log message.", "This shouldn't happen."));
					else
						bufferMessage(responseMsg);
				} else
					bufferMessage(new StringMessage(StringMessage.Type.ERROR_MESSAGE, "Message sending error.", "The input object passed is not a value message class defined in src-shared.networking. Try again."));
			} catch (IOException ioe) {
				Logger.error("Exception parsing I/O stream: {}", ioe);
				break; // //////////////////// So we don't lock up the thread. ////////////////////////////
			} catch (ClassNotFoundException cnfe) {
				Logger.error("Couldn't parse with a defined class. Check src-shared.networking? {}", cnfe);
				break; // //////////////////// So we don't lock up the thread. ////////////////////////////
			} catch (IllegalAccessException iae) {
				Logger.error("Error message was defined with incorrect parameters: {}", iae);
				break; // //////////////////// So we don't lock up the thread. ////////////////////////////
			} catch (Exception e) {
				Logger.error(e);
				break; // //////////////////// So we don't lock up the thread. ////////////////////////////
			}

		end();
	}

	/**
	 * Helper method to assist in the closing of a socket (and thereby its streams). Unexpected behavior may occur if called outside of this class.
	 */
	protected void end() {
		try {
			socket.close();
			run = false;
			server.removeClient(this);
		} catch (IOException ioe) {
			Logger.error("There was a issue trying to close the socket or I/O streams: {}", ioe);
		} catch (Exception e) {
			Logger.error(e);
		}

		Logger.info("{} sucessfully disconnected from the server", username);
	}

	/**
	 * Writes a message to the output socket.
	 * 
	 * @param message
	 *           The message formatted as a src-shared.networking.
	 * @return true if the message was successfully set, false if an error occurred and the client was somehow disconnected.
	 */
	public boolean bufferMessage(Object message) {
		if (!socket.isConnected() || socket.isClosed() || socket.isOutputShutdown()) {
			end();
			return false;
		}

		try {
			output.writeObject(message);
		} catch (IOException ioe) {
			Logger.error("{} had an error when attempting to write to the output stream: {}", username, ioe);
		} catch (Exception e) {
			Logger.error(e);
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

	/**
	 * Checks to see if there is any way that a client could have gotten disconnected and calls end if it is.
	 * 
	 * @return true if the client is connected; false otherwise.
	 */
	public boolean isClientConnected() {
		// Check to is socket hasn't been started yet.
		if (!socket.isConnected())
			return false;

		// Check to see see if I have closed this socket.
		if (socket.isClosed()) {
			end();
			return false;
		}

		// Don't know if we disconnected. Test a write.
		try {
			output.write(1);
			output.flush();
		} catch (IOException ioe) {
			// The connection was dropped.
			end();
			return false;
		} catch (Exception e) {
			Logger.error(e); // unknown error
		}

		// Must still be connected.
		return true;
	}
}
