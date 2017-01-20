/**
 * Main server thread that wakes up to check if any ASCII files have been added.
 * @author Anish Kunduru
 */

package main;

import java.io.File;
import java.io.IOException;

import parser.AsciiToCsv;

public class EarthModellingDaemon {

	public static final String INPUT_DIRECTORY_LOCATION = "Original_ASCII_files\\";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		File inputDir = new File(INPUT_DIRECTORY_LOCATION);

		while (true) {
			
			while (inputDir.list().length > 0) {
				// ASCII to CSV
				String firstFileLocation = inputDir.getAbsolutePath() + "//" + inputDir.list()[0];
				String[] arguments = {firstFileLocation};
				
				System.out.println("Converting file: " + firstFileLocation);
				AsciiToCsv.main(arguments);
				System.out.println("File converted!");
				
				File f = new File(firstFileLocation);
				if (f.delete())
					System.out.println("File is deleted!");
				else
					System.out.println("Delete operation failed!"); //SEVERE, shouldn't happen.
				
				//TO-DO: CSV to Excel
			}

			Thread.sleep(600000);
		}
	}
}
