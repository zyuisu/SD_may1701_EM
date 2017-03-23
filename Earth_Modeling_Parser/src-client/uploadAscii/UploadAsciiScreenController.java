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
 *         This program is our handler for UploadAsciiScreen.fxml.
 */

package uploadAscii;

import java.io.File;
import java.nio.file.Files;

import framework.AbstractNetworkedScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser.ExtensionFilter;
import networking.AsciiFileMessage;
import networking.DeleteMapMessage;
import utils.MapCompoundType;
import utils.MapProperties;
import utils.MapRegionType;

public class UploadAsciiScreenController extends AbstractNetworkedScreenController {
	@FXML
	private Label message;
	@FXML
	private TextArea selectedFilesTextArea;
	@FXML
	private ComboBox<MapRegionType> regionCB;
	@FXML
	private ComboBox<MapCompoundType> compoundCB;
	@FXML
	private TextField yearTextField;
	@FXML
	private TextField monthTextField;
	@FXML
	private Button selectFilesBtn;
	@FXML
	private Button sendToServerBtn;
	@FXML
	private Button goToMultipleAsciiScreenBtn;
	@FXML
	private CheckBox deleteMapCheckBox;
	@FXML
	private CheckBox overwriteCheckBox;
	@FXML
	private CheckBox yearlyMapCheckBox;

	private File selectedFile;

	/**
	 * Initializes the controller class. Automatically called after the FXML file has been loaded.
	 */
	@FXML
	public void initialize() {
		populateComboBoxes();

		regionCB.setOnAction(event -> {
			if (!regionCB.getSelectionModel().isEmpty())
				compoundCB.setVisible(true);
		});

		compoundCB.setOnAction(event -> {
			if (!regionCB.getSelectionModel().isEmpty())
				yearTextField.setVisible(true);
			else
				yearTextField.setVisible(true);
		});

		yearlyMapCheckBox.setOnAction(event -> {
			if (!yearlyMapCheckBox.isSelected())
				monthTextField.setVisible(true);
			else
				monthTextField.setVisible(false);
		});

		selectFilesBtn.setOnAction(event -> {
			ExtensionFilter[] filter = { new ExtensionFilter("ASCII Text Document", "*.txt") };
			File asciiFile = promptUserForFile("Select ASCII File", selectFilesBtn, filter);

			if (asciiFile != null) {
				selectedFilesTextArea.appendText(asciiFile.getName() + "\n");
				selectedFile = asciiFile;
			}
		});

		sendToServerBtn.setOnAction(event -> {
			try {
				MapProperties mp = parseMapProperties();

				if (mp != null)
					if (deleteMapCheckBox.isSelected()) {
						DeleteMapMessage dmm = new DeleteMapMessage(mp);

						sendMessageToServer(dmm);
					} else if (selectedFile != null) {
						byte[] fileAsBytes = Files.readAllBytes(selectedFile.toPath());
						AsciiFileMessage afm = new AsciiFileMessage(mp, fileAsBytes, overwriteCheckBox.isSelected());

						message.setText("Generating map: " + mp.toString());
						sendMessageToServer(afm);
						message.setText("Done processing map.");
					} else // Delete map option unselected && selectedFile == null.
						errorAlert("Unselected ASCII", "You must select an ASCII file.", "Please select an ASCII file to upload, and try again.");
			} catch (Exception e) {
				errorAlert("Cannot Construct Server Message", "Something is wrong with your selection:", e.getMessage());
			}
		});

		deleteMapCheckBox.setOnAction(event -> {
			setVisibilityOnDeleteCheckBox(!deleteMapCheckBox.isSelected());
		});

		goToMultipleAsciiScreenBtn.setOnAction(event -> {
			parentController.goToUploadMultipleAsciiScreen();
		});
	}

	/**
	 * Helper method to flip visibility of selectFilesBtn, overwriteCheckBox, and selectedFilesTextArea.
	 * 
	 * @param isVisible
	 *           true to make Nodes visible; false to hide them.
	 */
	private void setVisibilityOnDeleteCheckBox(boolean isVisible) {
		selectFilesBtn.setVisible(isVisible);
		overwriteCheckBox.setVisible(isVisible);
		selectedFilesTextArea.setVisible(isVisible);
	}

	/**
	 * Helper to parse map properties from user selectable fields.
	 * 
	 * @return A map properties, if all fields are valid, null if an Exception is thrown. In the event an Exception is thrown, the user will receive an error alert.
	 */
	private MapProperties parseMapProperties() {
		if (regionCB.getSelectionModel().isEmpty() || compoundCB.getSelectionModel().isEmpty())
			errorAlert("Unselected Fields", "All fields must be selected.", "Please fill out all the map properties and try again.");

		MapProperties mp = null;
		try {
			MapRegionType mr = regionCB.getSelectionModel().getSelectedItem();
			MapCompoundType mc = compoundCB.getSelectionModel().getSelectedItem();
			int year = Integer.parseInt(yearTextField.getText());
			int month = yearlyMapCheckBox.isSelected() ? -1 : Integer.parseInt(monthTextField.getText());
			mp = month == -1 ? new MapProperties(mr, mc, year) : new MapProperties(mr, mc, year, month);
		} catch (Exception e) {
			errorAlert("Cannot Construct Map Properties", "Something is wrong with your inputted information:", e.getMessage());
		}

		return mp;
	}

	/**
	 * Populates the region and compound combo boxes based on values from src-shared.utils enums.
	 */
	private void populateComboBoxes() {
		for (MapRegionType mr : MapRegionType.values())
			regionCB.getItems().add(mr);

		for (MapCompoundType mc : MapCompoundType.values())
			compoundCB.getItems().add(mc);
	}
}