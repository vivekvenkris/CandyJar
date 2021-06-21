package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mortennobel.imagescaling.ResampleOp;

import constants.Constants;
import constants.PsrcatConstants;
import data_holders.Angle;
import data_holders.Beam;
import data_holders.Candidate;
import data_holders.Candidate.CANDIDATE_PLOT_CATEGORY;
import data_holders.Candidate.CANDIDATE_TYPE;
import data_holders.MetaFile;
import data_holders.Pulsar;
import de.gsi.chart.Chart;
import exceptions.InvalidInputException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;
import readers.CandidateFileReader;
import readers.Psrcat;
import utilitites.AppUtils;
import utilitites.Utilities;

public class CandyJar extends Application implements Constants {

//	private static Screen primaryScreen = Screen.getPrimary();
//	private static Screen secondaryScreen = null;

	private static Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
	private static Rectangle2D secondaryScreenBounds = null;

	private static Integer numCharts = 2;
	private static Integer minNumCharts = 0;
	private static Integer maxNumCharts = 3;

	private static Boolean extendPng = false;

	Psrcat psrcat = new Psrcat();

	/* main pane*/
	BorderPane mainBorderPane = new BorderPane();

	/*Top Left: Load directory and CSV */
	File baseDir = null;
	TextAndButton rootDirTB = new TextAndButton(null,"Results directory","Get pointings", 10);
	Button fileSelectButton = new Button("...");
	final Button loadClassification = new Button("Load classification");


	/* Top left: Filter and sort candidates */
	final ComboBox<String> utcBox = new ComboBox<String>();
	final ComboBox<String> sortBox = new ComboBox<String>(FXCollections.observableArrayList(Candidate.SORTABLE_PARAMETERS_MAP.keySet()));
	
	 
     // Creating new Toggle buttons.
     ToggleButton ascendingButton = new ToggleButton("A");
     ToggleButton descendingButton = new ToggleButton("D");
	final SegmentedButton sortOrder = new SegmentedButton(ascendingButton, descendingButton);
	
	
	final CheckComboBox<CANDIDATE_TYPE> filterTypes = new CheckComboBox<CANDIDATE_TYPE>(FXCollections.observableArrayList(Arrays.asList(CANDIDATE_TYPE.values())));
	final List<CANDIDATE_TYPE> filteredTypes = new ArrayList<CANDIDATE_TYPE>();
	final Button filterCandidates = new Button("Go");


	final HBox candidateFilterHBox = new HBox(10, filterTypes, sortBox, sortOrder, filterCandidates);
	final VBox controlBox = new VBox(10, new HBox(10, rootDirTB.getTextField(),fileSelectButton,rootDirTB.getButton(), loadClassification),	new HBox(10, utcBox, candidateFilterHBox));


	/* Left Middle: Beam Map and tabbed pane */
	final NumberAxis beamMapXAxis = new NumberAxis();
	final NumberAxis beamMapYAxis = new NumberAxis();
	final MyScatterChart beamMapChart = new MyScatterChart(beamMapXAxis, beamMapYAxis);
	double initXLowerBound = 0, initXUpperBound = 0, initYLowerBound = 0, initYUpperBound = 0;


	final TabPane infoPane = new TabPane();
	final Tab candidateTab = new Tab("Candidate Info");
	final Tab diagnosticTab = new Tab("Diagnostics");


	/* Bottom left: message label, controls  and filters*/
	final Button previous = new Button("Previous (a)");
	final Button next = new Button("Next (d)");
	final LabelWithTextAndButton gotoCandidate = new LabelWithTextAndButton("Go to", "", "Go");
	final Label counterLabel = new Label();

	final ToggleButton rfi = new ToggleButton("RFI (y)");
	final ToggleButton noise = new ToggleButton("Noise (u)");
	final ToggleButton tier1 = new ToggleButton("Tier1 (i)");
	final ToggleButton tier2 = new ToggleButton("Tier2 (o)");
	final ToggleButton knownPulsar = new ToggleButton("Known pulsar (p)");
	final ToggleButton reset = new ToggleButton("Uncat (r)");

	final SegmentedButton candidateCategories = new SegmentedButton(FXCollections.observableArrayList(Arrays.asList(new ToggleButton[] {rfi, noise, tier1, tier2, knownPulsar, reset})));
	final Label message = new Label("All ok. Select a directory");

	final Button saveClassification = new Button("Save classification");


	final VBox actionsBox = new VBox(10, new HBox(10, previous, counterLabel, next, gotoCandidate.getTextField(), gotoCandidate.getButton(), saveClassification),
			new HBox(10, new Label("Select Category:"), candidateCategories));

	/*Right: imageView */
	final ImageView imageView = new ImageView();
	final HBox imageViewHBox = new HBox(10);

