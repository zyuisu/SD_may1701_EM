/*
 * 
 * Copyright (C) 2017 Anish Kunduru and Eli Devine
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
 *         This program generates a new JavaScript file that handles loading our map's HTML. This class is not very efficiently written, but runs fast enough that it shouldn't be a concern.
 */

package parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import main.ConvertedSet;
import utils.FileLocations;
import utils.MapCompoundType;
import utils.MapRegionType;

public class JavaScriptGenerator {

	public ConvertedSet convertedSet;

	/**
	 * Dynamically generates the HTML file based on values saved in ConvertedSet. It is expected that this method will be called upon each map generation/removal. NOTE: This method relies on the accuracy of the values stored in ConvertedSet.
	 * 
	 * @throws IOException
	 *            Had an issue creating the temp file or replacing the existing file with the temp file.
	 * @param set
	 *           The ConvertedSet that represents the maps that are already being hosted by the map server.
	 */
	public JavaScriptGenerator(ConvertedSet set) throws IOException {
		convertedSet = set;

		File temp = new File(FileLocations.TEMP_WORKING_DIRECTORY_LOCATION + "temp.html");
		PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(temp)));

		StringBuilder strBuff = new StringBuilder();

		// Get dropdown elements from DOM.
		strBuff.append("function loadList(){ var regionList = document.getElementById('region'); var compoundList = document.getElementById('compound'); var yearList = document.getElementById('year'); var monthList = document.getElementById('month'); var loadMapBtn = document.getElementById('loadMapBtn'); ");

		// Generate event listeners.
		allocateRegionList(strBuff);
		generateRegionEventListener(strBuff);
		generateCompoundEventListener(strBuff);
		generateYearEventListener(strBuff);
		/*
		 * Note about above helper methods from Anish: Honestly, the code to generate the JS is just plain bad. However, it runs quickly, works, and will likely never be expanded upon, so I have no incentive to refactor it. My assumption is if the number of maps ever gets sufficiently large, we would switch to querying a database.
		 */

		// Add button listener.
		strBuff.append(
				"loadMapBtn.addEventListener('click', function(){ require([ 'esri/Map', 'esri/views/SceneView', 'esri/layers/MapImageLayer', 'esri/widgets/Legend', 'dojo/domReady!', 'dojo/on', 'dojo/dom', ], function( Map, SceneView, MapImageLayer, Legend, domReady, on, dom) { var region = dom.byId('region'); var comp = dom.byId('compound'); var year = dom.byId('year'); var month = dom.byId('month'); var selectMonth; if (month.options[month.selectedIndex].text == 'Choose a month.'){ selectMonth = 'm-1'; } else{ selectMonth = month.value; } if (selectMonth != 'm-1') { new_url = 'http://proj-se491.cs.iastate.edu:6080/arcgis/rest/services/EarthModelingTest/' + region.value + compound.value + 'y' + year.value + 'm' + selectMonth + '/MapServer' } else { new_url = 'http://proj-se491.cs.iastate.edu:6080/arcgis/rest/services/EarthModelingTest/' + region.value + compound.value + 'y' + year.value + selectMonth + '/MapServer' } var url = new_url; var lyr = new MapImageLayer({ url: url, opacity: 0.75 }); var map = new Map({ basemap: 'oceans', layers: [lyr] }); var view = new SceneView({ container: 'viewDiv', map: map }); var monthTitle = month.options[month.selectedIndex].text; var legendTitle; if (monthTitle == 'Choose a month.') { legendTitle = comp.options[comp.selectedIndex].text + ' ' + year.options[year.selectedIndex].text; } else { legendTitle = month.options[month.selectedIndex].text + ' ' + year.options[year.selectedIndex].text; } view.then(function() { var legend = new Legend({ view: view, layerInfos: [{ layer: lyr, title: legendTitle }] }); view.ui.add(legend, 'bottom-right'); }); lyr.then(function() { view.goTo(lyr.fullExtent); }); }); }); }");

		// Output finalized JS.
		output.write(strBuff.toString());
		output.flush();
		output.close();
		Files.copy(temp.toPath(), new File(FileLocations.JAVASCRIPT_FILE_LOCATION).toPath(), StandardCopyOption.REPLACE_EXISTING);
		temp.delete();
	}

	/**
	 * Helper to allocate values in the regionList.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the region values should be appended to.
	 */
	private void allocateRegionList(StringBuilder strBuff) {
		for (MapRegionType mr : MapRegionType.values()) {
			strBuff.append("regionList[regionList.length] = new Option(");
			strBuff.append("'");
			strBuff.append(mr.name());
			strBuff.append("','");
			strBuff.append(mr.name());
			strBuff.append("'); ");
		}
	}

	/**
	 * Helper to generate the region event listener and allocate values in compoundList.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the event listener should be appended to.
	 */
	private void generateRegionEventListener(StringBuilder strBuff) {
		strBuff.append("regionList.addEventListener('click', function() {");
		ArrayList<String> regionArrays = generateRegionArrays(strBuff);

		// Clear arrays for reallocation.
		strBuff.append("for (var i = compoundList.length-1; i > 0; i--){ compoundList[i] = null; }");
		strBuff.append("for (var i = yearList.length-1; i > 0; i--){ yearList[i] = null; }");
		strBuff.append("for (var i = monthList.length-1; i > 0; i--){ monthList[i] = null; }");

		// Hide month upon change.
		strBuff.append("monthList.style.display = 'none'; ");

		// To dynamically allocate values in compoundList.
		strBuff.append("var region = regionList.options[regionList.selectedIndex].text; switch(region){ default: break; ");
		int index = 0;
		for (MapRegionType mr : MapRegionType.values()) {
			strBuff.append("case '");
			strBuff.append(mr.name());
			strBuff.append("': for (var i = 0; i < ");
			strBuff.append(regionArrays.get(index));
			strBuff.append(".length; ++i){");
			strBuff.append("compoundList[i+1] = new Option(");
			strBuff.append(regionArrays.get(index));
			strBuff.append("[i], ");
			strBuff.append(regionArrays.get(index));
			strBuff.append("[i]); ");
			strBuff.append("} break; ");

			index++;
		}
		strBuff.append(" }");
		strBuff.append("}); ");
	}

	/**
	 * Helper to generate the region arrays.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the region arrays should be appended to.
	 * @return An ArrayList of the names of the arrays.
	 */
	private ArrayList<String> generateRegionArrays(StringBuilder strBuff) {
		ArrayList<String> arrayNames = new ArrayList<String>();

		for (MapRegionType mr : MapRegionType.values()) {
			String s = mr.name() + "Arr";
			arrayNames.add(s);

			strBuff.append("var ");
			strBuff.append(s);
			strBuff.append(" = [");

			// Allocate valid compounds per MapRegion.
			MapCompoundType[] compounds = convertedSet.getPossibleMapCompounds(mr);
			for (MapCompoundType c : compounds) {
				strBuff.append("'");
				strBuff.append(c.name());
				strBuff.append("'");
				if (c != compounds[compounds.length - 1])
					strBuff.append(", ");
			}
			strBuff.append("]; ");
		}

		return arrayNames;
	}

	/**
	 * Helper to generate the compound event listener and allocate values in yearList.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the event listener should be appended to.
	 */
	private void generateCompoundEventListener(StringBuilder strBuff) {
		strBuff.append("compoundList.addEventListener('click', function() {");
		ArrayList<String> compoundArrays = generateCompoundArrays(strBuff);

		// Clear array for reallocation.
		strBuff.append("for (var i = yearList.length-1; i > 0; i--){ yearList[i] = null; }");
		strBuff.append("for (var i = monthList.length-1; i > 0; i--){ monthList[i] = null; }");

		// Hide month upon change.
		strBuff.append("monthList.style.display = 'none'; ");

		// To dynamically allocate values in yearList.
		strBuff.append("var region = regionList.options[regionList.selectedIndex].text; var compound = compoundList.options[compoundList.selectedIndex].text; var regionCompound = region.concat(compound); switch(regionCompound){ default: break; ");
		int index = 0;
		for (String arrName : compoundArrays) {
			strBuff.append("case '");
			String caseName = arrName.substring(0, arrName.length() - 3);
			strBuff.append(caseName);
			strBuff.append("': for (var i = 0; i < ");
			strBuff.append(compoundArrays.get(index));
			strBuff.append(".length; ++i){");
			strBuff.append("yearList[i+1] = new Option(");
			strBuff.append(compoundArrays.get(index));
			strBuff.append("[i], ");
			strBuff.append(compoundArrays.get(index));
			strBuff.append("[i]); ");
			strBuff.append("} break; ");

			index++;
		}
		strBuff.append(" }");
		strBuff.append("}); ");
	}

	/**
	 * Helper to generate the region-compound arrays.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the region-compound arrays should be appended to.
	 * @return An ArrayList of the names of the arrays.
	 */
	private ArrayList<String> generateCompoundArrays(StringBuilder strBuff) {
		ArrayList<String> arrayNames = new ArrayList<String>();

		for (MapRegionType mr : MapRegionType.values())
			for (MapCompoundType mc : MapCompoundType.values()) {
				String s = mr.name() + mc.name() + "Arr";
				arrayNames.add(s);

				strBuff.append("var ");
				strBuff.append(s);
				strBuff.append(" = [");

				// Allocate valid years per MapRegion and MapCompound.
				int[] years = convertedSet.getPossibleYears(mr, mc);
				for (int y : years) {
					strBuff.append("'");
					strBuff.append(y);
					strBuff.append("'");
					if (y != years[years.length - 1])
						strBuff.append(", ");
				}
				strBuff.append("]; ");
			}

		return arrayNames;
	}

	/**
	 * Helper to generate the year event listener and allocate values in monthList.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the event listener should be appended to.
	 */
	private void generateYearEventListener(StringBuilder strBuff) {
		strBuff.append("yearList.addEventListener('click', function() {");
		ArrayList<String> yearArrays = generateMonthArrays(strBuff);

		// Clear array for reallocation.
		strBuff.append("for (var i = monthList.length-1; i > 0; i--){ monthList[i] = null; }");

		// Hide month upon change.
		strBuff.append("monthList.style.display = 'none'; ");

		// To dynamically allocate values in monthList.
		strBuff.append("var region = regionList.options[regionList.selectedIndex].text; var compound = compoundList.options[compoundList.selectedIndex].text; var year = yearList.options[yearList.selectedIndex].text; var regionCompoundYear = region.concat(compound).concat(year); switch(regionCompoundYear){ default: break; ");
		int index = 0;
		for (String arrName : yearArrays) {
			strBuff.append("case '");
			String caseName = arrName.substring(0, arrName.length() - 3);
			strBuff.append(caseName);
			strBuff.append("': for (var i = 0; i < ");
			strBuff.append(yearArrays.get(index));
			strBuff.append(".length; ++i){ var monthName;");
			strBuff.append("switch (");
			strBuff.append(yearArrays.get(index));
			strBuff.append(
					"[i]){ default: break; case '0': monthName = 'January'; break; case '1': monthName = 'February'; break; case '2': monthName = 'March'; break; case '3': monthName = 'April'; break; case '4': monthName = 'May'; break; case '5': monthName = 'June'; break; case '6': monthName = 'July'; break; case '7': monthName = 'August'; break; case '8': monthName = 'September'; break; case '9': monthName = 'October'; break; case '10': monthName = 'November'; break; case '11': monthName = 'December'; break; ");
			strBuff.append("} monthList[i+1] = new Option(");
			strBuff.append("monthName,");
			strBuff.append(yearArrays.get(index));
			strBuff.append("[i]); ");
			strBuff.append("} if (");
			strBuff.append(arrName);
			strBuff.append(".length > 0){ monthList.style.display = 'inline'; } break; ");

			index++;
		}
		strBuff.append(" }");
		strBuff.append("}); ");
	}

	/**
	 * Helper to generate the region-compound-year arrays.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the region-compound arrays should be appended to.
	 * @return An ArrayList of the names of the arrays.
	 */
	private ArrayList<String> generateMonthArrays(StringBuilder strBuff) {
		ArrayList<String> arrayNames = new ArrayList<String>();

		for (MapRegionType mr : MapRegionType.values())
			for (MapCompoundType mc : MapCompoundType.values()) {
				int[] years = convertedSet.getPossibleYears(mr, mc);
				for (int y : years) {
					String s = mr.name() + mc.name() + y + "Arr";
					arrayNames.add(s);

					strBuff.append("var ");
					strBuff.append(s);
					strBuff.append(" = [");

					// Allocate valid months per MapRegion, MapCompound, and year.
					int[] months = convertedSet.getPossibleMonths(mr, mc, y);
					for (int m : months) {
						strBuff.append("'");
						strBuff.append(m);
						strBuff.append("'");
						if (m != months[months.length - 1])
							strBuff.append(", ");
					}
					strBuff.append("]; ");
				}
			}

		return arrayNames;
	}
}
