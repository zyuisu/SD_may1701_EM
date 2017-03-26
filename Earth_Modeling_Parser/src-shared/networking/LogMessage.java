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
 *         This class defines a message that describes a log request to the server or a response from the server.
 */

package networking;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;

public class LogMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		LIST_OF_LOGS_REQUEST, LOG_REQUEST, LIST_OF_LOGS_RESPONSE, LOG_RESPONSE;
	};

	private Type type;
	private ArrayList<String> listOfLogsResponse;
	private String logRequest;
	private byte[] logResponse;

	/**
	 * Constructs a log message of type list of logs request from the client to the server.
	 * 
	 * @param type
	 *           Must be of Type.LIST_OF_LOGS_REQUEST.
	 * @throws IllegalAccessException
	 *            If any of the passed arguments are null.
	 */
	public LogMessage(Type type) throws IllegalAccessException {
		checkArg(type);

		this.type = type;

		if (!isListOfLogsRequest())
			throw new IllegalArgumentException("This constructor can only define a client-side request of Type.LIST_OF_LOGS_REQUEST.");
	}

	/**
	 * Constructs a log message of type log request from the client to the server.
	 * 
	 * @param type
	 *           Must be of Type.LOG_REQUEST.
	 * @param logRequest
	 *           The name of the log that the client is requesting from the server (as passed in the ArrayList from the server).
	 * @throws IllegalAccessException
	 *            If any of the passed arguments are null.
	 */
	public LogMessage(Type type, String logRequest) throws IllegalAccessException {
		checkArg(type);
		checkArg(logRequest);
		if (logRequest.equals(""))
			throw new IllegalArgumentException("logRequest is empty; you must specify a filename.");

		this.type = type;
		this.logRequest = logRequest;

		if (!isLogRequest())
			throw new IllegalArgumentException("This constructor can only define a client-side request of type.LOG_REQUEST.");
	}

	/**
	 * Constructs a log message of type list of logs response from the server to the client.
	 * 
	 * @param type
	 *           Must be of Type.LIST_OF_LOGS_RESPONSE.
	 * @param listOfLogsResponse
	 *           An ArrayList representing the list of logs that the client can select.
	 * @throws IllegalAccessException
	 *            If any of the passed arguments are null.
	 */
	public LogMessage(Type type, ArrayList<String> listOfLogsResponse) throws IllegalAccessException {
		checkArg(type);
		checkArg(listOfLogsResponse);
		if (listOfLogsResponse.size() == 0)
			throw new IllegalArgumentException("There must be at least one viewable log (the current one).");

		this.type = type;
		this.listOfLogsResponse = listOfLogsResponse;

		if (!isListOfLogsResponse())
			throw new IllegalArgumentException("This constructor can only define a server-side request of type.LIST_OF_LOGS_RESPONSE.");
	}

	/**
	 * Constructs a log message of type log response from the server to the client.
	 * 
	 * @param type
	 *           Must be of Type.LOG_RESPONSE.
	 * @param logResponse
	 *           The byte array that represents the log that the client wants.
	 * @throws IllegalAccessException
	 *            If any of the passed arguments are null.
	 */
	public LogMessage(Type type, byte[] logResponse) throws IllegalAccessException {
		checkArg(type);
		checkArg(logResponse);
		if (logResponse.length < 1)
			throw new IllegalArgumentException("The log file array is empty. It must not represent a valid file.");

		this.type = type;
		this.logResponse = logResponse;

		if (!isLogResponse())
			throw new IllegalArgumentException("This constructor can only define a server-side request of type.LOG_RESPONSE");
	}

	/**
	 * Helper method to make sure passed argument isn't null.
	 * 
	 * @param arg
	 *           The Object that you wish to check for a null value.
	 * @throws IllegalAccessException
	 *            If the passed argument was null.
	 */
	private void checkArg(Object arg) throws IllegalAccessException {
		if (arg == null)
			throw new IllegalAccessException("You cannot pass null arguments.");
	}

	/**
	 * @return A String that represents the log that the client is requesting. Will return null if type is not Type.LOG_REQUEST.
	 */
	public String getRequestedLogName() {
		return logRequest;
	}

	/**
	 * @return An ArrayList with each entry representing one log that can be selected. Will return null if type is not Type.LIST_OF_LOGS_RESPONSE.
	 */
	public ArrayList<String> getListOfLogsResponse() {
		return listOfLogsResponse;
	}

	/**
	 * Writes the passed file byte array to a file at the specified location.
	 * 
	 * @param fileLocation
	 *           The location at which the new file should be stored.
	 * @return The file where the converted byte array is stored.
	 * @throws IOException
	 *            If the file created through fileLocation couldn't be written to.
	 */
	public File getLogResponse(String fileLocation) throws IOException {
		if (isLogResponse()) {
			File file = new File(fileLocation + "log.txt");
			Files.write(file.toPath(), logResponse);

			return file;
		}

		return null;
	}

	/**
	 * Determines if this message is a request for the server.
	 * 
	 * @return true if it is a request; false otherwise.
	 */
	public boolean isRequest() {
		if (type == Type.LIST_OF_LOGS_REQUEST || type == Type.LOG_REQUEST)
			return true;

		return false;
	}

	/**
	 * Determines if this message is a response to the client.
	 * 
	 * @return true if it is a response; false otherwise.
	 */
	public boolean isResponse() {
		if (type == Type.LIST_OF_LOGS_RESPONSE || type == Type.LOG_RESPONSE)
			return true;

		return false;
	}

	/**
	 * @return true if this message represents a client-side request for the list of viewable logs; false otherwise.
	 */
	public boolean isListOfLogsRequest() {
		if (type != Type.LIST_OF_LOGS_REQUEST)
			return false;

		return true;
	}

	/**
	 * @return true if this message represents a client-side request for a particular log; false otherwise.
	 */
	public boolean isLogRequest() {
		if (type != Type.LOG_REQUEST)
			return false;

		return true;
	}

	/**
	 * @return true if this message represents a list of viewable logs from the server; false otherwise.
	 */
	public boolean isListOfLogsResponse() {
		if (type != Type.LIST_OF_LOGS_RESPONSE)
			return false;

		return true;
	}

	/**
	 * @return true if this message represents a log response from the server; false otherwise.
	 */
	public boolean isLogResponse() {
		if (type != Type.LOG_RESPONSE)
			return false;

		return true;
	}

	/**
	 * Accessor for this message's type.
	 * 
	 * @return A Type.messageType
	 */
	public Type getType() {
		return type;
	}
}
