package constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data_holders.MetaFile;
import javafx.geometry.Insets;

public interface Constants {
	


	 
	String rfiColor = "#ff0000";
	String newColor = "#00ff00";
	String pulsarColor = "#0000ff";
	
	Double deg2Rad = Math.PI/180.0;
	Double rad2Deg = 180.0/Math.PI;
	Double toRadians = deg2Rad;
	Double toDegrees = rad2Deg;
	Double arcSec2Deg = 1/(60.0*60.0);
	 
	Double sec2Hrs = 1/(60.0*60.0);
	Double hrs2Day = 1/(24.0);
	Double day2Hrs = (24.0);
	Double day2Secs = (86400.0);


	Double hrs2Sec = 3600.0;

	Double hrs2Deg = 15.0;
	Double hrs2Rad = hrs2Deg*deg2Rad;
	
	Double rad2Hrs = rad2Deg/hrs2Deg;
	
	String DEFAULT_BEAM_MAP = "default";
	String SELECTED_BEAM_MAP = "selected";
	String SHORTLISTED_BEAM_MAP = "shortlisted";
	String NEIGHBOUR_BEAM_MAP = "neighbour";
	String CANDIDATE_BEAM_MAP = "candidate";
	String KNOWN_PULSAR_BEAM_MAP = "knownpsr";

	String CSV_FILE_NAME = "candidates.csv";
	String CSV_SEPARATOR = ",";
	
	public static final List<String> plotOrder = Arrays.asList(
			new String[] { CANDIDATE_BEAM_MAP, SHORTLISTED_BEAM_MAP, NEIGHBOUR_BEAM_MAP, SELECTED_BEAM_MAP, DEFAULT_BEAM_MAP, KNOWN_PULSAR_BEAM_MAP});
	
	
	String commonUTCFormat = "yyyy-MM-dd:kk:mm:ss";
	

	Integer DEFAULT_IMAGE_WIDTH=1247;
	Integer DEFAULT_IMAGE_HEIGHT=1277;

	Insets DEFAULT_INSETS = new Insets(5, 20, 5, 20);
	
	Integer MAX_GUESSING_HARMONICS=16;
	
	


}
