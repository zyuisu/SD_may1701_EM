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
 *         Main server thread that handles all generation of maps and related elements.
 */

package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.pmw.tinylog.Logger;

import networking.ClientServer;
import networking.ServerInformation;
import parser.AsciiToCsv;
import utils.FileLocations;
import utils.MapCompoundType;
import utils.MapProperties;
import utils.MapRegionType;
import utils.ReferenceScales;

public class EarthModellingDaemon {

	public static final Long TIME_TO_SLEEP = 60000L; // 1 minute before this daemon wakes up again.
	private static AsciiToCsv asciiParser;
	private static ConvertedSet convertedSet;

	public static void main(String[] args) throws IOException, IllegalAccessException {
		File asciiInputDir = new File(FileLocations.ASCII_INPUT_DIRECTORY_LOCATION);
		convertedSet = new ConvertedSet();
		asciiParser = new AsciiToCsv();

		Logger.info("Server daemon is starting up...");

		// Create required temp directories if they don't exist.
		File csvOutputDir = new File(FileLocations.CSV_OUTPUT_DIRECTORY_LOCATION);
		File tempOutputDir = new File(FileLocations.TEMP_WORKING_DIRECTORY_LOCATION);
		csvOutputDir.mkdir();
		tempOutputDir.mkdir();

		/*
		 * // DEBUG // To test the generator, input some values, if needed, into convertedSet. MapRegionType[] regions = MapRegionType.values(); MapCompoundType[] compounds = MapCompoundType.values(); int minYear = 1980; int maxYear = 2015; int maxMonth = 11; Random rand = new Random();
		 * 
		 * for (int i = 0; i < 100; i++) { MapRegionType r = regions[rand.nextInt(regions.length)]; MapCompoundType c = compounds[rand.nextInt(compounds.length)]; int y = minYear + rand.nextInt(maxYear - minYear); int m = c == MapCompoundType.CH4 ? rand.nextInt(maxMonth) : -1;
		 * 
		 * if (m == -1) convertedSet.add(new MapProperties(r, c, y)); else convertedSet.add(new MapProperties(r, c, y, m)); }
		 * 
		 * generateNewJavaScript();
		 */
		/////////////////////////////////

		Logger.info("Starting VEMS ClientServer.");
		ClientServer cs = new ClientServer(ServerInformation.SERVER_PORT, FileLocations.KEYSTORE_FILE_LOCATION, "password");
		cs.start();

		// To gracefully shut things down:
		// cs.end();

		while (true)
			try {
				Thread.sleep(TIME_TO_SLEEP);
			} catch (InterruptedException e) {
				// Do nothing, because map processing is likely happening right now.
			}
	}

	/**
	 * Helper to parse map properties based on file naming convention Dr. Lu uses. Only intended to be used for testing, as generated MapProperties are not correct and are dependent on file name formatting.
	 * 
	 * @param s
	 *           The string that represents the filename. If this string is not formatted, this method will throw some kind of exception.
	 * @return A MapProperties object that represents this map's properties.
	 * @throws IllegalAccessException
	 *            Probably means that we were not able to find a match for MapCompoundType.
	 */
	@Deprecated
	private static MapProperties parseMapProperties(String s) throws IllegalAccessException {
		int currentIndex = 0;

		MapCompoundType compound = null;
		if (s.substring(currentIndex, 3).toLowerCase().equals("ch4")) {
			compound = MapCompoundType.CH4;
			currentIndex = 4;
		} else if (s.substring(currentIndex, 3).toLowerCase().equals("co2"))
			// compound = MapCompoundType.CO2;
			currentIndex = 4;

		MapRegionType region = MapRegionType.GLOBAL; // Default to global.

		int year = Integer.parseInt(s.substring(currentIndex, currentIndex + 4));
		currentIndex += 5;

		int month = Integer.parseInt(s.substring(currentIndex).replace(".txt", ""));

		return new MapProperties(region, compound, year, month);
	}

	/**
	 * Overloaded helper if you wish to call a script with no arguments.
	 * 
	 * @param scriptLocation
	 *           The location of the Python script on the disk.
	 * @return An ArrayList of the standard output of the script.
	 * @throws IOException
	 *            Can't find the script at the specified location!
	 * @throws InterruptedException
	 *            Interrupt was encountered before the process could finish executing!
	 */
	private static ArrayList<String> runPythonScript(String scriptLocation) throws IOException, InterruptedException {
		return runPythonScript(scriptLocation, null);
	}

