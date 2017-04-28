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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.pmw.tinylog.Logger;

import utils.FileLocations;

public class AsciiToCsv {

	/*
	 * Values explicitly given in the Input ASCII File
	 */

	/**
	 * The Number of Columns (Incoming Client Document Variable)
	 */
	private double ncols;
	/**
	 * The Number of Rows (Incoming Client Document Variable)
	 */
	private double nrows;
	/**
	 * X (Longitude Coordinate of Lower Left Corner of Map (Incoming Client Document Variable)
	 */
	private double xllcorner;
	/**
	 * Y (Latitude) Coordinate of Lower Left Corner of Map (Incoming Client Document Variable)
	 */
	private double yllcorner;
	/**
	 * Size of step from one point to another (Incoming Client Document Variable)
	 */
	private double cellSize;
	/**
	 * Value which indicates no output to be read (Incoming Client Document Variable)
	 */
	private double NODATA_value;

	/*
	 * Parsed Values
	 */

	/**
	 * The number of lines found in the Header (should not exceed 30)
	 */
	private int linesInHeader;

	/**
	 * Whether or not the header has been successfully parsed
	 */
	private boolean headerParsed;

	/**
	 * The max value which shows up in the Ascii Table
	 */
	private double maxValue;

	/**
	 * The min value which shows up in the Ascii Table
	 */
	private double minValue;

	/**
	 * Longitude of the Upper Left Corner of the Map to be printed (the starting point when reading the table)
	 */
	private double longitude;

	/**
	 * Latitude of the Upper Left Corner of the map to be printed (the starting point when reading the table)
	 */
	private double latitude;

	/**
	 * Default constructor
	 */
	public AsciiToCsv() {
		this.ncols = 0;
		this.nrows = 0;
		this.xllcorner = 0;
		this.yllcorner = 0;
		this.cellSize = 0;
		this.NODATA_value = 0;
		this.linesInHeader = 0;
		this.headerParsed = false;
		this.maxValue = Double.MAX_VALUE;
		this.minValue = Double.MAX_VALUE;
	}

	/**
	 * 
	 * @return the max value that has been parsed from the ascii table
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * 
	 * @return the min value that has been parsed from the ascii table
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * 
	 * @return the longitude of the upper left corner of the ascii table (the starting point)
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * 
	 * @return the latitude of the upper left corner of the ascii table (the starting point)
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * 
	 * @return the number of columns parsed from the header of the ascii document. Represents the expected number of columns.
	 */
	public double getNcols() {
		return this.ncols;
	}

	/**
	 * 
	 * @return the number of rows parsed from the header of the ascii document. Represents the expected number of columns.
	 */
	public double getNrows() {
		return this.nrows;
	}

	/**
	 * 
	 * @return longitude coordinate of lower left corner (parsed from header)
	 */
	public double getXllCorner() {
		return this.xllcorner;
	}

	/**
	 * 
	 * @return latitude coordinate of lower left corner (parsed from header)
	 */
	public double getYllCorner() {
		return this.yllcorner;
	}

	/**
	 * 
	 * @return the cellsize extracted from the header.
	 */
	public double getCellSize() {
		return this.cellSize;
	}

	/**
	 * 
	 * @return the value to be ignored in the table (not sent to the resulting CSV)
	 */
	public double getNODATA() {
		return this.NODATA_value;
	}

	/**
	 * 
	 * @return the number of lines that were found in the header. Should not exceed 30.
	 */
	public int getLinesInHeader() {
		return this.linesInHeader;
	}

	/**
	 * 
	 * @return true if the header was successfully parsed. Returns false if invalid data was found or the lines are less than 30.
	 */
	public boolean getHeaderParsed() {
		return this.headerParsed;
	}

