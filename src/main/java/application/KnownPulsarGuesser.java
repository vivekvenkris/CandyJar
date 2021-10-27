package application;

import constants.Constants;
import data_holders.Angle;
import data_holders.Candidate;
import data_holders.Pulsar;
import utilitites.Utilities;

public class KnownPulsarGuesser {
	
	static Integer maxHarmonics = Constants.MAX_GUESSING_HARMONICS;
	
	static Integer percentCloseToHarmonic= 1;
	static Integer unitsCloseToDM= 2;
	
	public static String guessPulsar(Candidate candidate, Pulsar pulsar) {
		
		Double pulsarF0 = pulsar.getF0();
		Double pulsarDM = pulsar.getDm();
		Angle pulsarRA = pulsar.getRa();
		Angle pulsarDEC = pulsar.getDec();
		String psrName = pulsar.getName();
		
		String s = "";

		if(pulsarF0 == null || pulsarDM == null) return null;
		
		boolean closeInPeriod = false;
		
		boolean subHarmonic = candidate.getOptF0() <  pulsarF0;
		
		double ratio = subHarmonic? pulsarF0/candidate.getOptF0() : candidate.getOptF0()/pulsarF0;
		
//		int intRatio = (int)Math.round(ratio);
//
//		if (intRatio < maxHarmonics && Math.abs(Math.abs(ratio - intRatio)/ratio) * 100 < percentCloseToHarmonic/100) { // Within 1% of the absolute value of the harmonic
//			
//			double fundamental = candidate.getOptF0();
//			double errFundamental = candidate.getOptF0Err();
//			
//			if(intRatio > 1)  fundamental = subHarmonic? fundamental * ratio : fundamental / ratio;
//				
//			s += ( "Harmonic:" + (subHarmonic? "1/":"") + String.format("%.4f", ratio) + "\n");	
//			
//			closeInPeriod = true;
//		}
//		
//		
//		boolean closeInDM = false;
//		
//		if( Math.abs(Math.abs(pulsarDM - candidate.getOptDM())/candidate.getOptDMErr()) < unitsCloseToDM  ){
//			
//			s += String.format("DM: %.3f \n",Math.abs(pulsarDM - candidate.getOptDM())/candidate.getOptDMErr());
//			
//		}
//		
//		boolean  closeInSpace = false;
		
		s+= String.format("Angular distance: %.5f", Utilities.getAngularDistance(pulsarRA, pulsarDEC, candidate.getRa(), candidate.getDec()).getDegreeValue()) + " degrees. \n";
		s+= String.format("Absolute DM difference: %.3f \n",Math.abs(pulsarDM - candidate.getOptDM()));
		s+= String.format("Pulsar frequency / candidate frequency: %.6f \n",Math.abs(pulsarF0 / candidate.getOptF0()));
		s+= String.format("Candidate frequency / pulsar frequency: %.6f \n",Math.abs(candidate.getOptF0()/pulsarF0));

		return s;
		
	}

}
