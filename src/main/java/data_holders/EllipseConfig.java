package data_holders;

public class EllipseConfig {
	private Angle beamX;
	private Angle beamY;
	private Angle beamAngle;
	
	private Angle beamXEq;
	private Angle beamYEq;
	
	
	
	
	public EllipseConfig(Double beamX, Double beamY, Angle beamAngle) {
		super();
		this.beamX = new Angle(beamX, Angle.DEG);
		this.beamY = new Angle(beamY, Angle.DEG);
		this.beamAngle = beamAngle;
	}
	
	public EllipseConfig(Double beamX, Double beamY, Angle beamAngle, Angle beamXEq, Angle beamYEq) {
		this(beamX, beamY, beamAngle);
		this.beamXEq = beamXEq;
		this.beamYEq = beamYEq;

	}

	@Override
	public String toString() {
		String s= "beamX " + beamX + " \n" 
		+ "beamY " + beamY + " \n"  
		+ "beamAngle " + beamAngle + " \n"  
		+ "beamXEq " + beamXEq + " \n"  
		+ "beamYEq " + beamYEq + " \n"; 
		return s;
	}
	public Angle getBeamAngle() {
		return beamAngle;
	}
	public void setBeamAngle(Angle beamAngle) {
		this.beamAngle = beamAngle;
	}

	public Angle getBeamX() {
		return beamX;
	}

	public void setBeamX(Angle beamX) {
		this.beamX = beamX;
	}

	public Angle getBeamY() {
		return beamY;
	}

	public void setBeamY(Angle beamY) {
		this.beamY = beamY;
	}

	public Angle getBeamXEq() {
		return beamXEq;
	}

	public void setBeamXEq(Angle beamXEq) {
		this.beamXEq = beamXEq;
	}

	public Angle getBeamYEq() {
		return beamYEq;
	}

	public void setBeamYEq(Angle beamYEq) {
		this.beamYEq = beamYEq;
	}
	
	

}
