package application;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.controlsfx.glyphfont.Glyph;

import application.PointsClicker.DataPoint;
import data_holders.Candidate;
import data_holders.Candidate.CANDIDATE_PLOT_CATEGORY;
import data_holders.Candidate.CANDIDATE_TYPE;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.marker.DefaultMarker;
import de.gsi.chart.marker.Marker;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.MouseEventsHelper;
import de.gsi.chart.plugins.Panner;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DefaultErrorDataSet;
import de.gsi.dataset.spi.utils.Tuple;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import utilitites.Utilities;

public class ChartViewer {

	private final BorderPane mainBorderPane = new BorderPane();
	private final 	    Stage primaryStage = new Stage();
	private Scene scene = null;
	
	private CandyJar candyJar;

	Map<Integer, CandyChart> chartSection = new LinkedHashMap<Integer, CandyChart>();

	private static Map<CANDIDATE_PLOT_CATEGORY,ObservableList<Candidate>> candidatesMap = new LinkedHashMap<CANDIDATE_PLOT_CATEGORY, ObservableList<Candidate>>();
	private static Map<CANDIDATE_PLOT_CATEGORY,CandidateViewAttributes> candidateViewAttributesMap = new LinkedHashMap<CANDIDATE_PLOT_CATEGORY, CandidateViewAttributes>();
	private  List<Map<String, Tuple<Double, Double>>> markingConstraintsList = new ArrayList<Map<String,Tuple<Double,Double>>>();

	private static Integer minPointSize = 8; //pixels
	
	private List<Candidate> allCandidates = new ArrayList<Candidate>();
	
	public ChartViewer(Rectangle2D currentScreenBounds, Integer numCharts, CandyJar candyJar) {

		this.candyJar = candyJar;
		
		for(int i=0; i< numCharts; i++) {
			chartSection.put(i, new CandyChart(i));
		}

		double totalWidth = currentScreenBounds.getWidth();
		double totalHeight = currentScreenBounds.getHeight();

		configureWindow(primaryStage, currentScreenBounds.getMinX(), currentScreenBounds.getMinY(), totalWidth, totalHeight);

		candidateViewAttributesMap.put(CANDIDATE_PLOT_CATEGORY.ALL, 
				new CandidateViewAttributes(minPointSize, CANDIDATE_PLOT_CATEGORY.ALL.toString()));
		
		candidateViewAttributesMap.put(CANDIDATE_PLOT_CATEGORY.MARKED, 
				new CandidateViewAttributes(DefaultMarker.DIAMOND, minPointSize*2, CANDIDATE_PLOT_CATEGORY.MARKED.toString()));
		
		candidateViewAttributesMap.put(CANDIDATE_PLOT_CATEGORY.CURRENTLY_VIEWING, 
				new CandidateViewAttributes(Color.INDIANRED, DefaultMarker.RECTANGLE, minPointSize, CANDIDATE_PLOT_CATEGORY.CURRENTLY_VIEWING.toString()));

	}

	public void show() {
		primaryStage.show();
	}


	public void close() {
		primaryStage.close();
	}
	
	public boolean isShowing() {
		return primaryStage.isShowing();
	}
	
	public void refresh() {
		
		double totalChartSections = chartSection.size();
		for(int i=0; i< totalChartSections; i++) {
			CandyChart candyChart = chartSection.get(i);
			candyChart.plotAllCandidates();
			candyChart.getChart().requestLayout();
		}

		
	}

