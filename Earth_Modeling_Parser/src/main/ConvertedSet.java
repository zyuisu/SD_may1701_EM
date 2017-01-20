package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Keeps track of all the ASCII files that have been converted by the daemon.
 * 
 * @author Anish Kunduru
 */
public class ConvertedSet extends HashSet<String> implements Serializable {

	private static final long serialVersionUID = 1L; // If we wish to use
														// ObjectOutputStream at
														// a later date.
	public static final String CONVERTED_LOCATION = "resources\\converted.txt";
	private PrintWriter printWriter;
	HashSet<String> set;

	/**
	 * Constructor for ConvertedSet creates a set by checking against the
	 * previous converted.txt file (located in CONVERTED_LOCATION).
	 * 
	 * @throws IOException
	 */
	public ConvertedSet() throws IOException {
		set = new HashSet<String>();
		printWriter = new PrintWriter(CONVERTED_LOCATION);

		addFromConverted();
	}

	/**
	 * Scans the converted.txt file and adds all values to the HashSet.
	 * 
	 * @throws IOException
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
	 * @return true if was added, false if it already exists.
	 */
	public boolean add(String fileName) {
		if (set.add(fileName)) {
			printWriter.println(fileName);
			printWriter.flush();
			return true;
		}

		return false;
	}
}
