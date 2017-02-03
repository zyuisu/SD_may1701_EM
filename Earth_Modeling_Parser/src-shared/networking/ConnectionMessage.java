/**
 * @author Anish Kunduru
 * 
 *         This class defines the type of message that will happen upon client sign-on or sign-off.
 */

package networking;

import java.io.Serializable;

public class ConnectionMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		CONNECT, DISCONNECT
	};

	private Type messageType;
	private String username;
	private String password;

	/**
	 * Constructs a new ConnectionMessage.
	 * 
	 * @param type
	 *           The ConnectionMessage.Type that represents this message.
	 * @param username
	 *           The user of the person logging into the server.
	 * @param password
	 *           The password for the specified username.
	 * @throws IllegalAccessException
	 *            If a null value is passed.
	 */
	public ConnectionMessage(Type type, String usernamme, String password) throws IllegalAccessException {
		if (type == null || username == null || password == null)
			throw new IllegalAccessException("All values must be set.");

		this.username = username;
		this.password = password;

		messageType = type;
	}

	/**
	 * Accessor for this message's type.
	 * 
	 * @return A Type.messageType
	 */
	public Type getMessageType() {
		return messageType;
	}

	/**
	 * The username of the person who created this message.
	 * 
	 * @return A String representing the username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * The password for this user.
	 * 
	 * @return A String representing the password.
	 */
	public String getPassword() {
		return password;
	}
}
