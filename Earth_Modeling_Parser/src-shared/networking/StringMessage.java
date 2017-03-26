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
 *         This class defines the types of string messages that can be passed in server-client interactions.
 */

package networking;

import java.io.Serializable;

public class StringMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum Type {
		ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE
	};

	private Type messageType;
	private String msgHeader;
	private String msgContent;

	/**
	 * Constructs a StringMessage based on passed parameters.
	 * 
	 * @param type
	 *           The StringMessage.Type that this type represents.
	 * @param msgHeader
	 *           The String that will be displayed as the header of the message. Should be simple.
	 * @param msgContent
	 *           The String that will explain the issue in greater detail.
	 * @throws IllegalAccessException
	 *            If a null value is passed.
	 */
	public StringMessage(Type type, String msgHeader, String msgContent) throws IllegalAccessException {
		if (type == null || msgHeader == null || msgContent == null)
			throw new IllegalAccessException("All values must be set.");

		messageType = type;
		this.msgHeader = msgHeader;
		this.msgContent = msgContent;
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
	 * @return A brief message to summarize the issue. Should be simple.
	 */
	public String getMsgHeader() {
		return msgHeader;
	}

	/**
	 * @return A String representing the message in greater detail (full message).
	 */
	public String getMsgContent() {
		return msgContent;
	}
}
