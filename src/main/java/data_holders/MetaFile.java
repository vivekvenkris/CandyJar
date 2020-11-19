package data_holders;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import utilitites.Utilities;

public class MetaFile {
	
	private String fileName;
	private Double bandwidth;
	private Map<String,Beam> beams;
	private EllipseConfig ellipseConfig;
	private Beam boresight;
	private String outputDir;
	private String scheduleBlockId;
	private LocalDateTime utc;
	private String projectName;
	private Double tsampCoherent;
	private Integer numChansCoherent;
	private Double tsampIncoherent;
	private Integer numChansIncoherent;
	private Double centreFreq;
	private Angle minRa;
	private Angle maxRa;
	private Angle minDec;
	private Angle maxDec;
	
	
	public MetaFile() {
		beams = new HashMap<String, Beam>();
	}
	
	@Override
	public String toString() {
		String s = "";
		s += "fileName " + fileName + " \n"  
				+ "bandwidth " + bandwidth + " \n" 
				+ ellipseConfig
				+ "boresight " + boresight + " \n"  
				+ "outputDir " + outputDir + " \n"  
				+ "scheduleBlockId " + scheduleBlockId + " \n"  
				+ "utc " + utc + " \n"  
				+ "projectName " + projectName + " \n"  
				+ "tsampCoherent " + tsampCoherent + " \n"  
				+ "numChansCoherent " + numChansCoherent + " \n"  
				+ "tsampIncoherent " + tsampIncoherent+ " \n"  
				+ "numChansIncoherent " + numChansIncoherent + " \n"  
				+ "centreFreq " + centreFreq + " \n"  
				+ " " + beams.entrySet()+ " \n" ;

				
		return s;
	}
	
	public void findNeighbours() {
		
		for(Beam b1: beams.values()) {
			
			//System.err.println("Checking beam" + b1.getName());
			
			for(Beam b2: beams.values()) {
				
				
				
				Angle distance = Utilities.getAngularDistance(b1, b2);
				
				
				Angle theta = new Angle(b2.getEllipseConfig().getBeamAngle().getDegreeValue() - 90 + Utilities.getPositionAngle(b1, b2).getDegreeValue(), 
						Angle.DEG);
				
				Double r = Utilities.distanceFromEllipseCenter(b1.getEllipseConfig().getBeamX().getRadianValue(),
						b1.getEllipseConfig().getBeamY().getRadianValue(), theta.getRadianValue());
				
//				System.err.println(b2.getName() + " " + distance.getDegreeValue() + " " 
//				+ theta.getDegreeValue() + " " + r +  " " + distance.getRadianValue() / r  + " " + Utilities.isWithinEllipse(
//						b1.getRa().getRadianValue(), b1.getDec().getRadianValue(), b2.getRa().getRadianValue(), b2.getDec().getRadianValue(),
//						b1.getEllipseConfig().getBeamX().getRadianValue()*2, b1.getEllipseConfig().getBeamY().getRadianValue()*2));
			
				if (distance.getRadianValue() / r <= 2) {
					//System.err.println(b2.getName() + " is a neighbour of" + b1.getName());
					if (!b1.equals(b2))		b1.addNeighbour(b2);
				}
					
				
				
			}
			
			//System.err.println();
			
				
			
		}
		
	}
	
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Double getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(Double bandwidth) {
		this.bandwidth = bandwidth;
	}
	public Map<String, Beam> getBeams() {
		return beams;
	}
	public void setBeams(Map<String, Beam> beams) {
		this.beams = beams;
	}
	
	public EllipseConfig getEllipseConfig() {
		return ellipseConfig;
	}

	public void setEllipseConfig(EllipseConfig ellipseConfig) {
		this.ellipseConfig = ellipseConfig;
	}

	public Beam getBoresight() {
		return boresight;
	}
	public void setBoresight(Beam boresight) {
		this.boresight = boresight;
	}
	public String getOutputDir() {
		return outputDir;
	}
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public String getScheduleBlockId() {
		return scheduleBlockId;
	}
	public void setScheduleBlockId(String scheduleBlockId) {
		this.scheduleBlockId = scheduleBlockId;
	}
	public LocalDateTime getUtc() {
		return utc;
	}
	public void setUtc(LocalDateTime utc) {
		this.utc = utc;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Double getTsampCoherent() {
		return tsampCoherent;
	}
	public void setTsampCoherent(Double tsampCoherent) {
		this.tsampCoherent = tsampCoherent;
	}
	public Integer getNumChansCoherent() {
		return numChansCoherent;
	}
	public void setNumChansCoherent(Integer numChansCoherent) {
		this.numChansCoherent = numChansCoherent;
	}
	public Double getTsampIncoherent() {
		return tsampIncoherent;
	}
	public void setTsampIncoherent(Double tsampIncoherent) {
		this.tsampIncoherent = tsampIncoherent;
	}
	public Integer getNumChansIncoherent() {
		return numChansIncoherent;
	}
	public void setNumChansIncoherent(Integer numChansIncoherent) {
		this.numChansIncoherent = numChansIncoherent;
	}
	public Double getCentreFreq() {
		return centreFreq;
	}
	public void setCentreFreq(Double centreFreq) {
		this.centreFreq = centreFreq;
	}

	public Angle getMinRa() {
		return minRa;
	}

	public void setMinRa(Angle minRa) {
		this.minRa = minRa;
	}

	public Angle getMaxRa() {
		return maxRa;
	}

	public void setMaxRa(Angle maxRa) {
		this.maxRa = maxRa;
	}

	public Angle getMinDec() {
		return minDec;
	}

	public void setMinDec(Angle minDec) {
		this.minDec = minDec;
	}

	public Angle getMaxDec() {
		return maxDec;
	}

	public void setMaxDec(Angle maxDec) {
		this.maxDec = maxDec;
	}
	
	
	
	

}
