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
 *         Main server thread that handles all generation of maps and related elements.
 */

package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.pmw.tinylog.Logger;

import networking.ClientServer;
import networking.ServerInformation;
import parser.AsciiToCsv;
import parser.JavaScriptGenerator;
import utils.FileLocations;
import utils.MapProperties;
import utils.ReferenceScales;

public class EarthModellingDaemon {

	public static final long TIME_TO_SLEEP = 30000L; // 30 seconds before this daemon wakes up again.
	public static final long MAX_EXECUTABLE_RUNTIME_IN_MINUTES = 10L; // Represented in minutes.
	private static ConvertedSet convertedSet;
	private static boolean run = false;
	private static ClientServer clientServer;
	private static ReferenceScales referenceScales;

	private static String keystorePassword;
	private static String arcgisServerUsername;
	private static String arcgisServerPassword;
	private static String webServerUsername;
	private static String webServerPassword;

	/**
	 * Select to start or stop the service.
	 * 
	 * @param args
	 *           args[0] = start to start; stop to stop. args[1] = keystore password. args[2] = arcgis server username. args[3] = arcgis server password. args[4] = html server username. args[5] = html server password.
	 */
	public static void main(String[] args) {
		// NOTE: Since start() and stop() are not synchronized, but are called by procrun in different threads, this code has the potential for race conditions. This shouldn't be important for the context of how this operates, but will explain exceptions in the final log moments.
		if ("start".equals(args[0])) {
			keystorePassword = args[1];
			arcgisServerUsername = args[2];
			arcgisServerPassword = args[3];
			webServerUsername = args[4];
			webServerPassword = args[5];
			start();
		} else if ("stop".equals(args[0]))
			stop();
	}

	/**
	 * Startup the service.
	 */
	public static void start() {
		Logger.info("Server daemon is starting up...");

		try {
			convertedSet = new ConvertedSet();
			referenceScales = new ReferenceScales();
		} catch (IOException e) {
			Logger.error(e);
		}

		// Create required temp directories if they don't exist.
		File csvOutputDir = new File(FileLocations.CSV_OUTPUT_DIRECTORY_LOCATION);
		File tempOutputDir = new File(FileLocations.TEMP_WORKING_DIRECTORY_LOCATION);
		csvOutputDir.mkdir();
		tempOutputDir.mkdir();

		Logger.info("Starting VEMS ClientServer.");
		clientServer = new ClientServer(ServerInformation.SERVER_PORT, FileLocations.KEYSTORE_FILE_LOCATION, keystorePassword);
		clientServer.start();

		while (run)
			try {
				if (!clientServer.isAClientConnected())
					System.gc();

				Thread.sleep(TIME_TO_SLEEP);
			} catch (InterruptedException e) {
				// Do nothing, because map processing is likely happening right now.
			}
	}

