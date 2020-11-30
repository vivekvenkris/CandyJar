package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.SegmentedButton;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import constants.Constants;
import constants.PsrcatConstants;
import data_holders.Angle;
import data_holders.Beam;
import data_holders.Candidate;
import data_holders.MetaFile;
import data_holders.Pulsar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import readers.ApsuseMetaReader;
import readers.CandidateFileReader;
import readers.Psrcat;
import utilitites.Utilities;

public class CandyJar extends Application implements Constants {

	private static Screen currentScreen = Screen.getPrimary();

	Psrcat psrcat = new Psrcat();

	BorderPane root = new BorderPane();

	final Label message = new Label("All ok. Select a directory");
	final HBox imageViewHBox = new HBox(10);

	File baseDir = null;

	final NumberAxis xAxis = new NumberAxis();
	final NumberAxis yAxis = new NumberAxis();
	final MyScatterChart chart = new MyScatterChart(xAxis, yAxis);
	final List<Candidate> fullCandiatesList = new ArrayList<Candidate>();
	final Map<String, List<Candidate>> candidateMap = new HashMap<String, List<Candidate>>();
	String userName = System.getProperty("user.name");
	MetaFile metaFile = null;

	Integer imageCounter = 0;

	double initXLowerBound = 0, initXUpperBound = 0, initYLowerBound = 0, initYUpperBound = 0;

	final Button previous = new Button("Previous (a)");
	final Button next = new Button("Next (d)");
	final ToggleButton rfi = new ToggleButton("RFI (y)");
	final ToggleButton noise = new ToggleButton("Noise (u)");
	final ToggleButton tier1 = new ToggleButton("Tier1 (i)");
	final ToggleButton tier2 = new ToggleButton("Tier2 (o)");
	final ToggleButton knownPulsar = new ToggleButton("Known pulsar (p)");

	final SegmentedButton candidateCategories = new SegmentedButton();
	final ImageView imageView = new ImageView();

	final ComboBox<String> utcBox = new ComboBox<String>();

	final CheckBox rfiCB = new CheckBox("RFI");
	final CheckBox noiseCB = new CheckBox("Noise");
	final CheckBox tier1CB = new CheckBox("Tier1 candidates");
	final CheckBox tier2CB = new CheckBox("Tier2 candidates");
	final CheckBox knownPulsarCB = new CheckBox("Known Pulsars");
	final CheckBox uncategorizedCB = new CheckBox("Uncategorized");
	
	final CheckComboBox<CANDIDATE_TYPE> filterTypes = new CheckComboBox<CANDIDATE_TYPE>(FXCollections.observableArrayList(Arrays.asList(Constants.CANDIDATE_TYPE.values())));
	final List<CANDIDATE_TYPE> filteredTypes = new ArrayList<CANDIDATE_TYPE>();
	final Button filterCandidates = new Button("Go");


	// final List<Image> images = new ArrayList<>();
	final List<Candidate> candidates = new ArrayList<>();
	final Set<LocalDateTime> utcs = new TreeSet<LocalDateTime>();

	final Button resetCandidateCategory = new Button("Reset (R)");
	final LabelWithTextAndButton gotoCandidate = new LabelWithTextAndButton("Go to", "", "Go");
	final Label counterLabel = new Label();
	final TabPane pulsarPane = new TabPane();
	final Tab candidateTab = new Tab();
	final Tab diagnosticTab = new Tab();

	final XYChart.Series<Number, Number> tempSeries = new XYChart.Series<Number, Number>();

	final Button saveClassification = new Button("Save classification");
	final Button loadClassification = new Button("Load classification");

	final ComboBox<String> sortBox = new ComboBox<String>();
	final VBox controlBox = new VBox(10);
	final VBox actionsBox = new VBox(10);
	
	HBox candidateFilterHBox = new HBox(10, filterTypes, sortBox,
			filterCandidates);
	
	Integer pngHeight = Constants.DEFAULT_IMAGE_HEIGHT;
	Integer pngWidth = Constants.DEFAULT_IMAGE_WIDTH;
	

	Boolean goingForward = Boolean.valueOf(Boolean.TRUE);
	
	Boolean candidatesVisible=false;

