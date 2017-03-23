/*
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
 *         This program is our handler for UploadMulipleAsciiScreen.fxml.
 */

package uploadAscii;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import framework.AbstractNetworkedScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser.ExtensionFilter;
import networking.AsciiFileMessage;
import networking.StringMessage;
import utils.MapCompoundType;
import utils.MapProperties;
import utils.MapRegionType;

public class UploadMultipleAsciiScreenController extends AbstractNetworkedScreenController {
	@FXML
	private TextArea messageTextArea;
	@FXML
	private Button backBtn;
	@FXML
	private Button selectFilesBtn;
	@FXML
	private Button sendToServerBtn;

	private List<File> selectedFiles;

	/**
	 * Initializes the controller class. Automatically called after the FXML file has been loaded.
	 */
	@FXML
	public void initialize() {
		selectFilesBtn.setOnAction(event -> {
			ExtensionFilter[] filter = { new ExtensionFilter("ASCII Text Document", "*.txt") };
			selectedFiles = promptUserForMultipleFiles("Select ASCII Files", selectFilesBtn, filter);

			if (selectedFiles != null) {
				messageTextArea.appendText("Selected files:\n");
				for (File f : selectedFiles)
					messageTextArea.appendText(f.getName() + "\n");
			}
		});

		sendToServerBtn.setOnAction(event -> {

			if (selectedFiles != null)
				sendToServer();
			else
				errorAlert("Unselected ASCII Files", "You must select at least one ASCII file", "Please select the ASCII file(s), and try again.");

		});

		backBtn.setOnAction(event -> {
			parentController.goToUploadAsciiScreen();
		});
	}

	/**
	 * Designed to output descriptive messages from the server to the user via a text area.
	 * 
	 * @param sm
	 *           A StringMessage containing the message you wish to output.
	 */
	public void outputMessage(StringMessage sm) {
		messageTextArea.appendText("\n" + sm.getMessageType().name() + ": " + sm.getMsgHeader() + "\n");
		messageTextArea.appendText("\tDetailed information: " + sm.getMsgContent() + "\n");
	}

	/**
	 * Helper that sends all the selected files to the server. Intended to be run in its own thread.
	 * 
	 * @throws IOException
	 *            There was an issue reading one of the selected files from the disk.
	 * @throws IllegalAccessException
	 *            There was an issue parsing the map properties.
	 */
	private void sendToServer() {
		Thread thread = new Thread(() -> {

			for (File f : selectedFiles) {
				MapProperties mp = parseMapProperties(f);
				if (mp != null) {
					try {
						byte[] fileAsBytes = Files.readAllBytes(f.toPath());
						AsciiFileMessage afm = new AsciiFileMessage(mp, fileAsBytes, false);
						messageTextArea.appendText("\nData file for map : " + mp.toString() + " sent to the server.\n");
						sendMessageToServer(afm);
						Thread.sleep(5000L); // Wait for 5 seconds. -- Don't hammer the server.
					} catch (Exception e) {
						errorAlert("Cannot Construct Server Message", "Something is wrong with your selection:", e.getMessage());
					}
				} else
					messageTextArea.appendText("ERROR PROCESSING MAP PROPERTIES for: " + f.getName() + " -----\n");
			}
		});

		thread.setDaemon(true); // In case it gets stuck and the user terminates the application.
		thread.start();
	}

	/**
	 * Tries to determine a map properties based on the filename.getName() and rules defined under MapProperties.toString().
	 * 
	 * @param f
	 *           The file for with you want to determine the map properties of.
	 * @return null if the map properties couldn't be determined; otherwise a valid MapProperties will be returned.
	 */
	private MapProperties parseMapProperties(File f) {
		/*
		 * Rules: MAP_REGION_TYPE+MAP_COMPOUND_TYPE+y+year+m+month.txt For example, a CH4 file of the Mississippi river basin, year 2000, and month of December would be formatted as: MISSISSIPPI_RIVER_BASINCH4y2000m11.txt If the map is just a yearly map, it would have -1 for the month parameter. For example, a N20 file with a global region and year 1980 would
		 * be formatted: GLOBALN2Oy1980m-1.txt
		 */

		String filename = f.getName().replace(".txt", "");

		int indexOfM = filename.lastIndexOf('m');
		int indexOfY = filename.lastIndexOf('y');
		if (indexOfM == -1 || indexOfY == -1)
			return null;

		int month = Integer.parseInt(filename.substring(indexOfM + 1, filename.length()));
		int year = Integer.parseInt(filename.substring(indexOfY + 1, indexOfM));

		MapCompoundType mc = null;
		MapRegionType mr = null;
		String compound = "";
		for (int i = indexOfY - 1; i > 0; i--) {
			compound = filename.charAt(i) + compound;

			for (MapCompoundType c : MapCompoundType.values())
				if (c.name().equals(compound)) {
					mc = c;

					String region = filename.substring(0, i);
					for (MapRegionType r : MapRegionType.values())
						if (r.name().equals(region))
							mr = r;

					break;
				}
		}

		try {
			return month != -1 ? new MapProperties(mr, mc, year, month) : new MapProperties(mr, mc, year);
		} catch (IllegalAccessException e) {
			return null;
		}
	}
}