	/**
	 * Runs a Python script with the given arguments.
	 * 
	 * @param scriptLocation
	 *           The location of the *.py script on the disk. Example: C:\\Python\\awesome.py
	 * @param arguments
	 *           A tokenized array of arguments, if the program has any.
	 * @return An ArrayList of the standard output of the script.
	 * @throws IOException
	 *            Can't find the script at the specified location!
	 * @throws InterruptedException
	 *            Interrupt was encountered before the process could finish executing!
	 */
	private static ArrayList<String> runPythonScript(String scriptLocation, String[] arguments) throws IOException, InterruptedException {
		String[] strArr = new String[arguments.length + 1];
		strArr[0] = scriptLocation;
		for (int i = 0; i < arguments.length; i++)
			strArr[i + 1] = arguments[i];

		return runExecutable(FileLocations.PYTHON_EXECUTABLE_BINARY_LOCATION, strArr);
	}

	/**
	 * Overloaded helper if you wish to call a runnable program with no arguments.
	 * 
	 * @param exeLocation
	 *           The absolute file location of the runnable on the disk.
	 * @return An ArrayList of the standard output of the script.
	 * @throws IOException
	 *            Can't find the script at the specified location!
	 * @throws InterruptedException
	 *            Interrupt was encountered before the process could finish executing!
	 */
	private static ArrayList<String> runExecutable(String exeLocation) throws IOException, InterruptedException {
		return runExecutable(exeLocation, null);
	}

	/**
	 * Runs a an executable file with the given arguments.
	 * 
	 * @param exeLocation
	 *           The absolute file location of the runnable on the disk.
	 * @param arguments
	 *           A tokenized array of arguments, if the program has any.
	 * @return An ArrayList of the standard output of the script.
	 * @throws IOException
	 *            Can't find the script at the specified location!
	 * @throws InterruptedException
	 *            Interrupt was encountered before the process could finish executing!
	 */
	private static ArrayList<String> runExecutable(String exeLocation, String[] arguments) throws IOException, InterruptedException {
		if (arguments == null)
			Logger.info("Running executable: {} ", exeLocation);
		else
			Logger.info("Running executable: {} with arguments: {}", exeLocation, Arrays.toString(arguments));

		ProcessBuilder builder = new ProcessBuilder();

		ArrayList<String> commands = new ArrayList<String>();
		commands.add(exeLocation);
		if (arguments != null)
			for (String s : arguments)
				commands.add(s);
		builder.command(commands);

		builder.redirectErrorStream(true);

		final Process process = builder.start();
		return waitForProcess(process);
	}

	/**
	 * Creates a new thread to run the Process and blocks this.Thread until it is complete.
	 * 
	 * @param process
	 *           The process that you wish to execute.
	 * @return An ArrayList of the standard output of the process.
	 * @throws InterruptedException
	 *            Interrupt was encountered before the process could finish executing!
	 */
	private static ArrayList<String> waitForProcess(final Process process) throws InterruptedException {
		ArrayList<String> result = new ArrayList<String>();

		// Don't crash this thread if everything fails miserably.
		Runnable run = () -> {
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			try {
				while ((line = input.readLine()) != null)
					result.add(line);
			} catch (Exception e) {
				Logger.error(e);
				result.add("EXCEPTION THROWN!");
			}
		};

		new Thread(run).start();
		process.waitFor();

		Logger.info("Done running script.");

		return result;
	}

	/**
	 * Deletes the file at the specified location.
	 * 
	 * @param fileLocation
	 *           The absolute file path of the file on the disk.
	 * @return true if the file was successfully deleted; false otherwise.
	 */
	private static boolean deleteFile(String fileLocation) {
		return deleteFile(new File(fileLocation));
	}

	/**
	 * Deletes the specified file.
	 * 
	 * @param f
	 *           The file that you wish to delete.
	 * @return true if the file was successfully deleted; false otherwise.
	 */
	private static boolean deleteFile(File f) {
		if (f == null || f.isDirectory())
			return false;

		if (f.delete()) {
			Logger.info("File {} is deleted!", f.getName());
			return true;
		} else
			Logger.error("Delete operation on {} failed!", f.getName()); // SEVERE, shouldn't happen.
		return false;
	}