	/* task schedule for saving temporary file */ 

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);



	/* variables for data manipulation */

	MetaFile metaFile = null;
	final List<Candidate> fullCandiatesList = new ArrayList<Candidate>();
	final Map<String, List<Candidate>> candidateMap = new HashMap<String, List<Candidate>>();
	final List<Candidate> candidates = new ArrayList<>();
	final Set<LocalDateTime> utcs = new TreeSet<LocalDateTime>();
	final List<Pulsar> pulsarsInBeam = new ArrayList<Pulsar>();

	Candidate currentCandidate = null;

	Integer imageCounter = 0;

	Integer numImageGulp=10;



	// final List<Image> images = new ArrayList<>();


	final Button resetCandidateCategory = new Button("Reset (R)");

	final XYChart.Series<Number, Number> selectedCandidateSeries = new XYChart.Series<Number, Number>();

	String userName = System.getProperty("user.name");

	Integer pngPaneHeight = Constants.DEFAULT_IMAGE_HEIGHT;
	Integer pngPaneWidth = Constants.DEFAULT_IMAGE_WIDTH;


	Boolean goingForward = Boolean.valueOf(Boolean.TRUE);

	Boolean candidatesVisible=false;


	/* second screen */

	ChartViewer chartViewer = null;

	public void initialise() {
		
		beamMapChart.init();
	
		utcs.clear();
		utcBox.getSelectionModel().clearSelection();
		utcBox.valueProperty().set(null);
		utcBox.getItems().clear();
		beamMapChart.getData().clear();
		
		fullCandiatesList.clear();
		
		filterTypes.setTitle("FILTER_TYPE");
		utcBox.setPromptText("SELECT_UTC");
		sortBox.setPromptText("SORT_BY");

		filterTypes.setTooltip(new Tooltip("Select the types of candidates to filter"));
		utcBox.setTooltip(new Tooltip("Select UTC to view candidates"));
		sortBox.setTooltip(new Tooltip("Select value to sort by and the order"));



		filterTypes.getCheckModel().getCheckedItems().addListener(new ListChangeListener<CANDIDATE_TYPE>() {
			public void onChanged(ListChangeListener.Change<? extends CANDIDATE_TYPE> c) {
				filteredTypes.clear();
				filteredTypes.addAll(filterTypes.getCheckModel().getCheckedItems());
				filterTypes.setTitle("FILTER_TYPE");
			}
		});

		//candidateCategories.getButtons().addAll(rfi, noise, tier1, tier2, knownPulsar);
		candidateCategories.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
		loadClassification.setVisible(false);
		utcBox.setVisible(false);
		beamMapChart.setVisible(false);
		candidateFilterHBox.setVisible(false);
		sortBox.setVisible(false);
		actionsBox.setVisible(false);
		sortOrder.setVisible(false);
		infoPane.getTabs().clear();
		imageView.setVisible(false);
		gotoCandidate.getTextField().setPromptText("Go to Candidate");


		message.setTextFill(Paint.valueOf("darkred"));
		
		if(chartViewer != null && chartViewer.isShowing()) chartViewer.close();

		chartViewer = secondaryScreenBounds!=null? new ChartViewer(secondaryScreenBounds, numCharts, this): null;
	}


	@Override
	public void start(Stage stage) throws Exception {


		configureLayout();
		initialise();

		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				
				if(rootDirTB == null || rootDirTB.getTextField().getText().isBlank() || baseDir == null || userName == null || fullCandiatesList.isEmpty()) {
					return;
				}
				System.err.println("Autosaving to " + baseDir.getName() + "_" + userName+"_autosave.csv");

				writeToFile(new File(rootDirTB.getTextField().getText(), baseDir.getName() + "_" + userName+"_autosave.csv"));

				
			}
		}, 2, 2, TimeUnit.MINUTES);


		filterTypes.getCheckModel().check(filterTypes.getCheckModel().getItemIndex(CANDIDATE_TYPE.UNCAT));



		DirectoryChooser directoryChooser = new DirectoryChooser();
		fileSelectButton.setOnAction(e -> {
			File selectedDirectory = directoryChooser.showDialog(stage);
			if(selectedDirectory ==null) return;
			initialise();
			rootDirTB.getTextField().setText(selectedDirectory.getAbsolutePath());
			
		});

		rootDirTB.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if(rootDirTB.getTextField().getText().isBlank()) {
					message.setText("Select a valid directory");
					return;
				}

				System.err.println("Loading new root directory: " + rootDirTB.getTextField().getText());
				baseDir = new File(rootDirTB.getTextField().getText());

				String csv = baseDir.getAbsolutePath() + File.separator + Constants.CSV_FILE_NAME;



				imageCounter = 0;
				utcBox.setVisible(true);
				loadClassification.setVisible(true);

				try {
					fullCandiatesList.addAll(CandidateFileReader.readCandidateFile(csv, baseDir));

					utcs.addAll(fullCandiatesList.stream().map(f -> f.getStartUTC()).collect(Collectors.toSet()));

					utcBox.setItems(FXCollections.observableArrayList(utcs.stream()
							.map(f -> Utilities.getUTCString(f, commonUTCFormat)).collect(Collectors.toList())));

					for(LocalDateTime utc: utcs) { 
						System.err.println("utc: "+ utc);
						String utcString = Utilities.getUTCString(utc, DateTimeFormatter.ISO_DATE_TIME);
						List<Candidate> candidatesPerUtc = fullCandiatesList.stream().filter(f -> f.getStartUTC().equals(utc)).collect(Collectors.toList());
						candidateMap.put(utcString, candidatesPerUtc);
						checkSimilarity(candidatesPerUtc);

					}

					message.setText(utcs.size() + " utcs found");

				} catch (NoSuchFileException e) {
					message.setText(e.getMessage());
					e.printStackTrace();
					utcBox.setVisible(false);
					loadClassification.setVisible(false);
				}catch (IOException e) {
					message.setText(e.getMessage());
					e.printStackTrace();
					utcBox.setVisible(false);
					loadClassification.setVisible(false);
				} catch (InvalidInputException e) {
					message.setText(e.getMessage());
					e.printStackTrace();
					utcBox.setVisible(false);
					loadClassification.setVisible(false);
				}

			}

		});

		utcBox.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				String utcString = utcBox.getValue();
				if (utcString == null) {
					System.err.println("utc value not set");
					return;

					
				}

				
				// images.clear();
				candidates.stream().forEach(f-> f.setImage(null));
				candidates.clear();
				imageCounter = 0;

				candidateFilterHBox.setVisible(true);
				beamMapChart.setVisible(true);
				imageView.setVisible(false);
				actionsBox.setVisible(false);
				sortBox.setVisible(true);
				sortOrder.setVisible(true);
				candidatesVisible = false;

				
				System.err.println("UTC:" + utcString);

				LocalDateTime utc = Utilities.getUTCLocalDateTime(utcString, Constants.commonUTCFormat);

				/* stupid stub because you need atleast one candidate to get the meta file */
				shortlistCandidates(utc, Arrays.asList(CANDIDATE_TYPE.values()));

				metaFile = candidates.get(imageCounter).getMetaFile();

				metaFile.findNeighbours();
				pulsarsInBeam.clear();
				pulsarsInBeam.addAll(psrcat.getPulsarsInBeam(metaFile.getBoresight().getRa(),
						metaFile.getBoresight().getDec(), new Angle(1.0, Angle.DEG, Angle.DEG)));
				infoPane.getTabs().clear();
				candidateTab.setClosable(false);
				diagnosticTab.setClosable(false);
				updateTab(candidateTab, null);
				populatePulsarTabs(infoPane);
				infoPane.getTabs().add(0, candidateTab);
				infoPane.getTabs().add(1, diagnosticTab);

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

				beamMapXAxis.setLowerBound(xLowerBound);
				beamMapXAxis.setUpperBound(xUpperBound);
				beamMapXAxis.setTickUnit(0.01);

				beamMapYAxis.setLowerBound(yLowerBound);
				beamMapYAxis.setUpperBound(yUpperBound);
				beamMapYAxis.setTickUnit(-0.01);

				beamMapXAxis.setLabel("RA (hms)");
				beamMapYAxis.setLabel("DEC (dms)");

				addDefaultMap(metaFile, pulsarsInBeam);

				message.setText("Meta file and neighbouring pulsars added. Please filter required candidates.");

			

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
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.T1_CAND);
				progress();

			}
		});

		tier2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.T2_CAND);
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
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.KNOWN_PSR);
				progress();

			}
		});
		
		reset.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.UNCAT);

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

				if(saveFile == null ) {
					message.setText("Aborting saving classification.");
					return;
				}

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

					message.setText("Aborting saving classification.");

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


				if(loadFile == null) {
					message.setText("Aborted loading existing classification.");
				}

				else if(!loadFile.exists()) {
					message.setText("File does not exist");
				}
				else {
					loadFromFile(loadFile);
				}


			}
		});

		TextAndButton userNameTB = new TextAndButton(userName, "User Name", "save", 10);

		userNameTB.getButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				userName = userNameTB.getTextField().getText();
				message.setText("Username updated successfully.");
			}
		});


		mainBorderPane.setOnMouseClicked(f -> {
			mainBorderPane.requestFocus();
		});

		Platform.runLater(() -> mainBorderPane.requestFocus());

		Scene scene = new Scene(mainBorderPane, primaryScreenBounds.getWidth(), primaryScreenBounds.getHeight());

		stage.setX(primaryScreenBounds.getMinX());
		stage.setY(primaryScreenBounds.getMinY());

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				scheduler.shutdown();
				Platform.exit();


			}
		});

		scene.setOnKeyPressed(keyEventHandler);

		scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

		stage.setTitle("CandyJar");
		stage.setScene(scene);
		//stage.setResizable(false);
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

		beamMapChart.setOnMouseClicked(mouseHandler);
		beamMapChart.setOnMouseDragged(mouseHandler);
		beamMapChart.setOnMouseEntered(mouseHandler);
		beamMapChart.setOnMouseExited(mouseHandler);
		beamMapChart.setOnMouseMoved(mouseHandler);
		beamMapChart.setOnMousePressed(mouseHandler);
		beamMapChart.setOnMouseReleased(mouseHandler);

		rect = new Rectangle();
		rect.setFill(Color.web("LIGHTBLUE", 0.1));
		rect.setStroke(Color.LIGHTBLUE);
		rect.setStrokeDashOffset(50);

		rect.widthProperty().bind(rectX.subtract(rectinitX));
		rect.heightProperty().bind(rectY.subtract(rectinitY));
		mainBorderPane.getChildren().add(rect);

	}

	public void addDefaultMap(MetaFile metaFile, List<Pulsar> pulsars) {

		beamMapChart.getData().clear();

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

		beamMapChart.getData().add(beamPositions);
		//chart.getData().add(pulsarPositions);

	}


	public void updateTab(Tab tab, Candidate candidate) {
		tab.setText("Candidate Info");

		final TableView<Pair<String, Object>> table = new TableView<>();

		if (candidate != null) {

			Beam beam = metaFile.getBeams().get(candidate.getBeamName());
			List<Integer> neighbours = new ArrayList<Integer>();
			if (beam != null)
				neighbours.addAll(beam.getNeighbourBeams().stream()
						.map(f -> f.getIntegerBeamName()).collect(Collectors.toList()));
			else
				message.setText("Cannot find beam: " + candidate.getBeamName());

			table.getItems().add(new Pair<String, Object>("Pointing ID:", new CopyableLabel(candidate.getPointingID()))); 
			table.getItems().add(new Pair<String, Object>("Beam ID:", new CopyableLabel(candidate.getBeamID())));
			table.getItems().add(new Pair<String, Object>("Beam Name:", new CopyableLabel(candidate.getBeamName())));
			table.getItems().add(new Pair<String, Object>("Neighbour beams:", neighbours.toString()));
			table.getItems()
			.add(new Pair<String, Object>("PICS score (TRAPUM):", new CopyableLabel(candidate.getPicsScoreTrapum())));
			table.getItems()
			.add(new Pair<String, Object>("PICS score (PALFA):", new CopyableLabel(candidate.getPicsScorePALFA())));
			table.getItems().add(new Pair<String, Object>("FFT SNR:", new CopyableLabel(candidate.getFftSNR())));
			table.getItems().add(new Pair<String, Object>("Fold SNR: ", new CopyableLabel(candidate.getFoldSNR())));

			table.getItems().add(new Pair<String, Object>("Boresight:", new CopyableLabel(candidate.getSourceName())));
			table.getItems().add(new Pair<String, Object>("RA:", new CopyableLabel(candidate.getRa())));
			table.getItems().add(new Pair<String, Object>("DEC:", new CopyableLabel(candidate.getDec())));
			table.getItems().add(new Pair<String, Object>("GL:", new CopyableLabel(candidate.getGl())));
			table.getItems().add(new Pair<String, Object>("GB:", new CopyableLabel(candidate.getGb())));

			table.getItems().add(new Pair<String, Object>("Start MJD:", new CopyableLabel(candidate.getStartMJD())));
			table.getItems().add(new Pair<String, Object>("Start UTC:", new CopyableLabel(candidate.getStartUTC())));
			table.getItems().add(new Pair<String, Object>("Input F0:", new CopyableLabel(candidate.getUserF0())));
			table.getItems().add(
					new Pair<String, Object>("Best F0:", new CopyableLabel(candidate.getOptF0() + " +/- " + candidate.getOptF0Err())));
			table.getItems().add(new Pair<String, Object>("Input F1:", new CopyableLabel(candidate.getUserF1())));
			table.getItems().add(
					new Pair<String, Object>("Best F1:", new CopyableLabel(candidate.getOptF1() + " +/- " + candidate.getOptF1Err())));
			table.getItems().add(new Pair<String, Object>("Input Acc:", new CopyableLabel(candidate.getUserAcc())));
			table.getItems().add(
					new Pair<String, Object>("Best Acc:", new CopyableLabel(candidate.getOptAcc() + " +/- " + candidate.getOptAccErr())));
			table.getItems().add(new Pair<String, Object>("Input DM:", new CopyableLabel(candidate.getUserDM())));
			table.getItems().add(
					new Pair<String, Object>("Best DM: ", new CopyableLabel(candidate.getOptDM() + " +/- " + candidate.getOptDMErr())));

			table.getItems().add(new Pair<String, Object>("Epoch of F0:", new CopyableLabel(candidate.getPeopoch())));
			table.getItems().add(new Pair<String, Object>("Max DM (YMW16):", new CopyableLabel(candidate.getMaxDMYMW16())));
			table.getItems()
			.add(new Pair<String, Object>("Max Distance (YMW16):", new CopyableLabel(candidate.getDistYMW16())));

			table.getItems().add(new Pair<String, Object>("PNG path:", new CopyableLabel(candidate.getPngFilePath())));
			table.getItems().add(new Pair<String, Object>("Metafile path:", new CopyableLabel(candidate.getMetaFilePath())));
			table.getItems().add(new Pair<String, Object>("Filterbank path:", new CopyableLabel(candidate.getFilterbankPath())));
			table.getItems().add(new Pair<String, Object>("Tarball path:", new CopyableLabel(candidate.getTarballPath())));


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

	public void updateDiagnosticTab(TabPane tabPane, Candidate candidate) {
		final TableView<Pair<String, Object>> table = new TableView<>();
		VBox similarCandidateVBox = new VBox();
		similarCandidateVBox.getChildren().addAll(candidate.getSimilarCandidatesInFreq().stream().map(f -> {
			String fileStr = f.getPngFilePath();
			Hyperlink link = new Hyperlink(fileStr);
			link.setText(f.getF0DMString());
			link.setOnAction(e ->
				getHostServices().showDocument(new File( baseDir.getAbsolutePath() + File.separator + fileStr).toURI().toString()));

			return link;

		}).collect(Collectors.toList()));
		if(similarCandidateVBox.getChildren().isEmpty()) similarCandidateVBox.getChildren().add(new Label("None"));
		
		table.getItems().add(new Pair<String, Object>("Likely related candidates:" , similarCandidateVBox));

		if(pulsarsInBeam != null && !pulsarsInBeam.isEmpty()) {

			for (Pulsar pulsar : pulsarsInBeam) {
				table.getItems().add(new Pair<String, Object>(pulsar.getName() , KnownPulsarGuesser.guessPulsar(candidate, pulsar)));
				

			}

		}else {
			table.getItems().add(new Pair<String, Object>("Diagnostic information will be displayed here", ""));
		}
		
//		Button dspsrButton = new Button();
//		String dspsrText = "dspsr -t 4 -U 256 -k meerkat -c " + candidate.get
//		
//		table.getItems().add(new Pair<String, Object>("dspsr Predictor file", ""));
		
		boolean addAcc = false;
		if(Math.abs(candidate.getOptAcc() / candidate.getOptAccErr()) > 2 ) addAcc = true;
		

		
		Button prepfoldButton = new Button("prepfold");
		Button pulsarxButton = new Button("pulsarx");
		Button dspsrButton = new Button("dspsr");
		
		String outputStr = candidate.getSourceName()+"_"+ candidate.getBeamName() +"_" + candidate.getLineNum();
		String prepfoldTextGen = "prepfold" +  " -fixchi -dm " + candidate.getOptDM() + " -nsub 64 -npart 64 -f " + candidate.getF0AtStart();
		if(addAcc) prepfoldTextGen += " -fd " + candidate.getOptF1();
		prepfoldTextGen +=	" -o " + outputStr + " " + candidate.getFilterbankPath();			 
						
		String pulsarxTextGen = "psrfold_fil -v -t 4 --template /home/psr/software/PulsarX/include/template/meerkat_fold.template "
				+ "-L 10 --clfd 2.0 -z zdot -z kadaneF 8 4 -n 64 -b 64 -dspsr -plotx -dm " + candidate.getOptDM() + " -f0 " + + candidate.getOptF0();		
		if(addAcc)		pulsarxTextGen+= " -acc " + candidate.getOptAcc();		
		pulsarxTextGen+= " -o " + outputStr + " -f" + candidate.getFilterbankPath() ;
		
		String dspsrTextGen = "dspsr -t 4 -k meerkat -b 128 -A  -Lmin 15 -L 20";
		if(addAcc) {
			String predictor = "SOURCE: " + outputStr + "\\n"
					+ "PERIOD: " + candidate.getOptP0() + " s\\n"
					+ "DM: "+ candidate.getOptDM()+"\\n"
					+ "ACC: "+ candidate.getOptAcc()+" (m/s/s)\\n"
					+ "RA: "+ candidate.getRa()+"\\n"
					+ "DEC: "+ candidate.getDec();
			dspsrTextGen = "echo \"" + predictor + "\" > " + outputStr+"_pred.txt;" +  dspsrTextGen + " -P " + outputStr+"_pred.txt " +  " -O " + outputStr + candidate.getFilterbankPath() ;
		}
		else {
			dspsrTextGen = dspsrTextGen + " -c " + candidate.getP0AtStart() + " -D " + candidate.getOptDM()  + " -O " + outputStr ;
		}
		
		final String prepfoldText = prepfoldTextGen;
		final String pulsarxText = pulsarxTextGen;
		final String dspsrText = dspsrTextGen;
		
		prepfoldButton.setOnAction(e -> {
			AppUtils.copyToClipboardText(prepfoldText);
		});
		
		pulsarxButton.setOnAction(e -> {
			AppUtils.copyToClipboardText(pulsarxText);
		});
		
		dspsrButton.setOnAction(e -> {
			AppUtils.copyToClipboardText(dspsrText);
		});
		
		
		table.getItems().add(0, new Pair<String, Object>("Copy folding commands", new VBox(prepfoldButton, pulsarxButton, dspsrButton)));


		TableColumn<Pair<String, Object>, String> nameColumn = new TableColumn<>("Item");
		TableColumn<Pair<String, Object>, Object> valueColumn = new TableColumn<>("Description");
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

	public void populatePulsarTabs(TabPane tabPane) {

		for (Pulsar pulsar : pulsarsInBeam) {

			Tab tab = new Tab();
			tab.setText(pulsar.getName());

			final TableView<Pair<String, Object>> table = new TableView<>();
			table.getItems().add(new Pair<String, Object>("RA:", new CopyableLabel(pulsar.getRa().toHHMMSS())));
			table.getItems().add(new Pair<String, Object>("DEC:", new CopyableLabel(pulsar.getDec().toHHMMSS())));
			table.getItems().add(new Pair<String, Object>("DM:",new CopyableLabel(pulsar.getDm().toString())));
			table.getItems().add(new Pair<String, Object>("P0:", new CopyableLabel(pulsar.getP0().toString())));
			table.getItems().add(new Pair<String, Object>("F0:", new CopyableLabel(pulsar.getF0().toString())));

			String harmonics = "";
			for (int h = -8; h <= 8; h++) {
				harmonics += String.format("%.6f \n ", pulsar.getP0() * Math.pow(2, h));
			}

			table.getItems().add(new Pair<String, Object>("Harmonic periods:", new CopyableLabelArea(harmonics)));
			table.getItems().add(new Pair<String, Object>("Eph:", new CopyableLabelArea(pulsar.getEphemerides())));

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
			case R:
				reset.fire();
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

	public void addAllCandidates(LocalDateTime utc, List<CANDIDATE_TYPE> types) {

		candidates.clear();
		message.setText("Loading...");
		if(sortBox.getValue() ==null) sortBox.setValue(Candidate.DEFAULT_SORT_PARAMETER);
		if(!ascendingButton.isSelected() && !descendingButton.isSelected()) descendingButton.setSelected(true);

		List<Candidate> candidatesForUTC = fullCandiatesList.stream().filter(f-> f.getStartUTC().equals(utc)).collect(Collectors.toList());
		Function<Candidate, Double> fn = Candidate.SORTABLE_PARAMETERS_MAP.get(sortBox.getValue());
		
		List<Candidate> sortedCands = candidatesForUTC.stream()
				.filter(f -> f.getStartUTC().equals(utc) && types.contains(f.getCandidateType()) && f.isVisible())
				.sorted(Comparator.comparing(f -> {
					Double result = fn.apply((Candidate) f);
					if (result == null) result = 0.0;
					return result;
				})).collect(Collectors.toList());
		
		if(descendingButton.isSelected()) Collections.reverse(sortedCands);
		
		candidates.addAll(sortedCands);



		if(chartViewer != null) {
			chartViewer.clearMap();
			chartViewer.addToMap(CANDIDATE_PLOT_CATEGORY.ALL, candidatesForUTC);
			chartViewer.getAllCandidates().addAll(candidatesForUTC);
		}




	}

	public void organiseImages(Integer count) {

		AtomicInteger runCount = new AtomicInteger(0);

		candidates.stream().forEach(f -> {
			int index = runCount.getAndIncrement();
			if( Math.abs(count - index) <= numImageGulp) {
				if (f.getImage() == null) {
					File pngFile = new File(baseDir.getAbsolutePath() + File.separator + f.getPngFilePath());
					try {
						BufferedImage image = ImageIO.read(pngFile);

						if( (image.getWidth() == pngPaneWidth && image.getHeight() == pngPaneHeight) ||   
								(image.getWidth() < pngPaneWidth && image.getHeight() < pngPaneHeight && !CandyJar.extendPng) ) {

							f.setImage(new Image(pngFile.toURI().toString()));

						}

						else {
							ResampleOp resizeOp = new ResampleOp(pngPaneWidth,
									pngPaneHeight);
							BufferedImage scaledImage = resizeOp.filter(image, null);
							f.setImage(SwingFXUtils.toFXImage(scaledImage, null));
						}

					} catch (IOException e) {
						message.setText(e.getMessage());
						e.printStackTrace();

					}

				}
			}
			else {
				f.setImage(null);
			}

		});


	}

	public void consolidate(Integer count) {


		tier1.setSelected(false);
		tier2.setSelected(false);
		rfi.setSelected(false);
		knownPulsar.setSelected(false);
		noise.setSelected(false);
		reset.setSelected(true);

		organiseImages(count);


		Candidate candidate = candidates.get(count);


		imageView.setImage(candidate.getImage());
		imageView.setUserData(candidate);
		updateTab(candidateTab, candidate);
		updateDiagnosticTab(infoPane, candidate);
		// pulsarPane.getTabs().add(candidateTab);

		counterLabel.setText((imageCounter + 1) + "/" + candidates.size());

		message.setText("");

		beamMapChart.getData().removeAll(selectedCandidateSeries);
		selectedCandidateSeries.getData().clear();

		String beamName = candidates.get(imageCounter).getBeamName();
		Beam b = metaFile.getBeams().entrySet().stream().filter(f -> {
			Entry<String, Beam> e = (Entry<String, Beam>) f;
			return e.getValue().getName().endsWith(beamName);
		}).collect(Collectors.toList()).get(0).getValue();

		selectedCandidateSeries.setName(Constants.CANDIDATE_BEAM_MAP);
		Data<Number, Number> d = new Data<Number, Number>(b.getRa().getDecimalHourValue(), b.getDec().getDegreeValue());
		d.setExtraValue(b);
		selectedCandidateSeries.getData().add(d);

		beamMapChart.getData().add(selectedCandidateSeries);

		switch (candidate.getCandidateType()) {
		case T1_CAND:
			tier1.setSelected(true);
			break;
		case T2_CAND:
			tier2.setSelected(true);
			break;
		case RFI:
			rfi.setSelected(true);
			break;
		case KNOWN_PSR:
			knownPulsar.setSelected(true);
			break;
		case NOISE:
			noise.setSelected(true);
			break;
		case UNCAT:
			reset.setSelected(true);
			break;

		}

		infoPane.requestLayout();

		if(chartViewer!=null) chartViewer.addToMap(CANDIDATE_PLOT_CATEGORY.CURRENTLY_VIEWING, Arrays.asList(candidate));
	}



	public void configureLayout() {


		Rectangle2D bounds  = primaryScreenBounds;

		Insets insets = Constants.DEFAULT_INSETS;

		double width = bounds.getWidth();
		double height = bounds.getHeight();

		double min = width > height? width:height;

		int widthBy2 = (int)(width - 20 - insets.getLeft() -insets.getRight())/2;// 20 pixels for clarity 

		System.err.println("initial bounds height:" + height + " width:" + width);


		pngPaneWidth = widthBy2;
		pngPaneHeight = (int) (height - 20 - insets.getTop() -insets.getBottom());

		System.err.println("initial png height:" + pngPaneHeight + " width:" + pngPaneWidth);


		pngPaneWidth = pngPaneHeight > pngPaneWidth? pngPaneWidth : pngPaneHeight; //choose the smaller of the two
		pngPaneHeight = pngPaneWidth;

		System.err.println("final png height:" + pngPaneHeight + " width:" + pngPaneWidth);

		//		if(min > 2 * Constants.DEFAULT_IMAGE_HEIGHT) {
		//			pngPaneHeight = pngPaneWidth = Constants.DEFAULT_IMAGE_HEIGHT;
		//		}
		//		else {
		//			pngPaneHeight = (int) (height - 20 - insets.getTop() -insets.getBottom()); // 2d0 pizels for clarity 
		//			pngPaneWidth = (int) (height - 20 - insets.getLeft() -insets.getRight()); // use height to maintain aspect ratio
		//		}
		//		double remainingWidth = width - insets.getLeft() -insets.getRight() - pngPaneWidth;

		double remainingWidth = pngPaneWidth;

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

		double leftCentreHeight = pngPaneHeight - leftBottomHeight - leftTopHeight;

		beamMapChart.setMinHeight(0.5*leftCentreHeight);

		VBox centerLeft = new VBox(10, beamMapChart, infoPane, actionsBox);
		centerLeft.setPrefSize(remainingWidth, leftCentreHeight);
		VBox.setVgrow(centerLeft, Priority.ALWAYS);

		leftPane.setCenter(centerLeft);

		mainBorderPane.setLeft(leftPane);

		imageViewHBox.getChildren().add(imageView);
		imageViewHBox.setPrefSize(pngPaneWidth, pngPaneHeight);

		VBox rightPane = new VBox(10, imageViewHBox);
		rightPane.setPrefSize(pngPaneWidth, pngPaneHeight);

		mainBorderPane.setRight(rightPane);

		BorderPane.setMargin(leftTop, insets);
		BorderPane.setMargin(centerLeft, insets );
		BorderPane.setMargin(imageViewHBox, insets);
		BorderPane.setMargin(message, insets);
	}



	public void writeToFile(File saveFile) {
		
		if(fullCandiatesList.isEmpty()) {
			System.err.println("No candidates to write, ignoring this write call.");
		}


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
					if(!c1.equals(c2)) {
						c1.getSimilarCandidatesInFreq().add(c2);

					}
					
				}


			}

		}

	}



	static CommandLineParser parser = new DefaultParser();
	static Options options = new Options();
	static CommandLine commandLine;
	private static final Logger LOGGER = LoggerFactory.getLogger(Chart.class);




	public static void main(String[] args) throws IOException {

		LOGGER.atDebug().addArgument("test");

		System.err.println("*************************Candy Jar V2.1-alpha*******************************");

		if(System.getenv("PSRCAT_DIR") != null) {
			PsrcatConstants.psrcatDBs.add(System.getenv("PSRCAT_DIR") + File.separator + "psrcat.db");

		}


		Locale.setDefault(Locale.US);

		Option selectPrimaryScreen  = new Option("s1","primary_screen", true, "Choose primary screen to open the application in. "
				+ "The application opens full screen by default. You can provide an optional custom resolution if you like, of the format: <screen—num>:widthxheight. Eg: 1:1920x1080 will open the application on"
				+ "your first screen, with the resolution of 1920x1080");
		Option selectSecondaryScreen  = new Option("s2","secondary_screen", true, "Choose secondary screen to open the application in. "
				+ "You can provide custom resolution like for screen 1");
		Option numCharts = new Option("n","num_charts", true, "Number of charts needed on the secondary screen (Min:"+ minNumCharts+", max:"+ maxNumCharts+")");
		Option help = new Option("h","help",false, "show this help message");
		Option listScreens = new Option("l","list_screens",false, "List available screens");

		Option addPsrcatDB = new Option("d","add_psrcat_db",true, "Add a psrcat database to get known pulsars from. Currently only takes pulsars with positions in RA/DEC and in correct hms/dms format");

		Option extendPng = new Option("e", "extend_png", false, "Scale png beyond actual size. "
				+ "This is only ever useful for large resolution monitors where you want to resize the PNG to a higher resolution than original."); 

		options.addOption(selectPrimaryScreen);
		options.addOption(selectSecondaryScreen);
		options.addOption(help);
		options.addOption(listScreens);
		options.addOption(addPsrcatDB);
		options.addOption(numCharts);
		options.addOption(extendPng);

		try{

			commandLine = parser.parse(options, args);

			if(hasOption(help)){
				help();
				System.exit(0);
			}

			if(hasOption(numCharts)){
				Integer value = Integer.parseInt(getValue(numCharts));
				if(value < minNumCharts || value > maxNumCharts) {
					System.err.println("Enter valid number of charts.");
					System.exit(0);
				}

				CandyJar.numCharts = value;
				
				System.err.println("Displaying " + value + " Candy Charts");
			}

			if(hasOption(listScreens)) {
				int n=0;
				for(Screen screen: Screen.getScreens()) {
					Rectangle2D rec = screen.getBounds();
					System.err.println("Screen #"+ ++n + ": " + rec.getWidth() + "x" + rec.getHeight());

				}
				System.exit(0);
			}


			if(hasOption(extendPng)) {

				CandyJar.extendPng = true;
				
				System.err.println("Okay, performing lossless PNG scaling even if pane size > png size");


			}

			if(hasOption(selectPrimaryScreen)) {


				String stringValue = getValue(selectPrimaryScreen);
				CandyJar.primaryScreenBounds = parseScreenInput(stringValue);
				
				System.err.println("Okay, Using primary screen bounds:" + CandyJar.primaryScreenBounds);


			}

			if(hasOption(selectSecondaryScreen)) {

				String stringValue = getValue(selectSecondaryScreen);
				CandyJar.secondaryScreenBounds = parseScreenInput(stringValue);
				
				System.err.println("Okay, Using secondary screen bounds:" + CandyJar.secondaryScreenBounds);
				

			}
			else {
				secondaryScreenBounds = null;
			}


			if(hasOption(addPsrcatDB)) {
				String value = getValue(addPsrcatDB);
				PsrcatConstants.psrcatDBs.add(value);
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
			help();
			System.exit(0);
		}


		//Application.launch(ScatterAndBubbleRendererSample.class);
		Application.launch(CandyJar.class);

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



	public static Rectangle2D parseScreenInput(String strValue) throws InvalidInputException{


		Integer screenNum = null;
		Integer width = null;
		Integer height = null;
		try {

			if(strValue.contains(":")) {


				String[] chunks = strValue.split(":");

				screenNum = Integer.parseInt(chunks[0]);

				String[] wh = chunks[1].split("x");

				width = Integer.parseInt(wh[0]);
				height = Integer.parseInt(wh[1]);

			}

			else {
				screenNum = Integer.parseInt(strValue);
			}



			if(screenNum < 0 || screenNum > Screen.getScreens().size()) {

				System.err.println("Enter valid screen number.");
				System.exit(0);

			}

			Screen screen = Screen.getScreens().get(screenNum-1);

			Rectangle2D screenBounds = screen.getBounds();

			Rectangle2D rectangle2d = null;

			if(width ==null) width = (int) screenBounds.getWidth();
			if(height ==null) height = (int) screenBounds.getHeight();


			rectangle2d = new Rectangle2D(screenBounds.getMinX(), screenBounds.getMinY(), width, height);

			System.err.println(rectangle2d + " " + screen.getBounds().getWidth() + " " + screen.getBounds().getHeight());

			return rectangle2d;


		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			throw new InvalidInputException("Invalid screen value format: " + strValue + ". check help for syntax.");
		}


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

			Bounds chartSceneBounds = beamMapChart.localToScene(beamMapChart.getBoundsInLocal());

			if (mouseEvent.getButton() == MouseButton.SECONDARY) {

				((NumberAxis) beamMapChart.getXAxis()).setLowerBound(initXLowerBound);
				((NumberAxis) beamMapChart.getXAxis()).setUpperBound(initXUpperBound);
				((NumberAxis) beamMapChart.getXAxis()).setTickUnit((initXUpperBound - initXLowerBound) / 10.0);

				((NumberAxis) beamMapChart.getYAxis()).setLowerBound(initYLowerBound);
				((NumberAxis) beamMapChart.getYAxis()).setUpperBound(initYUpperBound);
				((NumberAxis) beamMapChart.getYAxis()).setTickUnit((initXUpperBound - initXLowerBound) / 10.0);

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

						NumberAxis yAxis = (NumberAxis) beamMapChart.getYAxis();
						double Tgap = yAxis.getHeight() / (yAxis.getUpperBound() - yAxis.getLowerBound());
						double axisShift = getSceneShiftY(yAxis);
						double ptY = yAxis.getUpperBound() - ((Y - axisShift) / Tgap);

						NumberAxis xAxis = (NumberAxis) beamMapChart.getXAxis();

						Tgap = xAxis.getWidth() / (xAxis.getUpperBound() - xAxis.getLowerBound());
						axisShift = getSceneShiftX(xAxis);
						double ptX = ((X - axisShift) / Tgap) + xAxis.getLowerBound();

					} else {

						double Tgap = 0;
						double newLowerBound, newUpperBound, axisShift;
						double xScaleFactor, yScaleFactor;
						double xaxisShift, yaxisShift;
						// Zoom in Y-axis by changing bound range.
						NumberAxis yAxis = (NumberAxis) beamMapChart.getYAxis();
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

						NumberAxis xAxis = (NumberAxis) beamMapChart.getXAxis();

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


	public Button getFilterCandidates() {
		return filterCandidates;
	}


	public CheckComboBox<CANDIDATE_TYPE> getFilterTypes() {
		return filterTypes;
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
				} else if (item instanceof Node) {
					setText(null);
					setGraphic((Node) item);
				} else if   (item instanceof Number) {
					setText(item.toString());
					setGraphic(null);
					
				} else {
					setText(item.toString());
					setGraphic(null);
				}
			} else {
				setText(null);
				setGraphic(null);
			}
		}
	}





}