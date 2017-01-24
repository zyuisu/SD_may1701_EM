package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Scanner;

import org.pmw.tinylog.Logger;

/**
 * Keeps track of all the ASCII files that have been converted by the daemon.
 * 
 * @author Anish Kunduru
 */
public class ConvertedSet extends HashSet<String> implements Serializable {

	private static final long serialVersionUID = 1L; // If we wish to use ObjectOutputStream at a later date.
	public static final String CONVERTED_LOCATION = "resources\\converted.txt";
	private FileWriter fileWriter;
	HashSet<String> set;

	/**
	 * Constructor for ConvertedSet creates a set by checking against the previous converted.txt file (located in CONVERTED_LOCATION).
	 * 
	 * @throws IOException
	 *            - Can't add to the existing converted.txt file!
	 */
	public ConvertedSet() throws IOException {
		set = new HashSet<String>();
		fileWriter = new FileWriter(CONVERTED_LOCATION, true);

		addFromConverted();
	}

	/**
	 * Scans the converted.txt file and adds all values to the HashSet.
	 * 
	 * @throws IOException
	 *            - Can't read to the existing converted.txt file!
	 */
	private void addFromConverted() throws IOException {
		BufferedReader buffR;
		buffR = new BufferedReader(new FileReader(CONVERTED_LOCATION));

		Scanner sc = new Scanner(buffR);
		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			if (!line.equals(""))
				set.add(line);
		}

		sc.close();
		buffR.close();
	}

	/**
	 * @Override Adds the fileName to the master list and adds it to the set.
	 * @return true if was added, false if it already exists or there was an I/O error.
	 */
	public boolean add(String fileName) {
		if (set.add(fileName)) {
			try {
				fileWriter.write(fileName + "\n");
				fileWriter.flush();
			} catch (IOException e) {
				Logger.error(e);
				return false;
			}
			return true;
		}

		return false;
	}
}
