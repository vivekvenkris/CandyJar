package utilitites;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import data_holders.Beam;
import data_holders.MetaFile;
import readers.ApsuseMetaReader;

public class GetCloseBeams {
	
	public static void main(String[] args) throws FileNotFoundException {
		
		MetaFile metafile1 = ApsuseMetaReader.parseFile(args[0]);
		MetaFile metafile2 = ApsuseMetaReader.parseFile(args[1]);
		
		for (Beam b1: metafile1.getBeams().values()) {
			
			Map<Beam, Double> beamResponses = new HashMap<Beam, Double>();

			
			for (Beam b2: metafile2.getBeams().values()) {
				
				double result = MetaFile.getBeamIntersection(b1, b2);
				beamResponses.putIfAbsent(b2, result);

				
			}
			System.err.println(b1.getName() + " " + beamResponses.entrySet().stream()
			.sorted(Map.Entry.comparingByValue())
			.collect(Collectors.toList()).subList(0, 15).stream()
			.map(f -> f.getKey().getName() + " " + f.getKey().getCoordString() +  " " + f.getValue() +"\n").collect(Collectors.toList()));
			
		}
		
		
		
		
	}

}
