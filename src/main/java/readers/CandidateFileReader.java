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
						

			List<String> headers = lines.stream().filter(f-> f.contains("utc_start")).collect(Collectors.toList());
			if (headers.isEmpty() || headers.size() > 1) {
				throw new InvalidInputException("CSV is either the wrong format, or has no/multiple headers.");
			}
			List<String> headerChunks = Arrays.asList(headers.get(0).split(","));
			Map<String, Integer> headerPositions = new HashMap<String, Integer>();
			for(String x : csvParams) {
				headerPositions.put(x, headerChunks.indexOf(x));
			}
			
		    AtomicInteger counter = new AtomicInteger(0);
			lines.stream().filter(f -> !f.startsWith("#") && !f.contains("pointing_id"))
			.forEach(f -> {
				counter.getAndIncrement();
				String[] chunks = f.split(",");
				Candidate c = new Candidate();
				c.setLineNum(counter.toString());
				if(headerPositions.getOrDefault(pointing_id, -1) != -1) c.setPointingID(Integer.parseInt(chunks[headerPositions.get(pointing_id)]));
				if(headerPositions.getOrDefault(beam_id, -1) != -1) c.setBeamID(Integer.parseInt(chunks[headerPositions.get(beam_id)]));
				if(headerPositions.getOrDefault(beam_name, -1) != -1) c.setBeamName(chunks[headerPositions.get(beam_name)]);
				if(headerPositions.getOrDefault(source_name, -1) != -1) c.setSourceName(chunks[headerPositions.get(source_name)]);
				if(headerPositions.getOrDefault(ra, -1) != -1) c.setRa(new Angle(chunks[headerPositions.get(ra)], Angle.HHMMSS));
				if(headerPositions.getOrDefault(dec, -1) != -1) c.setDec(new Angle(chunks[headerPositions.get(dec)], Angle.DDMMSS));
				if(headerPositions.getOrDefault(gl, -1) != -1) c.setGl(new Angle(chunks[headerPositions.get(gl)], Angle.DEG));
				if(headerPositions.getOrDefault(gb, -1) != -1) c.setGb(new Angle(chunks[headerPositions.get(gb)], Angle.DEG));
				if(headerPositions.getOrDefault(mjd_start, -1) != -1) c.setStartMJD(Double.parseDouble(chunks[headerPositions.get(mjd_start)]));
				
				if(headerPositions.getOrDefault(utc_start, -1) != -1) {
					c.setStartUTC(Utilities.getUTCLocalDateTime(chunks[headerPositions.get(utc_start)], DateTimeFormatter.ISO_DATE_TIME));
					c.setUtcString(Utilities.getUTCString(c.getStartUTC(), DateTimeFormatter.ISO_DATE_TIME));
				}
				
				if(headerPositions.getOrDefault(f0_user, -1) != -1) c.setUserF0(Double.parseDouble(chunks[headerPositions.get(f0_user)]));
				if(headerPositions.getOrDefault(f0_opt, -1) != -1) c.setOptF0(Double.parseDouble(chunks[headerPositions.get(f0_opt)]));
				if(headerPositions.getOrDefault(f0_opt_err, -1) != -1) c.setOptF0Err(Double.parseDouble(chunks[headerPositions.get(f0_opt_err)]));
				if(headerPositions.getOrDefault(f1_user, -1) != -1) c.setUserF1(Double.parseDouble(chunks[headerPositions.get(f1_user)]));
				if(headerPositions.getOrDefault(f1_opt, -1) != -1) c.setOptF1(Double.parseDouble(chunks[headerPositions.get(f1_opt)]));
				if(headerPositions.getOrDefault(f1_opt_err, -1) != -1) c.setOptF1Err(Double.parseDouble(chunks[headerPositions.get(f1_opt_err)]));
				if(headerPositions.getOrDefault(acc_user, -1) != -1) c.setUserAcc(Double.parseDouble(chunks[headerPositions.get(acc_user)]));
				if(headerPositions.getOrDefault(acc_opt, -1) != -1) c.setOptAcc(Double.parseDouble(chunks[headerPositions.get(acc_opt)]));
				if(headerPositions.getOrDefault(acc_opt_err, -1) != -1) c.setOptAccErr(Double.parseDouble(chunks[headerPositions.get(acc_opt_err)]));
				if(headerPositions.getOrDefault(dm_user, -1) != -1) c.setUserDM(Double.parseDouble(chunks[headerPositions.get(dm_user)]));
				if(headerPositions.getOrDefault(dm_opt, -1) != -1) c.setOptDM(Double.parseDouble(chunks[headerPositions.get(dm_opt)]));
				if(headerPositions.getOrDefault(dm_opt_err, -1) != -1) c.setOptDMErr(Double.parseDouble(chunks[headerPositions.get(dm_opt_err)]));
				if(headerPositions.getOrDefault(sn_fft, -1) != -1) c.setFftSNR(Double.parseDouble(chunks[headerPositions.get(sn_fft)]));
				if(headerPositions.getOrDefault(sn_fold, -1) != -1) c.setFoldSNR(Double.parseDouble(chunks[headerPositions.get(sn_fold)]));
				if(headerPositions.getOrDefault(pepoch, -1) != -1) c.setPeopoch(Double.parseDouble(chunks[headerPositions.get(pepoch)]));
				if(headerPositions.getOrDefault(maxdm_ymw16, -1) != -1) c.setMaxDMYMW16(Double.parseDouble(chunks[headerPositions.get(maxdm_ymw16)]));
				if(headerPositions.getOrDefault(dist_ymw16, -1) != -1) c.setDistYMW16(Double.parseDouble(chunks[headerPositions.get(dist_ymw16)]));
				if(headerPositions.getOrDefault(pics_trapum_ter5, -1) != -1) c.setPicsScoreTrapum(Double.parseDouble(chunks[headerPositions.get(pics_trapum_ter5)]));
				if(headerPositions.getOrDefault(pics_palfa, -1) != -1) c.setPicsScorePALFA(Double.parseDouble(chunks[headerPositions.get(pics_palfa)]));
				if(headerPositions.getOrDefault(png_path, -1) != -1) c.setPngFilePath(chunks[headerPositions.get(png_path)]);
				if(headerPositions.getOrDefault(metafile_path, -1) != -1) c.setMetaFilePath(chunks[headerPositions.get(metafile_path)]);
				if(headerPositions.getOrDefault(filterbank_path, -1) != -1) c.setFilterbankPath(chunks[headerPositions.get(filterbank_path)]);
				if(headerPositions.getOrDefault(candidate_tarball_path, -1) != -1) c.setTarballPath(chunks[headerPositions.get(candidate_tarball_path)]);
				
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
				
				candidates.add(c);
				
			});
			
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
