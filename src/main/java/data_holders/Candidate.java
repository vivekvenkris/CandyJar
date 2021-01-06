package data_holders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import de.gsi.chart.marker.DefaultMarker;
import de.gsi.chart.marker.Marker;
import de.gsi.dataset.spi.utils.Tuple;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import utilitites.Utilities;



public class Candidate {
	
	private Integer pointingID;
	private Integer beamID;
	private String beamName;
	private String sourceName;	
	private Angle ra;
	private Angle dec;
	private Angle gl;
	private Angle gb;
	private Double startMJD;
	private LocalDateTime startUTC;
	private Double userF0;
	private Double optF0;
	private Double optF0Err;
	private Double userF1;
	private Double optF1;
	private Double optF1Err;
	private Double userAcc;
	private Double optAcc;
	private Double optAccErr;
	private Double userDM;
	private Double optDM;
	private Double optDMErr;
	private Double fftSNR;
	private Double foldSNR;
	private Double peopoch;
	private Double maxDMYMW16;
	private Double distYMW16;
	private Double picsScoreTrapum;
	private Double picsScorePALFA;
	private String pngFilePath;
	private String metaFilePath;
	private String filterbankPath;
	private String tarballPath;

	private CANDIDATE_TYPE candidateType;
	
	private String utcString;
	
	private Image image; 
	
	private List<Candidate> similarParamCandidates; 
	
	
	private boolean visible;
	

	
	
	public Candidate(){
		similarParamCandidates = new ArrayList<Candidate>();
	}
	
	public Candidate(Integer pointingID, Integer beamID, String beamName, String sourceName, Angle ra, Angle dec,
			Angle gl, Angle gb, Double startMJD, LocalDateTime startUTC, Double userF0, Double optF0, Double optF0Err,
			Double userF1, Double optF1, Double optF1Err, Double userAcc, Double optAcc, Double optAccErr,
			Double userDM, Double optDM, Double optDMErr, Double fftSNR, Double foldSNR, Double peopoch,
			Double maxDMYMW16, Double distYMW16, Double picsScoreTrapum, Double picsScorePALFA, String pngFilePath,
			String metaFilePath, String filterbankPath, String tarballPath,CANDIDATE_TYPE candidateType) {
		super();
		this.pointingID = pointingID;
		this.beamID = beamID;
		this.beamName = beamName;
		this.sourceName = sourceName;
		this.ra = ra;
		this.dec = dec;
		this.gl = gl;
		this.gb = gb;
		this.startMJD = startMJD;
		this.startUTC = startUTC;
		this.userF0 = userF0;
		this.optF0 = optF0;
		this.optF0Err = optF0Err;
		this.userF1 = userF1;
		this.optF1 = optF1;
		this.optF1Err = optF1Err;
		this.userAcc = userAcc;
		this.optAcc = optAcc;
		this.optAccErr = optAccErr;
		this.userDM = userDM;
		this.optDM = optDM;
		this.optDMErr = optDMErr;
		this.fftSNR = fftSNR;
		this.foldSNR = foldSNR;
		this.peopoch = peopoch;
		this.maxDMYMW16 = maxDMYMW16;
		this.distYMW16 = distYMW16;
		this.picsScoreTrapum = picsScoreTrapum;
		this.picsScorePALFA = picsScorePALFA;
		this.pngFilePath = pngFilePath;
		this.metaFilePath = metaFilePath;
		this.filterbankPath = filterbankPath;
		this.tarballPath = tarballPath;
		this.candidateType = candidateType;
		
		similarParamCandidates = new ArrayList<Candidate>();
		
	}

