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
 *         Keeps track of all the ASCII files that have been converted by the daemon.
 */

package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.pmw.tinylog.Logger;

import utils.FileLocations;
import utils.MapProperties;

public class ConvertedSet extends HashSet<String> implements Serializable {

	private static final long serialVersionUID = 2L; // If we wish to use ObjectOutputStream at a later date. This would allow us to easily store MapProperties in this set versus unsafe strings.

	private FileWriter fileWriter;
	Set<String> set;

	/**
	 * Constructor for ConvertedSet creates a set by checking against the previous converted.txt file (located in CONVERTED_LOCATION).
	 * 
	 * @throws IOException
	 *            Can't add to the existing converted.txt file!
	 */
	public ConvertedSet() throws IOException {
		set = new HashSet<String>();
		fileWriter = new FileWriter(FileLocations.CONVERTED_FILE_LOCATION, true);

		addFromConverted();
	}

	/**
	 * Scans the converted.txt file and adds all values to the HashSet.
	 * 
	 * @throws IOException
	 *            Can't read from the existing converted.txt file!
	 */
	private void addFromConverted() throws IOException {
		BufferedReader buffR;
		buffR = new BufferedReader(new FileReader(FileLocations.CONVERTED_FILE_LOCATION));

		Scanner sc = new Scanner(buffR);
		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if (!line.equals(""))
				set.add(line);
		}

		sc.close();
		buffR.close();
	}

	/**
	 * Overrides the HashSet.add(E e) to ensure no one else can call it. Always returns false.
	 */
	@Override
	public boolean add(String str) {
		return false;
	}

	/**
	 * Adds a given map's properties to the set. For now, this is backed by MapProperties.toString() and HashSet<String>. It is unsafe to use any other method to add a map.
	 * 
	 * @param properties
	 *           The MapProperties of the map that you wish to add.
	 * @return true if it doesn't exist in the set and was added; false otherwise.
	 */
	public boolean add(MapProperties properties) {
		String str = properties.toString();

		if (super.add(str)) {
			try {
				fileWriter.write(str + "\n");
				fileWriter.flush();
			} catch (IOException e) {
				Logger.error(e);
				return false;
			}
			return true;
		}

		return false;
	}

	/**
	 * Removes a given map's properties from the set. For now, this is backed by MapPropreties.toString() and HashSet<String>. It is unsafe to use any other method to remove a map.
	 * 
	 * @param properties
	 *           The MapProperties of the map that you wish to remove.
	 * @return true if it exists in the set and was removed; false otherwise.
	 */
	public boolean remove(MapProperties properties) {
		return super.remove(properties.toString());
	}
}
