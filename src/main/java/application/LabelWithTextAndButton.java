package application;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LabelWithTextAndButton {
	
	public Label label;
	public TextField textField;
	public Button button;
	public HBox hBox;
	@Override
	public String toString() {
		return textField.getText();
	}
	public LabelWithTextAndButton(String labelStr, String textFieldStr) {
		label = new Label(labelStr);
		textField = new TextField(textFieldStr);
		hBox = new HBox(10,label,textField);
	}
	
	public LabelWithTextAndButton(String labelStr, String textFieldStr, Integer padding) {
		label = new Label(labelStr);
		textField = new TextField(textFieldStr);
		hBox = new HBox(padding,label,textField);
	}
	
	
	public LabelWithTextAndButton(String labelStr, String textFieldStr, String buttonStr) {
		label = new Label(labelStr);
		textField = new TextField(textFieldStr);
		this.button = new Button(buttonStr);
		hBox = new HBox(10,label,textField,button);
		
	}
	
	public LabelWithTextAndButton(String labelStr, String textFieldStr, String buttonStr, Integer padding) {
		label = new Label(labelStr);
		textField = new TextField(textFieldStr);
		this.button = new Button(buttonStr);
		hBox = new HBox(padding,label,textField,button);
	}
	
	
	public void setVisible(Boolean value){
		label.setVisible(value);
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
	
	
	
	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
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
