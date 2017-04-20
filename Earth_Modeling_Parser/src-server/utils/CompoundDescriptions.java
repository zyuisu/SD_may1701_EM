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
 *         Utility class which will store compound descriptions for automatic generation of JS, and return the appropriate reference scale given a MapCompoundType. Defined via Reflection to make it easy for Java newbs to add or change reference scales later.
 */

package utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;

public final class CompoundDescriptions {

	// REMINDER: SHOULD BE DEFINED WITH EXACT NAMES IN MapCompoundType. Like following:
	// public static final String MapCompoundType.name() {Full Name, Alias, What Does it Do, Summary}
	public static final String[] CH4 = { "CH<sub>4</sub>", "Carbon TetraHydride", "Methane", "Warm's the atmosphere", "Global Warming!" };
	public static final String[] ET = { "ET", "ET Full Name", "ET Alias", "ET What does it do", "ET Summary" };
	public static final String[] LEACHNO3 = { "LEACHNO<sub>3</sub>", "LEACHNO3 Full Name", "LEACHNO3 Alias", "LEACHNO3 What does it do", "LEACHNO3 Summary" };
	public static final String[] N2O = { "N<sub>2</sub>O", "N2O Full Name", "N2O Alias", "N2O What does it do", "N2O Summary" };
	public static final String[] NPP = { "NPP", "NPP Full Name", "NPP Alias", "NPP What does it do", "NPP Summary" };
	public static final String[] NUPTAKE = { "NUPTAKE", "NUPTAKE Full Name", "NUPTAKE Alias", "NUPTAKE What does it do", "NUPTAKE Summary" };
	public static final String[] RH = { "RH", "RH Full Name", "RH Alias", "RH What does it do", "RH Summary" };
	public static final String[] SOC = { "SOC", "SOC Full Name", "SOC Alias", "SOC What does it do", "SOC Summary" };
	// public static final String[] NEW_ENUMERATION_NAME = {Full Name, Alias, What Does it Do, Summary};

	// --------------------------------------------------------------------------------------------------
	// DON'T CHANGE ANYTHING BELOW the above line. Any additional variables should be added above this line.

	private Map<String, Field> fields;

	/**
	 * Creates a CompoundDescriptions object to compare compound description values. This class uses reflection to verify and return defined parameters at runtime. You need not understand how it works; just trust that it does. If you got an IllegalStateException upon initialization of an instance of this class, it means you forgot to define the appropriate
	 * constant for a given MapCompoundType.
	 * 
	 * @throws IllegalAccessException
	 *            Error retrieving the length of a field array.
	 * @throws IllegalArgumentException
	 *            Error retrieving the length of a field array.
	 */
	public CompoundDescriptions() throws IllegalArgumentException, IllegalAccessException {
		fields = new HashMap<String, Field>();

		for (Field f : this.getClass().getFields()) {
			int mod = f.getModifiers();
			if (Modifier.isFinal(mod) && Modifier.isStatic(mod) && Modifier.isPublic(mod) && f.getType().isArray() && f.getType().getComponentType().equals(String.class) && (Array.getLength(f.get(this)) == 5))
				;
			fields.put(f.getName(), f);
		}

		for (MapCompoundType mc : MapCompoundType.values())
			if (!fields.containsKey(mc.name())) {
				String issue = "You need to define a 'public static final String MapCompoundType.name()' compound description array for compound " + mc.name() + " in class utils.CompoundDescriptions.";
				Logger.error(issue);
				throw new IllegalStateException(issue);
			}
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
	public String[] getCompoundDescription(MapCompoundType mc) throws IllegalArgumentException, IllegalAccessException {
		return (String[]) fields.get(mc.name()).get(this);
	}
}