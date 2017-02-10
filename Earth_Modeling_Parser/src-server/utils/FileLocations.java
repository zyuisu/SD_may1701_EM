/**
 * @author Anish Kunduru
 * 
 *         Utility class whose sole purpose is to store directory locations among common classes. It will make it easy to change drive parameters on the disk.
 */

package utils;

public class FileLocations {

	public static final String ASCII_INPUT_DIRECTORY_LOCATION = "Original_ASCII_files\\";
	public static final String CSV_OUTPUT_DIRECTORY_LOCATION = "Parsed_CSV_files\\";
	public static final String CONVERTED_FILE_LOCATION = "resources\\converted.txt";
	public static final String PYTHON_BINARY_LOCATION = "C:\\Python27\\python.exe";
	// C:\\Python27\\ArcGISx6410.4\\python.exe --OR-- C:\\Python27\\ArcGIS10.4\\python.exe
	public static final String CSV_TO_GEODATABASE_SCRIPT_LOCATION = "C:\\Users\\Anish Kunduru\\Documents\\Spring 2017\\SE 492\\git\\SD_may1701_EM\\Earth_Modeling_Parser\\src\\parser\\CsvToGeodatabase.py";
	// C:\\EarthModeling\\SD_may1701_EM\\Earth_Modeling_Parser\\src\\parser\\CsvToGeodatabase.py
	public static final String APPROVED_CLIENTS_FILE_LOCATION = "approvedClients.txt";
	public static final String TEMP_WORKING_DIRECTORY_LOCATION = "Temp_Working_Files\\";

	private FileLocations() {
	};
}
