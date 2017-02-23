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
 *         This interface needs to implemented by all the screen controllers so that we can set multiple screens seamlessly.
 */

package framework;

public interface IControlledScreen {
	/**
	 * This method will allow us to pull the parent screen. (Allow injection of the Parent type).
	 * 
	 * @param screenController
	 *           The parent controller of our custom abstract type.
	 */
	public void setScreenParent(AbstractMainScreenController screenController);
}