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
	public static final String[] CH4 = {"CH<sub>4</sub>", "Methane", "Here we provide modeling estimate of soil-atmosphere exchange of methane. Negative values indicate CH<sub>4</sub> uptake by land ecosystem while positive values indicate CH<sub>4</sub> emission to the atmosphere.", "Major natural sources: Wetland, termites, and oceans. Major human sources: Fossil fuel, Livestock farming, Landfills and waste, Biomass burning, Rice agriculture.", "CH<sub>4</sub> is more efficient at trapping radiation than CO<sub>2</sub>. The cumulative impact of the emission of 1 g CH<sub>4</sub> is 28 times as much as that of 1 g CO<sub>2</sub> over a 100-year period according to the 5th IPCC report." };
	public static final String[] ET = { "ET", "Evapotranspiration", "Evapotranspiration is the sum of evaporation and plant transpiration from the Earth's land and ocean surface to the atmosphere.", "N/A", "ET is a major component of energy as well as water-vapor exchange between land surfaces and atmosphere, significantly impacting the water and energy balance of the earth. Transpiration is essential to the plant growth through transpiration pull to provide sufficient water and nutrient. In addition, Transpiration is closely coupled to the water use efficiency (the ratio of the rate of carbon assimilation to the rate of transpiration)." };
	public static final String[] LEACHNO3 = {"NO<sub>3</sub> Leaching",  "Nitrate Leaching", "Nitrate leaching refers to the loss of water-soluble nitrate from soil, due to rain and irrigation.", "Rock Weathering, Atmosphere deposition, Nitrification, Nitrogen fertilizer.", "The nutrients of plants is directly lost to the river due to NO<sub>3</sub>- leaching. Leached nitrate is also a big source of N<sub>2</sub>O. In addition, High levels of NO<sub>3</sub>- in water can adversely affect oxygen levels for both humans and aquatic systems. Human health issues include methemoglobinemia and anoxia, commonly referred to as blue baby syndrome. Eutrophication is another issue can cause the death of fish and other marine species." };
	public static final String[] N2O = {"N<sub>2</sub>O",  "Nitrous oxide ", "N<sub>2</sub>O is a chemical compound, an oxide of nitrogen with the formula N<sub>2</sub>O.", "Comes mainly from Denitrification, and small part from Fossil fuel combustion, and Nitrification.", "N<sub>2</sub>O has a significant global warming potential as a potent greenhouse gas. Over a 100-year period, the atmospheric heat trapping ability of 1 g N<sub>2</sub>O is 265 times as much as that of 1 g CO<sub>2</sub> according to the 5th IPCC report. N2O also acts as the single most important ozone-depleting gas." };
	public static final String[] NPP = {"NPP",  "Net Primary Productivity", "NPP represents the rate of biomass generation or carbon assimilation through photosynthesis. Net Primary Productivity is calculated as the difference between Gross Primary productivity and Plant Respiration.", "N/A", "NPP represents the net carbon fixation by plants, which is a process of mitigating the greenhouse effects." };
	public static final String[] NUPTAKE = {"N uptake",  "Nitrogen uptake by plants", "Plants absorb nitrogen from the soil in the form of nitrate (NO<sub>3</sub>-) and ammonium (NH<sub>4</sub>+).", "Rock weathering, Lighting, Biological nitrogen fixation, Atmosphere deposition, Nitrogen Fertilizer.", "Plants can take up nitrogen from soils, competing with the microbial community, and thus meet N demands for maintaining plant growth and productivity." };
	public static final String[] RH = {"R<sub>H</sub>",  "Heterotrophic Respiration", "The release of CO<sub>2</sub> during the process of decomposition of organic matter in the soil by soil animals, fungi, and other decomposer organisms.", "Plants debris, Soil organic matter, Soil microbes.", "R<sub>h</sub> releases carbon into the atmosphere and thus increases the atmospheric CO<sub>2</sub> concentration." };
	public static final String[] SOC = {"SOC",  "Soil organic carbon", "SOC is present as soil organic matter, consisting of plant and animal residues at various stages of decomposition, cells and tissues of soil organisms, and substances synthesized by soil organisms.", "Plants and animals residues, Soil microbes.", "SOC is vital to soil capacity to maintain nutrients. On the other hand, SOC is a big carbon pool. The increasing decomposition rate caused by global warming may considerably affect the atmospheric CO<sub>2</sub> content." };
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