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
 *         Main server thread that wakes up to check if any ASCII files have been added.
 */

package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import org.pmw.tinylog.Logger;

import parser.AsciiToCsv;
import utils.FileLocations;
import utils.MapCompoundType;
import utils.MapProperties;
import utils.MapRegion;

public class EarthModellingDaemon {

	public static final Long TIME_TO_SLEEP = 60000L; // 1 minute before this daemon wakes up again.
	public static AsciiToCsv asciiParser;
	private static ConvertedSet convertedSet;

	public static void main(String[] args) throws IOException, InterruptedException {
		File asciiInputDir = new File(FileLocations.ASCII_INPUT_DIRECTORY_LOCATION);
		convertedSet = new ConvertedSet();
		asciiParser = new AsciiToCsv();

		Logger.info("Server daemon is starting up...");

		// Create required temp directories if they don't exist.
		File csvOutputDir = new File(FileLocations.CSV_OUTPUT_DIRECTORY_LOCATION);
		File tempOutputDir = new File(FileLocations.TEMP_WORKING_DIRECTORY_LOCATION);
		csvOutputDir.mkdir();
		tempOutputDir.mkdir();

		while (true) {
			while (asciiInputDir.list().length > 0) {
				String firstAsciiFileName = asciiInputDir.list()[0];
				String firstAsciiFileLocation = asciiInputDir.getAbsolutePath() + "\\" + firstAsciiFileName;
				try {
					createMap(new File(firstAsciiFileLocation), parseMapProperties(firstAsciiFileName));
				} catch (Exception e) {
					Logger.error("Error parsing {} in default directory: {}", firstAsciiFileName, e);
				}
			}
			Thread.sleep(TIME_TO_SLEEP);
		}
	}

	/**
	 * Helper to parse map properties based on file naming convention Dr. Lu uses. Only intended to be used as a failsafe or for testing.
	 * 
	 * @param s
	 *           The string that represents the filename. If this string is not formatted, this method will throw some kind of exception.
	 * @return A MapProperties object that represents this map's properties.
	 * @throws IllegalAccessException
	 *            Probably means that we were not able to find a match for MapCompoundType.
	 */
	private static MapProperties parseMapProperties(String s) throws IllegalAccessException {
		int currentIndex = 0;

		MapCompoundType compound = null;
		if (s.substring(currentIndex, 3).toLowerCase().equals("ch4")) {
			compound = MapCompoundType.CH4;
			currentIndex = 4;
		} else if (s.substring(currentIndex, 3).toLowerCase().equals("co2")) {
			compound = MapCompoundType.CO2;
			currentIndex = 4;
		}
		
		MapRegion region = MapRegion.GLOBAL; // Default to global.

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

		return runExecutable(FileLocations.PYTHON_EXECUTABLE_BINARY_LOCATION, arguments);
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
				while ((line = input.readLine()) != null) {
					Logger.info(line);
					result.add(line);
				}
			} catch (IOException e) {
				Logger.error(e);
				result.add("IOEXCEPTION THROWN!");
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
	 */
	private static void deleteFile(String fileLocation) {
		deleteFile(new File(fileLocation));
	}

	/**
	 * Deletes the specified file.
	 * 
	 * @param file
	 *           The file that you wish to delete.
	 */
	private static void deleteFile(File f) {
		if (f.delete())
			Logger.info("File {} is deleted!", f.getName());
		else
			Logger.error("Delete operation on {} failed!", f.getName()); // SEVERE, shouldn't happen.
	}

	/**
	 *
	 * Converts an ASCII file to a CSV file via AsciiToCsv.java
	 * 
	 * @param asciiFile
	 *           The absolute file path of the ascii.txt file on the disk.
	 * @return A File reference to the newly created CSV file.
	 * @throws IOException
	 *            Can't find the file at the specified location!
	 */
	private static File convertAsciiToCsv(File asciiFile) throws IOException {
		Logger.info("Converting file: {} to CSV", asciiFile);
		File f = asciiParser.parseToCsv(asciiFile);
		Logger.info("File converted to CSV!");

		return f;
	}

	public static synchronized boolean removeExistingMap(MapProperties properties) {
		// TODO
		// If exists in convertedSet, stop map, remove from GIS server.
		// Remove from convertedSet.
		// Remove any associated files.
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

	private static synchronized boolean createMap(File asciiFile, MapProperties properties) throws IOException, InterruptedException {

		// Check against converted set.
		if (!convertedSet.add(properties)) {
			Logger.warn("The file {} has already been converted!", asciiFile.getName());
			return false;
		}

		File csvFile = convertAsciiToCsv(asciiFile);

		String[] arguments = { FileLocations.CSV_OUTPUT_DIRECTORY_LOCATION, FileLocations.CREATED_GDBS_OUTPUT_DIRECTORY_LOCATION, csvFile.getName(), FileLocations.CSV_TABLES_OUTPUT_DIRECTORY_LOCATION };
		runPythonScript(FileLocations.CSV_TO_GEODATABASE_SCRIPT_LOCATION, arguments);

		// TODO
		// OTHER PYTHON SCRIPTS.
		// NOTE: FOR PURPOSES OF DEBUGGING, I THINK WE WANT ERRORS TO BE THROWN. IT WOULD ULTIMATELY BE UP TO CALLING OBJECTS TO HANDLE/LOG ERRORS.

		// Delete files.
		deleteFile(asciiFile);

		return true;
	}
}
