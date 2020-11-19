package constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import data_holders.Pulsar;

public interface PsrcatConstants {
	
	
	String PSRJ = "PSRJ";
	String RAJ = "RAJ";
	String DECJ = "DECJ";
	String F0 = "F0";
	String P0 = "P0";
	String DM = "DM";
	String BINARY = "BINARY";
	String PB = "PB";
	String A1 = "A1";
	String ECC = "ECC";
	
	Integer endOfName=8;
	Integer endofValue = 34;
	
	List<String> psrcatDBs =  Arrays.asList(new String[] {"/Users/vkrishnan/trashcan/psrcat_tar/psrcat.db"});
	
	Map<String, Pulsar> pulsarMap = new LinkedHashMap<String, Pulsar>();
	
	
	
	
	

}
