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
 *         Keeps track of all the ASCII files that have been converted by the daemon. An object of this class is used in key daemon operations, so it is important to carefully consider implementation changes on this object. Making a change that requires an increase in the serialVersionUID nature of this object will cause inconsistencies and/or failures in
 *         server operations unless all previously stored values are converted to the new Serializable.
 */

package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pmw.tinylog.Logger;

import utils.FileLocations;
import utils.MapCompoundType;
import utils.MapProperties;
import utils.MapRegion;

public class ConvertedSet extends HashSet<MapProperties> implements Serializable {

	private static final long serialVersionUID = 3L;

	private Set<MapProperties> set;

	/**
	 * Constructor for ConvertedSet creates a set by checking against the serialized object.
	 * 
	 * @throws IOException
	 *            Can't add to the existing converted.ser file!
	 */
	public ConvertedSet() throws IOException {
		set = new HashSet<MapProperties>();

		addFromConverted();
	}

	/**
	 * Scans the converted.ser file and sets it to the HashSet reference.
	 * 
	 * @throws IOException
	 *            Can't read from the existing converted.ser file!
	 */
	private void addFromConverted() throws IOException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(FileLocations.CONVERTED_FILE_LOCATION)));

			Set<MapProperties> inSet = (Set<MapProperties>) ois.readObject();
			set = inSet;

			ois.close();
		} catch (IOException ioe) {
			if (!(ioe instanceof EOFException))
				throw ioe;
		} catch (Exception e) {
			Logger.error(e);
		}

	}

	/**
	 * Adds a given map's properties to the set. It is unsafe to use any other method to add a map.
	 * 
	 * @param properties
	 *           The MapProperties of the map that you wish to add.
	 * @return true if it doesn't exist in the set and was added; false otherwise.
	 */
	@Override
	public boolean add(MapProperties properties) {
		if (set.add(properties)) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileLocations.CONVERTED_FILE_LOCATION, false)));
				oos.writeObject(set);
				oos.close();
			} catch (Exception e) {
				Logger.error(e);
			}
			return true;
		}

		return false;
	}

	/**
	 * Removes a given map's properties from the set. It is unsafe to use any other method to remove a map.
	 * 
	 * @param properties
	 *           The MapProperties of the map that you wish to remove.
	 * @return true if it exists in the set and was removed; false otherwise.
	 */
	public boolean remove(MapProperties properties) {
		if (set.remove(properties)) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileLocations.CONVERTED_FILE_LOCATION, false)));
				oos.writeObject(set);
				oos.close();
			} catch (Exception e) {
				Logger.error(e);
			}
			return true;
		}

		return false;
	}

	/**
	 * Finds all the possible map compounds given a map region.
	 * 
	 * @param region
	 *           The region that you wish to check for valid compounds.
	 * @return All sorted array (sorted based on enum values) of all valid MapCompoundTypes that exist given a particular region. An empty array will be passed if no valid MapCompoundTypes exist.
	 */
	public MapCompoundType[] getPossibleMapCompounds(MapRegion region) {
		Set<MapCompoundType> types = new HashSet<MapCompoundType>();

		for (MapProperties p : set)
			if (p.getMapRegion() == region)
				types.add(p.getMapCompoundType());

		MapCompoundType[] typeArr = types.toArray(new MapCompoundType[types.size()]);
		Arrays.sort(typeArr);
		return typeArr;
	}

	/**
	 * Finds all the possible years given a map region and a map compound.
	 * 
	 * @param region
	 *           The region that you wish to check for valid years.
	 * @param compound
	 *           The compound that you wish to check for valid years.
	 * @return A sorted array of all valid years that exist given a particular region and compound. An empty array will be passed if no valid years exist.
	 */
	public int[] getPossibleYears(MapRegion region, MapCompoundType compound) {
		Set<Integer> years = new HashSet<Integer>();

		for (MapProperties p : set)
			if (p.getMapRegion() == region && p.getMapCompoundType() == compound)
				years.add(p.getYear());

		int[] ret = new int[years.size()];
		int index = 0;
		for (int y : years)
			ret[index++] = y;

		Arrays.sort(ret);
		return ret;
	}

	/**
	 * Finds all possible months given a map region, map compound, an a year.
	 * 
	 * @param region
	 *           The region that you wish to check for valid months.
	 * @param compound
	 *           The compound that you wish to check for valid months.
	 * @param year
	 *           The year that you wish to check for valid months.
	 * @return A sorted array of all valid months that exist given a particular region and compound. An empty array will be passed if no valid months exist.
	 */
	public int[] getPossibleMonths(MapRegion region, MapCompoundType compound, int year) {
		Set<Integer> months = new HashSet<Integer>();

		for (MapProperties p : set)
			if (p.getMapRegion() == region && p.getMapCompoundType() == compound && p.getYear() == year)
				months.add(p.getMonth());

		if (months.contains(-1))
			return new int[0];

		int[] ret = new int[months.size()];
		int index = 0;
		for (int y : months)
			ret[index++] = y;

		Arrays.sort(ret);
		return ret;
	}
}
