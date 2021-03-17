package utilitites;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import constants.Constants;
import data_holders.Angle;
import data_holders.Beam;
import de.gsi.dataset.spi.utils.Tuple;

public class Utilities {
	public static String asq(String str){ return "'"+str +"'";}
	
	
	public static double getEuclideanDistance(double x1, double y1, double x2, double y2){

		double xdist = Math.abs(x2 - x1);
		double ydist = Math.abs(y2 - y1);

		return ( Math.sqrt(xdist*xdist + ydist*ydist));
	}
	
	public static double getAngularDistance(double radRA1, double radDEC1, double radRA2,  double radDEC2) {
		double deltaRA  =  radRA1 - radRA2;
		double deltaDEC = radDEC1 - radDEC2;
		/* Using the Haversine formula */
		
		double firstTerm =  Math.sin(deltaDEC/2.0) * Math.sin(deltaDEC/2.0);
		double secondTerm = Math.cos(radDEC1) * Math.cos(radDEC2) * Math.sin(deltaRA/2.0) * Math.sin(deltaRA/2.0);
		
		return 2 * Math.asin(Math.sqrt(firstTerm + secondTerm));
		
	}
	
	public static boolean isWithinEllipse(double x1, double y1, double x2, double y2, double a, double b){
		double xdist = Math.abs(x2 - x1)* Math.cos(0.5*Math.abs(y1+y2));
		double ydist = Math.abs(y2 - y1);

		return ( ( xdist*xdist/(a*a) + ydist*ydist/(b*b)) <= 1.0);
	}

	public static Angle getAngularDistance( Angle RA1, Angle DEC1, Angle RA2, Angle DEC2){
		double radianDistance = getAngularDistance(RA1.getRadianValue(), DEC1.getRadianValue(), RA2.getRadianValue(), DEC2.getRadianValue());
		return new Angle(radianDistance, Angle.RAD, Angle.DEG);
	}
	
	
	
	public static Angle getAngularDistance(Beam b1, Beam b2) {
		return getAngularDistance(b1.getRa(), b1.getDec(), b2.getRa(), b2.getDec());
	}
	
	
	public static double getPositionAngle(double ra1, double dec1, double ra2,  double dec2) {
		
		double numerator = Math.sin(ra2-ra1);
		double denominator = Math.cos(dec1)*Math.tan(dec2) - Math.sin(dec1)*Math.cos(ra2-ra1);
		return Math.atan2(numerator, denominator);
		
	}
	
	public static double distanceFromEllipseCenter(double a, double b, double radTheta) {
		double term1 = b * b * Math.cos(radTheta) * Math.cos(radTheta);
		double term2 = a * a * Math.sin(radTheta) *  Math.sin(radTheta); 
		return a * b /Math.sqrt(term1 + term2);
	}
	

	
	public static Angle getPositionAngle(Angle RA1, Angle DEC1, Angle RA2, Angle DEC2) {
		
		double radianAngle = getPositionAngle(RA1.getRadianValue(), DEC1.getRadianValue(), RA2.getRadianValue(), DEC2.getRadianValue());
		return new Angle(radianAngle, Angle.RAD, Angle.DEG); 
		
	}
	
	public static Angle getPositionAngle(Beam b1, Beam b2) {
		return getPositionAngle(b1.getRa(), b1.getDec(), b2.getRa(), b2.getDec());
	}
	
	
	public static String getUTCString(LocalDateTime utcTime, String format){
		return utcTime.format(DateTimeFormatter.ofPattern(format));
	}
	public static String getUTCString(LocalDateTime utcTime, DateTimeFormatter format){
		return utcTime.format(format);
	}
	
	public static String getUTCString(LocalDateTime utcTime){
		return utcTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd kk:mm:ss"));
	}
	
	public static LocalDateTime getUTCLocalDateTime(String utc, String pattern){
		return LocalDateTime.parse(utc, DateTimeFormatter.ofPattern(pattern));
	}
	public static LocalDateTime getUTCLocalDateTime(String utc, DateTimeFormatter pattern){
		return LocalDateTime.parse(utc, pattern);
	}
	

	public static LocalDateTime getUTCLocalDateTime(String utc){
		return LocalDateTime.parse(utc, DateTimeFormatter.ofPattern("yyyy/MM/dd kk:mm:ss"));
	}
	
	public static double getTimeDifferenceInDays(LocalDateTime init, LocalDateTime fin) {
		
		Duration duration = Duration.between(init, fin);
		
		return duration.getSeconds()  * Constants.sec2Hrs * Constants.hrs2Day;
		
	}
	
	public static boolean valueIsWithinRange(double value, double min, double max) {
		return value >= min && value <= max;
	}
	
	public static boolean valueIsOutsideRange(double value, double min, double max) {
		return !valueIsWithinRange(value, min, max);
	}
	public static boolean valueIsWithinRange(double value, Tuple<Double, Double> minMax) {
		return valueIsWithinRange(value, minMax.getXValue(), minMax.getYValue());
	}
	
	public static boolean valueIsOutsideRange(double value, Tuple<Double, Double> minMax) {
		return !valueIsWithinRange(value, minMax.getXValue(), minMax.getYValue());
	}

	
	
}
