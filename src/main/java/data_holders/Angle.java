package data_holders;

import java.util.Locale;

import constants.Constants;

public class Angle implements Comparable<Angle>{
	
	public static final String HHMMSS = "hhmmss";
	public static final String DDMMSS = "ddmmss";
	public static final String RAD = "rad";
	public static final String DEG = "deg";
	private static final Double SECONDS_IN_A_SIDEREAL_DAY = 23*3600+ 56*60+4.1;
	private static final Double SECONDS_IN_A_SOLAR_DAY = 24*3600 + 0.0;
	Double radValue; // always in radians
	String toStringUnits;
	
	Integer sign = 0;
	
	public Angle clone() {
		return new Angle(this.radValue,Angle.RAD, this.toStringUnits);
	}
	
	public Angle(Double value, String format){
		this(value + "", format);
		
	}
	public Angle( String toStringUnits){
		this.toStringUnits = toStringUnits;
	}
	
	public Angle addSolarSeconds(int seconds){
		this.radValue = (this.getDecimalHourValue() + (seconds * SECONDS_IN_A_SOLAR_DAY / SECONDS_IN_A_SIDEREAL_DAY )*Constants.sec2Hrs) * Constants.hrs2Rad;
		return this;
	}

	public Angle addDegrees(Double degrees){
		this.radValue += degrees*Constants.deg2Rad;
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Angle)) return false;
		Angle angle = (Angle)obj;
		return this.radValue.equals(angle.getRadianValue());
	}
	
	public Angle(String value, String format){
		this(value, format, format);
		
	}
	
	public Angle(Double value, String format, String toStringUnits){
		this(value+"", format, toStringUnits);
	}
	
	public Angle(String value, String format, String toStringUnits){
		this.toStringUnits = toStringUnits;
		switch(format){
		case HHMMSS:
			fromHHMMSS(value);
			break;
		case DDMMSS:
			fromDDMMSS(value);
			break;
		case DEG:
			radValue = Double.parseDouble(value)*Constants.deg2Rad;
			break;
		case RAD:
			radValue = Double.parseDouble(value);
			break;
		}
		
	}
	
	
	
	public Double getDegreeValue(){
		return radValue*Constants.rad2Deg;
	}
	
	public Double getRadianValue(){
		return radValue;
	}
	
	public Double getDecimalHourValue(){
		return getDegreeValue()/15.0;
	}
	
	public void fromHHMMSS(String hhmmss){
		radValue = 0.0;
		if(hhmmss.equals("0.0")) return; // fix for tcc giving bad values int he beginning

		String[] hms = hhmmss.split(":");
		if(hms.length >=1) radValue +=  Integer.parseInt(hms[0])*15*Constants.deg2Rad;
		if(hms.length >=2) radValue +=  Integer.parseInt(hms[1])*15*Constants.deg2Rad/60.0;
		if(hms.length >=3) radValue +=  Double.parseDouble(hms[2])*15*Constants.deg2Rad/3600.0;
	}
	
	
	public void fromDDMMSS(String ddmmss){
		
		radValue = 0.0;
		if(ddmmss.equals("0.0")) return; // fix for tcc giving bad values in the beginning

		
		String[] dms = ddmmss.split(":");
		sign = (Integer.parseInt(dms[0])>0)? 1:-1;
		if(Integer.parseInt(dms[0]) == 0) sign =  (dms[0].contains("-"))? -1:1; 
		if(dms.length >=1) radValue+= Integer.parseInt(dms[0])*Constants.deg2Rad; 
		if(dms.length >=2) radValue+= sign*Integer.parseInt(dms[1])*Constants.deg2Rad/60.0;		
		if(dms.length >=3) radValue+= sign*Double.parseDouble(dms[2])*Constants.deg2Rad/3600.0;
	}
	public String toHHMMSS(){
		double DegVal = Math.abs(getDegreeValue());
		int hours = (int)(DegVal/15.0);
		int minutes = (int)((DegVal - hours*15.0)*60/15.0);
		double seconds = (DegVal - (hours + minutes/60.0)*15.0)*3600/15.0;
		String hhmmss = String.format(Locale.US,"%02d:%02d:%4.2f", hours,minutes,seconds);
		return hhmmss;
	}
	
	public static String toHHMMSS(double radValue){
		double DegVal = Math.abs(radValue*Constants.rad2Deg);
		int hours = (int)(DegVal/15.0);
		int minutes = (int)((DegVal - hours*15.0)*60/15.0);
		double seconds = (DegVal - (hours + minutes/60.0)*15.0)*3600/15.0;
		String hhmmss = String.format(Locale.US,"%02d:%02d:%4.2f", hours,minutes,seconds);
		return hhmmss;
	}
	
	
	public String toDDMMSS(){
		double DegVal = Math.abs(getDegreeValue());
		char sign = '-';
		if(radValue > 0) sign = '+'; 
		int degrees = (int)(DegVal);
		int minutes = (int)((DegVal - degrees)*60);
		double seconds = ((DegVal - (degrees + minutes/60.0))*3600.0);
		String ddmmss = String.format(Locale.US,"%c%02d:%02d:%4.2f",sign,degrees,minutes,seconds);
		return ddmmss;
	}
	
	public static String toDDMMSS(double radValue){
		double DegVal = Math.abs(radValue*Constants.rad2Deg);
		char sign = '-';
		if(radValue > 0) sign = '+'; 
		int degrees = (int)(DegVal);
		int minutes = (int)((DegVal - degrees)*60);
		double seconds = ((DegVal - (degrees + minutes/60.0))*3600.0);
		String ddmmss = String.format(Locale.US,"%c%02d:%02d:%4.2f",sign,degrees,minutes,seconds);
		return ddmmss;
	}
	
	@Override
	public String toString() {
		
		switch(toStringUnits){
		case HHMMSS:
			return toHHMMSS();
		case DDMMSS:
			return toDDMMSS();
		case DEG:
			return getDegreeValue().toString();
		case RAD:
			return radValue.toString();
		}
		return null;
				
	}
	public Double getRadValue() {
		return radValue;
	}
	public void setRadValue(Double radValue) {
		this.radValue = radValue;
	}

	public Integer getSign() {
		return sign;
	}

	public void setSign(Integer sign) {
		this.sign = sign;
	}

	@Override
	public int compareTo(Angle o) {
		
		return radValue.compareTo(o.radValue);
	}
	
	
}
