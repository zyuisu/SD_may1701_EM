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

import java.io.Serializable;

public class MapProperties implements Serializable {

	private static final long serialVersionUID = 2L;

	private MapCompoundType compound;
	private MapRegionType region;
	private int year;
	private int month;

	/**
	 * Constructor creates a new MapProperties that represents the values a map's properties can hold.
	 * 
	 * @param region
	 *           The extent of this map.
	 * @param compound
	 *           The type of molecule this map represents.
	 * @param year
	 *           The year this map represents. Can't be less than 1500 or greater than 2100.
	 * @param month
	 *           The month this map represents. Must be between 0 and 11, inclusive (Jan to Dec).
	 * @throws IllegalAccessException
	 *            If MapCompoundType or MapRegion is null.
	 */
	public MapProperties(MapRegionType region, MapCompoundType compound, int year, int month) throws IllegalAccessException {
		this(region, compound, year);

		if (month < 0 || month > 11)
			throw new IllegalArgumentException("Month cannot be less than 0 or greater than 11 (Jan to Dec).");
		this.month = month;
	}

	/**
	 * Constructor creates a new MapProperties that represents the values a map's properties can hold.
	 * 
	 * @param region
	 *           The extent of this map.
	 * @param compound
	 *           The type of molecule this map represents.
	 * @param year
	 *           The year this map represents. Can't be less than 1500 or greater than 2100.
	 * @throws IllegalAccessException
	 *            If MapCompoundType or MapRegion is null.
	 */
	public MapProperties(MapRegionType region, MapCompoundType compound, int year) throws IllegalAccessException {
		if (region == null || compound == null)
			throw new IllegalAccessException("Region and compound types must be set.");
		this.region = region;
		this.compound = compound;

		if (year < 1500)
			throw new IllegalArgumentException("Are you sure you set the correct year? It is less than 1500.");
		else if (year > 2100)
			throw new IllegalArgumentException("Are you sure you set the correct year? It is greater than 2100.");

		this.year = year;
		this.month = -1;

	}

	/**
	 * Returns a "Stringized" version of this map's properties.
	 */
	@Override
	public String toString() {
		return region.name() + compound.name() + "y" + year + "m" + month;
	}

	/**
	 * @return The MapRegion that represents the extent of this map.
	 */
	public MapRegionType getMapRegion() {
		return region;
	}

	/**
	 * @return The MapCompound that represents this map's molecule.
	 */
	public MapCompoundType getMapCompoundType() {
		return compound;
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

	/**
	 * Returns true if a type MapProperties and all instance variables are equal; false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MapProperties))
			return false;

		MapProperties mp = (MapProperties) o;
		if (mp.compound == this.compound && mp.region == this.region && mp.year == this.year && mp.month == this.month)
			return true;

		return false;
	}

	/**
	 * Generates a consistent and unique hashcode using this.toString().
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}
