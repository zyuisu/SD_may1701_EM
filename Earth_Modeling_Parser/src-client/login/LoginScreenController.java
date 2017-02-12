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

import framework.AbstractScreenController;
import framework.IControlledScreen;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import view.MainController;

public class LoginScreenController implements IControlledScreen {
	// PUBLIC CONSTANTS THAT WILL NEED TO BE UPDATED WHEN SERVER FIELDS CHANGE.
	public final String SERVER_ADDRESS = "localhost";
	// public final String SERVER_ADDRESS = "proj-se491.cs.iastate.edu";

	// Functional components.
	@FXML
	private Button loginButton;
	@FXML
   private Label errorMessage;

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
      loginButton.setOnAction(event ->
      {
      	errorMessage.setText("Don't poke me!");
      });

	}

	/**
	 * This method will allow for the injection of each screen's parent.
	 */
	@Override
	public void setScreenParent(AbstractScreenController screenParent) {
		parentController = (MainController) screenParent;
	}
}