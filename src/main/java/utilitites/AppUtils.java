package utilitites;

import java.io.File;
import java.io.FileFilter;
import java.time.LocalDateTime;

import data_holders.Angle;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;

public interface AppUtils {
	
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