	/**
	 * 
	 * @param ftp
	 *           - The file to parse headers from.
	 * @return True if the header is successfully parsed (all values extracted in less than 30 lines). False otherwise.
	 * @throws IOException
	 *            the File cannot be found, or the program was stopped.
	 */
	protected boolean parseHeaders(File ftp) throws IOException {

		// Open the file to Scan headers from
		BufferedReader f = new BufferedReader(new FileReader(ftp));
		// Open the scanner which will read line by line from ASCII file
		Scanner scanner = new Scanner(f);
		// The number of lines that have been scanned
		char count = 0;

		// Repeat until header is completely parsed or the function returns false
		while (scanner.hasNextLine() && !this.getHeaderParsed()) {

			// Skip lines for reading until a line with values other than whitespace is found
			String headers = "";
			while (scanner.hasNextLine() && headers.replace(" ", "").equals("")) {
				headers = scanner.nextLine();
				count++;

				// Avoids infinite loop by limiting lines in header to 30
				if (count > 30) {
					Logger.error("Over 30 lines found in header. Check input file and try again.");
					scanner.close();
					return false;
				}
			}

			// Decide which value is given in this line, set the corresponding value
			this.setHeaderValue(headers);

			// Set linesInHeader to corresponding count
			this.linesInHeader = count;

			// If all values have been set to something other than default constructor values, header has been successfully parsed
			if (this.ncols != 0 && this.nrows != 0 && this.xllcorner != 0 && this.yllcorner != 0 && this.cellSize != 0 && this.NODATA_value != 0)
				this.headerParsed = true;
			else if (count > 30) {
				System.out.println("The file header is having trouble being parsed. Please check the input file.");
				Logger.error("The file header is having trouble being parsed. Please check the input file.");
				// Close scanners to avoid resource leak
				scanner.close();
				return false;
			}
		}
		// Close scanner to avoid resource leak
		scanner.close();

		return true;
	}

	/**
	 * 
	 * Reads a single line from the header, and attempts to set the value associated with the defined label given in the line.
	 * 
	 * @param line
	 *           The line to parse from. Includes the header value and the associated value.
	 * @return true if a value was sucessfully parsed. False otherwise.
	 * @throws InputMismatchException
	 *            If a non-double value is attempted to be ripped from the file
	 */
	protected boolean setHeaderValue(String line) throws InputMismatchException {
		// Open scanner for reading line
		Scanner scanheaders = new Scanner(line);

		try {
		// Read first value in the line
			String head = scanheaders.next();
			
			// Decide which value is given in this line, set the corresponding value
			switch (head) {
				case "":
					// empty line, do nothing, shouldn't happen
					scanheaders.close();
					return false;
				case "ncols":
					this.ncols = scanheaders.nextDouble();
					break;
				case "nrows":
					this.nrows = scanheaders.nextDouble();
					break;
				case "xllcorner":
					this.xllcorner = scanheaders.nextDouble();
					break;
				case "yllcorner":
					this.yllcorner = scanheaders.nextDouble();
					break;
				case "cellsize":
					this.cellSize = scanheaders.nextDouble();
					break;
				case "NODATA_value":
					this.NODATA_value = scanheaders.nextDouble();
					break;
			}
		} catch (Exception e) {
			Logger.error("Non double attempted to be parsed from the header. Please re-check your input file");
			return false;
		}
		// Compute Longitude of Upper Left Corner
		this.longitude = this.getXllCorner();
		// Compute Latitude of Upper Left Corner
		this.latitude = this.getYllCorner() + (this.getCellSize() * (this.getNrows() - 1));

		// avoid resource leak
		scanheaders.close();
		return true;

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

		if (parseHeaders(ftp)) {
			ArrayList<String> printing = parseBody(ftp);
			if (printing == null)
				return null;
			else {

				String fileName = ftp.getName();
				// Avoid ESRI filename error in output file by changing dashes to underscores. Unknown reasoning.
				if (fileName.contains("-"))
					fileName = fileName.replace("-", "_");

				// Remove extension from file name
				fileName = fileName.substring(0, fileName.length() - 4);
				// Create the output file
				File outFile = new File(FileLocations.CSV_OUTPUT_DIRECTORY_LOCATION + fileName + ".csv");

				// PrintWriter for Writing to Output file
				PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
				// Write header to output CSV file
				output.println("latitude,longitude,value");

				while (!printing.isEmpty())
					// Print lines to output, with the first two lines being the min and max values
					output.println(printing.remove(0));
				// Avoid resource leak
				output.close();
				return outFile;
			}
		} else
			return null;
	}

