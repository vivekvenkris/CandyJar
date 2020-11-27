package application;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TextAndButton {
	public TextField textField;
	public Button button;
	public HBox hBox;
	@Override
	public String toString() {
		return textField.getText();
	}
	
	public TextAndButton(String textFieldStr, String promtText,  String buttonStr, Integer padding) {
		if(textFieldStr == null || textFieldStr.isEmpty()) {
			textField = new TextField();
		}
		else {
			textField = new TextField(textFieldStr);
		}
			
		
		textField.setPromptText(promtText);
		this.button = new Button(buttonStr);
		hBox = new HBox(padding,textField,button);
	}
	
	
	public void setVisible(Boolean value){
		textField.setVisible(value);
		if(button!=null) button.setVisible(value);
	}
	
	public void setValue(Object value){
		textField.setText(value.toString());
	}

	public  Double getDoubleValue(){
		return Double.parseDouble(textField.getText());
	}
	
	public  Integer getIntValue(){
		return Integer.parseInt(textField.getText());
	}
	

	public TextField getTextField() {
		return textField;
	}

	public void setTextField(TextField textField) {
		this.textField = textField;
	}

	public HBox gethBox() {
		return hBox;
	}

	public void sethBox(HBox hBox) {
		this.hBox = hBox;
	}
	public Button getButton() {
		return button;
	}
	public void setButton(Button button) {
		this.button = button;
	}
	
	

}
