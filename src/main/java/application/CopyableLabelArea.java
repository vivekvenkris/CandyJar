package application;

import javafx.scene.control.TextArea;

public class CopyableLabelArea extends TextArea{
	public CopyableLabelArea(String text) {
		super(text);
		this.setEditable(false);
		this.getStyleClass().add("copyable-label-area");
	}
	
	public CopyableLabelArea(Object obj) {
		this(obj.toString());

	}
}