	/**
	 * Attempts to parse the body of the given ASCII file. This is the "table" of values to be parsed.
	 * 
	 * 
	 * 
	 * @param ftp
	 *           the file to parse
	 * @return the arraylist of values parsed. Index 0 is the min and Index 1 is the max.
	 * @throws IOException
	 *            File Not Found
	 * @throws InputMismatchException
	 *            a non double value was found in the Table
	 */
	protected ArrayList<String> parseBody(File ftp) throws IOException, InputMismatchException {

		// Should never happen
		if (!this.getHeaderParsed()) {
			Logger.error("Header was not successfully parsed. Check the input file. Failing in ParseBody method of AsciiToCsv.");
			return null;
		} else {

			// Create Buffered Reader for Reading lines from input file
			BufferedReader f = new BufferedReader(new FileReader(ftp));
			// Create ArrayList to hold Strings for output
			ArrayList<String> lines = new ArrayList<String>();

			// Scanner for reading lines
			Scanner scanner;
			// Current line
			String scanning = "";

			// Set the Scanner to the input file
			scanner = new Scanner(f);

			// Skip all lines in header to access table.
			char skip = 0;
			while (scanner.hasNextLine() && skip != this.getLinesInHeader()) {
				scanner.nextLine();
				skip++;
			}

			// The current row
			int rows = 0;
			// The current column
			int columns = 0;

			// While there are lines in the input document
			while (scanner.hasNextLine()) {
				// Read until line has something besides whitespace
				scanning = "";
				while (scanning.replace(" ", "").equals(""))
					scanning = scanner.nextLine();

				// Scanner for the current line
				Scanner linescan = new Scanner(scanning);

				// While we continue to find doubles in the line
				while (linescan.hasNextDouble()) {
					try {

						// Get the next value in the line
						double value = linescan.nextDouble();

						// If we want to print the value
						if (value != NODATA_value)
							// add to array list
							this.addValueToList(value, lines, rows, columns);
					} catch (Exception e) {
						Logger.error("1: Non double value found in the body of the Table. Please check your input file.");
						linescan.close();
						return null;
					}
					columns++;
					// If the current number of columns equals NCols, go to next row
					if (columns % this.getNcols() == 0) {
						// Only reset columns if we are still within the valid table
						if (rows < this.getNrows() - 1)
							columns = 0;
						rows++;
					}
				}
				// Check if failed on non-double.
				if ((linescan.hasNext() && !(linescan.next().trim().equals("")))) {
					Logger.error("2: Non double value found in the body of the Table. Please check your input file.");
					linescan.close();
					return null;
				}

				// Avoid resource leak
				linescan.close();
			}

			// Print out Max and Min (TESTING PURPOSES)
			Logger.debug("Max: {}, Min: {}", this.getMaxValue(), this.getMinValue());

			// Avoid resource leak
			scanner.close();
			// Return the arrayList containing all of the read lines
			return lines;
		}
	}

	/**
	 * Helper method for adding values to the ArrayList to be printed Format's strings for output and places in the appropriate spot in ArrayList. Index 0 should always be min and Index 1 should always be Max.
	 * 
	 * @param value
	 *           the value to insert into the arraylist.
	 * @param lines
	 *           the arraylist of values being effected.
	 * @param rows
	 *           the current number of rows navigated through the body.
	 * @param columns
	 *           the current number of columns navigated through the body.
	 */
	protected void addValueToList(Double value, ArrayList<String> lines, int rows, int columns) {
		// No values have been added to array list
		// Set min and max. Set to value 0
		if (this.getMaxValue() == Double.MAX_VALUE && this.getMinValue() == Double.MAX_VALUE) {
			this.maxValue = value;
			this.minValue = value;
			lines.add((latitude - rows * this.getCellSize()) + "," + (longitude + columns * this.getCellSize()) + "," + value);
		} else if (value > this.getMaxValue()) {
			// Set the new max value, add at the 1 index
			this.maxValue = value;
			lines.add(1, (latitude - rows * this.getCellSize()) + "," + (longitude + columns * this.getCellSize()) + "," + value);
		} else if (value < this.getMinValue()) {
			// Set the new min value, add at the 0 index
			this.minValue = value;
			lines.add(0, (latitude - rows * this.getCellSize()) + "," + (longitude + columns * this.getCellSize()) + "," + value);
			// If there are more than two items, the current max has been pushed to the two index as a result of the push to 0 by min
			// Remove index at 2 and add at index 1
			if (lines.size() > 2) {
				String temp = lines.remove(2);
				lines.add(1, temp);
			}
		} else
			// Not Min or Max, add at end of list.
			lines.add((latitude - rows * this.getCellSize()) + "," + (longitude + columns * this.getCellSize()) + "," + value);
	}
}
