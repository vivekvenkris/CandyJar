package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.controlsfx.glyphfont.Glyph;

import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.AxisMode;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.plugins.Zoomer.ZoomState;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class PointsMarker extends Zoomer{ 
    private static final String FONT_AWESOME = "FontAwesome";
    private static final int FONT_SIZE = 20;

    private  Button shortlistButton; 
    private  Button filterButton;
    private Button resetButton;

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
    
    @Override
    public HBox getZoomInteractorBar() {
    	
    	/* had to initialise these here because this function is called when constructing the super class */ 
    	
    	shortlistButton = new Button("mark",new Glyph(FONT_AWESOME, "\uf022").size(FONT_SIZE));
        filterButton  = new Button("filter",  new Glyph(FONT_AWESOME, "\uf0b0").size(FONT_SIZE));
        resetButton = new Button("reset",  new Glyph(FONT_AWESOME, "\uf071").size(FONT_SIZE));
    	HBox buttonBar = super.getZoomInteractorBar();

    	if(shortlistButton == null) {
    		return buttonBar;
    	}
    	
        shortlistButton.setPadding(new Insets(3, 3, 3, 3));
        shortlistButton.setTooltip(new Tooltip("Mark observations with a different symbol"));
        
        filterButton.setPadding(new Insets(3, 3, 3, 3));
        filterButton.setTooltip(new Tooltip("Filter only these candidates"));

        resetButton.setPadding(new Insets(3, 3, 3, 3));
        resetButton.setTooltip(new Tooltip("Reset filters and markers"));
        
        buttonBar.getChildren().addAll(shortlistButton, resetButton);
 
    	return buttonBar;
    }
    
    
	public Button getShortlistButton() {
		return shortlistButton;
	}



	public Button getFilterButton() {
		return filterButton;
	}

	

	public Button getResetButton() {
		return resetButton;
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
