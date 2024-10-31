module iLabFX {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.swing;
	requires java.desktop;
	requires java.datatransfer;
	
	opens Server to javafx.graphics, javafx.fxml;
	opens Client to javafx.graphics, javafx.fxml;
}
