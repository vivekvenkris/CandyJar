package application;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import constants.Constants;
import data_holders.Angle;
import data_holders.Beam;
import data_holders.MetaFile;
import data_holders.Pulsar;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import readers.ApsuseMetaReader;
import readers.CandidateFileReader;
import readers.Psrcat;
import utilitites.Utilities;


public class CandyJar extends Application implements Constants {
	

	
	Psrcat psrcat = new Psrcat();


	BorderPane root = new BorderPane();

	final Label message = new Label("All ok.");
	
	final HBox imageViewHBox = new HBox(10);

	File baseDir = new File("/Users/vkrishnan/Downloads/pics_rescoring_MSGPS_run_1/");
	
	final NumberAxis xAxis = new NumberAxis();
	final NumberAxis yAxis = new NumberAxis();
	final MyScatterChart chart = new MyScatterChart(xAxis, yAxis);
	final List<Candidate> fullCandiatesList = new ArrayList<Candidate>();
	String userName = System.getProperty("user.name");
	MetaFile metaFile = null;
	
	
	
	Integer imageCounter = 0;

	double initXLowerBound = 0, initXUpperBound = 0, initYLowerBound = 0, initYUpperBound = 0;


    @Override
    public void start(Stage stage) throws Exception {
    	
    	
        //setUserAgentStylesheet(STYLESHEET_CASPIAN);
  	
    	final List<Image> images = new ArrayList<>();
    	final List<Candidate> candidates = new ArrayList<>();
    	final Set<LocalDateTime> utcs = new LinkedHashSet<LocalDateTime>();
    	final ComboBox<String> utcBox = new ComboBox<String>();
    	final ImageView imageView = new ImageView();
    	
		final Button previous = new Button("Previous");
		final Button next = new Button("Next");
		final Button rfi = new Button("RFI (y)");
		final Button noise = new Button("Noise (u)");
		final Button tier1 = new Button("Tier1 (i)");
		final Button tier2 = new Button("Tier2 (o)");
		final Button knownPulsar = new Button("Known pulsar (p)");
		
		
		final CheckBox rfiCB = new CheckBox("RFI");
		final CheckBox noiseCB = new CheckBox("Noise");
		final CheckBox tier1CB = new CheckBox("Tier1 candidates");
		final CheckBox tier2CB = new CheckBox("Tier2 candidates");
		final CheckBox knownPulsarCB = new CheckBox("Known Pulsars");
		final CheckBox uncategorizedCB = new CheckBox("Uncategorized");
		uncategorizedCB.setSelected(true);
		final Button filterCandidates = new Button("Filter candidates");
		
		final HBox filterhBox = new HBox(10,new Label("Category:"), rfi, noise, tier1, tier2, knownPulsar);
		filterhBox.setVisible(false);

		
		final Button resetCandidateCategory = new Button("Reset (R)");
		final LabelWithTextAndButton gotoCandidate = new LabelWithTextAndButton("Go to candidate", "","Go"); 
		final Label counterLabel = new Label();
		final TabPane pulsarPane = new TabPane();
		
		final XYChart.Series<Number,Number> tempSeries = new XYChart.Series<Number,Number>();
		
		message.setTextFill(Paint.valueOf("darkred"));

    	
    	
    	
    	/* initialise all chart stuff */

    	
		chart.setCursor(Cursor.CROSSHAIR);
		chart.setAlternativeRowFillVisible(true);
		chart.setAnimated(false);
		chart.setLegendVisible(false);
		chart.getData().clear();
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setVerticalZeroLineVisible(false);
        
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);

        xAxis.setAnimated(false);
        yAxis.setAnimated(false);
        
                
        
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			
			@Override
			public String toString(Number object) {
				Angle o = new Angle(object.doubleValue() * 15.0, Angle.DEG, Angle.DEG);
				return o.toHHMMSS();
			}
			
