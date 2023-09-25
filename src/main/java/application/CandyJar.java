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
import org.controlsfx.glyphfont.Glyph;
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
import javafx.animation.Interpolator;
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
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
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
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import net.kurobako.gesturefx.GesturePane;
import readers.CandidateFileReader;
import readers.Psrcat;
import utilitites.AppUtils;
import utilitites.GetCloseBeams;
import utilitites.Utilities;

public class CandyJar extends Application implements Constants {

//	private static Screen primaryScreen = Screen.getPrimary();
//	private static Screen secondaryScreen = null;

	public static Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
	public static Rectangle2D secondaryScreenBounds = null;
	
	public static double knownPulsarRadius = 1.0;
	public static double pngAspectRatio = 1.0;
	public static double maxPngOccupancy = 0.5;
	public static String crossMatchRationale = "32:0.00001";

	public static Integer numCharts = 2;
	public static Integer minNumCharts = 0;
	public static Integer maxNumCharts = 3;

	public static Boolean extendPng = false;
	public static final String FONT_AWESOME = "FontAwesome";
	public static final int FONT_SIZE = 20;

	Psrcat psrcat = new Psrcat();

	/* main pane*/
	BorderPane mainBorderPane = new BorderPane();

	/*Top Left: Load directory and CSV */
	File baseDir = null;
	TextAndButton rootDirTB = new TextAndButton(null,"Results directory","Get", 10);


	Button fileSelectButton = new Button("...");
	final Button loadClassification = new Button("Load classification");

	Button nextUTC = new Button(">>");
	Button prevUTC = new Button("<<");


	/* Top left: Filter and sort candidates */
	final ComboBox<String> utcBox = new ComboBox<String>();
	final ComboBox<String> sortBox = new ComboBox<String>();
	
	 
     // Creating new Toggle buttons.
     ToggleButton ascendingButton = new ToggleButton("A");
     ToggleButton descendingButton = new ToggleButton("D");
	final SegmentedButton sortOrder = new SegmentedButton(ascendingButton, descendingButton);
	
	
	final CheckComboBox<CANDIDATE_TYPE> filterTypes = new CheckComboBox<CANDIDATE_TYPE>(FXCollections.observableArrayList(Arrays.asList(CANDIDATE_TYPE.values())));
	final List<CANDIDATE_TYPE> filteredTypes = new ArrayList<CANDIDATE_TYPE>();
	final Button filterCandidates = new Button("Go");


	final HBox candidateFilterHBox = new HBox(10, filterTypes, sortBox, sortOrder, filterCandidates);
	final VBox controlBox = new VBox(10, new HBox(10, rootDirTB.getTextField(),fileSelectButton,rootDirTB.getButton(), loadClassification),	
									new HBox(10, prevUTC, utcBox, nextUTC, candidateFilterHBox));


	/* Left Middle: Beam Map and tabbed pane */
	final NumberAxis beamMapXAxis = new NumberAxis();
	final NumberAxis beamMapYAxis = new NumberAxis();
	final MyScatterChart beamMapChart = new MyScatterChart(beamMapXAxis, beamMapYAxis);
	TabPane beamMapPane = new TabPane();
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
	final ToggleButton knownPulsar = new ToggleButton("Known PSR (p)");
	final ToggleButton nbPulsar = new ToggleButton("NB PSR (l)");
	final ToggleButton reset = new ToggleButton("Uncat (r)");

	final SegmentedButton candidateCategories = 
			new SegmentedButton(FXCollections.observableArrayList(
					Arrays.asList(new ToggleButton[] {rfi, noise, tier1, tier2, knownPulsar, nbPulsar, reset})));
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
	private static CandyJar candyJar;
	private Stage myStage;


	public void initialise() {
		
		beamMapChart.init();

		this.rootDirTB.getButton().setTooltip(new Tooltip("Load results directory"));
	
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
		prevUTC.setVisible(false);
		nextUTC.setVisible(false);
		utcBox.setVisible(false);
		beamMapChart.setVisible(false);
		beamMapPane.setVisible(false);
		candidateFilterHBox.setVisible(false);
		sortBox.setVisible(false);
		actionsBox.setVisible(false);
		sortOrder.setVisible(false);
		infoPane.getTabs().clear();
		imageView.setVisible(false);
		gotoCandidate.getTextField().setPromptText("Go to Candidate");


		message.setTextFill(Paint.valueOf("darkred"));
		

	}

