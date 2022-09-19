package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import data_holders.Candidate;
import data_holders.Pulsar;
import exceptions.InvalidInputException;
import javafx.animation.Interpolator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import net.kurobako.gesturefx.GesturePane;

public class Helpers {

	private static List<Double> harmonics = new ArrayList<Double>();
	static {
		for(int i=1; i<=16; i++) {
			for(int j=1;j<=16;j++) {
				harmonics.add(((double)i)/j);
			}
		}
	}
	public static void findCandidateSimilarities(List<Candidate> candidates) {
		
		candidates.parallelStream().forEach(c1 -> {
			List<Candidate> simiarCands = candidates.parallelStream().map(c2 -> {
				if(c2.equals(c1)) return null;
				double minF0 = c1.getOptF0() - 1e-4;
				double maxF0 = c1.getOptF0() + 1e-4;
				for(Double harmonic: harmonics) {
					if(c2.getOptF0() >= harmonic * minF0 && c2.getOptF0() <= harmonic * maxF0 ) {
						return c2;
					}
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
			
			c1.getSimilarCandidatesInFreq().addAll(simiarCands);
			
		});
		
		System.err.println("Found and grouped related harmonics...");

	}
	

public static void populatePulsarTabs(TabPane tabPane, List<Pulsar> pulsarsInBeam) {
		

		for (Pulsar pulsar : pulsarsInBeam) {

			Tab tab = new Tab();
			tab.setText(pulsar.getName());

			final TableView<Pair<String, Object>> table = new TableView<>();
			table.getItems().add(new Pair<String, Object>("RA:", new CopyableLabel(pulsar.getRa().toHHMMSS())));
			table.getItems().add(new Pair<String, Object>("DEC:", new CopyableLabel(pulsar.getDec().toDDMMSS())));
			
			table.getItems().add(new Pair<String, Object>("Angular distance from Boresight:",
					new CopyableLabel(pulsar.getDistanceFromBoresight() + " deg = " + pulsar.getDistanceFromBoresight()*3600+"\"")));
			
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

			tabPane.getTabs().add(tab);

		}

		tabPane.setPrefWidth(200);

	}

	
	
	public static class PairKeyFactory
	implements Callback<TableColumn.CellDataFeatures<Pair<String, Object>, String>, ObservableValue<String>> {
		@Override
		public ObservableValue<String> call(TableColumn.CellDataFeatures<Pair<String, Object>, String> data) {
			return new ReadOnlyObjectWrapper<>(data.getValue().getKey());
		}
	}

	public static class PairValueFactory
	implements Callback<TableColumn.CellDataFeatures<Pair<String, Object>, Object>, ObservableValue<Object>> {
		@SuppressWarnings("unchecked")
		@Override
		public ObservableValue<Object> call(TableColumn.CellDataFeatures<Pair<String, Object>, Object> data) {
			Object value = data.getValue().getValue();
			return (value instanceof ObservableValue) ? (ObservableValue) value : new ReadOnlyObjectWrapper<>(value);
		}
	}
	

	public static class PairValueCell extends TableCell<Pair<String, Object>, Object> {
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


	public static double getSceneShiftX(Node node) {
		double shift = 0;
		do {
			shift += node.getLayoutX();
			node = node.getParent();
		} while (node != null);
		return shift;
	}

	public static double getSceneShiftY(Node node) {
		double shift = 0;
		do {
			shift += node.getLayoutY();
			node = node.getParent();
		} while (node != null);
		return shift;
	}

	
	public static GesturePane addGesture(Node n) {
		GesturePane pane = new GesturePane(n);
		
		pane.setOnMouseClicked(e -> {
			Point2D pivotOnTarget = pane.targetPointAt(new Point2D(e.getX(), e.getY()))
                    .orElse(pane.targetPointAtViewportCentre());
			if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
				
				// increment of scale makes more sense exponentially instead of linearly 
				pane.animate(Duration.millis(200))
						.interpolateWith(Interpolator.EASE_BOTH)
						.zoomBy(pane.getCurrentScale(), pivotOnTarget);
			}
			if (e.getButton() == MouseButton.SECONDARY) {
				pane.zoomTo(1,pivotOnTarget);
				
			}
		});
		return pane;
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



	




}
