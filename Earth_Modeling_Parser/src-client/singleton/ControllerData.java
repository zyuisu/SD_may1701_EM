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

import framework.AbstractNetworkedScreenController;

public class ControllerData {
	// All possible controllers.
	private AbstractNetworkedScreenController currentController;

	/**
	 * Default constructor to use in singleton.
	 */
	public ControllerData() {
	}

	/**
	 * Set the current controller so that it can be accessed by other pages. The loadScreen() types should be defined in MainController as MainController.NAME_SCREEN.
	 * 
	 * @param currentController
	 *           The currently active controller. This application will require all controllers to be able to handle networked requests by being of type AbstractNetworkedScreenController.
	 */
	public void setCurrentController(AbstractNetworkedScreenController controller) {
		currentController = controller;
	}

	/**
	 * Lets you know what the current controller is as set by the MainController.
	 * 
	 * @return The currently viewable controller.
	 */
	public AbstractNetworkedScreenController getCurrentController() {
		return currentController;
	}
}