	/**
	 * Deletes a folder and all its internal files and folders.
	 * 
	 * @param fileLocation
	 *           The absolute file path of the folder on the disk.
	 * @return true if the folder was successfully deleted; false otherwise.
	 */
	private static boolean deleteFolder(String fileLocation) {
		return deleteFolder(new File(fileLocation));
	}

	/**
	 * Deletes a filer and all its internal files and folders.
	 * 
	 * @param f
	 *           The folder that you wish to delete.
	 * @return true if the folder was successfully deleted; false otherwise.
	 */
	private static boolean deleteFolder(File f) {
		if (f == null || !f.isDirectory())
			return false;

		for (File file : f.listFiles()) {
			if (file.isDirectory())
				if (!deleteFolder(file))
					return false;

			if (!deleteFile(file))
				return false;
		}

		return f.delete();
	}

	/**
	 *
	 * Converts an ASCII file to a CSV file via AsciiToCsv.java
	 * 
	 * @param asciiFile
	 *           The absolute file path of the ascii.txt file on the disk.
	 * @return A File reference to the newly created CSV file. Null is returned in the event the parser had an issue parsing the file.
	 * @throws IOException
	 *            Can't find the file at the specified location!
	 */
	private static File convertAsciiToCsv(File asciiFile) throws IOException {
		Logger.info("Converting file: {} to CSV", asciiFile);
		File f = asciiParser.parseToCsv(asciiFile);
		Logger.info("File converted to CSV!");

		return f;
	}

	/**
	 * Removes a map from the ArcGIS server by executing a commandline argument.
	 * 
	 * @param properties
	 *           The MapProperties that represents what needs to be deleted.
	 * @return true if the map was successfully deleted; false otherwise.
	 * @throws IOException
	 *            Means that FileLocations.ARCSERVER_MANAGE_SERVICE_FILE_LOCATION couldn't be located.
	 * @throws InterruptedException
	 *            Probably means there was an issue while running the commandline arguments.
	 */
	public static synchronized boolean removeMapFromServer(MapProperties properties) throws IOException, InterruptedException {
		if (!removeLocalMapFiles(properties))
			return false;

		String auth[] = validateUser();

		// required arguments for the delete from server command using executable python script
		// python.exe "C:\Program Files\ArcGIS\Server\tools\admin\manageservice.py" -u username -p password -s https://proj-se491.iastate.edu:6443 -n EarthModelingTest/service_name -o delete
		String arguments[] = { "-u", auth[0], "-p", auth[1], "-s", "https://proj-se491.iastate.edu:6443", "-n", "EarthModelingTest/" + properties.toString(), "-o", "delete" };
		runPythonScript(FileLocations.ARCSERVER_MANAGE_SERVICE_FILE_LOCATION, arguments);

		convertedSet.remove(properties);
		generateNewJavaScript();

		return true;
	}

	/**
	 * Removes map files stored locally on the server.
	 * 
	 * @param properties
	 *           The MapProperites that represents what needs to be deleted.
	 * @return true if the files were found and deleted; false otherwise.
	 */
	public static synchronized boolean removeLocalMapFiles(MapProperties properties) {
		if (convertedSet.contains(properties)) {

			// For ESRI, since some of their functions don't accept negative values as file arguments.
			String nice = properties.toString().replace("-1", "_1");

			// Delete corresponding mxd in maps_publishing
			if (!deleteFile(FileLocations.ABS_MAPS_PUBLISHING_DIRECTORY_LOCATION + nice + ".mxd"))
				return false;

			// Delete service definition in temp_publishing
			if (!deleteFile(FileLocations.ABS_TEMP_PUBLISHING_FILES_DIRECTORY_LOCATION + properties.toString() + ".sd"))
				return false;

			// Delete table files from tables folder (.dbf, .dbf.xml, .cpg)
			if (!deleteFile(FileLocations.ABS_CSV_TABLES_OUTPUT_DIRECTORY_LOCATION + nice + ".dbf"))
				return false;
			if (!deleteFile(FileLocations.ABS_CSV_TABLES_OUTPUT_DIRECTORY_LOCATION + nice + ".dbf.xml"))
				return false;
			if (!deleteFile(FileLocations.ABS_CSV_TABLES_OUTPUT_DIRECTORY_LOCATION + nice + ".cpg"))
				return false;

			// Delete .lyr from created_layers
			if (!deleteFile(FileLocations.ABS_CREATED_LAYERS_DIRECTORY_LOCATION + nice + ".lyr"))
				return false;

			// Delete gdb from auto_gdbs
			if (!deleteFolder(FileLocations.ABS_AUTO_GDBS_OUTPUT_DIRECTORY_LOCATION + nice + ".gdb"))
				return false;

			return true;
		}

		return false;
	}

