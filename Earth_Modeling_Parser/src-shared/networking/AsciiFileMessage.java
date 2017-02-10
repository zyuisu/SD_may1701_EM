/*

Copyright (C) 2017 Anish Kunduru

    This file is part the Visual Earth Modeling System (VEMS).

    VEMS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    VEMS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with VEMS. If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * @author Anish Kunduru
 * 
 *         This class defines a message that includes an ASCII file. Represents an ASCII that will be parsed by the server.
 */

package networking;

import java.io.Serializable;

import utils.MapProperties;

public class AsciiFileMessage implements Serializable {

	private static final long serialVersionUID = 2L;

	private MapProperties properties;
	private byte[] file;
	private boolean overwriteExisting;

	/**
	 * Constructs an a new message with the given constraints.
	 * 
	 * @param mapProperties
	 *           The properties that make up this map.
	 * @param file
	 *           The ASCII file that needs to be converted to a map, represented as a byte array.
	 * @param overwriteExisting
	 *           true if an existing map should be overwritten; false otherwise.
	 * @throws IllegalAccessException
	 *            If null values are passed for file or mapProperties.
	 */
	public AsciiFileMessage(MapProperties mapProperties, byte[] file, boolean overwriteExisting) throws IllegalAccessException {
		if (file == null || mapProperties == null)
			throw new IllegalAccessException("file and mapProperties must be set.");

		if (file.length < 1) // Can be set to a larger number if we know the minimum size of header constants.
			throw new IllegalArgumentException("The file array is empty. It must not represent a valid file.");

		this.file = file;
		properties = mapProperties;
		this.overwriteExisting = overwriteExisting;
	}

	/**
	 * @return true if an existing map should be overwritten; false otherwise.
	 */
	public boolean getOverwriteExisting() {
		return overwriteExisting;
	}

	/**
	 * @return The byte array that represents the dataset file in this message.
	 */
	public byte[] getFile() {
		return file;
	}

	/**
	 * @return The utils.MapProperties that represent this ASCII file.
	 */
	public MapProperties getMapProperties() {
		return properties;
	}
}