	@Override
	public void start(Stage stage) throws Exception {
		
		//currentScreen = Screen.getScreens().get(1);
		
		System.err.println(currentScreen);
		
		configureLayout();
		
        filterTypes.setTitle("FILTER_TYPE");
        utcBox.setPromptText("SELECT_UTC");
		sortBox.setPromptText("SORT_BY");
		
		filterTypes.setTooltip(new Tooltip("Select the types of candidates to filter"));
		utcBox.setTooltip(new Tooltip("Select UTC to view candidates"));
		sortBox.setTooltip(new Tooltip("Select value to sort by"));
		
		loadClassification.setVisible(false);
		
		//filterTypes.getCheckModel().check(filterTypes.getCheckModel().getItemIndex(CANDIDATE_TYPE.UNCATEGORIZED));


		
		filterTypes.getCheckModel().getCheckedItems().addListener(new ListChangeListener<CANDIDATE_TYPE>() {
	         public void onChanged(ListChangeListener.Change<? extends CANDIDATE_TYPE> c) {
	        	 filteredTypes.clear();
	             filteredTypes.addAll(filterTypes.getCheckModel().getCheckedItems());
	             filterTypes.setTitle("FILTER_TYPE");
	         }
	     });
		
		utcBox.setVisible(false);

		candidateCategories.getButtons().addAll(rfi, noise, tier1, tier2, knownPulsar);
		candidateCategories.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
		uncategorizedCB.setSelected(true);

		candidateFilterHBox.setVisible(false);

		gotoCandidate.getTextField().setPromptText("Go to Candidate");


		message.setTextFill(Paint.valueOf("darkred"));

		actionsBox.getChildren().addAll(new HBox(10, previous, counterLabel, next, gotoCandidate.getTextField(),
				gotoCandidate.getButton(), saveClassification),
				new HBox(10, new Label("Select Category:"), candidateCategories));
		actionsBox.setVisible(false);

		sortBox.setItems(FXCollections.observableArrayList(
				Arrays.asList(new String[] { "FOLD_SNR", "FFT_SNR", "PICS_TRAPUM", "PICS_PALFA" })));

		sortBox.setVisible(false);


		/* initialise all chart stuff */

		chart.setCursor(Cursor.CROSSHAIR);
		chart.setAlternativeRowFillVisible(true);
		chart.setAnimated(false);
		chart.setLegendVisible(false);
		chart.getData().clear();
		chart.setLegendVisible(false);
		chart.setAnimated(false);
		chart.setVerticalZeroLineVisible(false);
		chart.setVisible(false);

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

		TextAndButton rootDirTB = new TextAndButton(null,"Results directory","Get pointings", 10);
		
		DirectoryChooser directoryChooser = new DirectoryChooser();
		Button fileSelectButton = new Button("...");
		fileSelectButton.setOnAction(e -> {
			File selectedDirectory = directoryChooser.showDialog(stage);
			rootDirTB.getTextField().setText(selectedDirectory.getAbsolutePath());
		});

		rootDirTB.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				if(rootDirTB.getTextField().getText().isBlank()) {
					message.setText("Select a valid directory");
					return;
				}

				System.err.println("Loading new root directory" + rootDirTB.getTextField().getText());
				baseDir = new File(rootDirTB.getTextField().getText());
				chart.getData().clear();
				utcs.clear();
				imageCounter = 0;
				utcBox.setVisible(true);
				loadClassification.setVisible(true);

				try {
					fullCandiatesList.addAll(CandidateFileReader
							.readCandidateFile(baseDir.getAbsolutePath() + File.separator + Constants.CSV_FILE_NAME));
					utcs.addAll(fullCandiatesList.stream().map(f -> f.getStartUTC()).collect(Collectors.toSet()));

					utcBox.getItems().clear();
					utcBox.setItems(FXCollections.observableArrayList(utcs.stream()
							.map(f -> Utilities.getUTCString(f, commonUTCFormat)).collect(Collectors.toList())));
					if (utcs.size() == 1) {

						String utc = utcs.toArray()[0].toString();
						utcBox.setValue(utc);

					}
					
					for(LocalDateTime utc: utcs) { 
						String utcString = Utilities.getUTCString(utc, DateTimeFormatter.ISO_DATE_TIME);
						List<Candidate> candidatesPerUtc = fullCandiatesList.stream().filter(f -> f.getStartUTC().equals(utc)).collect(Collectors.toList());
						candidateMap.put(utcString, candidatesPerUtc);
						checkSimilarity(candidatesPerUtc);
						
					}

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
				// images.clear();
				candidates.clear();
				imageCounter = 0;

				candidateFilterHBox.setVisible(true);
				chart.setVisible(true);
				imageView.setVisible(false);
				actionsBox.setVisible(false);
				sortBox.setVisible(true);
				candidatesVisible = false;

				String utcString = utcBox.getValue();
				if (utcString == null)
					return;

				LocalDateTime utc = Utilities.getUTCLocalDateTime(utcString, Constants.commonUTCFormat);

				/* stupid stub because you need atleast one candidate to get the meta file */
				shortlistCandidates(utc, Arrays.asList(Constants.CANDIDATE_TYPE.values()));

				try {
					metaFile = ApsuseMetaReader.parseFile(baseDir.getAbsolutePath() + File.separator
							+ candidates.get(imageCounter).getMetaFilePath());
					metaFile.findNeighbours();

					List<Pulsar> pulsarsInBeam = psrcat.getPulsarsInBeam(metaFile.getBoresight().getRa(),
							metaFile.getBoresight().getDec(), new Angle(1.0, Angle.DEG, Angle.DEG));
					pulsarPane.getTabs().clear();
					candidateTab.setClosable(false);
					updateTab(candidateTab, null);
					pulsarPane.getTabs().add(candidateTab);
					populateTabs(pulsarPane, pulsarsInBeam);

					double xLowerBound = metaFile.getMinRa().getDecimalHourValue();
					double xUpperBound = metaFile.getMaxRa().getDecimalHourValue();
					double xDiff = xUpperBound - xLowerBound;

					xLowerBound = xLowerBound - xDiff / 20;
					xUpperBound = xUpperBound + xDiff / 20;

					double yLowerBound = metaFile.getMinDec().getDegreeValue();
					double yUpperBound = metaFile.getMaxDec().getDegreeValue();
					double yDiff = yUpperBound - yLowerBound;

					yLowerBound = yLowerBound - yDiff / 20;
					yUpperBound = yUpperBound + yDiff / 20;

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

					addDefaultMap(metaFile, pulsarsInBeam);

					message.setText("Meta file and neighbouring pulsars added. Please filter required candidates.");

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

		filterCandidates.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {


				String utcString = utcBox.getValue();
				if (utcString == null)
					return;

				LocalDateTime utc = Utilities.getUTCLocalDateTime(utcString, Constants.commonUTCFormat);

				addAllCandidates(utc, filteredTypes);

				System.err.println("after filtering: " + candidates.size());

				imageCounter = 0;

				if (candidates.size() > 0) {

					imageView.setVisible(true);
					actionsBox.setVisible(true);

					message.setText(candidates.size() + " candidate PNGs found.");
					consolidate(imageCounter);
					
					candidatesVisible = true;
				} else {
					message.setText("No candidate PNGs for your filter type / UTC");
				}

			}
		});

		sortBox.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
//				imageCounter = 0;
//				if(images.size() > 0) {
//					consolidate(imageCounter);
//				}
				//filterCandidates.fire();

			}
		});

		previous.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;

				if (imageCounter - 1 >= 0) {
					imageCounter--;
					consolidate(imageCounter);
				}

				goingForward = false;

			}
		});

		next.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;

				if (imageCounter + 1 < candidates.size()) {
					imageCounter++;
					consolidate(imageCounter);
				}

				goingForward = true;

			}

		});

		gotoCandidate.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;

				int c = Integer.parseInt(gotoCandidate.getTextField().getText());

				if (c < 1 || c > candidates.size()) {
					message.setText("Invalid candidate number to go to.");
					return;
				}

				imageCounter = c - 1;
				consolidate(imageCounter);

			}
		});

		rfi.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.RFI);
				progress();
			}
		});

		tier1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.TIER1_CANDIDATE);
				progress();

			}
		});

		tier2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.TIER2_CANDIDATE);
				progress();

			}
		});

		noise.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.NOISE);
				progress();

			}
		});

		knownPulsar.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.KNOWN_PULSAR);
				progress();

			}
		});

		saveClassification.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileChooser saveFileChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files", "*.csv");
				saveFileChooser.getExtensionFilters().add(extFilter);
				saveFileChooser.setInitialDirectory(baseDir);
				saveFileChooser.setInitialFileName(baseDir.getName() + "_" + userName);
				File saveFile = saveFileChooser.showSaveDialog(stage);

				Optional<ButtonType> result = null;

				if (saveFile.exists()) {

					Alert alert = new Alert(AlertType.CONFIRMATION);

					alert.setTitle("Overwriting existing file");
					alert.setHeaderText("The selected file already exists.");
					alert.setContentText("Do you want to overwrite it?");

					result = alert.showAndWait();
				}

				if (!saveFile.exists() || (result != null && result.get() == ButtonType.OK)) {

					System.err.println("Saving file to " + saveFile.getAbsolutePath());

					writeToFile(saveFile);

				} else {

					message.setText("File save aborted");

				}

			}
		});
		
		
		loadClassification.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileChooser loadFileChooser = new FileChooser();
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files", "*.csv");
				loadFileChooser.getExtensionFilters().add(extFilter);
				loadFileChooser.setInitialDirectory(baseDir);
				loadFileChooser.setInitialFileName(baseDir.getName() + "_" + userName);
				File loadFile = loadFileChooser.showOpenDialog(stage);

				Optional<ButtonType> result = null;
				
				if(!loadFile.exists()) {
					message.setText("File does not exist");
				}
				else {
					loadFromFile(loadFile);
				}

				
			}
		});


		HBox utcSelectHBox = new HBox(10, utcBox);
		
		

		TextAndButton userNameTB = new TextAndButton(userName, "User Name", "save", 10);

		userNameTB.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				userName = userNameTB.getTextField().getText();
				message.setText("Username updated successfully.");
			}
		});

		controlBox.getChildren().addAll(new HBox(10, rootDirTB.getTextField(),fileSelectButton,rootDirTB.getButton(), loadClassification),
				new HBox(10, utcSelectHBox, candidateFilterHBox));

	
		
		
		
		

