package readers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import constants.CandidateFileConstants;
import data_holders.Angle;
import data_holders.Candidate;
import data_holders.MetaFile;
import data_holders.Candidate.CANDIDATE_TYPE;
import exceptions.InvalidInputException;
import utilitites.Utilities;

public class CandidateFileReader implements CandidateFileConstants {
	
	private static 	Map<String, MetaFile> metafiles = new HashMap<String, MetaFile>();

	
	public static List<Candidate> readCandidateFile(String candidateFileName, File baseDir) throws IOException, InvalidInputException {
		
		try {
			List<String> lines = Files.readAllLines(new File(candidateFileName).toPath());
			

			List<Candidate> candidates = new ArrayList<Candidate>();
//			List<Candidate> candidates = lines
//					.stream()
//					.filter(f -> !f.startsWith("#") && !f.contains("id")).map(f -> new Candidate(f)).collect(Collectors.toList());
						

			Set<String> headers = lines.stream().filter(f-> f.contains("utc_start")).collect(Collectors.toSet());
			if (headers.isEmpty() || headers.size() > 1) {
				throw new InvalidInputException("CSV is either the wrong format, or has no/multiple non-identical headers.");
			}

			List<String> headerChunks = Arrays.asList(headers.toArray()[0].toString().split(","));
			Map<String, Integer> headerPositions = new HashMap<String, Integer>();
			for(String x : csvParams) {

				if(headerChunks.contains(x)) headerPositions.put(x, headerChunks.indexOf(x));
			}
			
		    AtomicInteger counter = new AtomicInteger(0);
			lines.stream().filter(f -> !f.startsWith("#") && !f.contains("pointing_id"))
			.forEach(f -> {
				counter.getAndIncrement();
				String[] chunks = f.split(",");
  				Candidate c = new Candidate();
				c.setLineNum(counter.toString());
				if(headerPositions.containsKey(pointing_id)) c.setPointingID(Integer.parseInt(chunks[headerPositions.get(pointing_id)]));
				if(headerPositions.containsKey(beam_id)) c.setBeamID(Integer.parseInt(chunks[headerPositions.get(beam_id)]));
				if(headerPositions.containsKey(beam_name)) c.setBeamName(chunks[headerPositions.get(beam_name)]);
				if(headerPositions.containsKey(source_name)) c.setSourceName(chunks[headerPositions.get(source_name)]);
				if(headerPositions.containsKey(ra)) c.setRa(new Angle(chunks[headerPositions.get(ra)], Angle.HHMMSS));
				if(headerPositions.containsKey(dec)) c.setDec(new Angle(chunks[headerPositions.get(dec)], Angle.DDMMSS));
				if(headerPositions.containsKey(gl)) c.setGl(new Angle(chunks[headerPositions.get(gl)], Angle.DEG));
				if(headerPositions.containsKey(gb)) c.setGb(new Angle(chunks[headerPositions.get(gb)], Angle.DEG));
				if(headerPositions.containsKey(mjd_start)) c.setStartMJD(Double.parseDouble(chunks[headerPositions.get(mjd_start)]));
				
				if(headerPositions.containsKey(utc_start)) {
					c.setStartUTC(Utilities.getUTCLocalDateTime(chunks[headerPositions.get(utc_start)], DateTimeFormatter.ISO_DATE_TIME));
					c.setUtcString(Utilities.getUTCString(c.getStartUTC(), DateTimeFormatter.ISO_DATE_TIME));
				}
				
				if(headerPositions.containsKey(f0_user)) c.setUserF0(Double.parseDouble(chunks[headerPositions.get(f0_user)]));
				if(headerPositions.containsKey(f0_opt)) c.setOptF0(Double.parseDouble(chunks[headerPositions.get(f0_opt)]));
				if(headerPositions.containsKey(f0_opt_err)) c.setOptF0Err(Double.parseDouble(chunks[headerPositions.get(f0_opt_err)]));
				if(headerPositions.containsKey(f1_user)) c.setUserF1(Double.parseDouble(chunks[headerPositions.get(f1_user)]));
				if(headerPositions.containsKey(f1_opt)) c.setOptF1(Double.parseDouble(chunks[headerPositions.get(f1_opt)]));
				if(headerPositions.containsKey(f1_opt_err)) c.setOptF1Err(Double.parseDouble(chunks[headerPositions.get(f1_opt_err)]));
				if(headerPositions.containsKey(acc_user)) c.setUserAcc(Double.parseDouble(chunks[headerPositions.get(acc_user)]));
				if(headerPositions.containsKey(acc_opt)) c.setOptAcc(Double.parseDouble(chunks[headerPositions.get(acc_opt)]));
				if(headerPositions.containsKey(acc_opt_err)) c.setOptAccErr(Double.parseDouble(chunks[headerPositions.get(acc_opt_err)]));
				if(headerPositions.containsKey(dm_user)) c.setUserDM(Double.parseDouble(chunks[headerPositions.get(dm_user)]));
				if(headerPositions.containsKey(dm_opt)) c.setOptDM(Double.parseDouble(chunks[headerPositions.get(dm_opt)]));
				if(headerPositions.containsKey(dm_opt_err)) c.setOptDMErr(Double.parseDouble(chunks[headerPositions.get(dm_opt_err)]));
				if(headerPositions.containsKey(sn_fft)) c.setFftSNR(Double.parseDouble(chunks[headerPositions.get(sn_fft)]));
				if(headerPositions.containsKey(sn_fold)) c.setFoldSNR(Double.parseDouble(chunks[headerPositions.get(sn_fold)]));
				if(headerPositions.containsKey(pepoch)) c.setPeopoch(Double.parseDouble(chunks[headerPositions.get(pepoch)]));
				if(headerPositions.containsKey(maxdm_ymw16)) c.setMaxDMYMW16(Double.parseDouble(chunks[headerPositions.get(maxdm_ymw16)]));
				if(headerPositions.containsKey(dist_ymw16)) c.setDistYMW16(Double.parseDouble(chunks[headerPositions.get(dist_ymw16)]));
				if(headerPositions.containsKey(png_path)) c.setPngFilePath(chunks[headerPositions.get(png_path)]);
				if(headerPositions.containsKey(metafile_path)) c.setMetaFilePath(chunks[headerPositions.get(metafile_path)]);
				if(headerPositions.containsKey(filterbank_path)) c.setFilterbankPath(chunks[headerPositions.get(filterbank_path)]);
				if(headerPositions.containsKey(candidate_tarball_path)) c.setTarballPath(chunks[headerPositions.get(candidate_tarball_path)]);
				if(headerPositions.containsKey(tobs)) c.setTobs(Double.parseDouble(chunks[headerPositions.get(tobs)]));
				for (String classifier : classifierNames) {
					if(headerPositions.containsKey(classifier)) {
						c.addClassifierScore(classifier, Double.parseDouble(chunks[headerPositions.get(classifier)]));
					}
				}
				
				c.setCandidateType(CANDIDATE_TYPE.UNCAT);

				MetaFile metaFile = null;
				try {
					metaFile = metafiles.getOrDefault(c.getUtcString(), ApsuseMetaReader.parseFile(baseDir.getAbsolutePath() + File.separator +c.getMetaFilePath()));
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				c.setMetaFile(metaFile);
				c.setBeam(metaFile.getBeams().get(c.getBeamName()));
				c.setCsvLine(f);
				candidates.add(c);
				
			});
			
			// get all keys from headerPositions that contain PICS in their name
			List<String> picsClassifiers = headerChunks.stream().filter(f -> f.contains("pics")).collect(Collectors.toList());
			System.err.println("Found " + picsClassifiers.size() + " PICS classifiers:" + picsClassifiers);
			Candidate.initParamMapsWithClassifiers(picsClassifiers);

			System.err.println("Read all candidates...");


			
			return candidates;
			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (InvalidInputException e) {
			e.printStackTrace();
			throw e;
		}
		
		
	}

}
