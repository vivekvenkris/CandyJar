package application;

import javafx.scene.control.TextField;

public class CopyableLabel extends TextField {
	
	public CopyableLabel(String text) {
		super(text);
		this.setEditable(false);
		this.getStyleClass().add("copyable-label");
	}
	
	public CopyableLabel(Object obj) {
		this(obj.toString());

	}
	
}