//		BorderPane leftPane = new BorderPane();
//
//		VBox leftTop = new VBox(10, controlBox);
//		leftTop.setPrefSize(750, 80);
//		leftTop.setAlignment(Pos.CENTER);
//		leftPane.setTop(leftTop);
//
//		VBox leftBottom = new VBox(10, message, actionsHBox);
//		leftBottom.setPrefSize(750, 20);
//		leftPane.setBottom(leftBottom);
//
//		VBox centerLeft = new VBox(10, chart, pulsarPane);
//		centerLeft.setPrefSize(600, 800);
//
//		chart.setPrefWidth(600);
//		chart.setPrefHeight(600);
//		leftPane.setCenter(centerLeft);
//
//		root.setLeft(leftPane);
//
//		imageViewHBox.getChildren().add(imageView);
//		imageViewHBox.setPrefSize(Constants.RESAMPLED_IMAGE_WIDTH, Constants.RESAMPLED_IMAGE_HEIGHT);
//
//		VBox rightPane = new VBox(10, imageViewHBox);
//		rightPane.setPrefSize(Constants.RESAMPLED_IMAGE_WIDTH, Constants.RESAMPLED_IMAGE_HEIGHT);
//
//		root.setRight(rightPane);

		root.setOnMouseClicked(f -> {
			root.requestFocus();
		});
		
		Platform.runLater(() -> root.requestFocus());

//		Insets insets = new Insets(5, 20, 5, 20);
//
//		BorderPane.setMargin(leftTop, insets);
//		// BorderPane.setMargin(chart, insets );
//		BorderPane.setMargin(imageViewHBox, insets);
//		BorderPane.setMargin(message, insets);

		Scene scene = new Scene(root, currentScreen.getBounds().getWidth(), currentScreen.getBounds().getHeight());
		
		stage.setX(currentScreen.getBounds().getMinX());
		stage.setY(currentScreen.getBounds().getMinY());
		

		scene.setOnKeyPressed(keyEventHandler);

		scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

		stage.setTitle("CandyJar");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();



