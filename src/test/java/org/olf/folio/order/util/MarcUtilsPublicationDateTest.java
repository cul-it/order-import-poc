package org.olf.folio.order.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsPublicationDateTest extends MarcUtilsBaseTest { 
	
    boolean debug = false;

	@Test
	public void testGetPublicationDate() {
	    List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.singleharrass); 
		try {
		    for (String fname: myFnames) {
		        if (debug) {
                    System.out.println("fname = "+ fname);
                }
    			List<Record> records = getRecords(fname);
    			for (Record record: records) {
    			    if (debug) {
                        System.out.println(record.toString());
                    }
    				DataField df = (DataField) record.getVariableField("264");
    				String pubDate = marcUtils.getPublicationDate(df);
    				if (debug) {
    					System.out.println(fname + " - pubDate: " + pubDate);
    				} else {
    					assertNotNull(pubDate);
    					assertTrue(pubDate.length() > 0);
    				} 
    			}
		    }
		} catch (Exception e) {
			fail(e.getMessage());
		}		 
	} 

}
