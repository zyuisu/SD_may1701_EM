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

	public static void main(String[] args) throws IOException, InterruptedException {
		File asciiInputDir = new File(FileLocations.ASCII_INPUT_DIRECTORY_LOCATION);
		File csvOutputDir = new File(FileLocations.CSV_OUTPUT_DIRECTORY_LOCATION);

		ConvertedSet convertedSet = new ConvertedSet();

		while (true) {

			Logger.info("Server daemon waking up...");

			while (asciiInputDir.list().length > 0) {
				String firstAsciiFileName = asciiInputDir.list()[0];
				String firstAsciiFileLocation = asciiInputDir.getAbsolutePath() + "\\" + firstAsciiFileName;

				if (!convertedSet.add(asciiInputDir.list()[0])) {
					Logger.warn("The file {} has already been converted!", firstAsciiFileLocation);
				} else {
					convertAsciiToCsv(firstAsciiFileLocation);

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
				}

				deleteFile(firstAsciiFileLocation);
			}
			Thread.sleep(TIME_TO_SLEEP);
		}
	}

	/**
	 * Overloaded helper if you wish to call a script with no arguments.
	 * 
	 * @param scriptLocation
	 *           - The location of the Python script on the disk.
	 * @return An ArrayList of the standard output of the script.
	 * @throws IOException
	 *            - Can't find the script at the specified location!
	 * @throws InterruptedException
	 *            - Interrupt was encountered before the process could finish executing!
	 */
	private static ArrayList<String> runPythonScript(String scriptLocation) throws IOException, InterruptedException {
		return runPythonScript(scriptLocation, null);
	}

	/**
	 * Runs a Python script with the given arguments.
	 * 
	 * @param scriptLocation
	 *           - The location of the *.py script on the disk. Example: C:\\Python\\awesome.py
	 * @param arguments
	 *           - A tokenized array of arguments, if the program has any.
	 * @return An ArrayList of the standard output of the script.
	 * @throws IOException
	 *            - Can't find the script at the specified location!
	 * @throws InterruptedException
	 *            - Interrupt was encountered before the process could finish executing!
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
	 *           - The process that you wish to execute.
	 * @return An ArrayList of the standard output of the process.
	 * @throws InterruptedException
	 *            - Interrupt was encountered before the process could finish executing!
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
	 *           - The absolute file path of the file on the disk.
	 */
	private static void deleteFile(String fileLocation) {
		File f = new File(fileLocation);
		if (f.delete())
			Logger.info("File is deleted!");
		else
			Logger.error("Delete operation failed!"); // SEVERE, shouldn't happen.
	}

	/**
	 * Converts an ASCII file to a CSV file via AsciiToCsv.java
	 * 
	 * @param fileLocation
	 *           - The absolute file path of the ascii.txt file on the disk.
	 * @throws IOException
	 *            - Can't find the file at the specified location!
	 */
	private static void convertAsciiToCsv(String fileLocation) throws IOException {
		Logger.info("Converting file: {} to CSV", fileLocation);
		String[] arguments = { fileLocation };
		AsciiToCsv.main(arguments);
		Logger.info("File converted to CSV!");
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

	private static synchronized boolean createMap(File file, MapProperties properties) {
		// TO-DO
		// Check against converted set.
		// Call scripts.
		// Delete files.
		return false;
	}
}
