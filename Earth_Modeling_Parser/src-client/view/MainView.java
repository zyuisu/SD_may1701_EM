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
 * Our main GUI program displays our application.
 * 
 * @author Anish Kunduru
 */

package view;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import singleton.MainModel;

public class MainView extends Application {
	@Override
	public void start(Stage primaryStage) {
		// Initialize mainController.
		MainController mainController = new MainController();

		// Add the controller to the singleton.
		MainModel.getModel().getMainData().setMainController(mainController);

		// Initialize display components.
		// Group root = new Group();
		// Pane root = new Pane();
		// Scene scene = new Scene(root, 1280, 720);
		StackPane root = new StackPane();
		root.setAlignment(Pos.CENTER);
		// root.getChildren().add(mainController);
		Scene scene = new Scene(root, 1280, 720);

		// Add mainController.
		root.getChildren().addAll(mainController);

		// Pin the root to scene and display it.
		primaryStage.setScene(scene);
		primaryStage.show();

		// Properly terminate the application if the user presses the "X" window button.
		primaryStage.setOnCloseRequest(event -> {
			mainController.closeApplication();
			stop();
		});

		// Set the title and make the application a fixed size.
		primaryStage.setTitle("Visual Earth Modelling System");
		primaryStage.setResizable(true);
		primaryStage.sizeToScene();

		// Add the stage to the singleton.
		MainModel.getModel().getMainData().setMainStage(primaryStage);

		// Go to the first screen.
<<<<<<< HEAD
		mainController.goToLoginScreen();//resize done
		//mainController.goToUploadMultipleAsciiScreen();//resize done
		//mainController.goToBrowseLogsScreen();
		//mainController.goToUploadAsciiScreen();
		
=======
		mainController.goToLoginScreen();
>>>>>>> branch 'master' of file:///U:\git\SD_may1701_EM
	}

	/**
	 * To destroy resources upon application close. Should be called in all instances of a properly closed JavaFX application.
	 */
	@Override
	public void stop() {
		if (MainModel.getModel().getNetworkData().isHandlerSet())
			MainModel.getModel().getNetworkData().closeHandler();
	}

	/**
	 * This method is actually not used in a correctly deployed JavaFX application. Instead, the start method above is called. This main serves as a fallback in case of improper configuration.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}