	/**
	 * Creates a map by calling the correct parsers and Python script(s).
	 * 
	 * @param asciiFile
	 *           A byte array representing the ASCII file that you wish to generate a map from.
	 * @param properties
	 *           The map's properties as defined in MapProperties.
	 * @return true if the map was successfully created; false if it wasn't.
	 * @throws IOException
	 *            There was an error creating or reading from a temporary file/folder.
	 * @throws InterruptedException
	 *            Probably means one of the intermediary Python scipts were cut short before they could complete execution.
	 */
	public static synchronized boolean createMap(byte[] asciiFile, MapProperties properties) throws IOException, InterruptedException {
		File file = new File(FileLocations.TEMP_WORKING_DIRECTORY_LOCATION + properties.toString() + ".txt");
		Files.write(file.toPath(), asciiFile);

		return createMap(file, properties);
	}

	/**
	 * Creates a map by calling the correct parsers and Python script(s).
	 * 
	 * @param asciiFile
	 *           A File (linked to something on the local disk) representing the ASCII file that you wish to generate a map from.
	 * @param properties
	 *           The map's properties as defined in MapProperties.
	 * @return true if the map was successfully created; false if it wasn't.
	 * @throws IOException
	 *            There was an error creating or reading from a temporary file/folder.
	 * @throws InterruptedException
	 *            Probably means one of the intermediary Python scrips were cut short before they could complete execution.
	 */
	private static synchronized boolean createMap(File asciiFile, MapProperties properties) throws IOException, InterruptedException {

		// Check against converted set.
		if (!convertedSet.add(properties)) {
			Logger.warn("The file {} has already been converted!", asciiFile.getName());
			return false;
		}

		File csvFile = convertAsciiToCsv(asciiFile);
		if (csvFile == null) {
			Logger.error("File generated became null");
			return false;
		}

		String auth[] = validateUser();
		String template = properties.getMapRegion().toString() + properties.getMapCompoundType().toString();
		String referenceScale;
		try{
		ReferenceScales rs = new ReferenceScales();
		referenceScale = rs.getReferenceScale(properties.getMapRegion());
		}
		catch(Exception e){
			Logger.error("Error when getting Reference Scale. Check ReferenceScale Class.");
			return false;
		}
		
		String[] arguments = { FileLocations.ABS_CSV_OUTPUT_DIRECTORY_LOCATION, properties.toString(), FileLocations.CURRENT_WORKING_DIRECTORY_LOCATION, FileLocations.MAP_TEMPLATES_DIRECTORY_LOCATION, FileLocations.MAPS_PUBLISHING_DIRECTORY_LOCATION, FileLocations.TEMP_PUBLISHING_FILES_DIRECTORY_LOCATION, template, FileLocations.BLANK_MAP_FILE_LOCATION,
				FileLocations.CSV_TABLES_OUTPUT_DIRECTORY_LOCATION, FileLocations.CREATED_GDBS_OUTPUT_DIRECTORY_LOCATION, FileLocations.CREATED_LAYERS_DIRECTORY_LOCATION, auth[0], auth[1], referenceScale };

		ArrayList<String> al = runPythonScript(FileLocations.PUBLISH_MAP_SCRIPT_LOCATION, arguments);
		logExceptions(al);

		String[] arguments2 = { properties.toString(), auth[0], auth[1] };
		al = runPythonScript(FileLocations.PUBLISHING_PARAMS_SCRIPT_LOCATION, arguments2);
		logExceptions(al);

		deleteFile(asciiFile);

		generateNewJavaScript();

		return true;
	}

	/**
	 * Search a list for any exception or error and log it and everything that comes after it.
	 * 
	 * @param al
	 *           An ArrayList that contains the output piped from an executable.
	 */
	private static void logExceptions(ArrayList<String> al) {
		boolean foundError = false;

		for (String s : al) {
			String temp = s.toLowerCase();
			if (!(temp.contains("-error-") || temp.contains("-errors-")) && (temp.contains("error") || temp.contains("exception") || temp.contains("errno")))
				foundError = true;

			if (foundError)
				Logger.error(s);
		}
	}