	public Candidate(String line) {
		String[] chunks = line.strip().split(",");
		
		int i=0;
		this.pointingID = Integer.parseInt(chunks[i++]);
		this.beamID = Integer.parseInt(chunks[i++]);
		this.beamName = chunks[i++];;
		this.sourceName = chunks[i++];;
		this.ra = new Angle(chunks[i++], Angle.HHMMSS);
		this.dec = new Angle(chunks[i++], Angle.DDMMSS);
		this.gl = new Angle(chunks[i++], Angle.DEG);
		this.gb = new Angle(chunks[i++], Angle.DEG);
		this.startMJD = Double.parseDouble(chunks[i++]);
		this.startUTC = Utilities.getUTCLocalDateTime(chunks[i++], DateTimeFormatter.ISO_DATE_TIME);
		this.userF0 = Double.parseDouble(chunks[i++]);
		this.optF0 = Double.parseDouble(chunks[i++]);
		this.optF0Err = Double.parseDouble(chunks[i++]);
		this.userF1 = Double.parseDouble(chunks[i++]);
		this.optF1 = Double.parseDouble(chunks[i++]);
		this.optF1Err = Double.parseDouble(chunks[i++]);
		this.userAcc = Double.parseDouble(chunks[i++]);
		this.optAcc = Double.parseDouble(chunks[i++]);
		this.optAccErr = Double.parseDouble(chunks[i++]);
		this.userDM = Double.parseDouble(chunks[i++]);
		this.optDM = Double.parseDouble(chunks[i++]);
		this.optDMErr = Double.parseDouble(chunks[i++]);
		this.fftSNR = Double.parseDouble(chunks[i++]);
		this.foldSNR = Double.parseDouble(chunks[i++]);
		this.peopoch = Double.parseDouble(chunks[i++]);
		this.maxDMYMW16 = Double.parseDouble(chunks[i++]);
		this.distYMW16 = Double.parseDouble(chunks[i++]);
		this.picsScoreTrapum = Double.parseDouble(chunks[i++]);
		this.picsScorePALFA = Double.parseDouble(chunks[i++]);
		this.pngFilePath = chunks[i++];
		this.metaFilePath = chunks[i++];
		this.filterbankPath = chunks[i++];
		this.tarballPath = chunks[i++];

		
		
		this.candidateType = CANDIDATE_TYPE.UNCAT;
		
		this.utcString = Utilities.getUTCString(startUTC, DateTimeFormatter.ISO_DATE_TIME);
		
		similarParamCandidates = new ArrayList<Candidate>();
		

		
	}
	
	@Override
	public String toString() {
		
		return pngFilePath;
	}

	public Integer getPointingID() {
		return pointingID;
	}

	public void setPointingID(Integer pointingID) {
		this.pointingID = pointingID;
	}

	public Integer getBeamID() {
		return beamID;
	}

	public void setBeamID(Integer beamID) {
		this.beamID = beamID;
	}

	public String getBeamName() {
		return beamName;
	}

