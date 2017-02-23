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
 * This is the controller of controllers. Its job is to seamlessly manage handoffs between deifferent FXML pages and their respective controllers.
 * 
 * We create this by simply implementing the AbstractScreenController we created earlier (easy enough).
 */

package view;

import framework.AbstractMainScreenController;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Parent;
import javafx.util.Duration;
import login.LoginScreenController;
import singleton.MainModel;

public class MainController extends AbstractMainScreenController {
	// Constants that represent the screen names and locations in the workspace.
	// I assume that anyone that builds upon this application can be well behaved and doesn't need enums.
	public static final String LOGIN_SCREEN_FXML = "/login/LoginScreen.fxml";

	/**
	 * Method so that we can dynamically access the login screen at runtime.
	 */
	public void goToLoginScreen() {
		try {
			LoginScreenController controller = (LoginScreenController) loadScreen(LOGIN_SCREEN_FXML);
			MainModel.getModel().getControllerData().setCurrentController(controller);
		} catch (Exception e) {
			// DEBUG
			System.out.println("Error trying to load the login screen. This is likely an issue with its controller AND/OR FXML dependencies.\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Make sure the current page gets to destroy its elements if it has something to destroy. This will be called by the main application Stage.
	 */
	public void closeApplication() {
		if (destroyableController != null)
			destroyableController.onDestroy();
	}

	/**
	 * Replaces the page. We will override this when we extend this class because we are fancy and want style (animations).
	 * 
	 * @param loadScreen
	 *           The page that you wish to display.
	 */
	@Override
	protected void displayPage(final Parent screen) {
		// For transition effects.
		final DoubleProperty opacity = opacityProperty();

		// Check if a screen is being displayed.
		if (!getChildren().isEmpty()) {
			Timeline fade = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)), new KeyFrame(Duration.millis(275), action -> {
				// Remove the displayed screen.
				getChildren().remove(0);

				// Display the passed screen.
				getChildren().add(0, screen);

				Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)), new KeyFrame(Duration.millis(225), new KeyValue(opacity, 1.0)));

				fadeIn.play();
			}, new KeyValue(opacity, 0.0)));

			fade.play();

		} else {
			setOpacity(0.0);

			// There is nothing being displayed, just show the passed screen.
			getChildren().add(screen);

			Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)), new KeyFrame(Duration.millis(1000), new KeyValue(opacity, 1.0)));

			fadeIn.play();
		}
	}
}