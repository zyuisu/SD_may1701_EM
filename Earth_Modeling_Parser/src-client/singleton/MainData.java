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
 *         The purpose of this program is to store MainView's application state data.
 */

package singleton;

import javafx.stage.Stage;
import view.MainController;

public class MainData {
	// Store reference to our mainController for GUI logic.
	private MainController mainController;

	// Store a reference to our primaryStage for dialogs.
	private Stage primaryStage;

	/**
	 * Default constructor to use in singleton.
	 */
	public MainData() {
	}

	/**
	 * Sets the main controller.
	 * 
	 * @param mainController
	 *           The main controller.
	 */
	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

	/**
	 * Gets the main controller
	 * 
	 * @return A valid MainController object.
	 */
	public MainController getMainController() {
		return mainController;
	}

	/**
	 * Sets the primary Stage.
	 * 
	 * @param primaryStage
	 *           The primary stage of the application.
	 */
	public void setMainStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	/**
	 * Gets the primaryStage.
	 * 
	 * @return The main stage for this application.
	 */
	public Stage getMainStage() {
		return primaryStage;
	}
}