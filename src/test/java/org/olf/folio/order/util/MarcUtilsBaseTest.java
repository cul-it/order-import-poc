package org.olf.folio.order.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsBaseTest { 
	protected String file1 = "/Volumes/LaCie/cul/src/order-import-poc-dev/marc-test-files/harrass.mrc"; 
	protected String file2 = "/Volumes/LaCie/cul/src/order-import-poc-dev/marc-test-files/Casalini.1.mrc";
	protected String file3 = "/Volumes/LaCie/cul/src/order-import-poc-dev/marc-test-files/physical.mrc";
	protected String file4 = "/Volumes/LaCie/cul/src/order-import-poc-dev/test-mrc/AmazonFO.1.mrc";
	protected String file5 = "/Volumes/LaCie/cul/src/order-import-poc-dev/test-mrc/Midwest.1.mrc";
	protected String file6 = "/Volumes/LaCie/cul/src/order-import-poc-dev/test-mrc/CouttsUKFO.1.mrc";
	protected String file7 = "/Volumes/LaCie/cul/src/order-import-poc-dev/test-mrc/requesters_5-records_2021-03-11.mrc";
	
	
	public String[] fileArray = {
		"/Volumes/LaCie/cul/src/order-import-poc-dev/marc-test-files/harrass.mrc",
	};  
    
	public List<String> files = Arrays.asList(fileArray);
	
	MarcUtils marcUtils = new MarcUtils();

	 
	public List<Record> getRecords(String fname) throws Exception{
		List<Record> records = new ArrayList<Record>();
		FileInputStream in = new FileInputStream(fname);	 
		MarcReader reader = new MarcStreamReader(in);
		Record record = null;
		while (reader.hasNext()) {
			record = reader.next();
			records.add(record);
		}
		return records;
	}

}
