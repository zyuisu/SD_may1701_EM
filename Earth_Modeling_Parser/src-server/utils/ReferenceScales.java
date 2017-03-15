/*
 * 
 * Copyright (C) 2017 Anish Kunduru and Kellen Johnson
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
 *         Utility class which will store reference scales for Python Scripts, and return the appropriate reference scale given a MapRegionType. Defined via Reflection to make it easy for Java newbs to add or change reference scales later.
 */

package utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class ReferenceScales {

	// REMINDER: SHOULD BE DEFINED WITH EXACT NAMES IN MapRegionType. Like following:
	// private static final int MapRegionType.name()
	private static final int GLOBAL = 75000000;
	private static final int MIDWESTERN_US = 50000000; // As in 1:50000000
	private static final int MISSISSIPPI_RIVER_BASIN = 50000000;
	// private static final int NEW_ENUMERATION_NAME = 100000000;

	// --------------------------------------------------------------------------------------------------
	// DON'T CHANGE ANYTHING BELOW the above line. Any additional variables should be added above this line.

	private Map<String, Field> fields;

	/**
	 * Creates a ReferenceScales object to compare scale values. This class uses reflection to verify and return defined parameters at runtime. You need not understand how it works; just trust that it does. If you got an IllegalArgumentException upon initialisation of an instance of this class, it means you forgot to define the appropriate constant for a
	 * given MapRegionType.
	 */
	public ReferenceScales() {
		fields = new HashMap();

		for (Field f : ReferenceScales.class.getFields()) {
			int mod = f.getModifiers();
			if (Modifier.isFinal(mod) && Modifier.isStatic(mod) && Modifier.isPrivate(mod) && f.getType().equals(int.class))
				fields.put(f.getName(), f);
		}

		for (MapRegionType mr : MapRegionType.values())
			if (!fields.containsKey(mr.name()))
				throw new IllegalArgumentException("You need to define a 'private static final int MapRegionType.name()' reference scale for region " + mr.name());
	}

	/**
	 * Extracts the reference scale via a reflective widening conversion.
	 * 
	 * @param mr
	 *           The MapRegionType for which you wish to extract the reference scale for.
	 * @return An int that represents the reference scale, 1:returnedValue. 50000000, for example, represents 1:50000000.
	 * @throws IllegalArgumentException
	 *            The mr.name() is not an instance of this class.
	 * @throws IllegalAccessException
	 *            The underlying field is inaccessable (check access control modifier).
	 */
	public String getReferenceScale(MapRegionType mr) throws IllegalArgumentException, IllegalAccessException {
		//return fields.get(mr.name()).getInt(this);
		return fields.get(mr.name()).toString();
	}
}
