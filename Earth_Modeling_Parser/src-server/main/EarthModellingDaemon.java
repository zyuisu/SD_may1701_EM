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
import java.util.ArrayList;
import java.util.Arrays;

import org.pmw.tinylog.Logger;

import networking.AsciiFileMessage;
import parser.AsciiToCsv;
import utils.FileLocations;
import utils.MapCompoundType;
import utils.MapProperties;

public class EarthModellingDaemon {

	public static final Long TIME_TO_SLEEP = 60000L; // 1 minute before this daemon wakes up again.
	public static AsciiToCsv asciiParser;
	private static ConvertedSet convertedSet;

	public static void main(String[] args) throws IOException, InterruptedException {
		File asciiInputDir = new File(FileLocations.ASCII_INPUT_DIRECTORY_LOCATION);
		convertedSet = new ConvertedSet();
		asciiParser = new AsciiToCsv();

		Logger.info("Server daemon is starting up...");

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

		MapCompoundType type = null;
		if (s.substring(currentIndex, 3).toLowerCase().equals("ch4")) {
			type = MapCompoundType.CH4;
			currentIndex = 4;
		} else if (s.substring(currentIndex, 3).toLowerCase().equals("co2")) {
			type = MapCompoundType.CO2;
			currentIndex = 4;
		}

		int year = Integer.parseInt(s.substring(currentIndex, currentIndex + 4));
		currentIndex += 5;

		int month = Integer.parseInt(s.substring(currentIndex).replace(".txt", ""));

		return new MapProperties(type, year, month);
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
		if (arguments == null)
			Logger.info("Running Python scipt: {} ", scriptLocation);
		else
			Logger.info("Running Python scipt: {} with arguments: {}", scriptLocation, Arrays.toString(arguments));

		ProcessBuilder builder = new ProcessBuilder();

		ArrayList<String> commands = new ArrayList<String>();
		commands.add(FileLocations.PYTHON_BINARY_LOCATION);
		commands.add(scriptLocation);
		if (arguments != null) {
			for (String s : arguments)
				commands.add(s);
		}
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
	 * @param file The file that you wish to delete.
	 */
	private static void deleteFile(File f)
	{
		if (f.delete())
			Logger.info("File is deleted!");
		else
			Logger.error("Delete operation failed!"); // SEVERE, shouldn't happen.
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
		// TO-DO
		// If exists in convertedSet, stop map, remove from GIS server.
		// Remove from convertedSet.
		// Remove any associated files.
		return false;
	}

	public static synchronized boolean createMap(byte[] asciiFile, MapProperties properties) {
		// TO-DO
		// Convert byte array to file and save it.
		// createMap(file, properties)
		return false;
	}

	private static synchronized boolean createMap(File asciiFile, MapProperties properties) {

		try {
			// Check against converted set.
			if (!convertedSet.add(properties)) {
				Logger.warn("The file {} has already been converted!", asciiFile.getName());
				return false;
			} else {
				convertAsciiToCsv(asciiFile);
			}

			// Call scripts

			// CSV to Geodatabase
			// NOTE: str contains the output of the python script in case you need it.
			// Otherwise, you can simply use runPythonScript(scriptToRun...
			// Anything returned in ArrayList str is always logged.
			/*
			 * In your case, this would be: String csvFileLocation = csvInputDir.getAbsolutePath() + "\\" + firstAsciiFileName.substring(0, firstAsciiFileName.length() - 4); String[] arguments = { csvFileLocation }; runPythonScript(CSV_TO_GEODATABASE_LOCATION, arguments); deleteFile(csvFileLocation);
			 * 
			 */
			ArrayList<String> str = runPythonScript(FileLocations.CSV_TO_GEODATABASE_SCRIPT_LOCATION);
			// TEMP DEBUG
			for (String s : str)
				System.out.println(s);

			// Delete files.
			deleteFile(asciiFile);

		} catch (Exception e) {
			Logger.error("Error trying to create map.", e);
		}
		return true;
	}
}
