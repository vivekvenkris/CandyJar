package data_holders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Beam {
	private String name;
	private String boresightName;
	private String coordinates;
	private Angle ra;
	private Angle dec;
	private Angle raPixel;
	private Angle decPixel;
	private EllipseConfig ellipseConfig;
	List<Beam> neighbourBeams;
	private Integer integerBeamName;
	
	
	public static Integer getIntegerBeamName(String beamName) {
		if(beamName.contains("ifbf")) return -1;
		else {
			try {
				return Integer.parseInt(beamName.replaceAll("\\D+", ""));
			}catch (Exception e) {
				return 0;
			}
		}
	}
	
	public Beam() {
		
		this.neighbourBeams = new ArrayList<Beam>();
		
	}
	
	public Beam(String csv) {
		this();
		String[] chunks = csv.split(",");
		this.name = chunks[0];
		this.boresightName = chunks[0];
		this.coordinates = chunks[1];
		this.ra = new Angle(chunks[2].strip(), Angle.HHMMSS);
		this.dec = new Angle(chunks[3].strip(), Angle.DDMMSS);
		this.integerBeamName = Beam.getIntegerBeamName(this.name);
				
	}
	
	public Beam(String name, String csv) {
		this();
		String[] chunks = csv.split(",");
		this.name = name;
		this.boresightName = chunks[0];
		this.coordinates = chunks[1];
		this.ra = new Angle(chunks[2].strip(), Angle.HHMMSS);
		this.dec = new Angle(chunks[3].strip(), Angle.DDMMSS);
		this.integerBeamName = Beam.getIntegerBeamName(this.name);
		
	}
	public Beam(String name, String csv, EllipseConfig ellipseConfig) {
		this(name, csv);
		this.ellipseConfig = ellipseConfig;
		
	}

	public Beam(String name, Angle ra, Angle dec, Angle radius) {
		this();
		this.name = name;
		this.boresightName = name;
		this.coordinates = ra.toHHMMSS() + " " + dec.toDDMMSS();
		this.ra = ra;
		this.dec = dec;
		this.integerBeamName = Beam.getIntegerBeamName(this.name);
		this.ellipseConfig = new EllipseConfig(radius, radius);
	}
	

	
	public String getCoordString() {
		return ra.toHHMMSS() + " " + dec.toDDMMSS(); 
	}
	
	@Override
	public String toString() {
		String s = "";
		s += " name: " + name + " boresight Name: " + boresightName +  " coordinates: " + coordinates +  " ra:" + ra + " dec: " + dec+ "\n"; 
		return s;
	}
	
	public List<Beam> getNeighbourBeams() {
		return neighbourBeams;
	}
	public void setNeighbourBeams(List<Beam> neighbourBeams) {
		this.neighbourBeams = neighbourBeams;
	}
	
	public void addNeighbour(Beam b) {
		this.neighbourBeams.add(b);
	}
	public void addNeighbours(Collection<Beam> bs) {
		this.neighbourBeams.addAll(bs);
	}
	
	public boolean isNeighbour(Beam b) {
		return this.neighbourBeams.contains(b);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		return (obj instanceof Beam) && ((Beam)obj).getName().equals(this.name);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBoresightName() {
		return boresightName;
	}
	public void setBoresightName(String boresightName) {
		this.boresightName = boresightName;
	}
	public String getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
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
	public EllipseConfig getEllipseConfig() {
		return ellipseConfig;
	}
	public void setEllipseConfig(EllipseConfig ellipseConfig) {
		this.ellipseConfig = ellipseConfig;
	}

	public Integer getIntegerBeamName() {
		return integerBeamName;
	}

	public void setIntegerBeamName(Integer integerBeamName) {
		this.integerBeamName = integerBeamName;
	}

	public Angle getRaPixel() {
		return raPixel;
	}

	public void setRaPixel(Angle raPixel) {
		this.raPixel = raPixel;
	}

	public Angle getDecPixel() {
		return decPixel;
	}

	public void setDecPixel(Angle decPixel) {
		this.decPixel = decPixel;
	}

	

}
