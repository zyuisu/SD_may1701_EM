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
 *         This program is our handler for LoginScreen.fxml.
 */

package login;

import java.io.File;

import framework.AbstractScreenController;
import framework.IControlledScreen;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import networking.NetworkHandler;
import view.MainController;

public class LoginScreenController implements IControlledScreen {
	// PUBLIC CONSTANTS THAT WILL NEED TO BE UPDATED WHEN SERVER FIELDS CHANGE.
	public final String SERVER_ADDRESS = "localhost";
	// public final String SERVER_ADDRESS = "proj-se491.cs.iastate.edu";

	// Functional components.
	@FXML
	private Button loginButton;
	@FXML
	private Label message;
	@FXML
	private TextField usernameTextField;
	@FXML
	private PasswordField passwordField;

	// So we can set the screen's parent later on.
	MainController parentController;

	/**
	 * Initializes the controller class. Automatically called after the FXML file has been loaded.
	 */
	@FXML
	public void initialize() {
		// Event handlers for buttons.
		// The arrow means lambda expression in Java.
		// Lambda expressions allow you to create anonymous methods, which is perfect for eventHandling.
		loginButton.setOnAction(event -> {
			String username = usernameTextField.getText().trim();
			String password = passwordField.getText().trim();

			if (username.equals("") || password.equals(""))
				errorAlert("Invalid Login Fields", "Your username or password is blank.", "Please enter a valid username and password.");
			else {
				FileChooser fc = new FileChooser();
				fc.setTitle("Select KeyStore");
				fc.setSelectedExtensionFilter(new ExtensionFilter("KeyStore File", "*.jks"));
				File keyStoreFile = fc.showOpenDialog(loginButton.getScene().getWindow());

				if (keyStoreFile != null)
					try {
						NetworkHandler handler = new NetworkHandler(username, password, keyStoreFile, password); // keystore master password == user password for now.
					} catch (Exception e) {
						errorAlert("Error Connecting to VEMS", e.getMessage(), "Please correct the issue and try again.");
					}
			}
		});
	}

	/**
	 * Displays an error notification to the user. Basiclaly acts as a wrapper for Alert.
	 * 
	 * @param title
	 *           The text to be displayed in the title bar.
	 * @param header
	 *           The text to be displayed in the Alert's window.
	 * @param content
	 *           The text to be displayed in the Alert's content box.
	 */
	private void errorAlert(String title, String header, String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * This method will allow for the injection of each screen's parent.
	 */
	@Override
	public void setScreenParent(AbstractScreenController screenParent) {
		parentController = (MainController) screenParent;
	}
}