package readers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import data_holders.Candidate;

public class CandidateFileReader {
	
	public static List<Candidate> readCandidateFile(String candidateFileName) throws IOException {
		
		try {
			List<String> lines = Files.readAllLines(new File(candidateFileName).toPath());
			
			
			List<Candidate> candidates = lines
					.stream()
					.filter(f -> !f.startsWith("#") && !f.contains("id")).map(f -> new Candidate(f)).collect(Collectors.toList());
			
			
			return candidates;
			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
		
	}

}
