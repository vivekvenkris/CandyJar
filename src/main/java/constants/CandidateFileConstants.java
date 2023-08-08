package constants;

import java.util.Arrays;
import java.util.List;

public interface CandidateFileConstants {
	
	String  pointing_id = "pointing_id";
	String  beam_id = "beam_id";
	String  beam_name = "beam_name";
	String  source_name = "source_name";
	String  ra = "ra";
	String  dec = "dec";
	String  gl = "gl";
	String  gb = "gb";
	String  mjd_start = "mjd_start";
	String  utc_start = "utc_start";
	String  f0_user = "f0_user";
	String  f0_opt = "f0_opt";
	String  f0_opt_err = "f0_opt_err";
	String  f1_user = "f1_user";
	String  f1_opt = "f1_opt";
	String  f1_opt_err = "f1_opt_err";
	String  acc_user = "acc_user";
	String  acc_opt = "acc_opt";
	String  acc_opt_err = "acc_opt_err";
	String  f2_opt="f2_opt";
	String  f2_opt_err="f2_opt";
	String  f2_user="f2_opt";
	String  dm_user = "dm_user";
	String  dm_opt = "dm_opt";
	String  dm_opt_err = "dm_opt_err";
	String  sn_fft = "sn_fft";
	String  sn_fold = "sn_fold";
	String  pepoch = "pepoch";
	String  maxdm_ymw16 = "maxdm_ymw16";
	String  dist_ymw16 = "dist_ymw16";
	String  pics_trapum_ter5 = "pics_trapum_ter5";
	String  pics_palfa = "pics_palfa";
	String  pics_m_LS_recall = "pics_meerkat_l_sband_combined_best_recall";
	String  pics_pm_LS_fscore = "pics_palfa_meerkat_l_sband_best_fscore";	
	String  png_path = "png_path";
	String  metafile_path = "metafile_path";
	String  filterbank_path = "filterbank_path";
	String  candidate_tarball_path = "candidate_tarball_path"; 
	String  tobs = "tobs";

	List<String> classifierNames = Arrays.asList(new String[]{pics_palfa,pics_trapum_ter5,pics_m_LS_recall,pics_pm_LS_fscore});

	
	List<String> csvParams = Arrays.asList(new String[] {pointing_id,beam_id,beam_name,source_name,ra,dec,gl,gb,mjd_start,utc_start,
			f0_user,f0_opt,f0_opt_err,f1_user,f1_opt,f1_opt_err,f2_opt, f2_opt_err, f2_user, acc_user,acc_opt,acc_opt_err,dm_user,dm_opt,dm_opt_err,sn_fft,
			sn_fold,pepoch,maxdm_ymw16,dist_ymw16,pics_trapum_ter5,pics_palfa,png_path,metafile_path,filterbank_path,candidate_tarball_path, tobs,
		pics_palfa,pics_trapum_ter5,pics_m_LS_recall,pics_pm_LS_fscore});


}
