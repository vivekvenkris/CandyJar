package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collector;
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

	public static <T> Collector<T, ?, T> toSingleton() {
    return Collectors.collectingAndThen(
            Collectors.toList(),
            list -> {
                if (list.size() != 1) {
                    throw new IllegalStateException();
                }
                return list.get(0);
            }
    );
}
	// Unique harmonic ratios for i-> 0 to 16; j-> 0 to 16; and harmonic = i/j
	private static double[] harmonicRatios = new double[] {0.0625,  0.0667,  0.0714,  0.0769,  0.0833,  0.0909,  0.1 ,
        0.1111,  0.125 ,  0.1333,  0.1429,  0.1538,  0.1667,  0.1818,
        0.1875,  0.2   ,  0.2143,  0.2222,  0.2308,  0.25  ,  0.2667,
        0.2727,  0.2857,  0.3   ,  0.3077,  0.3125,  0.3333,  0.3571,
        0.3636,  0.375 ,  0.3846,  0.4   ,  0.4167,  0.4286,  0.4375,
        0.4444,  0.4545,  0.4615,  0.4667,  0.5   ,  0.5333,  0.5385,
        0.5455,  0.5556,  0.5625,  0.5714,  0.5833,  0.6   ,  0.6154,
        0.625 ,  0.6364,  0.6429,  0.6667,  0.6875,  0.6923,  0.7   ,
        0.7143,  0.7273,  0.7333,  0.75  ,  0.7692,  0.7778,  0.7857,
        0.8   ,  0.8125,  0.8182,  0.8333,  0.8462,  0.8571,  0.8667,
        0.875 ,  0.8889,  0.9   ,  0.9091,  0.9167,  0.9231,  0.9286,
        0.9333,  0.9375,  1.    ,  1.0667,  1.0714,  1.0769,  1.0833,
        1.0909,  1.1   ,  1.1111,  1.125 ,  1.1429,  1.1538,  1.1667,
        1.1818,  1.2   ,  1.2222,  1.2308,  1.25  ,  1.2727,  1.2857,
        1.3   ,  1.3333,  1.3636,  1.375 ,  1.4   ,  1.4286,  1.4444,
        1.4545,  1.5   ,  1.5556,  1.5714,  1.6   ,  1.625 ,  1.6667,
        1.7143,  1.75  ,  1.7778,  1.8   ,  1.8333,  1.8571,  1.875 ,
        2.    ,  2.1429,  2.1667,  2.2   ,  2.25  ,  2.2857,  2.3333,
        2.4   ,  2.5   ,  2.6   ,  2.6667,  2.75  ,  2.8   ,  3.    ,
        3.2   ,  3.25  ,  3.3333,  3.5   ,  3.6667,  3.75  ,  4.    ,
        4.3333,  4.5   ,  4.6667,  5.    ,  5.3333,  5.5   ,  6.    ,
        6.5   ,  7.    ,  7.5   ,  8.    ,  9.    , 10.    , 11.    ,
       12.    , 13.    , 14.    , 15.    , 16. };



	

	public static void getRelatedCandidates(List<Candidate> candidates){

		candidates.stream().forEach(c1 -> {

			if(candidates.indexOf(c1) == candidates.size()-1) return;

			List<Candidate> simiarCands = candidates.stream().skip(candidates.indexOf(c1)+1).limit(candidates.size()-candidates.indexOf(c1)-1).map(c2 -> {
				double f0 = c1.getOptF0();
				double minF0 = f0 - 1e-4;
				double maxF0 = f0 + 1e-4;
				for(double harmonic: harmonicRatios) {
					if(c2.getOptF0() >= harmonic * minF0 && c2.getOptF0() <= harmonic * maxF0 ) {
						return c2;
					}
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());

		simiarCands.stream().forEach(f-> f.getSimilarCandidatesInFreq().add(c1));	
		c1.getSimilarCandidatesInFreq().addAll(simiarCands);
			
		});
		
		System.err.println("Found and grouped related harmonics...");

	}

	public static boolean areTheyRelated(Candidate c1, Candidate c2, double fTol, boolean scaleTol, double dmTol){
			double f0 = c1.getOptF0();

			boolean closeInPeriod = false;

			double matchingHarmonic = 0;

			for(double harmonic: harmonicRatios) {
				double tol = scaleTol ? fTol * harmonic : fTol;
				if(Math.abs(c2.getOptF0() / (harmonic * f0) - 1) < tol) {
					closeInPeriod = true;
					matchingHarmonic = harmonic;
					break;
				}

			}
			if(!closeInPeriod) return false;

			// TODO: Identify how to compare DMs for candidates matching in harmonics. 

			if(Math.abs(c1.getOptDM() - c2.getOptDM()) > dmTol) return false;

			return true;
	}

	

	public static void findCandidateSimilarities(List<Candidate> candidates, double fTol, boolean scaleTol, double dmTol){
		 Map<Candidate, List<Candidate>> candidateRelationMap = new HashMap<Candidate, List<Candidate>>();

		 for(Candidate current: candidates){
		
			List<Candidate> relatedToCurrent = candidateRelationMap.keySet().stream().filter(c -> areTheyRelated(c, current, fTol, scaleTol, dmTol)).collect(Collectors.toList());

			if(relatedToCurrent.isEmpty()) {
				candidateRelationMap.put(current, new ArrayList<Candidate>());
			}

			else{
				Candidate primaryRelated = relatedToCurrent.get(0);
				List<Candidate> remainingRelated = relatedToCurrent.subList(1, relatedToCurrent.size());
				candidateRelationMap.get(primaryRelated).add(current);
				if (relatedToCurrent.size() > 1) {
					//System.err.println("Merging " + firstRelated.getOptF0() + " with " + remainingRelated.stream().map(c -> c.getOptF0()).collect(Collectors.toList()));
					remainingRelated.stream().forEach(related -> {
						candidateRelationMap.get(primaryRelated).add(related); // add related to firstRelated
						candidateRelationMap.get(primaryRelated).addAll(candidateRelationMap.get(related)); // add related of related to firstRelated
						candidateRelationMap.remove(related);
					});
				}
				
			}

		 }
		System.err.println("Total memory after mapping similarities: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024 + " MB");

		System.err.println("Clusters: " + candidateRelationMap.size());
		//sort candidateRelatoon Map by size and print
		// candidateRelationMap.entrySet().stream().sorted((e1, e2) -> e2.getValue().size() - e1.getValue().size())
		// 			.forEach(e -> {
		// 				System.err.println(e.getKey().getOptF0() + " " + e.getValue().size());
		// 				e.getValue().stream().forEach(c -> System.err.println("\t" + c.getOptF0() + " " + c.getLineNum()));
		// 			});
		


		
		
		System.err.println("---------------------------------------------------");

		 // now we have a map of candidates and their related candidates
		 candidateRelationMap.entrySet().stream().forEach(e -> { // for each candidate in map
			Candidate key = e.getKey();
			List<Candidate> value = e.getValue(); 
			key.getSimilarCandidatesInFreq().addAll(value);  // add related candidate to current candidate as similar

			value.stream().forEach( c -> {
				c.getSimilarCandidatesInFreq().addAll(value); // add all related candidates of current candidate to related candidate
				c.getSimilarCandidatesInFreq().remove(c); // remote itself
				c.getSimilarCandidatesInFreq().add(key); // add current candidate to related candidate as similar
			});


		 });

	}


	public static void findCandidateSimilarities2(List<Candidate> candidates) {

		
		candidates.stream().forEach(c1 -> {

			if(candidates.indexOf(c1) == candidates.size()-1) return;

			List<Candidate> simiarCands = candidates.parallelStream().skip(candidates.indexOf(c1)+1).limit(candidates.size()-candidates.indexOf(c1)-1).map(c2 -> {
				double f0 = c1.getOptF0();
				double minF0 = f0 - 1e-4;
				double maxF0 = f0 + 1e-4;
				for(double harmonic: harmonicRatios) {
					if(c2.getOptF0() >= harmonic * minF0 && c2.getOptF0() <= harmonic * maxF0 ) {
						return c2;
					}
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());

		simiarCands.stream().forEach(f-> f.getSimilarCandidatesInFreq().add(c1));	
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
