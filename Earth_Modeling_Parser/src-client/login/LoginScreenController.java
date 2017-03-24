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

import framework.AbstractNetworkedScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser.ExtensionFilter;
import networking.NetworkHandler;
import singleton.MainModel;

public class LoginScreenController extends AbstractNetworkedScreenController {
	@FXML
	private Button loginButton;
	@FXML
	private TextField usernameTextField;
	@FXML
	private PasswordField passwordField;

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
				ExtensionFilter[] filter = { new ExtensionFilter("KeyStore File", "*.jks") };
				File keyStoreFile = promptUserForFile("Select KeyStore", loginButton, filter);

				if (keyStoreFile == null)
					errorAlert("Unable to Authenticate", "You must provide your key to authenticate with the server.", "Please select a keystore file and try again.");
				else
					try {
						NetworkHandler handler = new NetworkHandler(username, password, keyStoreFile, password); // keystore master password == user password for now.

						if (handler.getLogin()) {
							MainModel.getModel().getNetworkData().setNewHandler(handler);
							parentController.goToUploadAsciiScreen();
						} else
							errorAlert("Invalid Credentials", "The server rejected your username or password!", "Please re-enter your username and password and try again.");
					} catch (Exception e) {
						errorAlert("Error Connecting to VEMS", e.getMessage(), "Please correct the issue and try again.");
					}

			}
		});

		passwordField.setOnAction(event -> {
			loginButton.fire();
		});
	}
}