	/**
	 * Gets the username and password required to access the ArcGIS admin portal.
	 * 
	 * @return A String representing the username, password to access the server.
	 * @throws FileNotFoundException
	 *            If FileLocations.Server_AUTH_FILE_LOCATION isn't found.
	 */
	private static String[] validateUser() throws FileNotFoundException {

		// New buffered reader for getting local file
		BufferedReader buffR;
		buffR = new BufferedReader(new FileReader(FileLocations.SERVER_AUTH_FILE_LOCATION));

		// scan the file
		Scanner s = new Scanner(buffR);
		// Define authentication array for return array
		String[] auth = new String[2];
		// line counter
		int line = 0;

		// fill the array with the first two lines found in the document
		while (s.hasNext()) {
			auth[line] = s.nextLine().trim();
			line++;
		}

		s.close();
		return auth;
	}

	/**
	 * Dynamically generates the HTML file based on values saved in ConvertedSet. It is expected that this method will be called upon each map generation/removal. NOTE: This method relies on the accuracy of the values stored in ConvertedSet.
	 * 
	 * @throws IOException
	 *            Had an issue creating the temp file or replacing the existing file with the temp file.
	 */
	private static void generateNewJavaScript() throws IOException {
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
				"loadMapBtn.addEventListener('click', function(){ require([ 'esri/Map', 'esri/views/SceneView', 'esri/layers/MapImageLayer', 'esri/widgets/Legend', 'dojo/domReady!', 'dojo/on', 'dojo/dom', ], function( Map, SceneView, MapImageLayer, Legend, domReady, on, dom) { var region = dom.byId('region'); var comp = dom.byId('compound'); var year = dom.byId('year'); var month = dom.byId('month'); var selectMonth; if(month.options[month.selectedIndex].text == 'Choose a month.'){ selectMonth = 'm-1'; } else{ selectMonth = month.value; } new_url = 'http://proj-se491.cs.iastate.edu:6080/arcgis/rest/services/EarthModelingTest/' + region.value + compound.value + 'y' + year.value + selectMonth + '/MapServer' var url = new_url; var lyr = new MapImageLayer({ url: url, opacity: 0.75 }); var map = new Map({ basemap: 'oceans', layers: [lyr] }); var view = new SceneView({ container: 'viewDiv', map: map }); view.then(function() { var legend = new Legend({ view: view, layerInfos: [{ layer: lyr, title: month.options[month.selectedIndex].text + '  ' + year.options[year.selectedIndex].text }] }); view.ui.add(legend, 'bottom-right'); }); lyr.then(function() { view.goTo(lyr.fullExtent); }); }); }); }");

		// Output finalized JS.
		output.write(strBuff.toString());
		output.flush();
		output.close();
		Files.copy(temp.toPath(), new File(FileLocations.HTML_FILE_LOCATION).toPath(), StandardCopyOption.REPLACE_EXISTING);
		deleteFile(temp);
	}

	/**
	 * Helper to allocate values in the regionList.
	 * 
	 * @param strBuff
	 *           The StringBuilder upon which the region values should be appended to.
	 */
	private static void allocateRegionList(StringBuilder strBuff) {
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
	private static void generateRegionEventListener(StringBuilder strBuff) {
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
	private static ArrayList<String> generateRegionArrays(StringBuilder strBuff) {
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
	private static void generateCompoundEventListener(StringBuilder strBuff) {
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
	private static ArrayList<String> generateCompoundArrays(StringBuilder strBuff) {
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
	private static void generateYearEventListener(StringBuilder strBuff) {
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
					"[i]){ default: break; case '0': monthName = 'January'; case '1': monthName = 'February'; case '2': monthName = 'March'; case '3': monthName = 'April'; case '4': monthName = 'May'; case '5': monthName = 'June'; case '6': monthName = 'July'; case '7': monthName = 'August'; case '8': monthName = 'September'; case '9': monthName = 'October'; case '10': monthName = 'November'; case '11': monthName = 'December'; ");
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
	private static ArrayList<String> generateMonthArrays(StringBuilder strBuff) {
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
