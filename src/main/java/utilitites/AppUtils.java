package utilitites;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.time.LocalDateTime;

import data_holders.Angle;
import de.gsi.chart.marker.Marker;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;
import javafx.util.StringConverter;

public interface AppUtils {
	
	Marker ROUNDED_RECTANGLE = new Marker() {
		
		@Override
		public void draw(GraphicsContext gc, double x, double y, double size) {
	        gc.fillRoundRect(x - size, y - size, 2.0 * size, 2.0 * size, size,  size);
			
		}
	};
	
	Callback<ListView<LocalDateTime>, ListCell<LocalDateTime>> utcViewFactory = new Callback<ListView<LocalDateTime>,ListCell<LocalDateTime>>(){
	    @Override
	    public ListCell<LocalDateTime> call(ListView<LocalDateTime> l){
	    	
	        return new ListCell<LocalDateTime>(){
	        	
	            @Override
	            protected void updateItem(LocalDateTime utc,boolean empty) {
	            	
	                super.updateItem(utc, empty);
	                
	                if (utc == null || empty) {
	                	
	                    setGraphic(null);
	                    
	                } else {
	                	
	                    setText(Utilities.getUTCString(utc, "yyyy-MM-dd:kk:mm:ss"));
	                    
	                }
	            }
	        } ;
	    }
	
	};
	
	
	public static void copyToClipboardText(String s) {

		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();

		content.putString(s);
		clipboard.setContent(content);

	}

	public static void copyToClipboardImage(Label lbl) {

		WritableImage snapshot = lbl.snapshot(new SnapshotParameters(), null);
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();

		content.putImage(snapshot);
		clipboard.setContent(content);

	}

	public static void copyToClipboardImageFromFile(String path) {

		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();

		content.putImage(AppUtils.getImage(path));
		clipboard.setContent(content);

	}
	
	public static ImageView setIcon(String path) {

		InputStream is = AppUtils.class.getResourceAsStream(path);
		ImageView iv = new ImageView(new Image(is));

		iv.setFitWidth(100);
		iv.setFitHeight(100);
		return iv;
	}
	
	public static Image getImage(String path) {

		InputStream is = AppUtils.class.getResourceAsStream(path);
		return new Image(is);
	}
	
	
	Callback<ListView<String>, ListCell<String>> fileNameViewFactory = new Callback<ListView<String>,ListCell<String>>(){
	    @Override
	    public ListCell<String> call(ListView<String> l){
	    	
	        return new ListCell<String>(){
	        	
	            @Override
	            protected void updateItem(String string,boolean empty) {
	            	
	                super.updateItem(string, empty);
	                
	                if (string == null || empty) {
	                	
	                    setGraphic(null);
	                    
	                } else {
	                	
	                    setText(string);
	                    
	                }
	            }
	        } ;
	    }
	
	};
	
	
	FileFilter directoryFileFilter = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
		
		@Override
		public String toString(){
			return "directory filter";
		}
	};

	
	FileFilter pngFileFilter = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".png");
		}
		
		@Override
		public String toString(){
			return "PNG file filter";
		}
	};
	
	
	FileFilter birdieFileFilter = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().contains("birdies");
		}
		
		@Override
		public String toString(){
			return "Birdie file filter";
		}
	};
	
	
	
	StringConverter<Number> raStringConverter = new StringConverter<Number>() {

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
	};
	
	StringConverter<Number> decStringConverter = new StringConverter<Number>() {

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
	};

	
}
