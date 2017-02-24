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

import java.io.File;
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
	 *           The password associated with this username.
	 * @param keyStoreFile
	 *           The keystore certification file. This is required to authenticate a secure connection to the server.
	 * @param keyStorePassword
	 *           The master password that the keystore was generated with.
	 * @throws IOException
	 *            Error creating IO Streams.
	 * @throws KeyStoreException
	 *            Instance of keystore is not correctly defined.
	 * @throws CertificateException
	 *            Error loading passed certificate keystore.
	 * @throws NoSuchAlgorithmException
	 *            Error loading keystore.
	 * @throws UnrecoverableKeyException
	 *            Passed password doesn't unlock keystore.
	 * @throws KeyManagementException
	 *            Issue retrieving keymanager.
	 */
	public NetworkHandler(String username, String password, File keyStoreFile, String keyStorePassword) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
		this.username = username;
		this.password = password;

		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, keyStorePassword.toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		SSLContext sc = SSLContext.getInstance("TLS");
		TrustManager[] trustManagers = tmf.getTrustManagers();
		sc.init(kmf.getKeyManagers(), trustManagers, null);

		SSLSocketFactory ssf = sc.getSocketFactory();
		socket = (SSLSocket) ssf.createSocket(SERVER_ADDRESS, SERVER_PORT);
		socket.startHandshake();

		// Grab output after handshake, before input.
		output = new ObjectOutputStream(socket.getOutputStream());
		input = new ObjectInputStream(socket.getInputStream());

		listener = new NetworkListener(input);
		listener.start();

		try {
			output.writeObject(new ConnectionMessage(Type.CONNECT, username, password));
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
	 * @return true if the message was successfully sent; false if something failed.
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
			output.flush();
		} catch (Exception e) {
			System.out.println("Error sending the disconnect request to the server.");
			e.printStackTrace();
		}

		close();
	}
}
