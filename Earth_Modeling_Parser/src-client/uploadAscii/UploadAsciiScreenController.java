package uploadAscii;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import singleton.MainModel;
import utils.MapCompoundType;
import utils.MapProperties;
import utils.MapRegion;

public class UploadAsciiScreenController extends AbstractNetworkedScreenController {
	// Functional components.
	@FXML
	private Label message;
	@FXML
	private TextArea selectedFilesTextArea;
	@FXML
	private ComboBox<MapRegion> regionCB;
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
	private CheckBox overwriteCheckBox;
	@FXML
	private CheckBox yearlyMapCheckBox;

	private ArrayList<File> selectedFiles;

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
			List<File> asciiFiles = promptUserForMultipleFiles("Select KeyStore", selectFilesBtn, filter);

			if (asciiFiles != null) {
				Iterator<File> iter = asciiFiles.iterator();
				selectedFiles = new ArrayList<File>();
				while (iter.hasNext()) {
					File f = iter.next();
					selectedFilesTextArea.appendText(f.getName() + "\n");
					selectedFiles.add(f);
				}
			}
		});

		sendToServerBtn.setOnAction(event -> {
			if (regionCB.getSelectionModel().isEmpty() || compoundCB.getSelectionModel().isEmpty())
				errorAlert("Unselected Fields", "All fields must be selected.", "Please fill out all the map properties and try again.");

			if (selectedFiles == null)
				errorAlert("Unselected ASCII", "You must select an ASCII file.", "Please select at least one ASCII file to upload, and try again.");

			for (File f : selectedFiles)
				try {
					MapRegion mr = regionCB.getSelectionModel().getSelectedItem();
					MapCompoundType mc = compoundCB.getSelectionModel().getSelectedItem();
					int year = Integer.parseInt(yearTextField.getText());
					int month = yearlyMapCheckBox.isSelected() ? -1 : Integer.parseInt(monthTextField.getText());
					MapProperties mp = new MapProperties(mr, mc, year, month);

					byte[] fileAsBytes = Files.readAllBytes(f.toPath());
					AsciiFileMessage afm = new AsciiFileMessage(mp, fileAsBytes, overwriteCheckBox.isSelected());

					MainModel.getModel().getNetworkData().getHandler().bufferAsciiFileMessage(afm);
				} catch (Exception e) {
					errorAlert("Cannot Construct Server Message", "Something is wrong with your selection:", e.getMessage());
				}
		});
	}

	/**
	 * Populates the region and compound combo boxes based on values from src-shared.utils enums.
	 */
	private void populateComboBoxes() {
		for (MapRegion mr : MapRegion.values())
			regionCB.getItems().add(mr);

		for (MapCompoundType mc : MapCompoundType.values())
			compoundCB.getItems().add(mc);
	}
}