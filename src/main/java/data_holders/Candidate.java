package data_holders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import constants.CandidateFileConstants;
import constants.Constants;
import data_holders.Candidate.CANDIDATE_TYPE;
import de.gsi.chart.marker.DefaultMarker;
import de.gsi.chart.marker.Marker;
import de.gsi.dataset.spi.utils.Tuple;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import utilitites.Utilities;



/**
 * @author vkrishnan
 *
 */
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
	private Map<String, Double> classifierScoresMap;
	// private Double picsScoreTrapum;
	// private Double picsScorePALFA;
	private String pngFilePath;
	private String metaFilePath;
	private String filterbankPath;
	private String tarballPath;
	private Double tobs;
	private boolean isPeriodAtStart;

	private CANDIDATE_TYPE candidateType;
	
	private String utcString;
	
	private Image image; 
	
	private List<Candidate> similarCandidatesInFreq; 

	public static String csvHeader;
	
	Beam beam;
	
	MetaFile metaFile;
	
	private String lineNum;
	
	private String csvLine;
	
	private boolean visible;

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Candidate) {
			Candidate c = (Candidate)obj;
			return this.pngFilePath.equals(c.pngFilePath);
		}
		return false;
	}
	
	public Double getOptP0() {
		return 1/optF0;
	}

	public void addClassifierScore(String classifier, Double score) {
		if(this.classifierScoresMap == null) this.classifierScoresMap = new LinkedHashMap<String, Double>();
		this.classifierScoresMap.put(classifier, score);
	}

	public Map<String, Double> getClassifierScoresMap() {
		return classifierScoresMap;
	}

	public void setClassifierScoresMap(Map<String, Double> classifierScoresMap) {
		this.classifierScoresMap = classifierScoresMap;
	}

	public double getClassifierScore(String classifier){
		return this.classifierScoresMap.get(classifier);
	}


	public String getLineNum() {
		return lineNum;
	}
	// just for sorting
	public Double getLineNumDouble() {
		return Integer.parseInt(lineNum) + 0.0;
	}


	public void setLineNum(String lineNum) {
		this.lineNum = lineNum;
	}

	public double getP0AtStart() {
		return 1/getF0AtStart();
	}
	
	public double getF0AtStart() {
		
		if(this.isPeriodAtStart) return this.optF0;

		return this.optF0 - this.optF1 * (this.peopoch - this.startMJD) * Constants.day2Secs;
		
	}

	

	// public boolean isSimilarTo(Candidate c2) {
		

	// 	double minF0 = this.getOptF0() - 1e-4;
	// 	double maxF0 = this.getOptF0() + 1e-4;
		


	// 	for(int i=1; i<=16; i++) {
	// 		for(int j=1;j<=16;j++) {
	// 			double harmonic = ((double)i)/j;
	// 			if(c2.getOptF0() >= harmonic * minF0 && c2.getOptF0() <= harmonic * maxF0 ) {
	// 				return true;
	// 			}
				
	// 		}
	// 	}
	// 	return false;
		
	// }
	
	
	public Candidate(){
		similarCandidatesInFreq = new ArrayList<Candidate>();
		this.candidateType = CANDIDATE_TYPE.UNCAT;
		
		this.visible = true;
		
		
		this.isPeriodAtStart = false;
		
		if(this.startMJD != null && this.peopoch !=null && Double.compare(this.peopoch, this.startMJD)<= 0) this.isPeriodAtStart = true;

		this.classifierScoresMap = new LinkedHashMap<String, Double>();
		
	}
	
	public String getF0DMString() {
		return String.format("%8.5f %4.2f", this.optF0, this.optDM);
	}
	
	public String getP0DMString() {
		return String.format("%8.5f %4.2f", this.getOptP0(), this.optDM);
	}
	
	public String getBeamP0DMString() {
		String name = "";
		if(this.filterbankPath.contains("dbs")){
			name = this.filterbankPath.split("dbs")[1].replace("_", " ").trim().replace(" ", "_");
		}
		else{
			this.beamName.replace("cfbf00", "");
		}
		return String.format("%s %s %8.4f %4.2f %.1f",this.lineNum, name, this.getOptP0()* 1000.0, this.optDM, this.foldSNR);
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

	// public Double getPicsScoreTrapum() {
	// 	return picsScoreTrapum;
	// }

	// public void setPicsScoreTrapum(Double picsScoreTrapum) {
	// 	this.picsScoreTrapum = picsScoreTrapum;
	// }

	// public Double getPicsScorePALFA() {
	// 	return picsScorePALFA;
	// }

	// public void setPicsScorePALFA(Double picsScorePALFA) {
	// 	this.picsScorePALFA = picsScorePALFA;
	// }
	

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

	
	

	public Beam getBeam() {
		return beam;
	}


	public void setBeam(Beam beam) {
		this.beam = beam;
	}


	public List<Candidate> getSimilarCandidatesInFreq() {
		return similarCandidatesInFreq;
	}


	public void setSimilarCandidatesInFreq(List<Candidate> similarCandidatesInFreq) {
		this.similarCandidatesInFreq = similarCandidatesInFreq;
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
	
	// public Tuple<Double, Double> getPicsPALFATuple(){
	// 	return new Tuple<Double, Double>(getPicsScorePALFA(),null);
	// }

	// public Tuple<Double, Double> getPicsTrapumTuple(){
	// 	return new Tuple<Double, Double>(getPicsScoreTrapum(),null);
	// }

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
	
	public MetaFile getMetaFile() {
		return metaFile;
	}

	
	public String getCsvLine() {
		return csvLine;
	}
	

	public void setCsvLine(String csvLine) {
		this.csvLine = csvLine;
	}

	public void setMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	public String getFilterbankPathGlobbed() {
		if (this.filterbankPath == null) return "";
		if (this.filterbankPath.lastIndexOf("/") == -1) return "*.fil";
		return this.filterbankPath.substring(0, filterbankPath.lastIndexOf("/")) + "/*.fil";
	}
	
	public double getAngleFromBoresight() {
		if(this.beam == null || this.metaFile == null) return -1;
		Beam boresight = metaFile.getBoresight();
		return Utilities.getAngularDistance(beam, boresight).getDegreeValue();
	}
	
	public Tuple<Double, Double> getAngleFromBoresightTuple() {
		return new Tuple<Double, Double>(getAngleFromBoresight(),null);
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
	

	public Double getTobs() {
		return tobs;
	}

	public void setTobs(Double tobs) {
		this.tobs = tobs;
	}

	public Tuple<Double, Double> getTobsTuple() {
		return new Tuple<Double, Double>(getTobs(),null);
	}

	public double getPicsPALFA(){
		return this.classifierScoresMap.getOrDefault(CandidateFileConstants.pics_palfa, 0.0);
	}
	public double getPicsTrapumTer5(){
		return this.classifierScoresMap.getOrDefault(CandidateFileConstants.pics_trapum_ter5, 0.0);
	}
	public double getPicsMLSRecall(){
		return this.classifierScoresMap.getOrDefault(CandidateFileConstants.pics_m_LS_recall, 0.0);
	}
	public double getPicsPMLSFscore(){
		return this.classifierScoresMap.getOrDefault(CandidateFileConstants.pics_pm_LS_fscore, 0.0);
	}

	public Tuple<Double, Double> getPicsPALFATuple(){
		return new Tuple<Double, Double>(getPicsPALFA(),null);
	}
	public Tuple<Double, Double> getPicsTrapumTer5Tuple(){
		return new Tuple<Double, Double>(getPicsTrapumTer5(),null);
	}
	public Tuple<Double, Double> getPicsMLSRecallTuple(){
		return new Tuple<Double, Double>(getPicsMLSRecall(),null);
	}
	public Tuple<Double, Double> getPicsPMLSFscoreTuple(){
		return new Tuple<Double, Double>(getPicsPMLSFscore(),null);
	}

	public static void initParamMapsWithClassifiers(List<String> classifiers) {

			for (String classifier : classifiers) {
				switch (classifier) {
					case CandidateFileConstants.pics_palfa:
						SORTABLE_PARAMETERS_MAP.put("PICS_PALFA", Candidate::getPicsPALFA);
						PLOTTABLE_PARAMETERS_MAP.put("PICS_PALFA", Candidate::getPicsPALFATuple);
						break;
					case CandidateFileConstants.pics_trapum_ter5:
						SORTABLE_PARAMETERS_MAP.put("PICS_TRAPUM", Candidate::getPicsTrapumTer5);
						PLOTTABLE_PARAMETERS_MAP.put("PICS_TRAPUM", Candidate::getPicsTrapumTer5Tuple);
						break;
					case CandidateFileConstants.pics_m_LS_recall:
						SORTABLE_PARAMETERS_MAP.put("PICS_M_LS_RECALL", Candidate::getPicsMLSRecall);
						PLOTTABLE_PARAMETERS_MAP.put("PICS_M_LS_RECALL", Candidate::getPicsMLSRecallTuple);
						break;
					case CandidateFileConstants.pics_pm_LS_fscore:
						SORTABLE_PARAMETERS_MAP.put("PICS_PM_LS_FSCORE", Candidate::getPicsPMLSFscore);
						PLOTTABLE_PARAMETERS_MAP.put("PICS_PM_LS_FSCORE", Candidate::getPicsPMLSFscoreTuple);
						break;
				}

			}
			
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
		sortableValuesMap.put("BEAM_NUM", Candidate::getBeamNumber);
		sortableValuesMap.put("BORESIGHT_ANG_DIST", Candidate::getAngleFromBoresight);
		sortableValuesMap.put("TOBS", Candidate::getTobs);
		sortableValuesMap.put("CSV_LINE", Candidate::getLineNumDouble);


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
		plottableValuesMap.put("TOBS", Candidate::getTobsTuple);
		plottableValuesMap.put("BEAM_NUM", Candidate::getBeamNumberTuple);
		plottableValuesMap.put("RA", Candidate::getRaTuple);
		plottableValuesMap.put("DEC", Candidate::getDecTuple);
		plottableValuesMap.put("BORESIGHT_ANG_DIST", Candidate::getAngleFromBoresightTuple);

		return plottableValuesMap;
	}
	
	
	private static Map<String, String> parameterUnits(){
		Map<String, String> unitsMap = new LinkedHashMap<String, String>();
		
		// if unit is not here, then it is assumed to be unitless. 
		unitsMap.put("DM", "pc/cc");
		unitsMap.put("F0", "Hz");
		unitsMap.put("F1", "Hz/Hz");
		unitsMap.put("ACC", "m/s^2");
		unitsMap.put("RA", "hours");
		unitsMap.put("DEC", "degrees");
		unitsMap.put("BORESIGHT_ANG_DIST", "degrees");
		unitsMap.put("TOBS", "seconds");
		
		
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
		UNCAT,
		NB_PSR;
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
		case NB_PSR:
			marker = DefaultMarker.PLUS;
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
		case NB_PSR:
			color = Color.DARKVIOLET.deriveColor(0, 1, 1, 0.2);
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
