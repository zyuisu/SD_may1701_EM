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
 *         Utility class which will store compound descriptions for automatic generation of JS, and return the appropriate reference scale given a MapCompoundType. Defined via Reflection to make it easy for Java newbs to add or change reference scales later.
 */

package utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class CompoundDescriptions {

	// REMINDER: SHOULD BE DEFINED WITH EXACT NAMES IN MapCompoundType. Like following:
	// public static final String MapCompoundType.name()
	public static final String CH4 = "methane";
	public static final String ET = "etetetet";
	public static final String LEACHNO3 = "leachleachleach";
	public static final String N2O = "nitrous oxide";
	public static final String NPP = "net primary production";
	public static final String NUPTAKE = "nitrogen uptake";
	public static final String RH = "rhrhrhrhrhrhrhrh";
	public static final String SOC = "socosocsocsoc";
	// public static final String NEW_ENUMERATION_NAME = "Description of compound.";

	// --------------------------------------------------------------------------------------------------
	// DON'T CHANGE ANYTHING BELOW the above line. Any additional variables should be added above this line.

	private Map<String, Field> fields;

	/**
	 * Creates a CompoundDescriptions object to compare compound description values. This class uses reflection to verify and return defined parameters at runtime. You need not understand how it works; just trust that it does. If you got an IllegalStateException upon initialization of an instance of this class, it means you forgot to define the appropriate
	 * constant for a given MapCompoundType.
	 */
	public CompoundDescriptions() {
		fields = new HashMap<String, Field>();

		for (Field f : ReferenceScales.class.getFields()) {
			int mod = f.getModifiers();
			if (Modifier.isFinal(mod) && Modifier.isStatic(mod) && Modifier.isPublic(mod) && f.getType().equals(String.class))
				fields.put(f.getName(), f);
		}

		for (MapCompoundType mc : MapCompoundType.values())
			if (!fields.containsKey(mc.name()))
				throw new IllegalStateException("You need to define a 'public static final String MapCompoundType.name()' compound description for compound " + mc.name() + " in class utils.CompoundDescriptions.");
	}

	/**
	 * Extracts the compound description via a reflective widening conversion.
	 * 
	 * @param mc
	 *           The MapCompoundType for which you wish to extract the reference scale for.
	 * @return A String that represents the compound description.
	 * @throws IllegalArgumentException
	 *            The mc.name() is not an instance of this class.
	 * @throws IllegalAccessException
	 *            The underlying field is inaccessible (check access control modifier).
	 */
	public String getCompoundDescription(MapCompoundType mc) throws IllegalArgumentException, IllegalAccessException {
		return (String) fields.get(mc.name()).get(this);
	}
}