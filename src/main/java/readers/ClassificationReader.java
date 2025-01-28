package readers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;

import data_holders.Candidate;
import data_holders.Candidate.CANDIDATE_TYPE;

public class ClassificationReader extends Task<String> {

	File loadFile;
	List<Candidate> fullCandiatesList;

	public ClassificationReader(File loadFile, List<Candidate> fullCandiatesList) {
		this.loadFile = loadFile;
		this.fullCandiatesList = fullCandiatesList;
	}

    @Override
    protected String call() throws Exception {
        updateMessage("Loading classifications, please wait..");
		String returnStr = loadFromFile();
		updateMessage(returnStr);
        return returnStr;
    }

    public String loadFromFile()  throws IOException{

		try {
			List<String> list = Files.readAllLines(this.loadFile.toPath());
			List<String> classifiedList = list.stream().filter(f -> !f.contains("UNCAT")).collect(Collectors.toList());
			if(list.isEmpty() || list.size() == 1) {
				return "Loaded classification contains no classified candidates";
			}
			int length = classifiedList.get(1).split(",").length;
			int candidate_type_idx = length == 3? 2: 3; // old format without beam id and new with beam id
					

			for (Candidate candidate : this.fullCandiatesList) {

				List<String> lines  = classifiedList.stream().filter(f -> f.contains(candidate.getPngFilePath())).collect(Collectors.toList());

				if(lines.isEmpty()) {
					continue;
				}
				else if(lines.size() > 1) { 
					System.err.println( lines.size() + " values for " + candidate.getPngFilePath());
					System.err.println("Using the last value: " + lines.get(lines.size()-1));
					candidate.setCandidateType(CANDIDATE_TYPE.valueOf(lines.get(lines.size()-1).split(",")[candidate_type_idx]));
				}
				else {
					candidate.setCandidateType(CANDIDATE_TYPE.valueOf(lines.get(0).split(",")[candidate_type_idx]));	
				}

				classifiedList.removeAll(lines);

			}
            return "Classifications loaded successfully";

		} catch (IOException e) {
			e.printStackTrace();
            throw e;
		}



	}

	
	
}