	public Stage getStage() {
		return myStage;
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.myStage = stage;

		configureLayout();
		initialise();
		if (CandyJar.candyJar == null) CandyJar.candyJar = this;

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
			rootDirTB.getTextField().setEditable(false);
			
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
				
				psrcat.readPersonalPulsarList(baseDir+ File.separator + "pulsars.list");

				String csv = baseDir.getAbsolutePath() + File.separator + Constants.CSV_FILE_NAME;



				imageCounter = 0;
				utcBox.setVisible(true);
				prevUTC.setVisible(true);
				nextUTC.setVisible(true);
				prevUTC.setDisable(true);
				nextUTC.setDisable(true);
				loadClassification.setVisible(true);

				try {
					fullCandiatesList.addAll(CandidateFileReader.readCandidateFile(csv, baseDir));


					System.err.println("Read " + fullCandiatesList.size() + " candidates from " + csv);



					utcs.addAll(fullCandiatesList.stream().map(f -> f.getStartUTC()).collect(Collectors.toSet()));

					utcBox.setItems(FXCollections.observableArrayList(utcs.stream()
							.map(f -> Utilities.getUTCString(f, commonUTCFormat)).collect(Collectors.toList())));

					for(LocalDateTime utc: utcs) { 
						System.err.println("utc: "+ utc);
						String utcString = Utilities.getUTCString(utc, DateTimeFormatter.ISO_DATE_TIME);
						List<Candidate> candidatesPerUtc = fullCandiatesList.stream().filter(f -> f.getStartUTC().equals(utc)).collect(Collectors.toList());
						candidateMap.put(utcString, candidatesPerUtc);
						Helpers.findCandidateSimilarities(candidatesPerUtc);

					}
					sortBox.getItems().clear();
					System.err.println("Sortable parameters: " + Candidate.SORTABLE_PARAMETERS_MAP.keySet());
					sortBox.getItems().addAll(FXCollections.observableArrayList(Candidate.SORTABLE_PARAMETERS_MAP.keySet()));
					message.setText(utcs.size() + " utcs found");
					if(chartViewer != null && chartViewer.isShowing()) chartViewer.close();

					chartViewer = secondaryScreenBounds!=null? new ChartViewer(secondaryScreenBounds, numCharts, candyJar): null;
					


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
				beamMapPane.setVisible(true);
				imageView.setVisible(false);
				actionsBox.setVisible(false);
				sortBox.setVisible(true);
				sortOrder.setVisible(true);
				candidatesVisible = false;

				prevUTC.setDisable(false);
				nextUTC.setDisable(false);
				prevUTC.setTooltip(new Tooltip("Go to previous UTC"));
				nextUTC.setTooltip(new Tooltip("Go to next UTC"));


				
				System.err.println("UTC:" + utcString);

				LocalDateTime utc = Utilities.getUTCLocalDateTime(utcString, Constants.commonUTCFormat);

				/* stupid stub because you need atleast one candidate to get the meta file */
				shortlistCandidates(utc, Arrays.asList(CANDIDATE_TYPE.values()));

				metaFile = candidates.get(imageCounter).getMetaFile();

				metaFile.findNeighbours();
				
				if(metaFile.getPng() != null) {
					if(beamMapPane.getTabs().size() > 1) beamMapPane.getTabs().remove(beamMapPane.getTabs().size()-1);
					BufferedImage beamImage;
					try {
						beamImage = ImageIO.read(metaFile.getPng());


//					BufferedImage scaledImage = Scalr.resize(beamImage, Method.ULTRA_QUALITY, Mode.FIT_TO_HEIGHT, 
//							(int)Math.round(3*beamMapChart.getWidth()),
//							(int)Math.round(3*beamMapChart.getHeight()));
					
				    GesturePane gesturePane = Helpers.addGesture(new ImageView(SwingFXUtils.toFXImage(beamImage, null)));
				    gesturePane.setMaxWidth(beamMapChart.getWidth());
				    gesturePane.setMaxHeight(beamMapChart.getHeight());				    
					beamMapPane.getTabs().add(new Tab("Beam map", gesturePane));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				pulsarsInBeam.clear();
				pulsarsInBeam.addAll(psrcat.getPulsarsInBeam(metaFile.getBoresight().getRa(),
						metaFile.getBoresight().getDec(), new Angle(knownPulsarRadius, Angle.DEG, Angle.DEG)));
				infoPane.getTabs().clear();
				candidateTab.setClosable(false);
				diagnosticTab.setClosable(false);
				updateTab(candidateTab, null);
				Helpers.populatePulsarTabs(infoPane, pulsarsInBeam);
				infoPane.getTabs().add(0, candidateTab);
				infoPane.getTabs().add(1, diagnosticTab);
				infoPane.getSelectionModel().select(diagnosticTab);

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

				beamMapChart.addDefaultMap(metaFile, pulsarsInBeam);

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

		prevUTC.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event){
				int currentUTCidx = utcBox.getItems().indexOf(utcBox.getValue());
				if (currentUTCidx == 0) {
					message.setText("This is the first UTC.");
					return;
				}
				String prevUTCString = utcBox.getItems().get(currentUTCidx - 1);
				utcBox.setValue(prevUTCString);
				filterCandidates.fire();
			}
		});

		nextUTC.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event){
				int currentUTCidx = utcBox.getItems().indexOf(utcBox.getValue());
				if (currentUTCidx == utcBox.getItems().size() - 1) {
					message.setText("This is the last UTC.");
					return;
				}
				String nextUTCString = utcBox.getItems().get(currentUTCidx + 1);
				utcBox.setValue(nextUTCString);
				filterCandidates.fire();
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
		nbPulsar.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(! candidatesVisible) return;
				candidates.get(imageCounter).setCandidateType(CANDIDATE_TYPE.NB_PSR);
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
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.initOwner(stage);
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
			table.getItems().add(new Pair<String, Object>("Position:", new CopyableLabel(candidate.getRa() + " " + candidate.getDec())));
			table.getItems().add(new Pair<String, Object>("Pointing ID:", new CopyableLabel(candidate.getPointingID()))); 
			table.getItems().add(new Pair<String, Object>("Beam ID:", new CopyableLabel(candidate.getBeamID())));
			table.getItems().add(new Pair<String, Object>("Beam Name:", new CopyableLabel(candidate.getBeamName())));
			table.getItems().add(new Pair<String, Object>("Neighbour beams:", new CopyableLabel(neighbours.toString())));
			// iterate over all classifier scores
			for (Entry<String, Double> classifier : candidate.getClassifierScoresMap().entrySet()) {
				table.getItems().add(new Pair<String, Object>(classifier.getKey() + " score:", new CopyableLabel(classifier.getValue() + "")));
			}
			table.getItems().add(new Pair<String, Object>("FFT SNR:", new CopyableLabel(candidate.getFftSNR())));
			table.getItems().add(new Pair<String, Object>("Fold SNR: ", new CopyableLabel(candidate.getFoldSNR())));

			table.getItems().add(new Pair<String, Object>("Boresight:", new CopyableLabel(candidate.getSourceName())));
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

		nameColumn.setCellValueFactory(new Helpers.PairKeyFactory());
		valueColumn.setCellValueFactory(new Helpers.PairValueFactory());

		table.getColumns().setAll(nameColumn, valueColumn);

		valueColumn.setCellFactory(
				new Callback<TableColumn<Pair<String, Object>, Object>, TableCell<Pair<String, Object>, Object>>() {
					@Override
					public TableCell<Pair<String, Object>, Object> call(
							TableColumn<Pair<String, Object>, Object> column) {
						return new Helpers.PairValueCell();
					}
				});

		tab.setContent(table);

	}

