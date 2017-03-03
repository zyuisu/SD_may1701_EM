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
 *         This class defines a message that describes a map for the server to delete.
 */

package networking;

import java.io.Serializable;

import utils.MapProperties;

public class DeleteMapMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private MapProperties properties;

	/**
	 * Constructs a new message with the given constraints.
	 * 
	 * @param properties
	 *           The MapProperties of the map that you wish to delete.
	 * @throws IllegalAccessException
	 *            The value of properties cannot be null.
	 */
	public DeleteMapMessage(MapProperties properties) throws IllegalAccessException {
		if (properties == null)
			throw new IllegalAccessException("Properties must be set.");

		this.properties = properties;
	}

	/**
	 * @return The utils.MapProperties that represent this ASCII file.
	 */
	public MapProperties getMapProperties() {
		return properties;
	}
}
