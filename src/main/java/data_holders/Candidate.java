package application;

import java.time.LocalDateTime;

import constants.Constants.CANDIDATE_TYPE;
import utilitites.Utilities;

public class Candidate {
	
	private String tarBallName;
	private String sourceName;	
	private Integer pointingID;
	private Integer beamID;
	private String beamName;
	private LocalDateTime utc;
	private String imageFileName;
	private CANDIDATE_TYPE candidateType;

	
	public Candidate(String line) {
		String[] chunks = line.strip().split(",");
		
		
		this.tarBallName = chunks[0];
		this.sourceName =  chunks[1];
		this.pointingID = Integer.parseInt(chunks[2]);
		this.beamID = Integer.parseInt(chunks[3]);
		this.beamName = chunks[4];
		this.utc = Utilities.getUTCLocalDateTime(chunks[5], "yyyy-MM-dd kk:mm:ss");
		this.imageFileName = chunks[6];
		this.candidateType = CANDIDATE_TYPE.UNCATEGORIZED;
		
		
	}
	
	@Override
	public String toString() {
		
		return imageFileName;
	}

	public String getTarBallName() {
		return tarBallName;
	}

	public void setTarBallName(String tarBallName) {
		this.tarBallName = tarBallName;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
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

	public LocalDateTime getUtc() {
		return utc;
	}

	public void setUtc(LocalDateTime utc) {
		this.utc = utc;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}

	public CANDIDATE_TYPE getCandidateType() {
		return candidateType;
	}

	public void setCandidateType(CANDIDATE_TYPE candidateType) {
		this.candidateType = candidateType;
	}
	
	


}
