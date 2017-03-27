/*
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
 *         This program is our handler for BrowseLogsScreen.fxml.
 */

package browseLogs;

import framework.AbstractNetworkedScreenController;
import framework.IMessageReceivable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import networking.LogMessage;

public class BrowseLogsScreenController extends AbstractNetworkedScreenController implements IMessageReceivable {
	@FXML
	private TextArea messageTextArea;
	@FXML
	private ListView<String> selectLogsListView;
	@FXML
	private Button backBtn;

	/**
	 * Initializes the controller class. Automatically called after the FXML file has been loaded.
	 */
	@FXML
	public void initialize() {
		// Initialize selectable logs.
		try {
			sendMessageToServer(new LogMessage(LogMessage.Type.LIST_OF_LOGS_REQUEST));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		selectLogsListView.getSelectionModel().selectedItemProperty().addListener((event, oldValue, newValue) -> {
			try {
				sendMessageToServer(new LogMessage(LogMessage.Type.LOG_REQUEST, newValue));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});

		backBtn.setOnAction(event -> {
			parentController.goToUploadAsciiScreen();
		});
	}

	/**
	 * Designed to output log information from the server to the user via a text area.
	 * 
	 * @param msg
	 *           A LogMessage of type response containing the information that the client requested.
	 */
	@Override
	public void outputMessage(Object msg) {

		if (msg instanceof LogMessage) {
			LogMessage lm = (LogMessage) msg;

			if (lm.isListOfLogsResponse())
				selectLogsListView.getItems().setAll(lm.getListOfLogsResponse());
			else if (lm.isLogResponse())
				messageTextArea.appendText(lm.getLogResponse());
		} else
			errorAlert("Communication Error", "Server is sending a message of an unexpected type.", "Check the server logs for additional information.");
	}
}