	/**
	 * Gracefully shut down the program.
	 */
	public static void stop() {
		Logger.info("Shutting down server.");
		clientServer.end();
		run = true;
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
	 * @throws TimeoutException
	 *            The process was terminated because it took too long to finish executing!
	 */
	private static ArrayList<String> runPythonScript(String scriptLocation) throws IOException, InterruptedException, TimeoutException {
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
	 * @throws TimeoutException
	 *            The process was terminated because it took too long to finish executing!
	 */
	private static ArrayList<String> runPythonScript(String scriptLocation, String[] arguments) throws IOException, InterruptedException, TimeoutException {
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
	 * @throws TimeoutException
	 *            The process was terminated because it took too long to finish executing!
	 */
	private static ArrayList<String> runExecutable(String exeLocation) throws IOException, InterruptedException, TimeoutException {
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
	 * @throws TimeoutException
	 *            The process was terminated because it took too long to finish executing!
	 */
	private static ArrayList<String> runExecutable(String exeLocation, String[] arguments) throws IOException, InterruptedException, TimeoutException {
		return runExecutable(exeLocation, arguments, MAX_EXECUTABLE_RUNTIME_IN_MINUTES, TimeUnit.MINUTES);
	}

	/**
	 * /** Runs a an executable file with the given arguments.
	 * 
	 * @param exeLocation
	 *           The absolute file location of the runnable on the disk.
	 * @param arguments
	 *           A tokenized array of arguments, if the program has any.
	 * @param timeoutValue
	 *           How long to wait before breaking off the program being executed.
	 * @param timeoutValueUnits
	 *           The units of the timeoutValue long.
	 * @return An ArrayList of the standard output of the script.
	 * @throws IOException
	 *            Can't find the script at the specified location!
	 * @throws InterruptedException
	 *            Interrupt was encountered before the process could finish executing!
	 * @throws TimeoutException
	 *            The process was terminated because it took too long to finish executing!
	 */
	private static ArrayList<String> runExecutable(String exeLocation, String[] arguments, Long timeoutValue, TimeUnit timeoutValueUnits) throws IOException, InterruptedException, TimeoutException {
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
		return waitForProcess(process, timeoutValue, timeoutValueUnits);
	}

	/**
	 * Creates a new thread to run the Process and blocks this.Thread until it is complete.
	 * 
	 * @param process
	 *           The process that you wish to execute.
	 * @param waitTime
	 *           How long we should wait for a process to finish executing before terminating the process.
	 * @param waitTimeUnit
	 *           The units of the long passed as waitTime.
	 * @return An ArrayList of the standard output of the process.
	 * @throws InterruptedException
	 *            Interrupt was encountered before the process could finish executing!
	 * @throws TimeoutException
	 *            The process took longer than the value given in waitTime to finish executing!
	 */
	private static ArrayList<String> waitForProcess(final Process process, long waitTime, TimeUnit waitTimeUnit) throws InterruptedException, TimeoutException {
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
		if (process.waitFor(waitTime, waitTimeUnit))
			Logger.info("Done running script.");
		else {
			Logger.info("Process terminated before script completion.");
			throw new TimeoutException();
		}

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
		File f = new AsciiToCsv().parseToCsv(asciiFile); // Init new obj to save memory.
		Logger.info("File converted to CSV!");

		return f;
	}

	/**
	 * Removes a map from the ArcGIS server by executing a command line argument.
	 * 
	 * @param properties
	 *           The MapProperties that represents what needs to be deleted.
	 * @return true if the map was successfully deleted; false otherwise.
	 * @throws IOException
	 *            Means that FileLocations.ARCSERVER_MANAGE_SERVICE_FILE_LOCATION couldn't be located.
	 * @throws InterruptedException
	 *            Probably means there was an issue while running the command line arguments.
	 * @throws TimeoutException
	 *            Means that the the service manager Python script was terminated before completion (took too long).
	 */
	public static synchronized boolean removeMapFromServer(MapProperties properties) throws IOException, InterruptedException, TimeoutException {
		if (!removeLocalMapFiles(properties))
			return false;

		// required arguments for the delete from server command using executable python script
		// python.exe "C:\Program Files\ArcGIS\Server\tools\admin\manageservice.py" -u username -p password -s https://proj-se491.iastate.edu:6443 -n EarthModelingTest/service_name -o delete
		String arguments[] = { "-u", arcgisServerUsername, "-p", arcgisServerPassword, "-s", "https://proj-se491.iastate.edu:6443", "-n", "EarthModelingTest/" + properties.toString(), "-o", "delete" };
		runPythonScript(FileLocations.ARCSERVER_MANAGE_SERVICE_FILE_LOCATION, arguments);

		convertedSet.remove(properties);
		return generateAndTransferJavaScript();
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
	 * @throws TimeoutException
	 *            Means an intermediary Python script was cut short because it took too long to process.
	 */
	public static synchronized boolean createMap(byte[] asciiFile, MapProperties properties) throws IOException, InterruptedException, TimeoutException {
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
	 *            Probably means one of the intermediary Python scripts were cut short before they could complete execution.
	 * @throws TimeoutException
	 *            Means an intermediary Python script was cut short because it took too long to process.
	 */
	private static synchronized boolean createMap(File asciiFile, MapProperties properties) throws IOException, InterruptedException, TimeoutException {

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

		String template = properties.getMapRegion().toString() + properties.getMapCompoundType().toString();
		String referenceScale;
		try {
			referenceScale = "" + referenceScales.getReferenceScale(properties.getMapRegion());
		} catch (Exception e) {
			Logger.error("Error when calling getReferenceScale. Check ReferenceScale Class.", e);
			return false;
		}

		String[] arguments = { FileLocations.ABS_CSV_OUTPUT_DIRECTORY_LOCATION, properties.toString(), FileLocations.CURRENT_WORKING_DIRECTORY_LOCATION, FileLocations.MAP_TEMPLATES_DIRECTORY_LOCATION, FileLocations.MAPS_PUBLISHING_DIRECTORY_LOCATION, FileLocations.TEMP_PUBLISHING_FILES_DIRECTORY_LOCATION, template, FileLocations.BLANK_MAP_FILE_LOCATION,
				FileLocations.CSV_TABLES_OUTPUT_DIRECTORY_LOCATION, FileLocations.CREATED_GDBS_OUTPUT_DIRECTORY_LOCATION, FileLocations.CREATED_LAYERS_DIRECTORY_LOCATION, arcgisServerUsername, arcgisServerPassword, referenceScale };

		ArrayList<String> al = runPythonScript(FileLocations.PUBLISH_MAP_SCRIPT_LOCATION, arguments);
		logExceptions(al);

		String[] arguments2 = { properties.toString(), arcgisServerUsername, arcgisServerPassword };
		al = runPythonScript(FileLocations.PUBLISHING_PARAMS_SCRIPT_LOCATION, arguments2);
		logExceptions(al);

		deleteFile(asciiFile);

		return generateAndTransferJavaScript();
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
	 * Generates a new JS model using the JavaScriptGenerator class and transfers it to the web server.
	 * 
	 * @return true if successfully created and transfers; false otherwise.
	 */
	private static boolean generateAndTransferJavaScript() {
		try {
			new JavaScriptGenerator(convertedSet);
		} catch (IOException e) {
			Logger.error("Issue generating new JavaScript.", e);
			return false;
		}

		return transferJSToWebServer(FileLocations.JAVASCRIPT_FILE_LOCATION, ServerInformation.WEB_SERVER_JAVASCRIPT_LOCATION);
	}

	/**
	 * Transfers the generated JS file to the web server using WINSCP.
	 * 
	 * @param pathOfFileToTransfer
	 *           The path of the file to transfer (on the local disk).
	 * @param pathOnDestinationServer
	 *           The path of the file to transfer (on the destination server).
	 * @return true if the file was successfully sent to the server; false otherwise.
	 */
	private static boolean transferJSToWebServer(String pathOfFileToTransfer, String pathOnDestinationServer) {
		String address = webServerUsername + ":" + webServerPassword + "@" + ServerInformation.WEB_SERVER_ADDRESS;
		String command = "\"put " + pathOfFileToTransfer + " " + pathOnDestinationServer + "\"";
		String[] arguments = { address, "/command", command };

		try {
			runExecutable(FileLocations.WINSCP_EXECUTABLE_LOCATION, arguments, 1L, TimeUnit.MINUTES);
		} catch (IOException | InterruptedException | TimeoutException e) {
			Logger.error("Failed to transfer the file.");
			Logger.trace(e);
			return false;
		}

		return true;
	}
}
