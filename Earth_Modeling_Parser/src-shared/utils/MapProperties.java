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
 *         Convenient wrapper for all the map properties that server-side class will have to frequently pass around.
 */

package utils;

public class MapProperties {

	private MapCompoundType type;
	private int year;
	private int month;

	/**
	 * Constructor creates a new MapProperties that represents the values a map's properties can hold.
	 * 
	 * @param type
	 *           The type of molecule this map represents.
	 * @param year
	 *           The year this map represents. Must be greater than 1500.
	 * @param month
	 *           The month this map represents. Must be between 0 and 11, inclusive (Jan to Dec).
	 * @throws IllegalAccessException
	 *            If MapCompoundType is null.
	 */
	public MapProperties(MapCompoundType type, int year, int month) throws IllegalAccessException {

		if (isValidYearAndType(type, year)) {
			this.type = type;
			this.year = year;
		}

		if (month < 0 || month > 11)
			throw new IllegalArgumentException("Month cannot be less than 0 or greater than 11 (Jan to Dec).");
		else
			this.month = month;
	}

	/**
	 * Constructor creates a new MapProperties that represents the values a map's properties can hold.
	 * 
	 * @param type
	 *           The type of molecule this map represents.
	 * @param year
	 *           The year this map represents. Must be greater than 1500.
	 * @throws IllegalAccessException
	 *            If MapCompoundType is null.
	 */
	public MapProperties(MapCompoundType type, int year) throws IllegalAccessException {
		if (isValidYearAndType(type, year)) {
			this.type = type;
			this.year = year;
		}

		this.month = -1;
	}

	/**
	 * Helper for constructor to check validity of a year and type.
	 * 
	 * @param type
	 *           The MapCompound that this map is to represent.
	 * @param year
	 *           The year that this map is to represent.
	 * @return true if valid; exception if not
	 * @throws IllegalAccessException
	 *            If a null value was passed as the type.
	 */
	private boolean isValidYearAndType(MapCompoundType type, int year) throws IllegalAccessException {
		if (year < 1500)
			throw new IllegalArgumentException("Did you set an invalid year?");

		if (type == null)
			throw new IllegalAccessException("type must be set.");

		return true;
	}

	/**
	 * Returns a "Stringized" version of this map's properties.
	 */
	@Override
	public String toString() {
		return type.name() + "y" + year + "m" + month;
	}

	/**
	 * @return The MapCompound that represents this map's molecule.
	 */
	public MapCompoundType getMapCompoundType() {
		return type;
	}

	/**
	 * @return The year that this map represents.
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @return The month that this map represents; -1 if this map does not have a representative month.
	 */
	public int getMonth() {
		return month;
	}
}
