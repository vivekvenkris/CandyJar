package readers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import data_holders.Angle;
import data_holders.Beam;
import data_holders.EllipseConfig;
import data_holders.MetaFile;
import utilitites.Utilities;



public class ApsuseMetaReader {
	
	public static MetaFile parseFile(String fileName) throws FileNotFoundException {
		
		MetaFile metaFile = new MetaFile();
		metaFile.setFileName(fileName);

		try {
		
			Reader reader = new FileReader(fileName);
			JsonReader jsonReader = Json.createReader(reader);
			JsonObject jsonObject = jsonReader.readObject();

			
			metaFile.setBandwidth(jsonObject.getJsonNumber("bandwidth").doubleValue());
			metaFile.setCentreFreq(jsonObject.getJsonNumber("centre_frequency").doubleValue());
			
			
			metaFile.setUtc(Utilities.getUTCLocalDateTime(jsonObject.getJsonString("utc_start").getString()));
			metaFile.setProjectName(jsonObject.getJsonString("project_name").getString());
			metaFile.setScheduleBlockId(jsonObject.getJsonString("sb_id").getString());
			metaFile.setOutputDir(jsonObject.getJsonString("output_dir").getString());
			
			metaFile.setTsampCoherent(jsonObject.getJsonNumber("coherent_tsamp").doubleValue());
			metaFile.setNumChansCoherent(jsonObject.getJsonNumber("coherent_tsamp").intValue());
			
			metaFile.setTsampIncoherent(jsonObject.getJsonNumber("incoherent_tsamp").doubleValue());
			metaFile.setNumChansIncoherent(jsonObject.getJsonNumber("coherent_tsamp").intValue());
			
			String beamShapeJson = jsonObject
					.get("beamshape")
					.toString();
			
			beamShapeJson = beamShapeJson.substring(1, beamShapeJson.length() -1).replaceAll("\\\\", "");

			
			JsonReader beamShapeJsonReader = Json.createReader(new StringReader(beamShapeJson));			
			JsonObject beamShapeJsonObject = beamShapeJsonReader.readObject();
			
			EllipseConfig ellipseConfig = new EllipseConfig(beamShapeJsonObject.getJsonNumber("x").doubleValue(),
					beamShapeJsonObject.getJsonNumber("y").doubleValue(), 
					new Angle(beamShapeJsonObject.getJsonNumber("angle").doubleValue() + "", Angle.DEG)); 

			
			metaFile.setEllipseConfig(ellipseConfig);
			

			
			metaFile.setBoresight(new Beam(jsonObject.getJsonString("boresight").getString()));
			
			
			
			JsonObject beamJsonObject = (JsonObject) jsonObject.get("beams");
			
			
			
			Set<Entry<String, JsonValue>> entrySet = beamJsonObject.entrySet();
			
			for(Entry<String, JsonValue> e: entrySet) {
				String name = (String) e.getKey();
				JsonValue value =(JsonValue) e.getValue();
				if (name.contains("if")) continue;
				Beam beam  = new Beam(name, value.toString().strip().replaceAll("\"", ""),ellipseConfig);
				metaFile.getBeams().put(name,beam);
				
			}
			
			double minRa = metaFile.getBeams()
					.values()
					.stream()
					.min(Comparator.comparing(Beam::getRa)).get().getRa().getRadianValue();
			
			double minDec = metaFile.getBeams()
					.values()
					.stream()
					.min(Comparator.comparing(Beam::getDec)).get().getDec().getRadianValue();
			
			double maxRa = metaFile.getBeams()
					.values()
					.stream()
					.max(Comparator.comparing(Beam::getRa)).get().getRa().getRadianValue();
			
			double maxDec = metaFile.getBeams()
					.values()
					.stream()
					.max(Comparator.comparing(Beam::getDec)).get().getDec().getRadianValue();
			
			double temp; 
			
			metaFile.setMinRa(new Angle(minRa, Angle.RAD, Angle.HHMMSS));
			metaFile.setMaxRa(new Angle(maxRa, Angle.RAD, Angle.HHMMSS));
			
			if (minDec < 0  && maxDec < 0) {
				temp = minDec;
				minDec = maxDec;
				maxDec = temp;
			}
			
			metaFile.setMaxDec(new Angle(maxDec, Angle.RAD, Angle.DDMMSS));
			metaFile.setMinDec(new Angle(minDec, Angle.RAD, Angle.DDMMSS));

			return metaFile;
		}
		catch(FileNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			
			throw e;
			
		} 
	}

}
