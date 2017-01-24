/**
 * Main server thread that wakes up to check if any ASCII files have been added.
 * 
 * @author Anish Kunduru
 */

package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.pmw.tinylog.Logger;

import parser.AsciiToCsv;

public class EarthModellingDaemon {

	public static final String INPUT_DIRECTORY_LOCATION = "Original_ASCII_files\\";
	public static final Long TIME_TO_SLEEP = 600000L; // 10 minutes before this daemon wakes up again.
	public static final String PYTHON_lOCATION = "C:\\Python27\\python.exe";
	// C:\\Python27\\ArcGISx6410.4\\python.exe --OR-- C:\\Python27\\ArcGIS10.4\\python.exe
	public static final String CSV_TO_GEODATABASE_LOCATION = "C:\\Users\\Anish Kunduru\\Documents\\Spring 2017\\SE 492\\git\\SD_may1701_EM\\Earth_Modeling_Parser\\src\\parser\\CsvToGeodatabase.py";
	// C:\\EarthModeling\\SD_may1701_EM\\Earth_Modeling_Parser\\src\\parser\\CsvToGeodatabase.py

	public static void main(String[] args) throws IOException, InterruptedException {
		File inputDir = new File(INPUT_DIRECTORY_LOCATION);
		ConvertedSet convertedSet = new ConvertedSet();

		while (true) {

			Logger.info("Server daemon waking up...");

			while (inputDir.list().length > 0) {
				// ASCII to CSV
				String firstFileLocation = inputDir.getAbsolutePath() + "\\" + inputDir.list()[0];

				if (!convertedSet.add(inputDir.list()[0])) {
					Logger.warn("The file {} has already been converted!", firstFileLocation);
				} else {
					convertAsciiToCsv(firstFileLocation);

					// CSV to Geodatabase
					// NOTE: str contains the output of the python script in case you need it.
					// Otherwise, you can simply use runPythonScript(scriptToRun...
					// Anything returned in str is always logged.
					ArrayList<String> str = runPythonScript(CSV_TO_GEODATABASE_LOCATION);
					for (String s : str)
						System.out.println(s);
				}

				deleteFile(firstFileLocation);
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
	 *           - An tokenized array of arguments, if the program has any.
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
		commands.add(PYTHON_lOCATION);
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
	 * Creates a new thread to run the Process and blocks this.Thread() until it is complete.
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
		Logger.info("Converting file: {}", fileLocation);
		String[] arguments = { fileLocation };
		AsciiToCsv.main(arguments);
		Logger.info("File converted!");
	}
}
