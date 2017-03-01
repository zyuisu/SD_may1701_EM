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
 *         Utility class whose sole purpose is to store directory locations among common classes. It will make it easy to change drive parameters on the disk.
 */

package utils;

public class FileLocations {

	public static final String KEYSTORE_FILE_LOCATION = "C:/Users/KFed/Desktop/KellenIsAwesome/keystore.jks";

	public static final String ASCII_INPUT_DIRECTORY_LOCATION = "Original_ASCII_files\\";
	public static final String CSV_OUTPUT_DIRECTORY_LOCATION = "Parsed_CSV_files\\";
	public static final String TEMP_WORKING_DIRECTORY_LOCATION = "Temp_Working_Files\\";

	public static final String HTML_FILE_LOCATION = "asdfasfdasdf.html";
	public static final String CONVERTED_FILE_LOCATION = "/home/akunduru/Desktop/converted.ser"; // "resources\\converted.ser";
	public static final String APPROVED_CLIENTS_FILE_LOCATION = "/home/akunduru/Desktop/approvedClients.txt"; // "resources\\approvedClients.txt";

	public static final String CURRENT_WORKING_DIRECTORY_LOCATION = "C:\\EarthModelingDaemon\\";
	public static final String MAP_TEMPLATES_DIRECTORY_LOCATION = "Map_Templates\\";
	public static final String MAPS_PUBLISHING_DIRECTORY_LOCATION = "Maps_Publishing\\";
	public static final String TEMP_PUBLISHING_FILES_DIRECTORY_LOCATION = "temp_publishing\\";
	public static final String CREATED_LAYERS_DIRECTORY_LOCATION = "Created_Layers\\";
	public static final String BLANK_MAP_FILE_LOCATION = "Maps_Publishing\\blank_map.mxd";
	public static final String CREATED_GDBS_OUTPUT_DIRECTORY_LOCATION = "Auto_GDB\\";
	public static final String CSV_TABLES_OUTPUT_DIRECTORY_LOCATION = "CSV_Tables\\";

	public static final String PYTHON_EXECUTABLE_BINARY_LOCATION = "C:\\Python27\\ArcGISx6410.4\\python.exe";
	public static final String PUBLISH_MAP_SCRIPT_LOCATION = "Python_Scripts\\publish_map.py";
	public static final String PUBLISHING_PARAMS_SCRIPT_LOCATION = "Python_Scripts\\publishing_params.py";
	
	public static final String ABS_MAPS_PUBLISHING_DIRECTORY_LOCATION = "C:\\EarthModelingDaemon\\Maps_Publishing\\";
	public static final String ABS_CREATED_LAYERS_DIRECTORY_LOCATION = "C:\\EarthModelingDaemon\\Created_Layers\\";
	public static final String ABS_CSV_TABLES_OUTPUT_DIRECTORY_LOCATION = "C:\\EarthModelingDaemon\\CSV_Tables\\";
	public static final String ABS_AUTO_GDBS_OUTPUT_DIRECTORY_LOCATION = "C:\\EarthModelingDaemon\\Auto_GDB\\";
	public static final String ABS_TEMP_PUBLISHING_FILES_DIRECTORY_LOCATION = "C:\\EarthModelingDaemon\\temp_publishing\\";
	
	
	// If the git repo is locally cloned, you can simply do something like this: C:\\EarthModeling\\SD_may1701_EM\\Earth_Modeling_Parser\\src\\Python_Scripts\\CsvToGeodatabase.py

	private FileLocations() {
	};
}
