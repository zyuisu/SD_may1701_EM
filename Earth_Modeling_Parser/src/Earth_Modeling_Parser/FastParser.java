package Earth_Modeling_Parser;
/**
 * @author Anish Kunduru
 * Fast ASCII parser for a SE 491 senior design project.
 * 
 * The code here isn't the most readable since I opted for speed over all else (conditional tables versus loops, etc).
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FastParser
{
	private static final int NUM_COLS = 1404;
	private static final float START_LAT = 7.0f;
	private static final float START_LONG = -169.0f;
	private static final float CELL_SIZE = 0.0833333f;

	private static final int INPUT_BUFFER_SIZE = 23500000;
	private static final int OUTPUT_BUFFER_SIZE = 2350000;

	public static void main(String[] args) throws Exception
	{
		BufferedInputStream buffIS = new BufferedInputStream(new FileInputStream("ch4y2001m0.txt"), INPUT_BUFFER_SIZE);
		RandomAccessFile raf = new RandomAccessFile("output.csv", "rw");
		FileChannel rwChannel = raf.getChannel();
		ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, OUTPUT_BUFFER_SIZE);

		// latitude,longitude,value in byte form
		wrBuf.put((byte) 108);
		wrBuf.put((byte) 97);
		wrBuf.put((byte) 116);
		wrBuf.put((byte) 105);
		wrBuf.put((byte) 116);
		wrBuf.put((byte) 117);
		wrBuf.put((byte) 100);
		wrBuf.put((byte) 101);
		wrBuf.put((byte) 44);
		wrBuf.put((byte) 108);
		wrBuf.put((byte) 111);
		wrBuf.put((byte) 110);
		wrBuf.put((byte) 103);
		wrBuf.put((byte) 105);
		wrBuf.put((byte) 116);
		wrBuf.put((byte) 117);
		wrBuf.put((byte) 100);
		wrBuf.put((byte) 101);
		wrBuf.put((byte) 44);
		wrBuf.put((byte) 118);
		wrBuf.put((byte) 97);
		wrBuf.put((byte) 108);
		wrBuf.put((byte) 117);
		wrBuf.put((byte) 101);
		wrBuf.put((byte) 10);

		// Go past first capital A in the file.
		// Can use since values are hard coded as constants.
		// If ASCII may change in the future, we can change the code to parse it (at a cost)...
		int next = buffIS.read();
		while (next != 65)
			next = buffIS.read();

		int cellNum = 0;
		float curLat = START_LAT;
		float curLong = START_LONG;

		int[] prev = new int[5];
		prev[0] = buffIS.read();
		prev[1] = buffIS.read();
		prev[2] = buffIS.read();
		prev[3] = buffIS.read();
		prev[4] = buffIS.read();

		// Keep going till EOF.
		next = buffIS.read();
		while (next != -1)
		{
			// Go past until we hit a decimal point.
			if (next == 46)
			{
				// Calc lat and long and add to buffer.
				curLong = (++cellNum) % NUM_COLS == 0 ? START_LONG : curLong + CELL_SIZE;
				curLat -= CELL_SIZE;

				// Make sure it isn't NODATA_VALUE
				if (prev[0] != 45 && prev[1] != 57 && prev[2] != 57 && prev[3] != 57 && prev[4] != 57)
				{
					wrBuf.put(ByteBuffer.allocate(4).putFloat(curLat));
					wrBuf.put((byte) 44);// comma
					wrBuf.putFloat(curLong);
					wrBuf.put((byte) 44);

					// Read previous #s till we hit a space.
					if (prev[4] != 32)
					{
						wrBuf.put((byte) prev[0]);
						wrBuf.put((byte) prev[1]);
						wrBuf.put((byte) prev[2]);
						wrBuf.put((byte) prev[3]);
						wrBuf.put((byte) prev[4]);
					}
					else if (prev[3] != 32)
					{
						wrBuf.put((byte) prev[0]);
						wrBuf.put((byte) prev[1]);
						wrBuf.put((byte) prev[2]);
						wrBuf.put((byte) prev[3]);
					}
					else if (prev[2] != 32)
					{
						wrBuf.put((byte) prev[0]);
						wrBuf.put((byte) prev[1]);
						wrBuf.put((byte) prev[2]);
					}
					else if (prev[1] != 32)
					{
						wrBuf.put((byte) prev[0]);
						wrBuf.put((byte) prev[1]);
					}
					else if (prev[0] != 32)
						wrBuf.put((byte) prev[0]);
					
					

					// Read next #s till we hit a space.
					while (next != 32 && next != 10)
					{
						wrBuf.put((byte) next);
						next = buffIS.read();
					}

					// Add LF (NL line feed).
					wrBuf.put((byte) 10);
				}
			}

			prev[0] = prev[1];
			prev[1] = prev[2];
			prev[2] = prev[3];
			prev[3] = prev[4];
			prev[4] = next;
			next = buffIS.read();
		}

		buffIS.close();
		raf.close();
		rwChannel.close();
	}
}