	public void progress() {
		candidateCategories.requestLayout();
		next.fire();
		
	}
	

	public void updateDiagnosticTab(TabPane tabPane, Candidate candidate) {

		final TableView<Pair<String, Object>> table = new TableView<>();
		// add diagnostics of known pulsars
		if(pulsarsInBeam != null && !pulsarsInBeam.isEmpty()) {
			
			pulsarsInBeam.sort(Comparator.comparing(f -> Math.abs(((Pulsar)f).getDm() - candidate.getOptDM())));
						
			pulsarsInBeam.stream().forEach(pulsar -> {
				table.getItems().add(new Pair<String, Object>(pulsar.getName() , KnownPulsarGuesser.guessPulsar(candidate, pulsar)));
			});
		}
		
		List<Candidate> uncatSimilarCandidates = candidate.getSimilarCandidatesInFreq().stream()
				.filter(c-> c.getCandidateType().equals(CANDIDATE_TYPE.UNCAT)).collect(Collectors.toList());
		
		if(candidate.getSimilarCandidatesInFreq().isEmpty()) {
			table.getItems().add(new Pair<String, Object>("Likely related uncategorized candidates:" , "None"));
		}
		else {
			VBox similarCandidateVBox = new VBox();
			
			TreeItem<Hyperlink> rootItem = new TreeItem<Hyperlink> (new Hyperlink(uncatSimilarCandidates.size() +
					" Likely related uncategorized candidates"));
			for(Candidate c: candidate.getSimilarCandidatesInFreq()) {
				String fileStr = c.getPngFilePath();
				Hyperlink link = new Hyperlink(fileStr);
				String similarCandInfo = c.getBeamP0DMString();
				String ratios = String.format("%.5f %.5f %s",Math.abs(c.getOptF0() / candidate.getOptF0()), 
						Math.abs(candidate.getOptF0() / c.getOptF0()), c.getCandidateType());

				link.setText(similarCandInfo + " " + ratios);
				link.setOnAction(e ->
					getHostServices().showDocument(new File(baseDir.getAbsolutePath() + File.separator + fileStr).toURI().toString()));
				TreeItem<Hyperlink> item = new TreeItem<Hyperlink>(link);
				rootItem.getChildren().add(item);
			}
			TreeView<Hyperlink> tree = new TreeView<Hyperlink>(rootItem);
			rootItem.setExpanded(false);
			similarCandidateVBox.getChildren().add(tree);
			Button button  = new Button("BULK CLASSIFY", new Glyph(FONT_AWESOME, "\uf02c").size(FONT_SIZE));
			similarCandidateVBox.getChildren().add(0, button);
			button.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					event.consume();
					ToggleGroup radioGroup = new ToggleGroup();
					CheckBox onlyThisBeam = new CheckBox("Only this beam");
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.initModality(Modality.APPLICATION_MODAL);
					alert.initOwner(CandyJar.candyJar.getStage());
					Node graphic = alert.getDialogPane().getGraphic();
					alert.setDialogPane(new DialogPane() {
						@Override
						protected Node createDetailsButton() {
							String s = "Classify all likely candidates as: ";
							HBox buttonHBox = new HBox();
							VBox vBox = new VBox(10,new Label(s),buttonHBox,onlyThisBeam);
							
							for(CANDIDATE_TYPE t : CANDIDATE_TYPE.values()) {
								ToggleButton tb = new ToggleButton(t.toString());
								tb.setToggleGroup(radioGroup);
								tb.setUserData(t);
								buttonHBox.getChildren().add(tb);
								
							}
							

							return vBox;
						}
						
					});
										
					
						
					alert.setTitle("Classify as");
					alert.getDialogPane().setExpandableContent(new Group());
				    alert.getDialogPane().setExpanded(true);
					alert.getDialogPane().setGraphic(graphic);
					alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
					Optional<ButtonType> result = alert.showAndWait();
					
					if(result != null && result.get() == ButtonType.OK) {
						
						Toggle toggle = radioGroup.getSelectedToggle();
						
						if(toggle != null && toggle.isSelected()) {
							
							CANDIDATE_TYPE type = (CANDIDATE_TYPE) toggle.getUserData();
							uncatSimilarCandidates.forEach(f -> {
								if( !onlyThisBeam.isSelected() || currentCandidate.getBeamName().equals(f.getBeamName())) {
									f.setCandidateType(type);
								}	
							});
							candidate.setCandidateType(type);
							CandyJar.candyJar.getFilterCandidates().fire();
							
						}
						
						
					}
					
					
				}
			});	
			
			

			table.getItems().add(new Pair<String, Object>("Likely related uncategorized candidates:" , similarCandidateVBox));

			
		}
		

		boolean addAcc = false;
		if(Math.abs(candidate.getOptAcc() / candidate.getOptAccErr()) > 2 ) addAcc = true;

		Button prepfoldButton = new Button("prepfold");
		Button pulsarxButton = new Button("pulsarx");
		Button dspsrButton = new Button("dspsr");
		
		String outputStr = candidate.getSourceName()+"_"+ candidate.getBeamName() +"_" + candidate.getLineNum();
		String filtoolTextGen="filtool -t 12 -i 0 --telescope meerkat -z zdot --cont -o " + outputStr + " -f " + candidate.getFilterbankPathGlobbed() + ";";

		String cleanedFil= outputStr + "*.fil";

		String prepfoldTextGen = filtoolTextGen + "prepfold" +  " -topo -fixchi -dm " + candidate.getOptDM() + " -nsub 64 -npart 64 -f " + candidate.getF0AtStart();
		if(addAcc) prepfoldTextGen += " -fd " + candidate.getOptF1();
		prepfoldTextGen +=	" -o " + outputStr + " " + cleanedFil;			 

						
		String pulsarxTextGen = "psrfold_fil -v -t 4 --template /home/psr/software/PulsarX/include/template/meerkat_fold.template"
				+ "-L 10 --clfd 2.0 -z zdot -n 64 -b 64 --dspsr --plotx --dm " + candidate.getOptDM() + " --f0 " + + candidate.getOptF0();		
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
		
		
		table.getItems().add(0, new Pair<String, Object>("Copy folding commands", new HBox(prepfoldButton, pulsarxButton, dspsrButton)));


		TableColumn<Pair<String, Object>, String> nameColumn = new TableColumn<>("Item");
		TableColumn<Pair<String, Object>, Object> valueColumn = new TableColumn<>("Description");
		valueColumn.setSortable(false);

		nameColumn.setCellValueFactory(new Helpers.PairKeyFactory());
		valueColumn.setCellValueFactory(new Helpers.PairValueFactory());

		table.getColumns().setAll(nameColumn, valueColumn);

		valueColumn.setCellFactory(
				new Callback<TableColumn<Pair<String, Object>, Object>, TableCell<Pair<String, Object>, Object>>() {
					@Override
					public TableCell<Pair<String, Object>, Object> call(
							TableColumn<Pair<String, Object>, Object> column) {
						return new Helpers.PairValueCell();
					}
				});

		diagnosticTab.setContent(table);


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
			case L:
				nbPulsar.fire();
				break;
			case R:
				reset.fire();
				break;

			case SPACE:
				if(event.isControlDown()) {
					
					if(metaFile != null && metaFile.getPng()!= null) {
						getHostServices().showDocument(metaFile.getPng().toURI().toString());
					}
				}
				else {
				if(!candidatesVisible) return;
				getHostServices().showDocument(new File( baseDir.getAbsolutePath() + File.separator + ((Candidate)imageView.getUserData()).getPngFilePath()).toURI().toString());
				}
			
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
		nbPulsar.setSelected(false);
		noise.setSelected(false);
		reset.setSelected(true);

		organiseImages(count);


		Candidate candidate = candidates.get(count);
		this.currentCandidate = candidate;


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
		case NB_PSR:
			nbPulsar.setSelected(true);
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
		double leftWidth = 0;
		pngPaneHeight = (int) (height - 20 - insets.getTop() -insets.getBottom());

		if (pngAspectRatio == 1) {
			int widthBy2 = (int)(width - 20 - insets.getLeft() -insets.getRight())/2;// 20 pixels for clarity 
			System.err.println("initial bounds height: " + height + " width:" + width);

			pngPaneWidth = widthBy2;

			System.err.println("initial png height: " + pngPaneHeight + " width: " + pngPaneWidth);


			pngPaneWidth = pngPaneHeight > pngPaneWidth? pngPaneWidth : pngPaneHeight; //choose the smaller of the two
			pngPaneHeight = pngPaneWidth;

		}
		else {
			
			if (1 - pngAspectRatio * pngPaneHeight/width >= maxPngOccupancy) {
				pngPaneWidth = (int)(pngAspectRatio * pngPaneHeight) - 20;
			}
			else {
				pngPaneWidth = (int) (maxPngOccupancy * width)- 20; 
				pngPaneHeight = (int) ((maxPngOccupancy * width) / pngAspectRatio);
			}
			

		}
		leftWidth = width - pngPaneWidth - 20 - insets.getLeft() -insets.getRight();
		System.err.println("final png height: " + pngPaneHeight + " width: " + pngPaneWidth);
	

		BorderPane leftPane = new BorderPane();

		double leftTopHeight = 80;

		VBox leftTop = new VBox(10, controlBox);
		leftTop.setPrefSize(leftWidth, leftTopHeight);
		leftTop.setAlignment(Pos.CENTER);
		leftPane.setTop(leftTop);


		double leftBottomHeight = 30;


		VBox leftBottom = new VBox(10, message);
		leftBottom.setPrefSize(leftWidth, leftBottomHeight);
		leftPane.setBottom(leftBottom);

		double leftCentreHeight = pngPaneHeight - leftBottomHeight - leftTopHeight;

		beamMapChart.setMinHeight(0.5*leftCentreHeight);
		
		beamMapPane.getTabs().add(new Tab("Beam map", beamMapChart));
		
		VBox centerLeft = new VBox(10, beamMapPane, infoPane, actionsBox);
		centerLeft.setPrefSize(leftWidth, leftCentreHeight);
		VBox.setVgrow(centerLeft, Priority.ALWAYS);
		leftPane.setCenter(centerLeft);
		

		mainBorderPane.setLeft(leftPane);

		imageViewHBox.getChildren().add(Helpers.addGesture(imageView));
		imageViewHBox.setPrefSize(pngPaneWidth, pngPaneHeight);

		VBox rightPane = new VBox(10, imageViewHBox);
		rightPane.setPrefSize(pngPaneWidth, pngPaneHeight);
		rightPane.setAlignment(Pos.BASELINE_CENTER);
		VBox.setVgrow(rightPane, Priority.NEVER);

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
			list.add(candidate.getBeamID() + Constants.CSV_SEPARATOR  + candidate.getUtcString() + Constants.CSV_SEPARATOR + candidate.getPngFilePath()
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
					
					if(chunks.length == 3) { // old format without beam id
						candidate.setCandidateType(CANDIDATE_TYPE.valueOf(chunks[2]));

					}
					else if(chunks.length == 4) { // new format with beam id
						candidate.setCandidateType(CANDIDATE_TYPE.valueOf(chunks[3]));

					}
					else {
						message.setText("Loaded classification contains invalid format, see console for more details");
						System.err.println("Invalid format in line: " + lines + "");
					}

				}



			}

		} catch (IOException e) {
			message.setText(e.getMessage());
			e.printStackTrace();
		}



	}

	
	
	
	/* Command line parsing */

	static CommandLineParser parser = new DefaultParser();
	static Options options = new Options();
	static CommandLine commandLine;
	private static final Logger LOGGER = LoggerFactory.getLogger(Chart.class);




	public static void main(String[] args) throws IOException {

		LOGGER.atDebug().addArgument("test");

		System.err.println("*************************Candy Jar V2.6*******************************");

		if(System.getenv("PSRCAT_DIR") != null) {
			PsrcatConstants.psrcatDBs.add(System.getenv("PSRCAT_DIR") + File.separator + "psrcat.db");

		}


		Locale.setDefault(Locale.US);

		Option selectPrimaryScreen  = new Option("s1","primary_screen", true, "Choose primary screen to open the application in. "
				+ "The application opens full screen by default. You can provide an optional custom size if you like, of the format: <screen—num>:widthxheight. Eg: 1:1920x1080 will open the application on"
				+ "your first screen, with the size of 1920x1080");
		Option selectSecondaryScreen  = new Option("s2","secondary_screen", true, "Choose secondary screen to open the application in. "
				+ "You can provide custom resolution like for screen 1");
		Option numCharts = new Option("n","num_charts", true, "Number of charts needed on the secondary screen (Min:"+ minNumCharts+", max:"+ maxNumCharts+")");
		Option help = new Option("h","help",false, "show this help message");
		Option listScreens = new Option("l","list_screens",false, "List available screens and exit.");

		Option addPsrcatDB = new Option("d","add_psrcat_db",true, "Add a psrcat database to get known pulsars from. Currently only takes pulsars with positions in RA/DEC and in correct hms/dms format");

		Option extendPng = new Option("e", "extend_png", false, "Scale png beyond actual size. "
				+ "This is only ever useful for large resolution monitors where you want to resize the PNG to a higher resolution than original.");
		
		Option knownPulsarRadius = new Option("r", "radius", true, "Add tabs for known pulsars within this radius from boresight"
				+ " Default:  " + CandyJar.knownPulsarRadius);
		
		Option crossMatchRationale = new Option("x", "crossmatch_rationale", true, "How to cross_match candidates? Valid entries: NONE for no cross match at all, [num]:threshold"
				+ " Default:  " + CandyJar.crossMatchRationale);
		
		Option aspectRatio = new Option("a", "aspect_ratio", true, "aspect ratio of the PNGs. Valid entries: PULSARX, PRESTO, <float value>"
				+ " Default: " + CandyJar.pngAspectRatio);
		
		Option maxPngOccupancy = new Option("p", "png_occupancy", true, "fraction of application width"
				+ "allocated for PNG. Should be between 0.25 and 0.75."
				+ " Default: " + CandyJar.maxPngOccupancy);
		Option compareMeta = new Option( "get_close_beams", true, "Compare meta files and provide close beams in metafile2 for each beam in metafile1");
		//compareMeta.setArgs(2);
		//compareMeta.setValueSeparator(',');

		options.addOption(selectPrimaryScreen);
		options.addOption(selectSecondaryScreen);
		options.addOption(help);
		options.addOption(listScreens);
		options.addOption(addPsrcatDB);
		options.addOption(numCharts);
		options.addOption(extendPng);
		options.addOption(knownPulsarRadius);
		options.addOption(aspectRatio);
		options.addOption(maxPngOccupancy);
		options.addOption(compareMeta);

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
				CandyJar.primaryScreenBounds = Helpers.parseScreenInput(stringValue);
				
				System.err.println("Okay, Using primary screen bounds:" + CandyJar.primaryScreenBounds);


			}

			if(hasOption(selectSecondaryScreen)) {

				String stringValue = getValue(selectSecondaryScreen);
				CandyJar.secondaryScreenBounds = Helpers.parseScreenInput(stringValue);
				
				System.err.println("Okay, Using secondary screen bounds:" + CandyJar.secondaryScreenBounds);
				

			}
			else {
				secondaryScreenBounds = null;
			}


			if(hasOption(addPsrcatDB)) {
				String value = getValue(addPsrcatDB);
				PsrcatConstants.psrcatDBs.add(value);
			}
			
			if(hasOption(knownPulsarRadius)) {
				String value = getValue(knownPulsarRadius);
				CandyJar.knownPulsarRadius = Double.parseDouble(value);
			}			
			if(hasOption(maxPngOccupancy)) {
				String value = getValue(maxPngOccupancy);
				Double dVal = Double.parseDouble(value);
				if (dVal < 0.25  || dVal > 0.75) {
					System.err.println("Max PNG occupancy should be between 0.25 to 0.75");
					help();
					System.exit(0);
				}
				CandyJar.maxPngOccupancy = Double.parseDouble(value);
			}
			if(hasOption(aspectRatio)) {
				String value = getValue(aspectRatio);
				if (value.equals("PULSARX")){
					CandyJar.pngAspectRatio = 1.0;
				}
				else if (value.equals("PRESTO")){
					CandyJar.pngAspectRatio = 1.41;
				}
				else {
				CandyJar.pngAspectRatio = Double.parseDouble(value);
				}
				System.err.println("Using aspect ratio: " + CandyJar.pngAspectRatio);
			}
			
			if(hasOption(compareMeta)) {
				String[] values = getValue(compareMeta).split(",");
				System.err.println(values);
				GetCloseBeams.getCloseBeams(values[0], values[1]);
				System.exit(0);
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
						double axisShift = Helpers.getSceneShiftY(yAxis);
						double ptY = yAxis.getUpperBound() - ((Y - axisShift) / Tgap);

						NumberAxis xAxis = (NumberAxis) beamMapChart.getXAxis();

						Tgap = xAxis.getWidth() / (xAxis.getUpperBound() - xAxis.getLowerBound());
						axisShift = Helpers.getSceneShiftX(xAxis);
						double ptX = ((X - axisShift) / Tgap) + xAxis.getLowerBound();

					} else {

						double Tgap = 0;
						double newLowerBound, newUpperBound, axisShift;
						double xScaleFactor, yScaleFactor;
						double xaxisShift, yaxisShift;
						// Zoom in Y-axis by changing bound range.
						NumberAxis yAxis = (NumberAxis) beamMapChart.getYAxis();
						Tgap = yAxis.getHeight() / (yAxis.getUpperBound() - yAxis.getLowerBound());
						axisShift = Helpers.getSceneShiftY(yAxis);
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
						axisShift = Helpers.getSceneShiftX(xAxis);
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

	


	public Button getFilterCandidates() {
		return filterCandidates;
	}


	public CheckComboBox<CANDIDATE_TYPE> getFilterTypes() {
		return filterTypes;
	}


	

}