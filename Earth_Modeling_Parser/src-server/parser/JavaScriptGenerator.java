/*
 * 
 * Copyright (C) 2017 Anish Kunduru, Kellen Johnson, and Eli Devine
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
import networking.ServerInformation;
import utils.CompoundDescriptions;
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
	 * @throws IllegalAccessException
	 *            Likely caused by an issue with the generateCompoundDescriptionEventListener().
	 * @throws IllegalArgumentException
	 *            Likely cause by an issue with the generateCompoundDescriptionEventListener().
	 */
	public JavaScriptGenerator(ConvertedSet set) throws IOException, IllegalArgumentException, IllegalAccessException {
		convertedSet = set;

		File temp = new File(FileLocations.TEMP_WORKING_DIRECTORY_LOCATION + "temp.html");
		PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(temp)));

		StringBuilder strBuff = new StringBuilder();

		// Get dropdown elements from DOM.
		strBuff.append(
				"function loadList(){ require(['esri/Map', 'esri/views/SceneView', 'esri/layers/MapImageLayer', 'esri/widgets/Legend', 'dojo/domReady!', 'dojo/on', 'dojo/dom', ],  function(Map, SceneView, MapImageLayer, Legend, domReady, on, dom) {var regionList = document.getElementById('region'); var compoundList = document.getElementById('compound'); var yearList = document.getElementById('year'); var monthList = document.getElementById('month'); var loadMapBtn = document.getElementById('loadMapBtn'); var legendCheck = document.getElementById('legendShow'); var explainBtn = document.getElementById('explain'); var PopupBtn = document.getElementById('Popup'); var helpBtn = document.getElementById('help'); var legend; var map = new Map({ basemap: 'oceans' }); var view = new SceneView({ container: 'viewDiv', map: map }); function popUp(reset){ if(reset == 0){ var popup = document.getElementById(\"Popup\"); popup.classList.toggle(\"show\"); $(\".popuptext\").show(); console.log(\"running popUp()\"); } } function replacePopupTextCompound(reset){ var popup = document.getElementById(\"Popup\"); if(reset == 1){ var compound = \"\";  } else{ var compoundList = document.getElementById(\"compound\"); var compound = compoundList.options[compoundList.selectedIndex].text; } var s; switch(compound){  default: s = 	\"<h2>Visualization of Earth's Modeling Systems</h2>1. Select a region.<br>2. Select a compound.<br>3. Select a year.<br>4. If applicable, select a month. Currently, only CH4 is updated monthly.<br>5. Click on the 'Load Map' button.<br><br>NOTE: Viewing freshly uploaded maps may require you to refresh your browser's cache. On most browsers, this can can be done by pressing the following buttons simultaneously: 'Ctrl+Shift+R'.\"; break; function populateList(list, arr) { for (var i = 0; i < arr.length; ++i) { list[i + 1] = new Option(arr[i], arr[i]); } } function clearList(list) { for (var i = list.length - 1; i > 0; i--) { list[i] = null; } } function populateMonthList(arr) { for (var i = 0; i < arr.length; ++i) { var monthName; switch (arr[i]) { default: break; case '0': monthName = 'January'; break; case '1': monthName = 'February'; break; case '2': monthName = 'March'; break; case '3': monthName = 'April'; break; case '4': monthName = 'May'; break; case '5': monthName = 'June'; break; case '6': monthName = 'July'; break; case '7': monthName = 'August'; break; case '8': monthName = 'September'; break; case '9': monthName = 'October'; break; case '10': monthName = 'November'; break; case '11': monthName = 'December'; break; } monthList[i + 1] = new Option(monthName, arr[i]); } if (arr.length > 0) { monthList.style.display = 'inline'; } }");

		// Generate event listeners.
		generateCompoundDescriptionEventListener(strBuff);
		allocateRegionList(strBuff);
		generateRegionEventListener(strBuff);
		generateCompoundEventListener(strBuff);
		generateYearEventListener(strBuff);
		/*
		 * Note about above helper methods from Anish: Honestly, the code to generate the JS is just plain bad. However, it runs quickly enough, works, and will likely never be expanded upon, so we have no incentive to refactor it. My assumption is if the number of maps ever gets sufficiently large, we would switch to querying a database.
		 */

		// Add button listener.
		strBuff.append(
				"loadMapBtn.addEventListener('click', function(){ require([ 'esri/Map', 'esri/views/SceneView', 'esri/layers/MapImageLayer', 'esri/widgets/Legend', 'dojo/domReady!', 'dojo/on', 'dojo/dom', ], function( Map, SceneView, MapImageLayer, Legend, domReady, on, dom) { var region = dom.byId('region'); var comp = dom.byId('compound'); var year = dom.byId('year'); var month = dom.byId('month'); var selectMonth; if (month.options[month.selectedIndex].text == 'Choose a month.'){ selectMonth = 'm-1'; } else{ selectMonth = month.value; } if (selectMonth != 'm-1') { new_url = '");
		strBuff.append(ServerInformation.ARCGIS_PUBLISH_URL);
		strBuff.append("' + region.value + compound.value + 'y' + year.value + 'm' + selectMonth + '/MapServer'; } else { new_url = '");
		strBuff.append(ServerInformation.ARCGIS_PUBLISH_URL);
		strBuff.append(
				"' + region.value + compound.value + 'y' + year.value + selectMonth + '/MapServer'; } var url = new_url; var request; if(window.XMLHttpRequest){ request = new XMLHttpRequest(); } else { request = new ActiveXObject('Microsoft.XMLHTTP'); } request.open('GET', url, false); request.send(); if (request.status !== 200) { alert('The server cannot find that map.'); } else { view.map.removeAll(); view.ui.remove(legend); var lyr = new MapImageLayer({ url: url, opacity: 0.75 }); view.map.add(lyr); var monthTitle = month.options[month.selectedIndex].text; var legendTitle; if (monthTitle == 'Choose a month.') { legendTitle = comp.options[comp.selectedIndex].text + ' ' + year.options[year.selectedIndex].text; } else { legendTitle = month.options[month.selectedIndex].text + ' ' + year.options[year.selectedIndex].text; } view.then(function() { legend = new Legend({ view: view, layerInfos: [{ layer: lyr, title: legendTitle }] }); view.ui.add(legend, 'bottom-right'); }); lyr.then(function() { view.goTo(lyr.fullExtent); }); } }); }); legendCheck.addEventListener('click', function(){ require(['esri/Map', 'esri/views/SceneView', 'esri/layers/MapImageLayer', 'esri/widgets/Legend', 'dojo/domReady!', 'dojo/on', 'dojo/dom', ], function(Map, SceneView, MapImageLayer, Legend, domReady, on, dom) { if(legendCheck.value === 'hide'){ view.then(function(){ view.ui.remove(legend); }); legendCheck.value = 'show'; } else { view.then(function(){ view.ui.add(legend, 'bottom-right'); }); legendCheck.value = 'hide'; } }); }); }); }");

		// Output finalized JS.
		output.write(strBuff.toString());
		output.flush();
		output.close();
		Files.copy(temp.toPath(), new File(FileLocations.JAVASCRIPT_FILE_LOCATION).toPath(), StandardCopyOption.REPLACE_EXISTING);
		temp.delete();
	}

	/**
	 * Helper to allocate the values in the compound descriptions.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the compound descriptions should be appended to.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private void generateCompoundDescriptionEventListener(StringBuilder strBuff) throws IllegalArgumentException, IllegalAccessException {

		CompoundDescriptions cd = new CompoundDescriptions();
		/*
		 * String CH4[] = cd.getCompoundDescription(MapCompoundType.CH4); String ET[] = cd.getCompoundDescription(MapCompoundType.ET); String LEACHNO3[] = cd.getCompoundDescription(MapCompoundType.LEACHNO3); String N2O[] = cd.getCompoundDescription(MapCompoundType.N2O); String NPP[] = cd.getCompoundDescription(MapCompoundType.NPP); String NUPTAKE[] =
		 * cd.getCompoundDescription(MapCompoundType.NUPTAKE); String RH[] = cd.getCompoundDescription(MapCompoundType.RH); String SOC[] = cd.getCompoundDescription(MapCompoundType.SOC);
		 * 
		 * String CH4[] = {"CH4", "Carbon TetraHydride", "Methane", "Warm's the atmosphere", "Global Warming!"}; strBuff.append("case \"" + CH4[0] + "\": s = \"<h2>CH<sub>4</sub> Explanation </h2><h3>Full Name:" + CH4[1] + "</h3><h3>Alias: " + CH4[2] + "</h3>What Does it Do?:" + CH4[3] + "<br>Summary: " + CH4[4] + "\" ; break;"); strBuff.append("");
		 * strBuff.append(""); strBuff.append(""); strBuff.append(""); strBuff.append(""); strBuff.append(""); strBuff.append("");
		 */

		for (MapCompoundType mc : MapCompoundType.values()) {
			String[] arr = cd.getCompoundDescription(mc);
			strBuff.append("case \"" + mc.name() + "\": s = \"<h2>CH<sub>4</sub> Explanation </h2><h3>Full Name:" + arr[0] + "</h3><h3>Alias: " + arr[1] + "</h3>What Does it Do?:" + arr[2] + "<br>Summary: " + arr[3] + "\" ; break;");
		}

		strBuff.append("} if(s === popup.innerHTML){ popUp(0);} else{ popup.innerHTML = s; popUp(1); } } helpBtn.addEventListener('click', function(){ replacePopupTextCompound(1); }); explainBtn.addEventListener('click', function(){ replacePopupTextCompound(0); });");
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
		strBuff.append("regionList.addEventListener('change', function() {");
		ArrayList<String> regionArrays = generateRegionArrays(strBuff);

		// Clear arrays for reallocation.
		strBuff.append("clearList(compoundList); clearList(yearList); clearList(monthList);");

		// Hide month upon change.
		strBuff.append("monthList.style.display = 'none'; ");

		// To dynamically allocate values in compoundList.
		strBuff.append("var region = regionList.options[regionList.selectedIndex].text; switch(region){ default: break; ");
		int index = 0;
		for (MapRegionType mr : MapRegionType.values()) {
			strBuff.append("case '");
			strBuff.append(mr.name());
			strBuff.append("': populateList(compoundList, ");
			strBuff.append(regionArrays.get(index));
			strBuff.append("); break; ");

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
		strBuff.append("compoundList.addEventListener('change', function() {");
		ArrayList<String> compoundArrays = generateCompoundArrays(strBuff);

		// Clear array for reallocation.
		strBuff.append("clearList(yearList); clearList(monthList);");

		// Hide month upon change.
		strBuff.append("monthList.style.display = 'none'; ");

		// To dynamically allocate values in yearList.
		strBuff.append("var region = regionList.options[regionList.selectedIndex].text; var compound = compoundList.options[compoundList.selectedIndex].text; var regionCompound = region.concat(compound); switch(regionCompound){ default: break; ");
		int index = 0;
		for (String arrName : compoundArrays) {
			strBuff.append("case '");
			String caseName = arrName.substring(0, arrName.length() - 3);
			strBuff.append(caseName);
			strBuff.append("': populateList(yearList, ");
			strBuff.append(compoundArrays.get(index));
			strBuff.append("); break; ");

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
		strBuff.append("yearList.addEventListener('change', function() {");
		ArrayList<String> yearArrays = generateMonthArrays(strBuff);

		// Clear array for reallocation.
		strBuff.append("clearList(monthList);");

		// Hide month upon change.
		strBuff.append("monthList.style.display = 'none'; ");

		// To dynamically allocate values in monthList.
		strBuff.append("var region = regionList.options[regionList.selectedIndex].text; var compound = compoundList.options[compoundList.selectedIndex].text; var year = yearList.options[yearList.selectedIndex].text; var regionCompoundYear = region.concat(compound).concat(year); switch(regionCompoundYear){ default: break; ");
		int index = 0;
		for (String arrName : yearArrays) {
			strBuff.append("case '");
			String caseName = arrName.substring(0, arrName.length() - 3);
			strBuff.append(caseName);
			strBuff.append("': populateMonthList(");
			strBuff.append(yearArrays.get(index));
			strBuff.append("); break; ");

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