//		stage.xProperty().addListener((obs, oldVal, newVal) -> {
//			System.out.println("X: " + newVal);
//
//		});
//		stage.yProperty().addListener((obs, oldVal, newVal) -> {
//			System.out.println("Y: " + newVal);
//		});

		if (baseDir != null && baseDir.exists()) {
			rootDirTB.getTextField().setText(baseDir.getAbsolutePath());
			rootDirTB.getButton().fire();
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

	public void addDefaultMap(MetaFile metaFile, List<Pulsar> pulsars) {
		
		chart.getData().clear();

		XYChart.Series<Number, Number> beamPositions = new XYChart.Series<Number, Number>();
		beamPositions.setName(Constants.DEFAULT_BEAM_MAP);

		for (Entry<String, Beam> e : metaFile.getBeams().entrySet()) {
			Data<Number, Number> d = new Data<Number, Number>(e.getValue().getRa().getDecimalHourValue(),
					e.getValue().getDec().getDegreeValue());
			Beam b = e.getValue();
			d.setExtraValue(b);
			beamPositions.getData().add(d);
		}

		XYChart.Series<Number, Number> pulsarPositions = new XYChart.Series<Number, Number>();

		for (Pulsar pulsar : pulsars) {

			Data<Number, Number> d = new Data<Number, Number>(pulsar.getRa().getDecimalHourValue(),
					pulsar.getDec().getDegreeValue());
			pulsarPositions.getData().add(d);

		}
		pulsarPositions.setName(Constants.KNOWN_PULSAR_BEAM_MAP);

		chart.getData().add(beamPositions);
		// chart.getData().add(pulsarPositions);

	}

	/* Code for chart zooming */

	Rectangle rect;
	SimpleDoubleProperty rectinitX = new SimpleDoubleProperty();
	SimpleDoubleProperty rectinitY = new SimpleDoubleProperty();
	SimpleDoubleProperty rectX = new SimpleDoubleProperty();
	SimpleDoubleProperty rectY = new SimpleDoubleProperty();

	EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent mouseEvent) {

			Bounds chartSceneBounds = chart.localToScene(chart.getBoundsInLocal());

			if (mouseEvent.getButton() == MouseButton.SECONDARY) {

				((NumberAxis) chart.getXAxis()).setLowerBound(initXLowerBound);
				((NumberAxis) chart.getXAxis()).setUpperBound(initXUpperBound);
				((NumberAxis) chart.getXAxis()).setTickUnit((initXUpperBound - initXLowerBound) / 10.0);

				((NumberAxis) chart.getYAxis()).setLowerBound(initYLowerBound);
				((NumberAxis) chart.getYAxis()).setUpperBound(initYUpperBound);
				((NumberAxis) chart.getYAxis()).setTickUnit((initXUpperBound - initXLowerBound) / 10.0);

			}

			if (mouseEvent.getButton() == MouseButton.PRIMARY) {

				if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {

					rect.setX(mouseEvent.getX() + chartSceneBounds.getMinX());
					rect.setY(mouseEvent.getY() + chartSceneBounds.getMinY());
					rectinitX.set(mouseEvent.getX() + chartSceneBounds.getMinX());
					rectinitY.set(mouseEvent.getY() + chartSceneBounds.getMinY());

				}

				else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {

					double newX = mouseEvent.getX() + chartSceneBounds.getMinX();
					double newY = mouseEvent.getY() + chartSceneBounds.getMinY();

					newX = newX > chartSceneBounds.getMaxX() ? chartSceneBounds.getMaxX() : newX;
					newY = newY > chartSceneBounds.getMaxY() ? chartSceneBounds.getMaxY() : newY;

					rectX.set(newX);
					rectY.set(newY);
				} else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {

					System.err.println("released:" + mouseEvent.getX() + " " + mouseEvent.getY());

					if ((rectinitX.get() >= rectX.get()) && (rectinitY.get() >= rectY.get())) {
						@SuppressWarnings("unchecked")
						double X = mouseEvent.getX() + chartSceneBounds.getMinX();
						double Y = mouseEvent.getY() + chartSceneBounds.getMinY();

						NumberAxis yAxis = (NumberAxis) chart.getYAxis();
						double Tgap = yAxis.getHeight() / (yAxis.getUpperBound() - yAxis.getLowerBound());
						double axisShift = getSceneShiftY(yAxis);
						double ptY = yAxis.getUpperBound() - ((Y - axisShift) / Tgap);

						NumberAxis xAxis = (NumberAxis) chart.getXAxis();

						Tgap = xAxis.getWidth() / (xAxis.getUpperBound() - xAxis.getLowerBound());
						axisShift = getSceneShiftX(xAxis);
						double ptX = ((X - axisShift) / Tgap) + xAxis.getLowerBound();

					} else {

						double Tgap = 0;
						double newLowerBound, newUpperBound, axisShift;
						double xScaleFactor, yScaleFactor;
						double xaxisShift, yaxisShift;
						// Zoom in Y-axis by changing bound range.
						NumberAxis yAxis = (NumberAxis) chart.getYAxis();
						Tgap = yAxis.getHeight() / (yAxis.getUpperBound() - yAxis.getLowerBound());
						axisShift = getSceneShiftY(yAxis);
						yaxisShift = axisShift;

						newUpperBound = yAxis.getUpperBound() - ((rectinitY.get() - axisShift) / Tgap);
						newLowerBound = yAxis.getUpperBound() - ((rectY.get() - axisShift) / Tgap);

						if (newUpperBound > yAxis.getUpperBound())
							newUpperBound = yAxis.getUpperBound();

						yScaleFactor = (yAxis.getUpperBound() - yAxis.getLowerBound())
								/ (newUpperBound - newLowerBound);
						yAxis.setLowerBound(newLowerBound);
						yAxis.setUpperBound(newUpperBound);

						yAxis.setTickUnit((newUpperBound - newLowerBound) / 10.0);
						// Zoom in X-axis by removing first and last data values.

						NumberAxis xAxis = (NumberAxis) chart.getXAxis();

						Tgap = xAxis.getWidth() / (xAxis.getUpperBound() - xAxis.getLowerBound());
						axisShift = getSceneShiftX(xAxis);
						xaxisShift = axisShift;

						newLowerBound = ((rectinitX.get() - axisShift) / Tgap) + xAxis.getLowerBound();
						newUpperBound = ((rectX.get() - axisShift) / Tgap) + xAxis.getLowerBound();

						if (newUpperBound > xAxis.getUpperBound())
							newUpperBound = xAxis.getUpperBound();

						xScaleFactor = (xAxis.getUpperBound() - xAxis.getLowerBound())
								/ (newUpperBound - newLowerBound);
						xAxis.setLowerBound(newLowerBound);
						xAxis.setUpperBound(newUpperBound);
						xAxis.setTickUnit((newUpperBound - newLowerBound) / 10.0);

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

	public void updateTab(Tab tab, Candidate candidate) {
		tab.setText("Candidate Info");

		final TableView<Pair<String, Object>> table = new TableView<>();

		if (candidate != null) {

			Beam beam = metaFile.getBeams().get(candidate.getBeamName());
			List<Integer> neighbours = new ArrayList<Integer>();
			if (beam != null)
				neighbours.addAll(beam.getNeighbourBeams().stream()
						.map(f -> Integer.parseInt(f.getName().replaceAll("\\D+", ""))).collect(Collectors.toList()));
			else
				message.setText("Cannot find beam: " + candidate.getBeamName());
			
//			table.getItems().add(new Pair<String, Object>("similar candidates:", candidate.getSimilarParamCandidates().stream()
//					.map(f ->{
//						return f.getBeamName() + " " + f.getPngFilePath();	
//					}).collect(Collectors.toList()).toString()));

			table.getItems().add(new Pair<String, Object>("Pointing ID:", candidate.getPointingID()));
			table.getItems().add(new Pair<String, Object>("Beam ID:", candidate.getBeamID()));
			table.getItems().add(new Pair<String, Object>("Beam Name:", candidate.getBeamName()));
			table.getItems().add(new Pair<String, Object>("Neighbour beams:", neighbours.toString()));
			table.getItems()
					.add(new Pair<String, Object>("PICS score (TRAPUM):", candidate.getPicsScoreTrapum().toString()));
			table.getItems()
					.add(new Pair<String, Object>("PICS score (PALFA):", candidate.getPicsScorePALFA().toString()));
			table.getItems().add(new Pair<String, Object>("FFT SNR:", candidate.getFftSNR().toString()));
			table.getItems().add(new Pair<String, Object>("Fold SNR: ", candidate.getFoldSNR().toString()));
			
			table.getItems().add(new Pair<String, Object>("Boresight:", candidate.getSourceName()));
			table.getItems().add(new Pair<String, Object>("RA:", candidate.getRa().toHHMMSS()));
			table.getItems().add(new Pair<String, Object>("DEC:", candidate.getDec().toDDMMSS()));
			table.getItems().add(new Pair<String, Object>("GL:", candidate.getGl().getDegreeValue().toString()));
			table.getItems().add(new Pair<String, Object>("GB:", candidate.getGb().getDegreeValue().toString()));

			table.getItems().add(new Pair<String, Object>("Start MJD:", candidate.getStartMJD().toString()));
			table.getItems().add(new Pair<String, Object>("Start UTC:", candidate.getStartUTC().toString()));
			table.getItems().add(new Pair<String, Object>("Input F0:", candidate.getUserF0().toString()));
			table.getItems().add(
					new Pair<String, Object>("Best F0:", candidate.getOptF0() + " +/- " + candidate.getOptF0Err()));
			table.getItems().add(new Pair<String, Object>("Input F1:", candidate.getUserF1().toString()));
			table.getItems().add(
					new Pair<String, Object>("Best F1:", candidate.getOptF1() + " +/- " + candidate.getOptF1Err()));
			table.getItems().add(new Pair<String, Object>("Input Acc:", candidate.getUserAcc().toString()));
			table.getItems().add(
					new Pair<String, Object>("Best Acc:", candidate.getOptAcc() + " +/- " + candidate.getOptAccErr()));
			table.getItems().add(new Pair<String, Object>("Input DM:", candidate.getUserDM().toString()));
			table.getItems().add(
					new Pair<String, Object>("Best DM: ", candidate.getOptDM() + " +/- " + candidate.getOptDMErr()));

			table.getItems().add(new Pair<String, Object>("Epoch of F0:", candidate.getPeopoch().toString()));
			table.getItems().add(new Pair<String, Object>("Max DM (YMW16):", candidate.getMaxDMYMW16().toString()));
			table.getItems()
					.add(new Pair<String, Object>("Max Distance (YMW16):", candidate.getDistYMW16().toString()));

			table.getItems().add(new Pair<String, Object>("PNG path:", candidate.getPngFilePath()));
			table.getItems().add(new Pair<String, Object>("Metafile path:", candidate.getMetaFilePath()));
			table.getItems().add(new Pair<String, Object>("Filterbank path:", candidate.getFilterbankPath()));
			table.getItems().add(new Pair<String, Object>("Tarball path:", candidate.getTarballPath()));
			
			
		} else {
			table.getItems().add(new Pair<String, Object>("Candidate information will be displayed here", ""));
		}

		TableColumn<Pair<String, Object>, String> nameColumn = new TableColumn<>("Name");
		TableColumn<Pair<String, Object>, Object> valueColumn = new TableColumn<>("Value");
		valueColumn.setSortable(false);

		nameColumn.setCellValueFactory(new PairKeyFactory());
		valueColumn.setCellValueFactory(new PairValueFactory());

		table.getColumns().setAll(nameColumn, valueColumn);

		valueColumn.setCellFactory(
				new Callback<TableColumn<Pair<String, Object>, Object>, TableCell<Pair<String, Object>, Object>>() {
					@Override
					public TableCell<Pair<String, Object>, Object> call(
							TableColumn<Pair<String, Object>, Object> column) {
						return new PairValueCell();
					}
				});

		tab.setContent(table);

	}

	public void progress() {
		candidateCategories.requestLayout();
		next.fire();
//		if (goingForward || imageCounter == 0)
//			next.fire();
//		else
//			previous.fire();
	}
	
	public void updateDiagnosticTab(TabPane tabPane,List<Pulsar> pulsars, Candidate candidate) {
		
		diagnosticTab.setText("Diagnostic");
		final TableView<Pair<String, Object>> table = new TableView<>();

		for (Pulsar pulsar : pulsars) {
			table.getItems().add(new Pair<String, Object>(pulsar.getName() , KnownPulsarGuesser.guessPulsar(candidate, pulsar)));
			
		}
		
		TableColumn<Pair<String, Object>, String> nameColumn = new TableColumn<>("Name");
		TableColumn<Pair<String, Object>, Object> valueColumn = new TableColumn<>("Value");
		valueColumn.setSortable(false);

		nameColumn.setCellValueFactory(new PairKeyFactory());
		valueColumn.setCellValueFactory(new PairValueFactory());

		table.getColumns().setAll(nameColumn, valueColumn);

		valueColumn.setCellFactory(
				new Callback<TableColumn<Pair<String, Object>, Object>, TableCell<Pair<String, Object>, Object>>() {
					@Override
					public TableCell<Pair<String, Object>, Object> call(
							TableColumn<Pair<String, Object>, Object> column) {
						return new PairValueCell();
					}
				});

		diagnosticTab.setContent(table);

		
	}

	public void populateTabs(TabPane tabPane, List<Pulsar> pulsars) {

		for (Pulsar pulsar : pulsars) {

			Tab tab = new Tab();
			tab.setText(pulsar.getName());

			final TableView<Pair<String, Object>> table = new TableView<>();
			table.getItems().add(new Pair<String, Object>("RA:", pulsar.getRa().toHHMMSS()));
			table.getItems().add(new Pair<String, Object>("DEC:", pulsar.getDec().toHHMMSS()));
			table.getItems().add(new Pair<String, Object>("DM:", pulsar.getDm().toString()));
			table.getItems().add(new Pair<String, Object>("P0:", pulsar.getP0().toString()));
			table.getItems().add(new Pair<String, Object>("F0:", pulsar.getF0().toString()));

			String harmonics = "";
			for (int h = -8; h <= 8; h++) {
				harmonics += String.format("%.6f \n ", pulsar.getP0() * Math.pow(2, h));
			}

			table.getItems().add(new Pair<String, Object>("Harmonic periods:", harmonics));
			table.getItems().add(new Pair<String, Object>("Eph:", pulsar.getEphemerides()));

			TableColumn<Pair<String, Object>, String> nameColumn = new TableColumn<>("Name");
			TableColumn<Pair<String, Object>, Object> valueColumn = new TableColumn<>("Value");
			valueColumn.setSortable(false);

			nameColumn.setCellValueFactory(new PairKeyFactory());
			valueColumn.setCellValueFactory(new PairValueFactory());

			table.getColumns().setAll(nameColumn, valueColumn);

			valueColumn.setCellFactory(
					new Callback<TableColumn<Pair<String, Object>, Object>, TableCell<Pair<String, Object>, Object>>() {
						@Override
						public TableCell<Pair<String, Object>, Object> call(
								TableColumn<Pair<String, Object>, Object> column) {
							return new PairValueCell();
						}
					});

			tab.setContent(table);

			tabPane.getTabs().add(tab);

		}

		tabPane.setPrefWidth(200);

	}

	class PairKeyFactory
			implements Callback<TableColumn.CellDataFeatures<Pair<String, Object>, String>, ObservableValue<String>> {
		@Override
		public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<String, Object>, String> data) {
			return new ReadOnlyObjectWrapper<>(data.getValue().getKey());
		}
	}

	class PairValueFactory
			implements Callback<TableColumn.CellDataFeatures<Pair<String, Object>, Object>, ObservableValue<Object>> {
		@SuppressWarnings("unchecked")
		@Override
		public ObservableValue<Object> call(TableColumn.CellDataFeatures<Pair<String, Object>, Object> data) {
			Object value = data.getValue().getValue();
			return (value instanceof ObservableValue) ? (ObservableValue) value : new ReadOnlyObjectWrapper<>(value);
		}
	}

	class PairValueCell extends TableCell<Pair<String, Object>, Object> {
		@Override
		protected void updateItem(Object item, boolean empty) {
			super.updateItem(item, empty);

			if (item != null) {
				if (item instanceof String) {
					setText((String) item);
					setGraphic(null);
				} else if (item instanceof Integer) {
					setText(Integer.toString((Integer) item));
					setGraphic(null);
				} else if (item instanceof Boolean) {
					CheckBox checkBox = new CheckBox();
					checkBox.setSelected((boolean) item);
					setGraphic(checkBox);
				} else if (item instanceof Image) {
					setText(null);
					ImageView imageView = new ImageView((Image) item);
					imageView.setFitWidth(100);
					imageView.setPreserveRatio(true);
					imageView.setSmooth(true);
					setGraphic(imageView);
				} else {
					setText("N/A");
					setGraphic(null);
				}
			} else {
				setText(null);
				setGraphic(null);
			}
		}
	}

	/* process key events */

	EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {

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
				if(!candidatesVisible) return;
				getHostServices().showDocument(new File( baseDir.getAbsolutePath() + File.separator + ((Candidate)imageView.getUserData()).getPngFilePath()).toURI().toString());

			default:
				break;
			}

		}
	};

	public void shortlistCandidates(LocalDateTime utc, List<CANDIDATE_TYPE> types) {
		candidates.clear();
		candidates.addAll(fullCandiatesList.stream()
				.filter(f -> f.getStartUTC().equals(utc) && types.contains(f.getCandidateType()))
				.collect(Collectors.toList()));

	}

	public void sortCandidates() {

		candidates.addAll(candidates.stream().sorted(Comparator.comparing(f -> {

			Candidate candidate = (Candidate) f;

			if (sortBox.getValue() != null) {

				switch (sortBox.getValue()) {
				case "FOLD_SNR":
					return candidate.getFoldSNR();
				case "FFT_SNR":
					return candidate.getFftSNR();
				case "PICS_TRAPUM":
					return candidate.getPicsScoreTrapum();
				case "PICS_PALFA":
					return candidate.getPicsScorePALFA();
				default:
					return candidate.getFoldSNR();

				}
			} else {
				sortBox.setValue("FOLD_SNR");
				return candidate.getFoldSNR();

			}

		}).reversed()).collect(Collectors.toList()));

	}

	public void addAllCandidates(LocalDateTime utc, List<CANDIDATE_TYPE> types) {

		candidates.clear();
		message.setText("Loading...");
		candidates.addAll(fullCandiatesList.stream()
				.filter(f -> f.getStartUTC().equals(utc) && types.contains(f.getCandidateType()))
				.sorted(Comparator.comparing(f -> {

					Candidate candidate = (Candidate) f;

					if (sortBox.getValue() != null) {

						switch (sortBox.getValue()) {
						case "FOLD_SNR":
							return candidate.getFoldSNR();
						case "FFT_SNR":
							return candidate.getFftSNR();
						case "PICS_TRAPUM":
							return candidate.getPicsScoreTrapum();
						case "PICS_PALFA":
							return candidate.getPicsScorePALFA();
						default:
							return candidate.getFoldSNR();

						}
					} else {
						sortBox.setValue("FOLD_SNR");
						return candidate.getFoldSNR();

					}

				}).reversed()).collect(Collectors.toList()));

		candidates.stream().forEach(f -> {
			if (f.getImage() == null) {
				File pngFile = new File(baseDir.getAbsolutePath() + File.separator + f.getPngFilePath());
				try {
					BufferedImage image = ImageIO.read(pngFile);

					if (image.getWidth() != pngWidth
							|| image.getHeight() != pngHeight) {

						ResampleOp resizeOp = new ResampleOp(pngWidth,
								pngHeight);
						BufferedImage scaledImage = resizeOp.filter(image, null);
						f.setImage(SwingFXUtils.toFXImage(scaledImage, null));
						
					}

					else {
						f.setImage(new Image(pngFile.getAbsolutePath()));
					}

				} catch (IOException e) {
					message.setText(e.getMessage());
					e.printStackTrace();

				}

			}
		});
 
	}

	public void consolidate(Integer count) {

		tier1.setSelected(false);
		tier2.setSelected(false);
		rfi.setSelected(false);
		knownPulsar.setSelected(false);
		noise.setSelected(false);

		Candidate candidate = candidates.get(count);
		imageView.setImage(candidate.getImage());
		imageView.setUserData(candidate);
		updateTab(candidateTab, candidate);
		// pulsarPane.getTabs().add(candidateTab);

		counterLabel.setText((imageCounter + 1) + "/" + candidates.size());

		message.setText("");

		chart.getData().removeAll(tempSeries);
		tempSeries.getData().clear();

		String beamName = candidates.get(imageCounter).getBeamName();
		Beam b = metaFile.getBeams().entrySet().stream().filter(f -> {
			Entry<String, Beam> e = (Entry<String, Beam>) f;
			return e.getValue().getName().endsWith(beamName);
		}).collect(Collectors.toList()).get(0).getValue();

		tempSeries.setName(Constants.CANDIDATE_BEAM_MAP);
		Data<Number, Number> d = new Data<Number, Number>(b.getRa().getDecimalHourValue(), b.getDec().getDegreeValue());
		d.setExtraValue(b);
		tempSeries.getData().add(d);

		chart.getData().add(tempSeries);

		switch (candidate.getCandidateType()) {
		case TIER1_CANDIDATE:
			tier1.setSelected(true);
			break;
		case TIER2_CANDIDATE:
			tier2.setSelected(true);
			break;
		case RFI:
			rfi.setSelected(true);
			break;
		case KNOWN_PULSAR:
			knownPulsar.setSelected(true);
			break;
		case NOISE:
			noise.setSelected(true);
			break;

		}

		pulsarPane.requestLayout();

	}

	

	public void configureLayout() {
		
		
		Rectangle2D bounds  = currentScreen.getVisualBounds();
		
		Insets insets = Constants.DEFAULT_INSETS;

		double width = bounds.getWidth();
		double height = bounds.getHeight();
		
		double min = width > height? height : width;
		
		if(min > 2 * Constants.DEFAULT_IMAGE_HEIGHT) {
			pngHeight = pngWidth = Constants.DEFAULT_IMAGE_HEIGHT;
		}
		else {
			pngHeight = (int) (height - 50 - insets.getTop() -insets.getBottom()); // 50 pizels for clarity 
			pngWidth = (int) (height - 50 - insets.getLeft() -insets.getRight()); // use height to maintain aspect ratio
		}
		double remainingWidth = width - insets.getLeft() -insets.getRight() - pngWidth;
		
		
		BorderPane leftPane = new BorderPane();
		
		double leftTopHeight = 80;

		VBox leftTop = new VBox(10, controlBox);
		leftTop.setPrefSize(remainingWidth, leftTopHeight);
		leftTop.setAlignment(Pos.CENTER);
		leftPane.setTop(leftTop);
		
		
		double leftBottomHeight = 30;


		VBox leftBottom = new VBox(10, message);
		leftBottom.setPrefSize(remainingWidth, leftBottomHeight);
		leftPane.setBottom(leftBottom);
		
		double leftCentreHeight = pngHeight - leftBottomHeight - leftTopHeight;
		
		chart.setMinHeight(0.5*leftCentreHeight);

		VBox centerLeft = new VBox(10, chart, pulsarPane, actionsBox);
		centerLeft.setPrefSize(remainingWidth, leftCentreHeight);
		VBox.setVgrow(centerLeft, Priority.ALWAYS);

		leftPane.setCenter(centerLeft);

		root.setLeft(leftPane);

		imageViewHBox.getChildren().add(imageView);
		imageViewHBox.setPrefSize(pngWidth, pngHeight);

		VBox rightPane = new VBox(10, imageViewHBox);
		rightPane.setPrefSize(pngWidth, pngHeight);

		root.setRight(rightPane);
		
		BorderPane.setMargin(leftTop, insets);
		BorderPane.setMargin(centerLeft, insets );
		BorderPane.setMargin(imageViewHBox, insets);
		BorderPane.setMargin(message, insets);
	}

	
	
	public void writeToFile(File saveFile) {


		List<String> list = new ArrayList<String>();
		list.add("utc,png,classification");
		for (Candidate candidate : fullCandiatesList)
			list.add(candidate.getUtcString() + Constants.CSV_SEPARATOR + candidate.getPngFilePath()
					+ Constants.CSV_SEPARATOR + candidate.getCandidateType());

		try {
			FileUtils.writeLines(saveFile, list);
		} catch (IOException e) {
			message.setText(e.getMessage());
			e.printStackTrace();
		}

	}
	public void loadFromFile(File loadFile) {
		
		try {
			List<String> list = Files.readAllLines(loadFile.toPath());
			for (Candidate candidate : fullCandiatesList) {
				
				List<String> lines  = list.stream().filter(f -> f.contains(candidate.getPngFilePath())).collect(Collectors.toList());
				
				if(lines.isEmpty()) {
					message.setText("Problem loading classification, see console for more details");
					System.err.println("Cannot find category for " + candidate.getPngFilePath());
				}
				else if(lines.size() > 1) { 
					message.setText("Loaded classification contains duplicates, see console for more details");
					System.err.println( lines.size() + " values for " + candidate.getPngFilePath());
				}
				else {
					
					String[] chunks = lines.get(0).split(",");
					if (chunks.length != 3) {
						message.setText("Loaded classification contains invalid format, see console for more details");
						System.err.println("Invalid format in line: " + lines + "");

					}
					candidate.setCandidateType(CANDIDATE_TYPE.valueOf(chunks[2]));
					
				}
				
				
				
			}
			
		} catch (IOException e) {
			message.setText(e.getMessage());
			e.printStackTrace();
		}
		

		
	}
	
	
	public void checkSimilarity(List<Candidate> candidates) {
		
		for(Candidate c1: candidates) {
			
			for(Candidate c2: candidates) {
				
				
				if (c1.isSimilarTo(c2)) { 
					if(!c1.equals(c2)) c1.getSimilarParamCandidates().add(c2);
				}
				
				
			}
			
		}
		
	}
	
	
	
	static CommandLineParser parser = new DefaultParser();
	static Options options = new Options();
	static CommandLine commandLine;

	
	public static void main(String[] args) throws IOException {
		
		if(System.getenv("PSRCAT_DIR") != null) {
			PsrcatConstants.psrcatDBs.add(System.getenv("PSRCAT_DIR") + File.separator + "psrcat.db");
			
		}
		
		
		Locale.setDefault(Locale.US);

		
		Option selectScreen  = new Option("s","screen", true, "Choose which screen to open the application in. Use --list_screens to get a list of screens");
		Option help = new Option("h","help",false, "show this help message");
		Option listScreens = new Option("l","list_screens",false, "List available screens");
		
		Option addPsrcatDB = new Option("d","add_psrcat_db",true, "Add a psrcat database to get known pulsars from");
		
		options.addOption(selectScreen);
		options.addOption(help);
		options.addOption(listScreens);
		options.addOption(addPsrcatDB);

		try{
			
			commandLine = parser.parse(options, args);

			if(hasOption(help)){
				help();
				System.exit(0);
			}
			
			if(hasOption(listScreens)) {
				int n=0;
				for(Screen screen: Screen.getScreens()) {
					Rectangle2D rec = screen.getBounds();
					System.err.println("Screen #"+ ++n + ": " + rec.getWidth() + "x" + rec.getHeight());
					
				}
				System.exit(0);
			}
			if(hasOption(selectScreen)) {
				Integer value = Integer.parseInt(getValue(selectScreen));
				
				currentScreen = Screen.getScreens().get(value);
				
				
			}
			
			if(hasOption(addPsrcatDB)) {
				String value = getValue(addPsrcatDB);
				PsrcatConstants.psrcatDBs.add(value);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		

		Application.launch(CandyJar.class);

		//launch(args);
	}
	
	public static boolean hasOption(Option option){
		return commandLine.hasOption(option.getOpt());
	}
	
	public static String getValue(Option option){
		return commandLine.getOptionValue(option.getOpt());
	}
	
	
	public static void help(){
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("CandyJar", options);
	}
}