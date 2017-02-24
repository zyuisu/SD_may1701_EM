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
 *         This defined a networked controller. It allows a NetworkListener to inject values or alerts onto a screen (from the server).
 */

package framework;

import java.io.File;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import view.MainController;

public class AbstractNetworkedScreenController implements IControlledScreen {
	// So we can set the screen's parent later on.
	protected MainController parentController;

	/**
	 * Displays an error notification to the user. Basically acts as a wrapper for Alert.
	 * 
	 * @param title
	 *           The text to be displayed in the title bar.
	 * @param header
	 *           The text to be displayed in the Alert's window.
	 * @param content
	 *           The text to be displayed in the Alert's content box.
	 */
	public void errorAlert(String title, String header, String content) {
		generateAlert(title, header, content, AlertType.ERROR);
	}

	/**
	 * Displays an information notification to the user. Basically acts as a wrapper for Alert.
	 * 
	 * @param title
	 *           The text to be displayed in the title bar.
	 * @param header
	 *           The text to be displayed in the Alert's window.
	 * @param content
	 *           The text to be displayed in the Alert's content box.
	 */
	public void informationAlert(String title, String header, String content) {
		generateAlert(title, header, content, AlertType.INFORMATION);
	}

	/**
	 * Displays a warning notification to the user. Basically acts as a wrapper for Alert.
	 * 
	 * @param title
	 *           The text to be displayed in the title bar.
	 * @param header
	 *           The text to be displayed in the Alert's window.
	 * @param content
	 *           The text to be displayed in the Alert's content box.
	 */
	public void warningAlert(String title, String header, String content) {
		generateAlert(title, header, content, AlertType.WARNING);
	}

	/**
	 * Helper method to generate basic alerts.
	 * 
	 * @param title
	 *           The text to be displayed in the title bar.
	 * @param header
	 *           The text to be displayed in the Alert's window.
	 * @param content
	 *           The text to be displayed in the Alert's content box.
	 * @param type
	 *           The AlertType that you wish to broadcast.
	 */
	protected void generateAlert(String title, String header, String content, AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	/**
	 * Prompt the user to select a file from their system explorer.
	 * 
	 * @param promptTitle
	 *           The title of the prompt.
	 * @param node
	 *           The Node that you wish the prompt to be generated on. (via its getScene().getWindow())
	 * @return The file that the usre selected; null if they didn't select anything (hit the cancel button).
	 */
	protected File promptUserForFile(String promptTitle, Node node) {
		return promptUserForFile(promptTitle, node, null);
	}

	/**
	 * Prompt the user to select a file from their system explorer.
	 * 
	 * @param promptTitle
	 *           The title of the prompt.
	 * @param node
	 *           The Node that you wish the prompt to be generated on. (via its getScene().getWindow())
	 * @param filters
	 *           A list of ExtensionFilters, if you wish to limit the visible and allowed files for a user to select.
	 * @return The file that the user selected; null if they didn't select anything (hit the cancel button).
	 */
	protected File promptUserForFile(String promptTitle, Node node, ExtensionFilter[] filters) {
		FileChooser fc = new FileChooser();
		fc.setTitle(promptTitle);

		if (filters != null)
			for (ExtensionFilter f : filters)
				fc.getExtensionFilters().add(f);

		return fc.showOpenDialog(node.getScene().getWindow());
	}

	/**
	 * This method will allow for the injection of each screen's parent.
	 */
	@Override
	public void setScreenParent(AbstractMainScreenController screenParent) {
		parentController = (MainController) screenParent;
	}
}