			@Override
			public Number fromString(String string) {
				Angle o = new Angle(string, Angle.HHMMSS);
				return o.getDecimalHourValue();
			}
		});
        
        
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
			
			@Override
			public String toString(Number object) {
				Angle o = new Angle(object.doubleValue(), Angle.DEG, Angle.DEG);
				return o.toDDMMSS();
			}
			
			@Override
			public Number fromString(String string) {
				Angle o = new Angle(string, Angle.DDMMSS);
				return o.getDegreeValue();
			}
		});
        
        
        
		LabelWithTextAndButton rootDirLWT = new LabelWithTextAndButton("Results directory:", baseDir.getAbsolutePath(),"Get pointings");


		rootDirLWT.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				System.err.println("Loading new root directory");
				baseDir = new File(rootDirLWT.getTextField().getText());
				chart.getData().clear();
				imageCounter = 0;

				
				try {
					fullCandiatesList.addAll(CandidateFileReader.readCandidateFile(baseDir.getAbsolutePath() + File.separator + Constants.OVERVIEW_CSV));
					utcs.addAll(fullCandiatesList.stream().map(f -> f.getUtc()).collect(Collectors.toSet()));

					utcBox.getItems().clear();
					utcBox.setItems(FXCollections.observableArrayList(utcs.stream().map(f -> Utilities.getUTCString(f, commonUTCFormat)).collect(Collectors.toList())));
					if(utcs.size() == 1){
						
						String utc =  utcs.toArray()[0].toString();
						utcBox.setValue(utc);
						
					}
		
					metaFile = ApsuseMetaReader.parseFile("/Users/vkrishnan/trashcan/trapum-observation-report-plot_tiling-6945bf77a0541936174651760b19a11751185a5f/apsuse.meta");
					metaFile.findNeighbours();
					double xLowerBound = metaFile.getMinRa().getDecimalHourValue();
					double xUpperBound = metaFile.getMaxRa().getDecimalHourValue();
					double xDiff = xUpperBound - xLowerBound;
					
					xLowerBound = xLowerBound - xDiff/20;
					xUpperBound = xUpperBound + xDiff/20;
					
					double yLowerBound = metaFile.getMinDec().getDegreeValue();
					double yUpperBound = metaFile.getMaxDec().getDegreeValue();
					double yDiff = yUpperBound - yLowerBound;
					
					yLowerBound = yLowerBound - yDiff/20;
					yUpperBound = yUpperBound + yDiff/20;
					
					initXUpperBound = xUpperBound;
					initXLowerBound = xLowerBound;
					initYLowerBound = yLowerBound;
					initYUpperBound = yUpperBound;
					
					
					xAxis.setLowerBound(xLowerBound);
					xAxis.setUpperBound(xUpperBound);
					xAxis.setTickUnit(0.01);
				
					
					yAxis.setLowerBound(yLowerBound);
					yAxis.setUpperBound(yUpperBound);
					yAxis.setTickUnit(-0.01);

					xAxis.setLabel("RA (hours)");
					yAxis.setLabel("DEC (degrees)");
					
					
					addDefaultMap(metaFile);
					
					message.setText(utcs.size() + " utcs found");


					
				} catch (IOException e) {
					message.setText(e.getMessage());
					e.printStackTrace();
				}
				
			}
			
		});
		
		utcBox.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				images.clear();
				candidates.clear();
				imageCounter = 0;

				
				filterhBox.setVisible(true);
				
				String utcString = utcBox.getValue();
				if(utcString == null) return;
				
				LocalDateTime utc = Utilities.getUTCLocalDateTime(utcString, Constants.commonUTCFormat);
				
				candidates.addAll(fullCandiatesList
									.stream()
									.filter(f -> f.getUtc().equals(utc))
									.collect(Collectors.toList())
									);

				images.addAll(candidates
						.stream()
						.map(f -> { 
								//File png =new File(baseDir.getAbsolutePath() + File.separator +  f.getImageFileName());
								File png =new File("/Users/vkrishnan/Downloads/test_pulsarx.png");
								

								try {
									return new Image(png.toURI().toURL().toExternalForm());
								} catch (MalformedURLException e) {
									message.setText(e.getMessage());
									e.printStackTrace();
									return null;
								}
									
							})
						.filter(f -> f != null )
						.collect(Collectors.toList()));
				
				
				if(images.size() > 0) {
					
					imageView.setImage(images.get(imageCounter));
					imageView.setUserData(candidates.get(0));
					counterLabel.setText( (imageCounter+1) +"/"+images.size()); 
					imageView.setVisible(true);
					message.setText(images.size() + " candidate PNGs found.");
				}
				else {
					message.setText("utc=" + utcString + " had no candidate PNGs.");
				}
				
				
				
			}
		});

		
		previous.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if( imageCounter - 1 >= 0 ){
					imageView.setImage(images.get(--imageCounter));
					imageView.setUserData(candidates.get(imageCounter));
					
					counterLabel.setText( (imageCounter+1) +"/"+images.size()); 
					message.setText("");
					
					
					chart.getData().removeAll(tempSeries);
					tempSeries.getData().clear();
					
					String beamName = candidates.get(imageCounter).getBeamName();
					Beam b = metaFile.getBeams().entrySet().stream().filter(f -> {
						Entry<String, Beam> e = (Entry<String, Beam>)f;
						return e.getValue().getName().endsWith(beamName);
					}).collect(Collectors.toList()).get(0).getValue();
					

					tempSeries.setName(Constants.CANDIDATE_BEAM_MAP);
					Data<Number, Number> d = new Data<Number, Number>(b.getRa().getDecimalHourValue(), b.getDec().getDegreeValue());
					d.setExtraValue(b);
					tempSeries.getData().add(d);
					
			 	
			    	chart.getData().add(tempSeries);

				}
			}
		});
		
		next.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if( imageCounter + 1 < images.size() ){ 
					imageView.setImage(images.get(++imageCounter));
					imageView.setUserData(candidates.get(imageCounter));
					counterLabel.setText( (imageCounter+1) +"/"+images.size()); 

					message.setText("");
					
					chart.getData().removeAll(tempSeries);
					tempSeries.getData().clear();
					
					String beamName = candidates.get(imageCounter).getBeamName();
					Beam b = metaFile.getBeams().entrySet().stream().filter(f -> {
						Entry<String, Beam> e = (Entry<String, Beam>)f;
						return e.getValue().getName().endsWith(beamName);
					}).collect(Collectors.toList()).get(0).getValue();
					

					tempSeries.setName(Constants.CANDIDATE_BEAM_MAP);
					Data<Number, Number> d = new Data<Number, Number>(b.getRa().getDecimalHourValue(), b.getDec().getDegreeValue());
					d.setExtraValue(b);
					tempSeries.getData().add(d);
					
			 	
			    	chart.getData().add(tempSeries);

				}

			}
			
	
		});
		
		gotoCandidate.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				int c = Integer.parseInt(gotoCandidate.getTextField().getText());
				
				if( c < 1 || c > images.size()) {
					message.setText("Invalid candidate number to go to.");
					return;
				}
				
				
				imageCounter = c-1;
				
				imageView.setImage(images.get(imageCounter));
				imageView.setUserData(candidates.get(imageCounter));
				counterLabel.setText( (imageCounter+1) +"/"+images.size()); 


				message.setText("");

				chart.getData().removeAll(tempSeries);
				tempSeries.getData().clear();
				
				String beamName = candidates.get(imageCounter).getBeamName();
				Beam b = metaFile.getBeams().entrySet().stream().filter(f -> {
					Entry<String, Beam> e = (Entry<String, Beam>)f;
					return e.getValue().getName().endsWith(beamName);
				}).collect(Collectors.toList()).get(0).getValue();
				

				tempSeries.setName(Constants.CANDIDATE_BEAM_MAP);
				Data<Number, Number> d = new Data<Number, Number>(b.getRa().getDecimalHourValue(), b.getDec().getDegreeValue());
				d.setExtraValue(b);
				tempSeries.getData().add(d);
				
		 	
		    	chart.getData().add(tempSeries);




			}
		});
		
		rfi.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.RFI);
				
			}
		});
		
		tier1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.TIER1_CANDIDATE);
				
			}
		});
		
		tier2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.TIER2_CANDIDATE);
				
			}
		});
		
		noise.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.NOISE);
			}
		});
		
		knownPulsar.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.KNOWN_PULSAR);
			}
		});
		
		
		
		filterCandidates.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				List<CANDIDATE_TYPE> filteredCandidateTypes = new ArrayList<Constants.CANDIDATE_TYPE>();
				
				if(rfiCB.isSelected()) filteredCandidateTypes.add(CANDIDATE_TYPE.RFI);
				if(noiseCB.isSelected()) filteredCandidateTypes.add(CANDIDATE_TYPE.NOISE);
				if(tier1CB.isSelected()) filteredCandidateTypes.add(CANDIDATE_TYPE.TIER1_CANDIDATE);
				if(tier2CB.isSelected()) filteredCandidateTypes.add(CANDIDATE_TYPE.TIER2_CANDIDATE);
				if(knownPulsarCB.isSelected()) filteredCandidateTypes.add(CANDIDATE_TYPE.KNOWN_PULSAR);
				if(uncategorizedCB.isSelected()) filteredCandidateTypes.add(CANDIDATE_TYPE.UNCATEGORIZED);
				candidates.clear();
				candidates.stream().filter( f-> filteredCandidateTypes.contains(f.getCandidateType())).collect(Collectors.toList());
				
				
				
			}
		});
		
		chart.setPrefWidth(600);
		chart.setPrefHeight(600);

		
    	
    	Label utcLabel = new Label("Select UTC:");
    	HBox utcSelectHBox = new HBox(10, utcLabel, utcBox);
    	
    	LabelWithTextAndButton userNameLWT = new LabelWithTextAndButton("User Name", userName, "save");
    	
    	userNameLWT.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				userName = userNameLWT.getTextField().getText();
				message.setText("Username updated successfully.");
			}
		});

    	
    	VBox controlBox = new VBox(10,
    			new HBox(10,userNameLWT .gethBox(), rootDirLWT.gethBox()),
    			utcSelectHBox,
    			new HBox(10,rfiCB, noiseCB, tier1CB, tier2CB, knownPulsarCB, uncategorizedCB, filterCandidates));
    	
		VBox top = new VBox(10, controlBox);
		top.setAlignment(Pos.CENTER);
		root.setTop(top);

		
		imageViewHBox.getChildren().add(imageView);
    	//HBox.setHgrow(imageViewHBox, Priority.ALWAYS);



    	root.setOnMouseClicked(f -> {
    		root.requestFocus();
    	});
    
    	
		root.setRight(new VBox(10, chart));
		
		root.setCenter(new VBox(10, imageViewHBox, 
								new HBox(10,previous,counterLabel, next, gotoCandidate.gethBox()),
								filterhBox

								)
					  );
		
		root.setBottom(message);
		
		
		Insets insets = new Insets(20, 20, 0, 25);
		
		BorderPane.setMargin(top, insets );
		//BorderPane.setMargin(chart, insets );
		BorderPane.setMargin(imageViewHBox, insets );
		//BorderPane.setMargin(message, insets );
        Scene scene = new Scene(root, 1600,1200);
        
        
        /* process key events */
        
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				
				switch (event.getCode()) {
				
				case A:
					previous.fire();
					break;
					
				case D:
					next.fire();
					break;	
					
				case Y:
					rfi.fire();
					break;
					
				case U:
					noise.fire();
					break;
					
				case I:
					tier1.fire();
					break;
					
				case O:
					tier2.fire();
					break;
					
				case P:
					knownPulsar.fire();
					break;
					
				case SPACE:
					
					
					ImageView zoomImageView = new ImageView();
					zoomImageView.setImage(new Image(imageView.getImage().getUrl()));
					
					ScrollPane zoomImagePane = new ZoomableScrollPane(zoomImageView);
					zoomImagePane.setContent(zoomImageView);
					
					
					Scene zoomImageScene = new Scene(zoomImagePane, 
							zoomImageView.getImage().getWidth(),zoomImageView.getImage().getHeight());
					
					Stage zoomImageStage = new Stage();
					zoomImageStage.setScene(zoomImageScene);
					zoomImageStage.show();
					
					zoomImageScene.setOnKeyPressed(new EventHandler<KeyEvent>() {

						@Override
						public void handle(KeyEvent zoomKeyEvent) {
							switch (zoomKeyEvent.getCode()) {
							case ESCAPE:
								zoomImageStage.close();
								break;
							
							}
							
						}
						
					});
					
			
				default:
					break;
				}
							
			}
		});
        
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        stage.setTitle("CandyJar");
        stage.setScene(scene);
        stage.show();
        
        if(baseDir != null && baseDir.exists()) {
			rootDirLWT.getTextField().setText(baseDir.getAbsolutePath());
			rootDirLWT.getButton().fire();
		}
        
        chart.setOnMouseClicked(mouseHandler);
        chart.setOnMouseDragged(mouseHandler);
        chart.setOnMouseEntered(mouseHandler);
        chart.setOnMouseExited(mouseHandler);
        chart.setOnMouseMoved(mouseHandler);
        chart.setOnMousePressed(mouseHandler);
        chart.setOnMouseReleased(mouseHandler);

		
		rect = new Rectangle();
		rect.setFill(Color.web("LIGHTBLUE", 0.1));
		rect.setStroke(Color.LIGHTBLUE);
		rect.setStrokeDashOffset(50);

		rect.widthProperty().bind(rectX.subtract(rectinitX));
		rect.heightProperty().bind(rectY.subtract(rectinitY));
		root.getChildren().add(rect);
        
    }
    
    
  
    public void addDefaultMap(MetaFile metaFile) {

    	XYChart.Series<Number,Number> beamPositions = new XYChart.Series<Number, Number>();
    	beamPositions.setName(Constants.DEFAULT_BEAM_MAP);
 	
    	for(Entry<String, Beam> e: metaFile.getBeams().entrySet()) {
    		Data<Number, Number> d = new Data<Number, Number>(e.getValue().getRa().getDecimalHourValue(), e.getValue().getDec().getDegreeValue()); 		
    		Beam b = e.getValue();
    		d.setExtraValue(b);
    		beamPositions.getData().add(d);
    	}
    	beamPositions.setName("beams");
    	
    	XYChart.Series<Number,Number> pulsarPositions = new XYChart.Series<Number, Number>();
    	
    	for(Pulsar pulsar: psrcat.getPulsarsInBeam(metaFile.getBoresight().getRa(), metaFile.getBoresight().getDec(), new Angle(1.0, Angle.DEG, Angle.DEG))) {
    		
    		Data<Number, Number> d = new Data<Number, Number>(pulsar.getRa().getDecimalHourValue(), pulsar.getDec().getDegreeValue());
    		pulsarPositions.getData().add(d);
    		
    	}
    	pulsarPositions.setName("pulsars");
    	

    	
    	chart.getData().add(beamPositions);
    	chart.getData().add(pulsarPositions);

	}
	

    

    
    
    
    
    
    
    /* Code for chart zooming  */
    
    
	Rectangle rect;
	SimpleDoubleProperty rectinitX = new SimpleDoubleProperty();
	SimpleDoubleProperty rectinitY = new SimpleDoubleProperty();
	SimpleDoubleProperty rectX = new SimpleDoubleProperty();
	SimpleDoubleProperty rectY = new SimpleDoubleProperty();
    
	EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent mouseEvent) {
			if(mouseEvent.getButton() == MouseButton.SECONDARY){

				((NumberAxis) chart.getXAxis()).setLowerBound(initXLowerBound);
				((NumberAxis) chart.getXAxis()).setUpperBound(initXUpperBound);
				((NumberAxis) chart.getXAxis()).setTickUnit((initXUpperBound - initXLowerBound)/10.0);

				((NumberAxis) chart.getYAxis()).setLowerBound(initYLowerBound);
				((NumberAxis) chart.getYAxis()).setUpperBound(initYUpperBound);
				((NumberAxis) chart.getYAxis()).setTickUnit((initXUpperBound - initXLowerBound)/10.0);


			}
			
			
			if(mouseEvent.getButton() == MouseButton.PRIMARY){
				
				if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
					rect.setX(mouseEvent.getX());
					rect.setY(mouseEvent.getY());
					rectinitX.set(mouseEvent.getX());
					rectinitY.set(mouseEvent.getY());
					
				
				}
			
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
					rectX.set(mouseEvent.getX());
					rectY.set(mouseEvent.getY());
				}
				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
				

					if ((rectinitX.get() >= rectX.get())&&(rectinitY.get() >= rectY.get()))
					{
						@SuppressWarnings("unchecked")
						double X = mouseEvent.getX();
						double Y = mouseEvent.getY();
	
						NumberAxis yAxis = (NumberAxis) chart.getYAxis();
						double Tgap = yAxis.getHeight()/(yAxis.getUpperBound() - yAxis.getLowerBound());
						double axisShift = getSceneShiftY(yAxis);
						double ptY = yAxis.getUpperBound() - (( Y - axisShift) / Tgap);
	
						NumberAxis xAxis = (NumberAxis) chart.getXAxis();
	
	
						Tgap = xAxis.getWidth()/(xAxis.getUpperBound() - xAxis.getLowerBound());            
						axisShift = getSceneShiftX(xAxis);                        
						double ptX = ((X - axisShift) / Tgap) + xAxis.getLowerBound();                    
	
	
					} else {
					
						double Tgap = 0;
						double newLowerBound, newUpperBound, axisShift;
						double xScaleFactor, yScaleFactor;
						double xaxisShift, yaxisShift;
						// Zoom in Y-axis by changing bound range.            
						NumberAxis yAxis = (NumberAxis) chart.getYAxis();
						Tgap = yAxis.getHeight()/(yAxis.getUpperBound() - yAxis.getLowerBound());
						axisShift = getSceneShiftY(yAxis);
						yaxisShift = axisShift;
	
						newUpperBound = yAxis.getUpperBound() - ((rectinitY.get() - axisShift) / Tgap);
						newLowerBound = yAxis.getUpperBound() - (( rectY.get() - axisShift) / Tgap);
	
						if (newUpperBound > yAxis.getUpperBound())
							newUpperBound = yAxis.getUpperBound();
	
						yScaleFactor = (yAxis.getUpperBound() - yAxis.getLowerBound())/(newUpperBound - newLowerBound);
						yAxis.setLowerBound(newLowerBound);
						yAxis.setUpperBound(newUpperBound);
	
						yAxis.setTickUnit((newUpperBound-newLowerBound)/10.0);
						// Zoom in X-axis by removing first and last data values.
	
						NumberAxis xAxis = (NumberAxis) chart.getXAxis();
	
	
						Tgap = xAxis.getWidth()/(xAxis.getUpperBound() - xAxis.getLowerBound());            
						axisShift = getSceneShiftX(xAxis);                        
						xaxisShift = axisShift;
	
	
	
						newLowerBound = ((rectinitX.get() - axisShift) / Tgap) + xAxis.getLowerBound();
						newUpperBound = ((rectX.get() - axisShift) / Tgap) + xAxis.getLowerBound();                
	
						if (newUpperBound > xAxis.getUpperBound())
							newUpperBound = xAxis.getUpperBound();
	
						xScaleFactor = (xAxis.getUpperBound() - xAxis.getLowerBound())/(newUpperBound - newLowerBound);
						xAxis.setLowerBound( newLowerBound );
						xAxis.setUpperBound( newUpperBound );
						xAxis.setTickUnit((newUpperBound-newLowerBound)/10.0);
					
					
					}
				
					rectX.set(0);
					rectY.set(0);
				
				
				}
			
			
			
			}
		}
		
	};
	
	
	private static double getSceneShiftX(Node node) {
		double shift = 0;
		do { 
			shift += node.getLayoutX(); 
			node = node.getParent();
		} while (node != null);
		return shift;
	}
	private static double getSceneShiftY(Node node) {
		double shift = 0;
		do { 
			shift += node.getLayoutY(); 
			node = node.getParent();
		} while (node != null);
		return shift;
	}
	
	
    public static void main(String[] args) throws IOException {
    	

    	
        launch(args);
    }
}