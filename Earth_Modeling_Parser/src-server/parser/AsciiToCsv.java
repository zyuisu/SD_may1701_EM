/*
 * 
 * Copyright (C) 2016-2017 Kellen Johnson
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
 * @author Kellen Johnson
 * 
 *         Converts ASCII text to CSV.
 */

package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.pmw.tinylog.Logger;

import utils.FileLocations;

public class AsciiToCsv {

	/**
	 * Empty constructor.
	 */
	public AsciiToCsv() {
	}

	/**
	 * Parse an ASCII file to CSV and output a reference to the parsed file.
	 * 
	 * @param ftp
	 *           The file that you wish to parse.
	 * @return The File reference where the parsed file is stored.
	 * @throws IOException
	 *            Likely means that a file wasn't found.
	 */
	public File parseToCsv(File ftp) throws IOException {

		String fileName = ftp.getName();
		// Because ESRI garbage throws an exception if a '-' character is in the file name. No one, including ESRI knows why this is.
		if (fileName.contains("-"))
			fileName = fileName.replace("-", "_");
		
		fileName = fileName.substring(0, fileName.length() - 4);
		File outFile = new File(FileLocations.CSV_OUTPUT_DIRECTORY_LOCATION + fileName + ".csv");

		ArrayList<String> lines = new ArrayList<String>();

		BufferedReader f = new BufferedReader(new FileReader(ftp));
		Scanner scanner;
		double ncols = 0, nrows = 0, xllcorner = 0, yllcorner = 0, cellsize = 0, NODATA_value = 0;
		String scanning = "";
		try {
			PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
			output.println("latitude,longitude,value");
			scanner = new Scanner(f);

			char count = 0;

			boolean extract_headers = false;
			while (!extract_headers) {
				String headers = "";
				while (headers.replace(" ", "").equals(""))
					headers = scanner.nextLine();
				Scanner scanheaders = new Scanner(headers);
				String head = scanheaders.next();
				switch (head) {
					case "":
						// empty line, do nothing
						break;
					case "ncols":
						ncols = scanheaders.nextDouble();
						break;
					case "nrows":
						nrows = scanheaders.nextDouble();
						break;
					case "xllcorner":
						xllcorner = scanheaders.nextDouble();
						break;
					case "yllcorner":
						yllcorner = scanheaders.nextDouble();
						break;
					case "cellsize":
						cellsize = scanheaders.nextDouble();
						break;
					case "NODATA_value":
						NODATA_value = scanheaders.nextDouble();
						break;
				}
				count++;
				if (ncols != 0 && nrows != 0 && xllcorner != 0 && yllcorner != 0 && cellsize != 0 && NODATA_value != 0)
					extract_headers = true;
				else if (count > 30) {
					// TODO put in the logger
					System.out.println("The file header is having trouble being parsed. Please check the input file.");
					Logger.debug("The file header is having trouble being parsed. Please check the input file.");
					return null;
				}
			}

			int counter = 0;
			double longitude = xllcorner;
			double latitude = yllcorner + (cellsize * (nrows - 1));
			int rows = 0;
			int columns = 0;
			double max = 2017;
			double min = 2017;

			while (scanner.hasNextLine()) {
				scanning = "";
				while (scanning.replace(" ", "").equals(""))
					scanning = scanner.nextLine();
				Scanner linescan = new Scanner(scanning);
				while (linescan.hasNextDouble()) {
					double value = linescan.nextDouble();
					if (value != NODATA_value)
						if (max == 2017 && min == 2017) {
							max = value;
							min = value;
							lines.add((latitude - rows * cellsize) + "," + (longitude + columns * cellsize) + "," + value);
						} else if (value > max) {
							max = value;
							lines.add(1, (latitude - rows * cellsize) + "," + (longitude + columns * cellsize) + "," + value);
						} else if (value < min) {
							min = value;
							lines.add(0, (latitude - rows * cellsize) + "," + (longitude + columns * cellsize) + "," + value);
							if (lines.size() > 2) {
								String temp = lines.remove(2);
								lines.add(1, temp);
							}
						} else
							lines.add((latitude - rows * cellsize) + "," + (longitude + columns * cellsize) + "," + value);
					columns++;
					if (columns % ncols == 0) {
						if (rows < nrows - 1)
							columns = 0;
						rows++;
					}
					counter++;
				}
				linescan.close();
			}

			while (!lines.isEmpty())
				output.println(lines.remove(0));

			// Print out Max and Min (TESTING PURPOSES)
			Logger.debug("Max: {}, Min: {}", max, min);

			output.close();
			scanner.close();
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}

		return outFile;
	}
}