	public void setBeamName(String beamName) {
		this.beamName = beamName;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public Angle getRa() {
		return ra;
	}

	public void setRa(Angle ra) {
		this.ra = ra;
	}

	public Angle getDec() {
		return dec;
	}

	public void setDec(Angle dec) {
		this.dec = dec;
	}

	public Angle getGl() {
		return gl;
	}

	public void setGl(Angle gl) {
		this.gl = gl;
	}

	public Angle getGb() {
		return gb;
	}

	public void setGb(Angle gb) {
		this.gb = gb;
	}

	public Double getStartMJD() {
		return startMJD;
	}

	public void setStartMJD(Double startMJD) {
		this.startMJD = startMJD;
	}

	public LocalDateTime getStartUTC() {
		return startUTC;
	}

	public void setStartUTC(LocalDateTime startUTC) {
		this.startUTC = startUTC;
	}

	public Double getUserF0() {
		return userF0;
	}

	public void setUserF0(Double userF0) {
		this.userF0 = userF0;
	}

	public Double getOptF0() {
		return optF0;
	}

	public void setOptF0(Double optF0) {
		this.optF0 = optF0;
	}

	public Double getOptF0Err() {
		return optF0Err;
	}

	public void setOptF0Err(Double optF0Err) {
		this.optF0Err = optF0Err;
	}

	public Double getUserF1() {
		return userF1;
	}

	public void setUserF1(Double userF1) {
		this.userF1 = userF1;
	}

	public Double getOptF1() {
		return optF1;
	}

	public void setOptF1(Double optF1) {
		this.optF1 = optF1;
	}

	public Double getOptF1Err() {
		return optF1Err;
	}

	public void setOptF1Err(Double optF1Err) {
		this.optF1Err = optF1Err;
	}

	public Double getUserAcc() {
		return userAcc;
	}

	public void setUserAcc(Double userAcc) {
		this.userAcc = userAcc;
	}

	public Double getOptAcc() {
		return optAcc;
	}

	public void setOptAcc(Double optAcc) {
		this.optAcc = optAcc;
	}

	public Double getOptAccErr() {
		return optAccErr;
	}

	public void setOptAccErr(Double optAccErr) {
		this.optAccErr = optAccErr;
	}

	public Double getUserDM() {
		return userDM;
	}

	public void setUserDM(Double userDM) {
		this.userDM = userDM;
	}

	public Double getOptDM() {
		return optDM;
	}

	public void setOptDM(Double optDM) {
		this.optDM = optDM;
	}

	public Double getOptDMErr() {
		return optDMErr;
	}

	public void setOptDMErr(Double optDMErr) {
		this.optDMErr = optDMErr;
	}

	public Double getFftSNR() {
		return fftSNR;
	}

	public void setFftSNR(Double fftSNR) {
		this.fftSNR = fftSNR;
	}

	public Double getFoldSNR() {
		return foldSNR;
	}

	public void setFoldSNR(Double foldSNR) {
		this.foldSNR = foldSNR;
	}

	public Double getPeopoch() {
		return peopoch;
	}

	public void setPeopoch(Double peopoch) {
		this.peopoch = peopoch;
	}

	public Double getMaxDMYMW16() {
		return maxDMYMW16;
	}

	public void setMaxDMYMW16(Double maxDMYMW16) {
		this.maxDMYMW16 = maxDMYMW16;
	}

	public Double getDistYMW16() {
		return distYMW16;
	}

	public void setDistYMW16(Double distYMW16) {
		this.distYMW16 = distYMW16;
	}

	public Double getPicsScoreTrapum() {
		return picsScoreTrapum;
	}

	public void setPicsScoreTrapum(Double picsScoreTrapum) {
		this.picsScoreTrapum = picsScoreTrapum;
	}

	public Double getPicsScorePALFA() {
		return picsScorePALFA;
	}

	public void setPicsScorePALFA(Double picsScorePALFA) {
		this.picsScorePALFA = picsScorePALFA;
	}
	

	public String getPngFilePath() {
		return pngFilePath;
	}

	public void setPngFilePath(String pngFilePath) {
		this.pngFilePath = pngFilePath;
	}

	public String getMetaFilePath() {
		return metaFilePath;
	}

	public void setMetaFilePath(String metaFilePath) {
		this.metaFilePath = metaFilePath;
	}

	public String getFilterbankPath() {
		return filterbankPath;
	}

	public void setFilterbankPath(String filterbankPath) {
		this.filterbankPath = filterbankPath;
	}

	public String getTarballPath() {
		return tarballPath;
	}

	public void setTarballPath(String tarballPath) {
		this.tarballPath = tarballPath;
	}

	public CANDIDATE_TYPE getCandidateType() {
		return candidateType;
	}

	public void setCandidateType(CANDIDATE_TYPE candidateType) {
		this.candidateType = candidateType;
	}

	public String getUtcString() {
		return utcString;
	}

	public void setUtcString(String utcString) {
		this.utcString = utcString;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public List<Candidate> getSimilarParamCandidates() {
		return similarParamCandidates;
	}

	public void setSimilarParamCandidates(List<Candidate> similarParamCandidates) {
		this.similarParamCandidates = similarParamCandidates;
	}



	public boolean isSimilarTo(Candidate c2) {
		
		if (Math.abs(this.getOptDM() - c2.getOptDM()) /this.getOptDMErr() < 1 &&
				Math.abs(this.getOptF0() - c2.getOptF0()) / this.getOptF0Err() < 1 &&
					Math.abs(this.getOptAcc() - c2.getOptAcc())/this.getOptAccErr() < 1) return true;
		else return false;
				
		
	}
	
	
	public Tuple<Double, Double> getOptDMTuple(){
		return new Tuple<Double, Double>(getOptDM(), getOptDMErr());
	}
	
	public Tuple<Double, Double> getOptF0Tuple(){
		return new Tuple<Double, Double>(getOptF0(), getOptF0Err());
	}
	
	
	public Tuple<Double, Double> getOptF1Tuple(){
		return new Tuple<Double, Double>(getOptF1(), getOptF1Err());
	}
	
	
	public Tuple<Double, Double> getOptAccTuple(){
		return new Tuple<Double, Double>(getOptAcc(), getOptAccErr());
	}
	
	public Tuple<Double, Double> getPicsPALFATuple(){
		return new Tuple<Double, Double>(getPicsScorePALFA(),null);
	}

	public Tuple<Double, Double> getPicsTrapumTuple(){
		return new Tuple<Double, Double>(getPicsScoreTrapum(),null);
	}

	public Tuple<Double, Double> getFoldSNRTuple(){
		return new Tuple<Double, Double>(getFoldSNR(),null);
	}
	
	public Tuple<Double, Double> getFftSNRTuple(){
		return new Tuple<Double, Double>(getFftSNR(),null);
	}
	
	
	public Tuple<Double, Double> getRaTuple(){
		return new Tuple<Double, Double>(getRa().getDecimalHourValue(),null);
	}
	
	public Tuple<Double, Double> getDecTuple(){
		return new Tuple<Double, Double>(getDec().getDegreeValue(),null);
	}
	
	
	public Tuple<Double, Double> getBeamNumberTuple(){
		return new Tuple<Double, Double>(Beam.getIntegerBeamName(getBeamName()).doubleValue(), null);
	}
	
	public Double getBeamNumber(){
		return Beam.getIntegerBeamName(getBeamName()).doubleValue();
	}
	

	
	public static Tuple<Double, Double> getMinMax(List<Candidate> candidates, Function<Candidate, Tuple<Double, Double>> valueFunction) {
		
		Double min = valueFunction.apply(
				candidates.stream()
						  .min((first, second) -> Double.compare(valueFunction.apply(first).getXValue(), valueFunction.apply(second).getXValue()))
						  .get()
						  ).getXValue();
		
		Double max = valueFunction.apply(
				candidates.stream()
						  .max((first, second) -> Double.compare(valueFunction.apply(first).getXValue(), valueFunction.apply(second).getXValue()))
						  .get()
						  ).getXValue();
		return new Tuple<Double, Double>(min, max);
		
	}

	
	
	private static Map<String, Function<Candidate, Double>>  sortableParameters() {
		Map<String, Function<Candidate, Double>> sortableValuesMap = 
				new LinkedHashMap<String, Function<Candidate, Double>>();
		
		sortableValuesMap.put("DM", Candidate::getOptDM);
		sortableValuesMap.put("F0", Candidate::getOptF0);
		sortableValuesMap.put("F1", Candidate::getOptF1);
		sortableValuesMap.put("ACC", Candidate::getOptAcc);
		sortableValuesMap.put("FOLD_SNR", Candidate::getFoldSNR);
		sortableValuesMap.put("FFT_SNR", Candidate::getFftSNR);
		sortableValuesMap.put("PICS_TRAPUM", Candidate::getPicsScoreTrapum);
		sortableValuesMap.put("PICS_PALFA", Candidate::getPicsScorePALFA);
		sortableValuesMap.put("BEAM_NUM", Candidate::getBeamNumber);
		return sortableValuesMap;
	}

	
	private static Map<String, Function<Candidate, Tuple<Double, Double>>>  plottableParameters() {
		Map<String, Function<Candidate, Tuple<Double, Double>>> plottableValuesMap = 
				new LinkedHashMap<String, Function<Candidate, Tuple<Double, Double>>>();
		
		
		plottableValuesMap.put("DM", Candidate::getOptDMTuple);
		plottableValuesMap.put("F0", Candidate::getOptF0Tuple);
		plottableValuesMap.put("F1", Candidate::getOptF1Tuple);
		plottableValuesMap.put("ACC", Candidate::getOptAccTuple);
		plottableValuesMap.put("FOLD_SNR", Candidate::getFoldSNRTuple);
		plottableValuesMap.put("FFT_SNR", Candidate::getFftSNRTuple);
		plottableValuesMap.put("PICS_TRAPUM", Candidate::getPicsTrapumTuple);
		plottableValuesMap.put("PICS_PALFA", Candidate::getPicsPALFATuple);
		plottableValuesMap.put("BEAM_NUM", Candidate::getBeamNumberTuple);
		plottableValuesMap.put("RA", Candidate::getRaTuple);
		plottableValuesMap.put("DEC", Candidate::getDecTuple);

		return plottableValuesMap;
	}
	
	
	private static Map<String, String> parameterUnits(){
		Map<String, String> unitsMap = new LinkedHashMap<String, String>();
		
		unitsMap.put("DM", "pc/cc");
		unitsMap.put("F0", "Hz");
		unitsMap.put("F1", "Hz/Hz");
		unitsMap.put("ACC", "m/s^2");
		unitsMap.put("FOLD_SNR", null);
		unitsMap.put("FFT_SNR", null);
		unitsMap.put("PICS_TRAPUM", null);
		unitsMap.put("PICS_PALFA", null);
		unitsMap.put("BEAM_NUM", null);
		unitsMap.put("RA", "hours");
		unitsMap.put("DEC", "degrees");
		
		return unitsMap;
		
	}
	

	public static final Map<String, String> PARAMETER_UNITS_MAP = parameterUnits();
	public static final Map<String, Function<Candidate, Double>>  SORTABLE_PARAMETERS_MAP = sortableParameters();
	public static final String DEFAULT_SORT_PARAMETER = "FOLD_SNR";

	public static final Map<String, Function<Candidate, Tuple<Double, Double>>>  PLOTTABLE_PARAMETERS_MAP = plottableParameters();
	
	
	public enum CANDIDATE_TYPE {
		KNOWN_PSR, 
		T1_CAND,
		T2_CAND,
		RFI, 
		NOISE,
		UNCAT;
	}
	
	
	public enum CANDIDATE_PLOT_CATEGORY{
		ALL,
		MARKED,
		CURRENTLY_VIEWING
	}


	/**
	 * Marked for removal due to bug in graphics context plotting unfilled shapes. 
	 * @return
	 */
	@Deprecated
	private Marker getMarkerByCandidateType() {
		Marker marker = null;
		
		switch (this.getCandidateType()) {
		case T1_CAND:
			marker = DefaultMarker.DIAMOND;
			break;
		case T2_CAND:
			marker = DefaultMarker.DIAMOND;
			break;
		case RFI:
			marker = DefaultMarker.CROSS;
			break;
		case KNOWN_PSR:
			marker = DefaultMarker.PLUS;
			break;
		case NOISE:
			marker = DefaultMarker.RECTANGLE2;
			break;
		case UNCAT:
			marker = DefaultMarker.CIRCLE;

		}
		return marker;
		
	}
	
	
	public static Color getColorByCandidateType(CANDIDATE_TYPE type) {
		Color color = null;
		
		switch (type) {
		case T1_CAND:
			color = Color.DEEPSKYBLUE.deriveColor(0, 1, 1, 0.9);
			break;
		case T2_CAND:
			color = Color.DEEPSKYBLUE.deriveColor(0, 1, 1, 0.3);
			break;
		case RFI:
			color =  Color.DARKORANGE.deriveColor(0, 1, 1, 0.5);
			break;
		case KNOWN_PSR:
			color = Color.DARKVIOLET.deriveColor(0, 1, 1, 0.5);
			break;
		case NOISE:
			color = Color.ROSYBROWN.deriveColor(0, 1, 1, 0.8);
			break;
		case UNCAT:
			color = Color.DARKGREY.deriveColor(0, 1, 1, 0.5);

		}
		return color;
		
	}
	
	
	
	private Color getColorByCandidateType() {
		return Candidate.getColorByCandidateType(this.getCandidateType());
		
	}
	public String getStyle(Integer size, Integer index) {
		return getStyle(null, null, size, index);
	}
	
	public String getStyle(Color color, Integer size, Integer index) {
		return getStyle(null, color, size, index);
	}
	
	public String getStyle(Marker marker, Integer size, Integer index) {
		return getStyle(marker, null, size, index);
	}
	
	public String getStyle(Marker marker, Color color, Integer size, Integer index) {
		
		String style = "";
		
		if(marker == null) marker = DefaultMarker.CIRCLE;
		if(color == null) color = getColorByCandidateType();
		
		style += "markerSize=" + size+ ";";
		style += "markerColor=" + color + ";";
		style+= "strokeColor="+ color + ";";
		style+= "fillColor="+ color + ";";
		style += "markerType=" + marker + ";";
		if (index != null) style += "index=" + index + ";";
		

		return style;
		
	}
	
	
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}


}
