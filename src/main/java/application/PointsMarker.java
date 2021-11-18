package application;

import java.util.Arrays;

import org.controlsfx.glyphfont.Glyph;

import data_holders.Candidate.CANDIDATE_TYPE;
import de.gsi.chart.axes.AxisMode;
import de.gsi.chart.plugins.Zoomer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;

public class PointsMarker extends Zoomer{ 
    private static final String FONT_AWESOME = "FontAwesome";
    private static final int FONT_SIZE = 20;

    private Button shortlistButton; 
    private Button filterButton;
    private Button shortlistResetButton;
    private Button filterResetButton;
    
    private HBox toolBar;
    
	private ComboBox<CANDIDATE_TYPE> classifyBox;
	
	private Button classifyButton;

    public PointsMarker() {
    	this(AxisMode.XY, false);
    	this.setSliderVisible(false);
    }
    
    public PointsMarker(final AxisMode zoomMode) {
    	super(zoomMode); 
    }
    
    public PointsMarker(final AxisMode zoomMode, final boolean animated) {
    	super(zoomMode, animated);
    }
    
    public void resetClassificationBox() {
    	toolBar.getChildren().remove(classifyBox);
        classifyBox = new ComboBox<CANDIDATE_TYPE>(FXCollections.observableArrayList(Arrays.asList(CANDIDATE_TYPE.values())));
        classifyBox.setPromptText("CLASSIFY_AS");
    	toolBar.getChildren().add(classifyBox);
    }
    
    public HBox getCustomToolBar() {
    	shortlistButton = new Button("mark",new Glyph(FONT_AWESOME, "\uf022").size(FONT_SIZE));
        filterButton  = new Button("filter",  new Glyph(FONT_AWESOME, "\uf0b0").size(FONT_SIZE));
        shortlistResetButton = new Button("",  new Glyph(FONT_AWESOME, "\uf28d").size(FONT_SIZE));
        filterResetButton = new Button("",  new Glyph(FONT_AWESOME, "\uf28d").size(FONT_SIZE));
        classifyBox = new ComboBox<CANDIDATE_TYPE>(FXCollections.observableArrayList(Arrays.asList(CANDIDATE_TYPE.values())));
        classifyBox.setPromptText("CLASSIFY_AS");
    	classifyButton = new Button("CLASSIFY_ALL");
    	HBox buttonBar = super.getZoomInteractorBar();

    	if(shortlistButton == null) {
    		return buttonBar;
    	}
    	Separator separator1 = new Separator();
    	separator1.setOrientation(Orientation.VERTICAL);    	
    	Separator separator2 = new Separator();
    	separator1.setOrientation(Orientation.VERTICAL);    	
    	Separator separator3 = new Separator();
    	separator1.setOrientation(Orientation.VERTICAL);
    	
        shortlistButton.setPadding(new Insets(3, 3, 3, 3));
        shortlistButton.setTooltip(new Tooltip("Mark observations with a different symbol"));
        
        filterButton.setPadding(new Insets(3, 3, 3, 3));
        filterButton.setTooltip(new Tooltip("Filter only these candidates"));

        shortlistResetButton.setPadding(new Insets(3, 3, 3, 3));
        shortlistResetButton.setTooltip(new Tooltip("Reset markers"));
        
        filterResetButton.setPadding(new Insets(3, 3, 3, 3));
        filterResetButton.setTooltip(new Tooltip("Reset filters"));
        toolBar = new HBox(separator1, shortlistButton, shortlistResetButton, separator2, filterButton, filterResetButton, separator3, classifyButton);
        return toolBar;
    }
    
    @Override
    public HBox getZoomInteractorBar() {

        HBox buttonBar = super.getZoomInteractorBar();
        buttonBar.getChildren().addAll(getCustomToolBar().getChildren());
    	return buttonBar;
    }
    
    
    
    public Button getClassifyButton() {
    	return classifyButton;
    }
    
    
	public Button getShortlistButton() {
		return shortlistButton;
	}



	public Button getFilterButton() {
		return filterButton;
	}

	

	public Button getShortlistResetButton() {
		return shortlistResetButton;
	}
	
	public Button getFilterResetButton() {
		return filterResetButton;
	}

	
	
	public ComboBox<CANDIDATE_TYPE> getClassifyBox(){
		return classifyBox;
	}
	
//    private void registerZoomerChangeListener(final Zoomer zoomer) {
//    	
//
//        zoomer.zoomStackDeque().addListener((ListChangeListener<Map<Axis, Zoomer.ZoomState>>) (change -> {
//        	
//            while (change.next()) {
//                List<? extends Map<Axis, ZoomState>> added = change.getAddedSubList();
//                if (added != null) {
//                	System.err.println("added size" + added.size());
//                    added.forEach(ch -> ch.forEach((a, s) -> System.err.println("added: " + " " + a.getSide() + " " + s.getZoomRangeMin() + " " + s.getZoomRangeMax())));
//                    zoomStates.addAll(added);
//                }
//
//                List<? extends Map<Axis, ZoomState>> removed = change.getRemoved();
//                if (removed != null) {
//                	System.err.println("removed size" + removed.size());
//
//                    removed.forEach(
//                            ch -> ch.forEach((a, s) -> System.err.println("removed: " + " " + a.getSide() + " " + s.getZoomRangeMin() + " " + s.getZoomRangeMax())));
//                    zoomStates.remove(zoomStates.size()-1);
//                }
//                
//            }
//        }));
//    }

}
