package org.olf.folio.order.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsBarcodeTest extends MarcUtilsBaseTest { 
	
	boolean debug = false;

	// disabled unil a mrc files with barcodes is available
	
	@Test
	public void testGetBarcode() {
	    
	    List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.shelfReadyAux);
		 
		try {
		    for (String fname: myFnames) {
    			List<Record> records = getRecords(fname);
    			for (Record record: records) {
    				DataField df = (DataField) record.getVariableField("976");
    				String barcode = marcUtils.getBarcode(df);
    				if (debug) {
    					System.out.println(fname + " - barcode: " + barcode);
    				} else {
    					assertNotNull(barcode);
    					assertTrue(barcode.length() > 0);
    					break; // only need to test first
    				} 
    			}
		    }
		} catch (Exception e) {
		    e.printStackTrace();
			fail(e.getMessage());
		}
		 
	} 

}
