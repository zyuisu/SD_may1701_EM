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
 *         This is the "controller of controllers". Its job is to seamlessly manage handoffs between different FXML pages and their respective controllers.
 */

package framework;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public abstract class AbstractMainScreenController extends StackPane {
	// Destroyable will allow us to gracefully shut down dependencies and threading operations a screen may need to create.
	protected IDestroyable destroyableController;

	/**
	 * Load a screen by calling this method (to be used by the calling class that extends this).
	 * 
	 * @param fxmlLocation
	 *           The location of the FXML file in the workspace.
	 * @return a ControlledScreen type (the controller of the FXML as defined in the fx.controller link).
	 */
	protected IControlledScreen loadScreen(String fxmlLocation) {
		// Get the FXML loader and attempt to load it.
		FXMLLoader fxmlLoader = new FXMLLoader(AbstractMainScreenController.class.getResource(fxmlLocation));

		// There might be an error loading the FXML.
		try {
			Parent loadScreen = (Parent) fxmlLoader.load();

			// Set on destroy.
			if (destroyableController != null)
				destroyableController.onDestroy();

			// Replace the parent.
			displayPage(loadScreen);
		} catch (IOException ioe) {
			System.out.println("Check to make sure the controller is properly defined. Is the FXML linked? Are you importing the correct fields?");
			System.out.println("There was an error loading the FXML: " + ioe.getMessage() + "\n");
			ioe.printStackTrace();
		}

		// Get our controller from the FXML.
		IControlledScreen controller = (IControlledScreen) fxmlLoader.getController();

		// Only attempt to destroy a page if it has implemented destroyable.
		if (controller instanceof IDestroyable)
			destroyableController = (IDestroyable) controller;
		else
			destroyableController = null;

		// Set the parent.
		controller.setScreenParent(this);

		return controller;
	}

	/**
	 * Load a screen by calling this method. The passed Parent must enable the ControlledScreen interface.
	 * 
	 * @param loadScreen
	 *           The screen that you wish to load.
	 */
	protected void loadScreen(Parent loadScreen) {
		// Make sure the calling class knows what's up.
		if (loadScreen instanceof IControlledScreen)
			((IControlledScreen) loadScreen).setScreenParent(this);
		else
			throw new ClassCastException("You need to implement ControlledScreen!");

		displayPage(loadScreen);
	}

	/**
	 * Replaces the page. We will override this when we extend this class because we are fancy and want style (animations).
	 * 
	 * @param loadScreen
	 *           The page that you wish to display.
	 */
	protected void displayPage(Parent loadScreen) {
		if (!getChildren().isEmpty())
			getChildren().remove(0); // KILL EVERYTHING! :)

		getChildren().add(loadScreen); // IT'S ALIVE!
	}
}