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
 *         Server-side class that handles client connections and interactions between a ClientThread and the main daemon.
 */

package networking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.pmw.tinylog.Logger;

import main.EarthModellingDaemon;
import utils.FileLocations;

public class ClientServer extends Thread {

	private Set<ClientThread> clients;
	private Map<String, String> approvedClients;
	private int serverPort;
	private boolean run;
	private String keyStoreLocation;
	private String keyStorePassword;

	/**
	 * Creates a new ClientServer and starts its operation.
	 * 
	 * @param portNumber
	 *           The port that the server should listen for connections on.
	 * @param keyStoreLocation
	 *           The location, on the local disk, of the keystore.
	 * @param keyStorePassword
	 *           The master password to unlock the keystore.
	 */
	public ClientServer(int portNumber, String keyStoreLocation, String keyStorePassword) {
		serverPort = portNumber;
		clients = new HashSet<ClientThread>();
		run = true;
		this.keyStoreLocation = keyStoreLocation;
		this.keyStorePassword = keyStorePassword;

		approvedClients = new HashMap<String, String>();
		addFromApprovedList();
	}

	/**
	 * Scans the approvedClients.txt file and adds all values to the HashMap.
	 * 
	 * @throws IOException
	 *            Can't read from the existing approvedClients.txt file!
	 */
	private void addFromApprovedList() {
		try {
			BufferedReader buffR;
			buffR = new BufferedReader(new FileReader(FileLocations.APPROVED_CLIENTS_FILE_LOCATION));

			Scanner sc = new Scanner(buffR);
			while (sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				StringTokenizer tok = new StringTokenizer(line);
				if (tok.countTokens() == 2)
					approvedClients.put(tok.nextToken(), tok.nextToken());
			}

			sc.close();
			buffR.close();
		} catch (IOException iae) {
			Logger.error("Error reading from the approved clients file: {}", iae);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	/**
	 * Starts the server and waits for connections.
	 */
	@Override
	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(keyStoreLocation), keyStorePassword.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keyStorePassword.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			SSLContext sc = SSLContext.getInstance("TLS");
			TrustManager[] trustManagers = tmf.getTrustManagers();
			sc.init(kmf.getKeyManagers(), trustManagers, null);

			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(serverPort);

			Logger.info("Waiting for clients on port: {}", serverPort);

			while (run) {
				SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

				if (!run)
					break; // Previous call is blocking, so check again to avoid IOExceptions or stalled threads.

				ClientThread client = new ClientThread(clientSocket, this);
				clients.add(client);

				client.start();
			} // end run loop

			try {
				serverSocket.close();

				for (ClientThread client : clients)
					client.end();
			} catch (IOException ioe) {
				Logger.error("Error closing the serverSocket: {}", ioe);
			}
		} catch (IOException ioe) {
			Logger.error("IOException while creating ServerSocket on port: {}\n{}", serverPort, ioe);
		} catch (KeyStoreException e) {
			Logger.error("Instance of keystore isn't correctly defined.", e);
		} catch (NoSuchAlgorithmException e) {
			Logger.error("Error loading keystore.", e);
		} catch (CertificateException e) {
			Logger.error("Error loading passed certificate keystore.", e);
		} catch (UnrecoverableKeyException e) {
			Logger.error("Passed password doesn't unlock the keystore.", e);
		} catch (KeyManagementException e) {
			Logger.error("Issue retrieving keymanager.", e);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	/**
	 * Gracefully shuts the server down by tripping the flag.
	 */
	public void end() {
		run = false;

		// Break a blocking call by connecting to the socket.
		try {
			new Socket("localhost", serverPort);
		} catch (IOException ioe) {
			Logger.error("Error trying to stop the server: {}", ioe);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	/**
	 * Responds to a log message request from the client.
	 * 
	 * @param lm
	 *           The log message from the client (must return true for logMessage.isRequest()).
	 * @return A log message response (will return true for logMessage.isReponse()).
	 */
	public synchronized LogMessage parseLogMessage(LogMessage lm) {
		try {
			if (lm.isListOfLogsRequest()) {
				File logDir = new File(FileLocations.LOGS_DIRECTORY_LOCATION);

				ArrayList<String> logNames = new ArrayList();
				for (File f : logDir.listFiles())
					if (f.getName().contains(".log"))
						logNames.add(f.getName().replace(".log", "").replace("log.", ""));

				return new LogMessage(LogMessage.Type.LIST_OF_LOGS_RESPONSE, logNames);
			} else if (lm.isLogRequest()) {
				File logDir = new File(FileLocations.LOGS_DIRECTORY_LOCATION);

				File logFile = null;
				for (File f : logDir.listFiles())
					if (f.getName().contains(lm.getRequestedLogName()))
						logFile = f;

				if (logFile == null)
					return null; // Can't be a valid request, as such a file doesn't exist.
				else
					return new LogMessage(LogMessage.Type.LOG_RESPONSE, Files.readAllBytes(logFile.toPath()));
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * Creates a new map by calling the appropriate daemon methods.
	 * 
	 * @param afm
	 *           The AsciiFileMessage that represents the instructions for this map's creation.
	 * @return A StringMessage letting the user know if the process was successful or not (outputs the error).
	 */
	public synchronized StringMessage parseAsciiFileMessage(AsciiFileMessage afm) {
		try {
			if (afm.getOverwriteExisting()) {
				String exceptions = EarthModellingDaemon.removeMapFromServer(afm.getMapProperties());
				if (exceptions != null)
					return new StringMessage(StringMessage.Type.ERROR_MESSAGE, "There was an issue removing map: " + afm.getMapProperties().toString() + ".", exceptions);
			} else
				try {
					String exceptions = EarthModellingDaemon.createMap(afm.getFile(), afm.getMapProperties());
					if (exceptions != null)
						return new StringMessage(StringMessage.Type.ERROR_MESSAGE, "There was an issue creating map: " + afm.getMapProperties().toString() + ".", "Is it possible that the map you wish to create already exists?\n" + exceptions);
					else
						return new StringMessage(StringMessage.Type.INFORMATION_MESSAGE, "Success!", "The map " + afm.getMapProperties().toString() + " was sucessfully created.");
				} catch (Exception e) {
					Logger.error(e);
					return new StringMessage(StringMessage.Type.ERROR_MESSAGE, "Map generation for map: " + afm.getMapProperties().toString() + " failed.", "Try again, utilizing the overwrite setting.\n" + e.getMessage());
				}
		} catch (IllegalAccessException iae) {
			Logger.error("StringMessage message was defined with incorrect parameters: {}", iae);
		} catch (Exception e) {
			Logger.error(e);
		}

		return null;
	}

	/**
	 * Deletes a map by calling the appropriate daemon method.
	 * 
	 * @param dmm
	 *           The DeleteMapMessage that represents the map to be deleted.
	 * @param client
	 *           A reference to the ClientThread that is making the call (to return error or success messages). * @return A StringMessage letting the user know if the process was successful or not (outputs the error).
	 */
	public synchronized StringMessage parseDeleteMapMessage(DeleteMapMessage dmm) {
		try {
			String exceptions = EarthModellingDaemon.removeMapFromServer(dmm.getMapProperties());
			if (exceptions == null)
				return new StringMessage(StringMessage.Type.INFORMATION_MESSAGE, "Success!", "The map " + dmm.getMapProperties().toString() + " was sucessfully deleted.");
			else
				return new StringMessage(StringMessage.Type.ERROR_MESSAGE, "There was an issue removing the existing map.", "Check if the given map exists in the server manager and try again.\n" + exceptions);
		} catch (IllegalAccessException iae) {
			Logger.error("StringMessage message was defined with incorrect parameters: {}", iae);
		} catch (Exception e) {
			Logger.error(e);
		}

		return null;
	}

	/**
	 * Remove the stored reference after the client disconnects.
	 * 
	 * @param client
	 *           The ClientThread that will be disconnected.
	 */
	public synchronized void removeClient(ClientThread client) {
		clients.remove(client);
	}

	/**
	 * Checks if a user client is currently connected to the daemon.
	 * 
	 * @return true if a client is connected; false otherwise.
	 */
	public synchronized boolean isAClientConnected() {
		for (ClientThread c : clients)
			if (c.isClientConnected())
				return true;

		return false;
	}

	/**
	 * Validates a user by checking against the approvedClients HashMap.
	 * 
	 * @param username
	 *           The username of the client to validate.
	 * @param password
	 *           The password associated with the username you wish to validate.
	 * @param socket
	 *           The Socket that the client is connecting on (for logging functionality).
	 * @return true if the user is authorized to access the web interface; false otherwise.
	 */
	public synchronized boolean validateUser(String username, String password, Socket socket) {
		if (!approvedClients.containsKey(username)) {
			Logger.warn("Authentication failure with username: {} @ {}", username, socket.getInetAddress());
			return false;
		}

		// TODO
		// Salt passwords instead of storing as plaintext.
		if (!approvedClients.get(username).equals(password)) {
			Logger.warn("Authentication failure with username: {}, password: {}, @ {}", username, password, socket.getInetAddress());
			return false;
		}

		Logger.info("Username: {} connected from: {}", username, socket.getInetAddress());
		return true;
	}
}
