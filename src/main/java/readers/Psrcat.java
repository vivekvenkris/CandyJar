package readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import constants.PsrcatConstants;
import data_holders.Angle;
import data_holders.Pulsar;
import utilitites.Utilities;

public class Psrcat implements PsrcatConstants {
	
	
	public Psrcat() {
		try {
			read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void read() throws IOException{
		
		try {
			loadDBs();
			System.err.println(pulsarMap.size() + " pulsars loaded from PSRCAT");
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	
	public void readPersonalPulsarList(String fileName) {
		try {
			File file = new File(fileName);
			if(file.exists()) {
				List<String> lines =  Files.readAllLines(new File(fileName).toPath());
				for (String line: lines) {
					if(line.isBlank() || line.startsWith("#")) continue;
					
					String[] chunks = line.strip().split("\\s+");
					Pulsar pulsar = new Pulsar();
					pulsar.setName(chunks[0]+ "_L");
					pulsar.setRa(new Angle(chunks[1], Angle.HHMMSS));
					pulsar.setDec(new Angle(chunks[2], Angle.DDMMSS));
					pulsar.setP0(Double.parseDouble(chunks[3]));
					pulsar.setDm(Double.parseDouble(chunks[4]));
					pulsar.setBinary(Boolean.parseBoolean(chunks[5]));
					pulsarMap.put(chunks[0]+"_L", pulsar);
					
				}
				System.err.println("Successfully read " + fileName);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Pulsar>  getPulsarsInBeam(Angle raBoresight, Angle decBoresight, Angle radius) {
		
		return pulsarMap
			.entrySet()
			.stream()
			.filter(f -> {

				Pulsar pulsar = (Pulsar)f.getValue();
				

				Angle raPulsar = pulsar.getRa();
				Angle decPulsar = pulsar.getDec();

				
				Angle distance = Utilities.getAngularDistance(raPulsar, decPulsar, raBoresight, decBoresight);
				Angle theta = new Angle( - 90 + Utilities.getPositionAngle(raPulsar, decPulsar, raBoresight, decBoresight).getDegreeValue(), 
						Angle.DEG);
				
				Double r = Utilities.distanceFromEllipseCenter(radius.getRadianValue(),
						radius.getRadianValue(), theta.getRadianValue());
				
				pulsar.setDistanceFromBoresight(distance.getDegreeValue());
				if (distance.getRadianValue() / r <= 1) return true;

				return false;
			}).map(f ->  f.getValue()).sorted(Comparator.comparing(f -> ((Pulsar)f).getDistanceFromBoresight())).collect(Collectors.toList());
		
	}
	
	/**
	 * Loads data onto the pulsar map. Currently does not work for ELAT ELONG.
	 * @throws IOException
	 */
	
	public  void loadDBs() throws IOException{
		
		pulsarMap.clear();
				
		for(String psrcatDB: psrcatDBs) {
			
			if (! new File(psrcatDB).exists()) {
				System.err.println(psrcatDB + "not found. skipping");
				continue;
			}
			
			BufferedReader br = null;
			int count = 0;
			String line = "";
			try {
			
				br = new BufferedReader(new FileReader(psrcatDB)); 
				
				Pulsar pulsar = new Pulsar();
				boolean problematic = false;
				while((line=br.readLine())!=null){
					
					
					if(line.contains("#") || line.contains("@") || line.equals("")) continue;
					
					try {
						String[] chunks = line.trim().split("\\s+");
						String name = chunks[0];
						String value = chunks[1];


						if(name.equals(PSRJ)) {
							if(pulsar != null && pulsar.getRa() !=null && pulsar.getDec() !=null && !problematic) pulsarMap.put(value, pulsar);
							else if(pulsar != null) {
								System.err.println("Skipping pulsar: " + pulsar.getName() + " because of missing data: RA/DEC = " 
								+ pulsar.getRa() + "/" + pulsar.getDec() + "or otherwise problematic:" + problematic);
							}
							
							pulsar = new Pulsar();
							pulsar.setName(value);
							problematic = false;
	
						}
						else if(name.equals(RAJ))  pulsar.setRa(new Angle(value, Angle.HHMMSS));
						else if(name.equals(DECJ))  pulsar.setDec(new Angle(value, Angle.DDMMSS));
						else if(name.equals(F0)) pulsar.setF0(Double.parseDouble(value));
						else if(name.equals(P0)) pulsar.setP0(Double.parseDouble(value));
						else if(name.equals(DM)) pulsar.setDm(Double.parseDouble(value));
						else if (name.equals(BINARY)) pulsar.setBinary(true);
						pulsar.addToEphemerides(line);

						//System.err.println(pulsar.getRa() + " " + pulsar.getDec() + " " + pulsar.getP0() + " " + pulsar.getDm() + " " + pulsar.getBinary());
					
					}catch (NumberFormatException e) {
						
						System.err.println("Problem with line:" + line );
						System.err.println("Problem: " + e.getMessage());
						problematic = true;
						
					}

					
					
				}

				if(pulsar != null && pulsar.getRa() !=null && pulsar.getDec() !=null && !problematic) pulsarMap.put(pulsar.getName(), pulsar);
				else if(pulsar != null) {
					System.err.println("Skipping pulsar: " + pulsar.getName() + " because of missing data: " 
					+ pulsar.getRa() + " " + pulsar.getDec() + "or problematic:" + problematic);
					System.err.println(pulsar.getEphemerides());
				}
				
			}
			
			catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			finally {
					try {
						if(br!=null) br.close();
						
					} catch (IOException e) {
						e.printStackTrace();
						throw e;
					}
			}
			
			
			
			
		}
		
	}

	

}
