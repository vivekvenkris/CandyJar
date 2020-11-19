package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import constants.Constants;
import data_holders.Beam;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class MyScatterChart extends ScatterChart<Number, Number> {
	private final List<Node> ellipseList = new ArrayList<Node>();

	private Map<String, Node> ellipseMap = new LinkedHashMap<String, Node>();

	public List<Node> getEllipseList() {
		return ellipseList;
	}

	public MyScatterChart(Axis<Number> xAxis, Axis<Number> yAxis) {
		super(xAxis, yAxis);
	}

	@Override
	protected void layoutPlotChildren() {
		
		getPlotChildren().removeAll(ellipseList);
		ellipseList.clear();
		ellipseMap.clear();
		
		NumberAxis xAxis = (NumberAxis) getXAxis();
		NumberAxis yAxis = (NumberAxis) getYAxis();
		
		double xScale = xAxis.getScale();
		double yScale = yAxis.getScale();
		
		
		ObservableList<Series<Number, Number>> observableList = getData();
		Map<String, ArrayList<Series<Number, Number>>> seriesMap = new HashMap<String, ArrayList<Series<Number, Number>>>();
		
		
		for(Series<Number, Number> series: observableList){
			
			ArrayList<Series<Number, Number>> groupList = seriesMap.getOrDefault(series.getName(), new ArrayList<Series<Number, Number>>());
			groupList.add(series);
			seriesMap.put(series.getName(), groupList);

		}
		
		for(String name: Constants.plotOrder) {
			
			ArrayList<Series<Number, Number>> list = seriesMap.get(name);
			if(list ==null || list.isEmpty()) continue;
			
			
			for(Series<Number, Number> series: list) {
				
				
				for(Data<Number, Number> d: series.getData()) {
					
					double x1 = getXAxis().getDisplayPosition(d.getXValue());
					double y1 = getYAxis().getDisplayPosition(d.getYValue());
					
					if(x1 == 0 && y1 == 0 ) continue;
					
					Beam b = (Beam) d.getExtraValue();
					

					
					double radiusX = b.getEllipseConfig().getBeamX().getDecimalHourValue() * xScale;
					double radiusY = b.getEllipseConfig().getBeamY().getDegreeValue() * yScale;
					
	

					
					Ellipse e = new Ellipse(x1,y1, radiusX, radiusY );
					
					Rotate rotate = new Rotate();
					rotate.setAngle(b.getEllipseConfig().getBeamAngle().getDegreeValue());
					rotate.setPivotX(x1);
					rotate.setPivotY(y1);
					
					Integer beamNo = Integer.parseInt(b.getName().replaceAll("cfbf", ""));
					
					e.getTransforms().add(rotate);
					if(name.equals(Constants.DEFAULT_BEAM_MAP)) {
						e.getStyleClass().addAll("ellipse", "ellipse:hover", "ellipse:pressed");

					}
					else if (name.equals(Constants.CANDIDATE_BEAM_MAP)) {
						e.getStyleClass().addAll("candidate");

					}
					e.toFront();
					ellipseList.add(e);
					ellipseMap.put(b.getName(), e);
					getPlotChildren().add(e); 
					e.setUserData(b);
					
					e.setOnMouseClicked(new EventHandler<Event>() {

						@Override
						public void handle(Event event) {
							
							Ellipse clickedEllipse = (Ellipse) event.getSource();
							Beam clickedBeam = (Beam) clickedEllipse.getUserData();
							
							for(Beam neighbour: clickedBeam.getNeighbourBeams()) {
								
								Ellipse neighbourEllipse = (Ellipse) ellipseMap.get(neighbour.getName());
								
								neighbourEllipse.getStyleClass().clear();
								neighbourEllipse.getStyleClass().addAll("neighbour");
							}
							
						}
					});		
					
					
					//Text txt = new Text(x1, y1, beamNo.toString());
					//ellipseList.add(txt);
					//getPlotChildren().add(txt); 


					
					
					Tooltip t = new Tooltip(beamNo.toString());
		    		t.setShowDelay(Duration.millis(10));
		    		Tooltip.install(e,t);
		    		
	
					
				}
				
			}
						
		}
		
	

		super.layoutPlotChildren();
	}

	
}
