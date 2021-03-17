package application;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.ChartPlugin;
import de.gsi.chart.plugins.MouseEventsHelper;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.DataSet;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class PointsClicker extends ChartPlugin{
	
    public static final int DEFAULT_PICKING_DISTANCE = 5;

    private static boolean shiftDown;
    private static boolean controlDown;
    
    
    
    protected Cursor originalCursor;


    protected final Predicate<MouseEvent> defaultSelectFilter = event -> 
    MouseEventsHelper.isOnlyPrimaryButtonDown(event) && event.isControlDown() && event.isShiftDown() && isMouseEventWithinCanvas(event);
    
    
    private final EventHandler<KeyEvent> keyReleasedHandler = keyEvent -> {
        if (keyEvent.getCode() == KeyCode.CONTROL) {
            controlDown = false;
            keyEvent.consume();
        }

        if (keyEvent.getCode() == KeyCode.SHIFT) {
            shiftDown = false;
            keyEvent.consume();
        }
        
        if(!controlDown || !shiftDown) {
        	 uninstallCursor();
        }            

    };
    
    
    private final EventHandler<KeyEvent> keyPressedHandler = keyEvent -> {
        if (keyEvent.getCode() == KeyCode.CONTROL) {
            controlDown = true;
            keyEvent.consume();
        }

        if (keyEvent.getCode() == KeyCode.SHIFT) {
            shiftDown = true;
            keyEvent.consume();
        }
        
        if(controlDown && shiftDown) {
        	installCursor();
        }            


    };


    
    public PointsClicker(){
    	
    	registerMouseHandlers();
        registerKeyHandlers();
    	
    }
    
    protected boolean isMouseEventWithinCanvas(final MouseEvent mouseEvent) {
        final Canvas canvas = getChart().getCanvas();
        // listen to only events within the canvas
        final Point2D mouseLoc = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY());
        final Bounds screenBounds = canvas.localToScreen(canvas.getBoundsInLocal());
        return screenBounds.contains(mouseLoc);
    }

    
    private final DoubleProperty pickingDistance = new SimpleDoubleProperty(this, "pickingDistance", DEFAULT_PICKING_DISTANCE) {
        @Override
        protected void invalidated() {
            if (get() <= 0) {
                throw new IllegalArgumentException("The " + getName() + " must be a positive value");
            }
        }
    };

    

    private final EventHandler<MouseEvent> selectionStartHandler = event -> {
    	
    	
        if (defaultSelectFilter.test(event)) {
           
            event.consume();
            
            final Bounds plotAreaBounds = getChart().getPlotArea().getBoundsInLocal();
            
            Optional<DataPoint> point = findDataPoint(event, plotAreaBounds);
            
            
            
            

        }

    };
    
    
    protected String getDataLabelSafe(final DataSet dataSet, final int index) {
        String labelString = dataSet.getDataLabel(index);
        if (labelString == null) {
            return String.format("%s [%d]", dataSet.getName(), index);
        }
        return labelString;
    }

    
    protected void installCursor() {
        final Region chart = getChart();
        originalCursor = chart.getCursor();
        chart.setCursor(Cursor.HAND);
    }
    
    protected void uninstallCursor() {
        getChart().setCursor(originalCursor);
    }
    
    private void registerKeyHandlers() {
        registerInputEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        registerInputEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }
    

    private void registerMouseHandlers() {
        registerInputEventHandler(MouseEvent.MOUSE_PRESSED, selectionStartHandler);
     
    }
    

    private Optional<DataPoint> findDataPoint(final MouseEvent event, final Bounds plotAreaBounds) {
        if (!plotAreaBounds.contains(event.getX(), event.getY())) {
            return Optional.empty();
        }

        final Point2D mouseLocation = getLocationInPlotArea(event);

        return findNearestDataPointWithinPickingDistance(mouseLocation);
    }

    private Optional<DataPoint> findNearestDataPointWithinPickingDistance(final Point2D mouseLocation) {
        final Chart chart = getChart();
        if (!(chart instanceof XYChart)) {
            return Optional.empty();
        }
        final XYChart xyChart = (XYChart) chart;
        return xyChart.getRenderers().stream() // for all renderers
                .flatMap(r -> Stream.of(r.getDatasets(), xyChart.getDatasets()).flatMap(List::stream) // combine global and renderer specific Datasets
                                      .flatMap(d -> getPointsCloseToCursor(d, r, mouseLocation))) // get points in range of cursor
                .reduce((p1, p2) -> p1.distanceFromMouse < p2.distanceFromMouse ? p1 : p2); // find closest point
    }

    private Stream<DataPoint> getPointsCloseToCursor(final DataSet d, final Renderer r, final Point2D mouseLocation) {
        // Get Axes for the Renderer
        final Axis xAxis = r.getAxes().stream().filter(ax -> ax.getSide().isHorizontal()).findFirst().orElse(null);
        final Axis yAxis = r.getAxes().stream().filter(ax -> ax.getSide().isVertical()).findFirst().orElse(null);
        if (xAxis == null || yAxis == null) {
            return Stream.empty(); // ignore this renderer because there are no valid axes available
        }
      
        // get the screen x coordinates and dataset indices between which points can be in picking distance
        final double xMin = xAxis.getValueForDisplay(mouseLocation.getX() - getPickingDistance());
        final double xMax = xAxis.getValueForDisplay(mouseLocation.getX() + getPickingDistance());
        final boolean sorted = r instanceof ErrorDataSetRenderer && ((ErrorDataSetRenderer) r).isAssumeSortedData();
        final int minIdx = sorted ? Math.max(0, d.getIndex(DataSet.DIM_X, xMin) - 1) : 0;
        final int maxIdx = sorted ? Math.min(d.getDataCount(), d.getIndex(DataSet.DIM_X, xMax) + 1) : d.getDataCount();
        return IntStream.range(minIdx, maxIdx) // loop over all candidate points
                .mapToObj(i -> getDataPointFromDataSet(r, d, i, xAxis, yAxis, mouseLocation)) // get points with distance to mouse
                .filter(p -> p.distanceFromMouse <= getPickingDistance()); // filter out points which are too far away
    }
    
    public final DoubleProperty pickingDistanceProperty() {
        return pickingDistance;
    }
    
    public final double getPickingDistance() {
        return pickingDistanceProperty().get();
    }
    
    private DataPoint getDataPointFromDataSet(final Renderer renderer, final DataSet d, final int i, final Axis xAxis, final Axis yAxis, final Point2D mouseLocation) {
        final DataPoint point = new DataPoint(renderer, d.get(DataSet.DIM_X, i), d.get(DataSet.DIM_Y, i), getDataLabelSafe(d, i));
        final double x = xAxis.getDisplayPosition(point.x);
        final double y = yAxis.getDisplayPosition(point.y);
        final Point2D displayPoint = new Point2D(x, y);
        point.distanceFromMouse = displayPoint.distance(mouseLocation);
        return point;
    }

    public static class DataPoint {
        public final Renderer renderer;
        public final double x;
        public final double y;
        public final String label;
        public double distanceFromMouse;

        protected DataPoint(final Renderer renderer, final double x, final double y, final String label) {
            this.renderer = renderer;
            this.x = x;
            this.y = y;
            this.label = label;
        }
        
        @Override
        public String toString() {
        	// TODO Auto-generated method stub
        	return label + ": " + x + " " + y + " " + distanceFromMouse;
        }
    }
   
}
