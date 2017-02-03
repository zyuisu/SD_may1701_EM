/**
 * @author Anish Kunduru
 * 
 * This class defines a message that includes an ASCII file.
 * Represents an ASCII that will be parsed by the server.
 */

package networking;

import java.io.Serializable;

public class AsciiFileMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum CompoundType{CH4, CO2};
	
	private byte[] file;
	private CompoundType compoundType;
	private int year;
	private int month;
	
	/**
	 * Constructs a new object that will represent a new ASCII file to be sent to the parser.
	 * @param compoundType The AsciiFileMessage.compoundType that represents the molecule that this map plots.
	 * @param file The byte array that represents this ASCII file.
	 * @param year An int that is greater than or equal to 1000. Represents the year that this dataset is to represent.
	 * @param month An int that is between 0 and 12, inclusive. Represents the month that this dataset is to represent.
	 * @throws IllegalAccessException If an null file or compoundType is passed.
	 */
	public AsciiFileMessage(CompoundType compoundType, byte[] file, int year, int month) throws IllegalAccessException
	{
		if (file == null || compoundType == null)
			throw new IllegalAccessException("file and compoundType must be set.");
		
		if (year < 1000)
			throw new IllegalArgumentException("Did you set an invalid year?");
		
		if (month < 0 || month > 12)
			throw new IllegalArgumentException("Month cannot be less than 0 or greater than 12.");
		
		if (file.length < 1) // Can be set to a larger number if we know the minimum size of header constants.
			throw new IllegalArgumentException("The file array is empty. It must not represent a valid file.");
		
		this.file = file;
		this.compoundType = compoundType;
		this.year = year;
		this.month = month;
	}
	
	/**
	 * @return The byte array that represents the dataset file in this message.
	 */
	public byte[] getFile()
	{
		return file;
	}
	
	/**
	 * @return The compound type that this dataset represents, as specified in AsciiFileMessage.CompoundType
	 */
	public CompoundType getCompoundType()
	{
		return compoundType;
	}
	
	/**
	 * @return The year that the dataset in this model represents.
	 */
	public int getYear()
	{
		return year;
	}
	
	/**
	 * @return The month that the dataset in this model represents.
	 */
	public int getMonth()
	{
		return month;
	}
}
