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
 *         This program is the client-side class that initializes a connection with the server. It will be called by a screen.
 */

package networking;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import networking.ConnectionMessage.Type;

public class NetworkHandler {

	// THESE CONSTANTS WILL NEED TO BE UPDATED WHEN THE SERVER FIELDS CHANGE.
	public final String SERVER_ADDRESS = ServerInformation.SERVER_ADDRESS;
	public final int SERVER_PORT = ServerInformation.SERVER_PORT;
	public final String KEYSTORE_FILE = "/home/akunduru/Desktop/keystore.jks";

	private SSLSocket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private String username;
	private String password;

	private NetworkListener listener;

	/**
	 * Starts the network manager and initializes a connection to the server.
	 * 
	 * @param username
	 *           A valid username that will be accepted by the server.
	 * @param password
	 *           A valid password that will be accepted by the server. Also represents the master password for the keystore.
	 */
	public NetworkHandler(String username, String password) {
		this.username = username;
		this.password = password;

		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(KEYSTORE_FILE), password.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, password.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			SSLContext sc = SSLContext.getInstance("TLS");
			TrustManager[] trustManagers = tmf.getTrustManagers();
			sc.init(kmf.getKeyManagers(), trustManagers, null);

			SSLSocketFactory ssf = sc.getSocketFactory();
			socket = (SSLSocket) ssf.createSocket(SERVER_ADDRESS, SERVER_PORT);
			socket.startHandshake();
			
			
			//DEBUG
			System.out.println(socket.getEnableSessionCreation());
			System.out.println(socket.isConnected());
			System.out.println("yo");
			
			input = new ObjectInputStream(socket.getInputStream());
			
			//DEBUG
			System.out.println("yo");
			
			output = new ObjectOutputStream(socket.getOutputStream());
			
			listener = new NetworkListener(input);
			listener.start();

			ConnectionMessage cm = new ConnectionMessage(Type.CONNECT, username, password);			
			output.writeObject(cm);

		} catch (IOException ioe) {
			System.out.println("IOException while creating the socket or initalizing socket streams.");
			ioe.printStackTrace();
		} catch (KeyStoreException e) {
			System.out.println("Instance of keystore isn't correctly defined.");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error loading keystore.");
			e.printStackTrace();
		} catch (CertificateException e) {
			System.out.println("Error loading passed certificate keystore.");
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			System.out.println("Passed password doesn't unlock the keystore.");
			e.printStackTrace();
		} catch (KeyManagementException e) {
			System.out.println("Issue retrieving keymanager.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("An invalid ConnectionMessage was created.");
			e.printStackTrace();
		} 
	}

	/**
	 * Closes out the I/O streams, socket, and sets the NetworkListener run boolean to false by calling its end() method.
	 */
	private void close() {
		try {
			socket.close();
			input.close();
			output.close();
		} catch (IOException ioe) {
			System.out.println("Error closing I/O streams.");
			ioe.printStackTrace();
		}

		listener.end();
	}

	/**
	 * Writes an AsciiFileMessage to the buffer (socket stream).
	 * 
	 * @param afm
	 *           The AsciiFileMessage that you wish to send through the buffer.
	 * @return true if the message was sucessfully sent; false if something failed.
	 */
	public boolean bufferAsciiFileMessage(AsciiFileMessage afm) {
		if (!socket.isConnected()) {
			close();
			return false;
		}

		try {
			output.writeObject(afm);
		} catch (IOException ioe) {
			System.out.println("I/O error while attempting to write to the buffer.");
			ioe.printStackTrace();
		}

		return true;
	}

	/**
	 * Send a disconnect type message to the buffer, close out the streams, and end the listener thread.
	 */
	public void end() {

		try {
			ConnectionMessage cm = new ConnectionMessage(Type.DISCONNECT, username, password);
			output.writeObject(cm);
		} catch (Exception e) {
			System.out.println("Error sending the disconnect request to the server.");
			e.printStackTrace();
		}

		close();
	}
}
