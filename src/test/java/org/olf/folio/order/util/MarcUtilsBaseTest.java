package org.olf.folio.order.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader; 
import org.marc4j.marc.Record;
 
 
public class MarcUtilsBaseTest {
	
	private String buildDir;
	
	protected String file1; 
	protected String file2;
	protected String file3;
	protected String file4;
	protected String file5;
	protected String file6;
	protected String file7;
	 
	MarcUtils marcUtils = new MarcUtils();
	public MarcUtilsBaseTest() {
		init(); 
		this.file1 = this.buildDir +"/marc-test-files/harrass.mrc"; 
		this.file2 = this.buildDir +"/marc-test-files/Casalini.1.mrc";
		this.file3 = this.buildDir +"/marc-test-files/physical.mrc";
		this.file4 = this.buildDir +"/marc-test-files/AmazonFO.1.mrc";
		this.file5 = this.buildDir +"/marc-test-files/MIDWEST.1.mrc";
		this.file6 = this.buildDir +"/marc-test-files/CouttsUKFO.1.mrc";
		this.file7 = this.buildDir +"/marc-test-files/requesters_5-records_2021-03-11.mrc";
	}
	 
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
	
    public void init() {
    	CompositeConfiguration config = new CompositeConfiguration();
    	PropertiesConfiguration props = new PropertiesConfiguration();
    	
		String use_env = System.getenv("USE_SYSTEM_ENV");
		if (StringUtils.isNotEmpty(use_env) && StringUtils.equals(use_env, "true")) {
			config.setProperty("buildDir",  System.getenv("buildDir")); 
		 	
		} else { 
		   try {
			   props.load(ClassLoader.getSystemResourceAsStream("application.properties"));
			   this.buildDir = (String) props.getProperty("buildDir"); 
		   } catch (ConfigurationException e) { 
		      throw new RuntimeException("Could not load application.properties file");
		   }
		   config.addConfiguration(props);
		}
    }
}
