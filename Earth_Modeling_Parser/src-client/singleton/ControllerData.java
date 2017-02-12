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
 *         The purpose of this program is to store a reference to the current controller so that other threads can set public state data.
 */

package singleton;

import login.LoginScreenController;
import view.MainController;

public class ControllerData {
	// Check current controller.
	String currentController;

	// All possible controllers.
	LoginScreenController loginScreenController;

	/**
	 * Default constructor to use in singleton.
	 */
	public ControllerData() {
	}

	/**
	 * Set the current controller so that it can be accessed by other pages.
	 * 
	 * @param currentController
	 *           The currently active controller.
	 */
	public void setCurrentController(String currentController) {
		this.currentController = currentController;
	}

	/**
	 * Lets you know what the current controller is as set by the MainController.
	 * 
	 * @return A String representing the current controller.
	 */
	public String getCurrentController() {
		return currentController;
	}

	/**
	 * Allows you to set a LoginScreenControllerController.
	 * 
	 * @param controller
	 *           The controller that you want to assign.
	 * @return True if the passed controller was set; false if it was determined not to be a LoginScreenController.
	 */
	public boolean setLoginScreenController(LoginScreenController controller) {
		if (controller instanceof LoginScreenController) {
			loginScreenController = controller;
			return true;
		}

		// Implied else.
		return false;
	}

	/**
	 * @return LoginScreenController if it is the currently active screen. Otherwise you will get a null.
	 */
	public LoginScreenController getLoginScreenController() {
		if (currentController.equals(MainController.LOGIN_SCREEN))
			return loginScreenController;
		else
			return null;
	}
}