package org.olf.folio.order.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsPublisherTest extends MarcUtilsBaseTest { 
	
    boolean debug = false;    

	@Test
	public void testGetPublisher() {
	    List<String> myFnames = new ArrayList<String>();
	    myFnames.add(this.amazonFO);
		try {
		    for (String fname: myFnames) {
    			List<Record> records = getRecords(fname);
    			for (Record record: records) {
    				
    				String publisher = marcUtils.getPublisher(record);
    				if (debug) {
    					System.out.println(fname + " - publisher: " + publisher);
    				} else {
    					assertNotNull(publisher);
    					assertTrue(publisher.length() > 0);
    				} 
    			}
		    }
		} catch (Exception e) {
			fail(e.getMessage());
		}		 
	} 

}