	public void configureWindow(Stage primaryStage, double minX, double minY, double totalWidth, double totalHeight) {


		double totalChartSections = chartSection.size();

		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		
		double spaceForEachChart = 1.0/totalChartSections;

		for(int i=0; i< totalChartSections; i++) {

			VBox vBox = chartSection.get(i).getChartVBox();

			if( vBox == null) {
				System.err.println("Internal problem configuring chart sections. ");
			}
			vBox.setPadding(new Insets(3, 3, 3, 3));
			vBox.setPrefHeight(totalHeight/totalChartSections - 20);
			vBox.setPrefWidth(totalWidth);
			splitPane.getItems().add(vBox);
			splitPane.setDividerPosition(i, (i+1)*spaceForEachChart);
		}


		mainBorderPane.setCenter(splitPane);

		scene = new Scene(mainBorderPane, totalWidth, totalHeight);
		primaryStage.setX(minX);
		primaryStage.setY(minY);
		primaryStage.setTitle("CandyChart");
		primaryStage.setScene(scene);
		primaryStage.setResizable(true);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
									event.consume();
									Alert alert = new Alert(AlertType.INFORMATION);
				
									alert.setTitle("Closing window alert");
									alert.setHeaderText("You cannot close this window.");
									alert.setContentText("Please close the primary window to close the application.");
				
									alert.showAndWait();

			}
		});
	}
	
	
	public List<Candidate> getAllCandidates() {
		return allCandidates;
	}

	public void setAllCandidates(List<Candidate> allCandidates) {
		this.allCandidates = allCandidates;
	}

	public void clear() {
		ChartViewer.candidatesMap.clear();
	}
	
	public void addToMap(CANDIDATE_PLOT_CATEGORY category, List<Candidate> candidates) {
		if(candidates.isEmpty()) {
			System.err.println("Warning no candidates to plot for category:" + category);
			return;
		}
		ChartViewer.candidatesMap.put(category, FXCollections.observableArrayList(candidates));
		if(!isShowing()) show();
		refresh();


	}
	
	public  List<Candidate> getFromMap(CANDIDATE_PLOT_CATEGORY category){
		return ChartViewer.candidatesMap.getOrDefault(category,FXCollections.observableArrayList() );
	}

	public void removeFromMap(CANDIDATE_PLOT_CATEGORY category) {
		ChartViewer.candidatesMap.remove(category);
		if(!isShowing()) show();
		refresh();


	}
	
	public void classifyCandidates(Map<String,  Tuple<Double, Double>> filterMap, CANDIDATE_TYPE type) {

		
		this.allCandidates.forEach(f -> {
			Candidate c = (Candidate)f;
			
			int num = 0;

			for(Entry<String,  Tuple<Double, Double>> entry : filterMap.entrySet()) {
				
				String key = entry.getKey();

				Function<Candidate, Tuple<Double, Double>> valueFunction = Candidate.PLOTTABLE_PARAMETERS_MAP.get(key);
				Double value = valueFunction.apply(c).getXValue();
				
				if(Utilities.valueIsWithinRange(value,entry.getValue())) {
					num++;
				}
				

			}
			
			if(num == filterMap.size()) {
				f.setCandidateType(type);
			}
			
			
			
		});
		candyJar.getFilterTypes().getCheckModel().check(candyJar.getFilterTypes().getCheckModel().getItemIndex(type));
		candyJar.getFilterCandidates().fire();

	}
	
	public void filterCandidates(Map<String,  Tuple<Double, Double>> filterMap) {
		
		this.allCandidates.forEach(f -> {
			Candidate c = (Candidate)f;
			c.setVisible(true);
			for(Entry<String,  Tuple<Double, Double>> entry : filterMap.entrySet()) {
				
				String key = entry.getKey();

				Function<Candidate, Tuple<Double, Double>> valueFunction = Candidate.PLOTTABLE_PARAMETERS_MAP.get(key);
				Double value = valueFunction.apply(c).getXValue();
				
				
				if(value < entry.getValue().getXValue() || value > entry.getValue().getYValue()) {
					c.setVisible(false);
				}

			}
			
		});
		
		candyJar.getFilterCandidates().fire();
		
	}
	
	public void resetFilters() {
		this.allCandidates.forEach(f -> f.setVisible(true));
		candyJar.getFilterCandidates().fire();

	}
	

	public void addMarkedCandidates() {
		List<Candidate> markedCandidates = FXCollections.observableArrayList();
		
		List<Candidate> allCandidates = this.allCandidates.stream().collect(Collectors.toList());
		
		
		for(Map<String, Tuple<Double, Double>> markingConstraintsMap: markingConstraintsList) {
			
			markedCandidates.addAll( allCandidates
										.stream()
										.filter(f -> {
													Candidate c = (Candidate)f;
													
													for(Entry<String,  Tuple<Double, Double>> entry : markingConstraintsMap.entrySet()) {
														String key = entry.getKey();
														// get value of the parameter for the given candidate
														Function<Candidate, Tuple<Double, Double>> valueFunction = Candidate.PLOTTABLE_PARAMETERS_MAP.get(key);
														Double value = valueFunction.apply(c).getXValue();
														
														//compare with the constraint
														if(value < entry.getValue().getXValue() || value > entry.getValue().getYValue()) return false;
														
								
														
													}
													
													return true;
												})
										.collect(Collectors.toList())
									);
		}
		
		if(!markedCandidates.isEmpty()) {
			addToMap(CANDIDATE_PLOT_CATEGORY.MARKED, markedCandidates);
			
		} else {
			removeFromMap(CANDIDATE_PLOT_CATEGORY.MARKED);
		}
		
		allCandidates.removeAll(markedCandidates);
		addToMap(CANDIDATE_PLOT_CATEGORY.ALL, allCandidates);
		
	}


	

	public void clearMap() {
		ChartViewer.candidatesMap.clear();
	}

	

	public class CandyChart {

		private XYChart chart;

		private AxisAttributes xAxisAttributes;
		private AxisAttributes yAxisAttributes;

		private boolean initialised;
		
		private Integer chartID;

		private final ComboBox<String> xAxisBox = 
				new ComboBox<String>(FXCollections.observableArrayList(Candidate.PLOTTABLE_PARAMETERS_MAP.keySet()));

		private final  ComboBox<String> yAxisBox = 
				new ComboBox<String>(FXCollections.observableArrayList(Candidate.PLOTTABLE_PARAMETERS_MAP.keySet()));

		private final  CheckBox logX = new CheckBox();
		private final  CheckBox logY = new CheckBox();
		private final  Button go = new Button("Go");
		

		private HBox controlBox = new HBox(10, new Label("X-Axis:"), xAxisBox, new Label("Log?:"), logX,
				new Label("Y-Axis:"), yAxisBox, new Label("Log?:"), logY,
				go); 
	


		public VBox getChartVBox() {
			return new VBox(10, controlBox, chart);
		}


		public CandyChart(Integer chartID) {
		
			
			this.chartID = chartID;
			
			chart = new XYChart();
			
			PointsMarker pointsMarker = new PointsMarker();
			
			DataPointTooltip dataPointTooltip = new DataPointTooltip();
			dataPointTooltip.setAddButtonsToToolBar(true);
			
			Panner panner = new Panner();
			panner.setMouseFilter(event -> MouseEventsHelper.isOnlyCtrlModifierDown(event));

			PointsClicker pointsClicker = new PointsClicker();
			
			chart.getPlugins().add(pointsMarker);
			chart.getPlugins().add(dataPointTooltip);
			chart.getPlugins().add(panner);
			chart.getPlugins().add(pointsClicker);

			HBox.setHgrow(chart, Priority.ALWAYS);
			VBox.setVgrow(chart, Priority.ALWAYS);

			go.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {

					String xParam = xAxisBox.getValue();

					Function<Candidate, Tuple<Double, Double>> xFunction = Candidate.PLOTTABLE_PARAMETERS_MAP.get(xParam);

					Boolean isXLog = logX.isSelected();

					String yParam = yAxisBox.getValue();
					Boolean isYLog = logY.isSelected();

					Function<Candidate, Tuple<Double, Double>> yFunction = Candidate.PLOTTABLE_PARAMETERS_MAP.get(yParam);

					AxisAttributes xAxisAttributes = new AxisAttributes(xParam, Candidate.PARAMETER_UNITS_MAP.get(xParam), isXLog, true, xFunction);
					AxisAttributes yAxisAttributes = new AxisAttributes(yParam, Candidate.PARAMETER_UNITS_MAP.get(yParam), isYLog, true, yFunction);
					initialiseChart(xAxisAttributes, yAxisAttributes);
					
					plotAllCandidates();

				}
			});
			
			pointsMarker.getShortlistButton().setOnAction( e-> {
																	e.consume();
																	markingConstraintsList.add(getCurrentMinMaxMap());
																	addMarkedCandidates();
															   });
		
			pointsMarker.getFilterButton().setOnAction(e-> {
																e.consume();
																filterCandidates(getCurrentMinMaxMap());
														   });
			
			pointsMarker.getShortlistResetButton().setOnAction(e-> {
																	e.consume();
																	markingConstraintsList.clear();
																	addMarkedCandidates();
															   });
			
			
			pointsMarker.getFilterResetButton().setOnAction(e -> { 
																	e.consume(); 
																	resetFilters();  
																  });
				
			
			pointsMarker.getClassifyBox().setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent event) {
						event.consume();
						
						Alert alert = new Alert(AlertType.CONFIRMATION);
						
						Map<String, Tuple<Double, Double>> currentMinMaxMap = getCurrentMinMaxMap();
						
						CANDIDATE_TYPE type = pointsMarker.getClassifyBox().getValue();
						
						String s = "You are about to classify all candidates in the following bounds as " +  type + "\n";
						
						for(Entry<String,  Tuple<Double, Double>> entry : currentMinMaxMap.entrySet()) {
							
							s += entry.getKey() + " =" + entry.getValue().getXValue() + " to " + entry.getValue().getYValue() + "\n";
						}
						
						alert.setTitle("Are you sure?");
						alert.setContentText(s);

						Optional<ButtonType> result = alert.showAndWait();
						
						if(result != null && result.get() == ButtonType.OK) {
							
							classifyCandidates(currentMinMaxMap, type);
							
						}
						
						
						pointsMarker.getClassifyBox().setPromptText("CLASSIFY_AS");
						
					}
				});
			
			 
		
			 

		}
		
		private Map<String, Tuple<Double, Double>> getCurrentMinMaxMap() {
			
			Axis xAxis = chart.getXAxis();
			Axis yAxis = chart.getYAxis();
			
			Map<String, Tuple<Double, Double>> minMaxMap = new HashMap<String, Tuple<Double,Double>>();
			
			minMaxMap.put(xAxis.getName(), new Tuple<Double, Double>(xAxis.getMin(), xAxis.getMax()));
			minMaxMap.put(yAxis.getName(), new Tuple<Double, Double>(yAxis.getMin(), yAxis.getMax()));
			
			return minMaxMap;
			
		}

		public void initialiseChart(AxisAttributes xAxisAttributes, AxisAttributes yAxisAttributes) {

			chart.getAllDatasets().clear();
			chart.getAxes().clear();

			this.xAxisAttributes = xAxisAttributes;
			this.yAxisAttributes = yAxisAttributes;

			DefaultNumericAxis xAxis = new DefaultNumericAxis(xAxisAttributes.getLabel(), xAxisAttributes.getUnit());
			DefaultNumericAxis yAxis = new DefaultNumericAxis(yAxisAttributes.getLabel(), yAxisAttributes.getUnit());

			xAxis.setLogAxis(xAxisAttributes.getLog());
			yAxis.setLogAxis(yAxisAttributes.getLog());

			xAxis.setAutoRangePadding(xAxisAttributes.getAutoRangePadding());
			yAxis.setAutoRangePadding(yAxisAttributes.getAutoRangePadding());

			xAxis.setAutoRanging(xAxisAttributes.getAutoRanging());
			yAxis.setAutoRanging(yAxisAttributes.getAutoRanging());

			xAxis.setSide(Side.BOTTOM);
			yAxis.setSide(Side.LEFT);

			chart.getAxes().addAll(xAxis, yAxis);
			chart.setTitle(xAxisAttributes.getLabel() + " vs " + yAxisAttributes.getLabel());
			
			

			initialised = true;

		}
		
		
		public void plotAllCandidates() {
			
			if(!initialised) return;
			
			chart.getRenderers().clear();
			final ErrorDataSetRenderer errorRenderer = new ErrorDataSetRenderer();

			errorRenderer.setPolyLineStyle(LineStyle.NONE);
			errorRenderer.setErrorType(ErrorStyle.NONE);
			errorRenderer.setDrawMarker(true);
			errorRenderer.setAssumeSortedData(false); // !! important since DS is unsorted
			
			
			
			errorRenderer.setParallelImplementation(true);
			
			chart.getRenderers().add(errorRenderer);

			
			List<Double> xMins = new ArrayList<Double>();
			List<Double> xMaxs = new ArrayList<Double>();
			List<Double> yMins = new ArrayList<Double>();
			List<Double> yMaxs = new ArrayList<Double>();
			
			
			

			for(Entry<CANDIDATE_PLOT_CATEGORY, ObservableList<Candidate>> entry: candidatesMap.entrySet()) {
				CandidateViewAttributes candidateViewAttributes = candidateViewAttributesMap.get(entry.getKey());
				errorRenderer.getDatasets().addAll(getDataset(entry.getValue(), entry.getKey().toString(), candidateViewAttributes));
				
				Tuple<Double, Double> xMinMax = Candidate.getMinMax(entry.getValue(), xAxisAttributes.getValueFunction());
				Tuple<Double, Double> yMinMax = Candidate.getMinMax(entry.getValue(), yAxisAttributes.getValueFunction());
				
				xMins.add(xMinMax.getXValue());
				xMaxs.add(xMinMax.getYValue());
				
				yMins.add(yMinMax.getXValue());
				yMaxs.add(yMinMax.getYValue());
			}
			chart.getXAxis().set(Collections.min(xMins), Collections.max(xMaxs));
			chart.getYAxis().set(Collections.min(yMins), Collections.max(yMaxs));
			chart.setHorizontalGridLinesVisible(true);
			chart.setVerticalGridLinesVisible(true);
			chart.getGridRenderer().setDrawOnTop(true);
			chart.getGridRenderer().setStyle("-fx-stroke-width: 8px;");
			chart.getGridRenderer().autosize();
			chart.fireInvalidated();
			chart.setLegendSide(Side.TOP);

			if (chartID != 0 ) chart.setLegendVisible(false);
		}

		private List<DefaultErrorDataSet> getDataset(List<Candidate> candidates, String legend, CandidateViewAttributes candidateViewAttributes) {
			
			List<DefaultErrorDataSet> defaultErrorDataSets = new ArrayList<DefaultErrorDataSet>();
			
			for(CANDIDATE_TYPE type: CANDIDATE_TYPE.values()) {
				
				DefaultErrorDataSet ds = new DefaultErrorDataSet(legend + ": " + type);
				
				List<Candidate> shortlistedCandidates = candidates.stream().filter(f -> f.getCandidateType().equals(type)).collect(Collectors.toList());
				
				if(shortlistedCandidates.isEmpty()) continue;
				
				IntStream.range(0, shortlistedCandidates.size()).forEach(i -> {

					Candidate candidate = shortlistedCandidates.get(i);
					Tuple<Double, Double> x = xAxisAttributes.getValueFunction().apply(candidate);
					Tuple<Double, Double> y = yAxisAttributes.getValueFunction().apply(candidate);
					ds.add(x.getXValue(), y.getXValue());

					//		        	if(y.getYValue() != null) ds.add(x.getXValue(), y.getXValue(), y.getYValue(), y.getYValue());
					//		        	else ds.add(x.getXValue(), y.getXValue(), 0,0);

					//ds.addDataStyle(i, candidate.getStyle(candidateViewAttributes.getMarker(), candidateViewAttributes.getColor(), candidateViewAttributes.getSize(), i));
				});
				ds.setStyle(shortlistedCandidates.get(0).getStyle(candidateViewAttributes.getMarker(), candidateViewAttributes.getColor(), candidateViewAttributes.getSize(),null));
				defaultErrorDataSets.add(ds);
			}
			
			return defaultErrorDataSets;


		}



		public XYChart getChart() {
			return chart;
		}
		public void setChart(XYChart chart) {
			this.chart = chart;
		}

		public AxisAttributes getxAxisAttributes() {
			return xAxisAttributes;
		}

		public void setxAxisAttributes(AxisAttributes xAxisAttributes) {
			this.xAxisAttributes = xAxisAttributes;
		}

		public AxisAttributes getyAxisAttributes() {
			return yAxisAttributes;
		}

		public void setyAxisAttributes(AxisAttributes yAxisAttributes) {
			this.yAxisAttributes = yAxisAttributes;
		}


		


	}

	class CandidateViewAttributes {
		
		Color color;
		Marker marker;
		Integer size;
		String legend;
		
		public Color getColor() {
			return color;
		}
		public void setColor(Color color) {
			this.color = color;
		}
		public Marker getMarker() {
			return marker;
		}
		public void setMarker(Marker marker) {
			this.marker = marker;
		}
		public Integer getSize() {
			return size;
		}
		public void setSize(Integer size) {
			this.size = size;
		}
		public CandidateViewAttributes(Color color, Marker marker, Integer size, String legend) {
			super();
			this.color = color;
			this.marker = marker;
			this.size = size;
			this.legend =legend;

		} 
		
		public CandidateViewAttributes(Integer size, String legend) {
			super();
			this.size = size;
			this.legend =legend;

		} 
		
		public CandidateViewAttributes(Marker marker, Integer size, String legend) {
			super();
			this.size = size;
			this.legend =legend;
			this.marker = marker;

		} 


		public CandidateViewAttributes() {
			super();
			size = ChartViewer.minPointSize;
		}
		
		public String getLegend() {
			return legend;
		}
		public void setLegend(String legend) {
			this.legend = legend;
		} 
		
		
		
		
		
		
	}

	
	

}
