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
 *         The purpose of this program is to store a reference to a NetworkHandler so that it can be called across multiple AbstractNetworkedScreenControllers.
 */

package singleton;

import networking.NetworkHandler;

public class NetworkData {
	private NetworkHandler handler;

	/**
	 * Default constructor to use in singleton.
	 */
	public NetworkData() {
	}

	/**
	 * Safely sets a new handler.
	 * 
	 * @param handler
	 *           A newly created NetworkHandler.
	 * @return true if the handler was set; false if a handler has already been created.
	 */
	public boolean setHandler(NetworkHandler handler) {
		if (!isHandlerSet()) {
			setNewHandler(handler);
			return true;
		}

		return false;
	}

	/**
	 * @return true if a NetworkHandler has been set; false otherwise.
	 */
	public boolean isHandlerSet() {
		if (handler == null)
			return false;

		return true;
	}

	/**
	 * Kills any existing handler and sets the current handler to this.
	 * 
	 * @param handler
	 */
	public void setNewHandler(NetworkHandler handler) {
		closeHandler();
		this.handler = handler;
	}

	/**
	 * End the NetworkHandler and associated threads, if a handler exists.
	 */
	public void closeHandler() {
		if (isHandlerSet())
			handler.end();
	}

	/**
	 * @return The current NetworkHandler object (null if not yet defined).
	 */
	public NetworkHandler getHandler() {
		return handler;
	}
}
