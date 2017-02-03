/**
 * @author Anish Kunduru
 * 
 *         This class defines the types of string messages that can be passed in server-client interactions.
 */

package networking;

import java.io.Serializable;

public class StringMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		ERROR_MESSAGE, MESSAGE
	};

	private Type messageType;
	private String message;

	/**
	 * Constructs a StringMessage based on passed parameters.
	 * 
	 * @param type
	 *           The StringMessage.TYPE that this type represents.
	 * @param message
	 *           The String that you would like to send along with your message.
	 * @throws IllegalAccessException
	 *            If a null value is passed.
	 */
	public StringMessage(Type type, String message) throws IllegalAccessException {
		if (type == null || message == null)
			throw new IllegalAccessException("All values must be set.");

		messageType = type;
		this.message = message;
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
	 * Returns the String included in this message.
	 * 
	 * @return The String that this object was constructed with.
	 */
	public String getMessage() {
		return message;
	}
}
