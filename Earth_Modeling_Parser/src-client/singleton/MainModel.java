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
 *         The purpose of this program is to have one central model for all our user's game data. This utilizes a software design pattern called the singleton pattern. We separate each controller's game data into a new submodel that will be implemented in this class.
 */

package singleton;

public class MainModel {
	// Singleton.
	private final static MainModel model = new MainModel();

	/**
	 * @return The current instance of MainModel.
	 */
	public static MainModel getModel() {
		return model;
	}

	// Instantiate data classes to store information.
	private MainData mainData = new MainData();
	private ControllerData controllerData = new ControllerData();

	/**
	 * @return The current instance of MainData.
	 */
	public MainData currentMainData() {
		return mainData;
	}

	/**
	 * @return the current instance of ControllerData.
	 */
	public ControllerData currentControllerData() {
		return controllerData;
	}
}