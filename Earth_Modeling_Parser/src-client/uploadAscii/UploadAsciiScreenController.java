package uploadAscii;

import framework.AbstractNetworkedScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UploadAsciiScreenController extends AbstractNetworkedScreenController {
	// Functional components.
	@FXML
	private Label message;

	/**
	 * Initializes the controller class. Automatically called after the FXML file has been loaded.
	 */
	@FXML
	public void initialize() {
		// Event handlers for buttons.
		// The arrow means lambda expression in Java.
		// Lambda expressions allow you to create anonymous methods, which is perfect for eventHandling.
		// loginButton.setOnAction(event -> {
		// }
		// });
	}
}