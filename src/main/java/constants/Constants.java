package constants;

import java.util.Arrays;
import java.util.List;

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


	Double hrs2Sec = 3600.0;

	Double hrs2Deg = 15.0;
	Double hrs2Rad = hrs2Deg*deg2Rad;
	
	Double rad2Hrs = rad2Deg/hrs2Deg;
	
	String DEFAULT_BEAM_MAP = "default";
	String SELECTED_BEAM_MAP = "selected";
	String SHORTLISTED_BEAM_MAP = "shortlisted";
	String NEIGHBOUR_BEAM_MAP = "neighbour";
	String CANDIDATE_BEAM_MAP = "neighbour";

	String OVERVIEW_CSV = "overview.csv";
	
	public static final List<String> plotOrder = Arrays.asList(
			new String[] { SHORTLISTED_BEAM_MAP, NEIGHBOUR_BEAM_MAP, SELECTED_BEAM_MAP, DEFAULT_BEAM_MAP});
	
	
	String commonUTCFormat = "yyyy-MM-dd:kk:mm:ss";
	
	enum CANDIDATE_TYPE {
		KNOWN_PULSAR, 
		TIER1_CANDIDATE,
		TIER2_CANDIDATE,
		RFI, 
		NOISE,
		UNCATEGORIZED
	}


}
