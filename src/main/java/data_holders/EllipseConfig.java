package data_holders;

public class EllipseConfig {
	private Angle beamX;
	private Angle beamY;
	private Angle beamAngle;
	
	
	
	
	public EllipseConfig(Double beamX, Double beamY, Angle beamAngle) {
		super();
		this.beamX = new Angle(beamX, Angle.DEG);
		this.beamY = new Angle(beamY, Angle.DEG);
		this.beamAngle = beamAngle;
	}

	@Override
	public String toString() {
		String s= "beamX " + beamX + " \n" 
		+ "beamY " + beamY + " \n"  
		+ "beamAngle " + beamAngle + " \n";  
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
	
	

}
