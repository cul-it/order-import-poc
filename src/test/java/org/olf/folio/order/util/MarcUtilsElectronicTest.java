package org.olf.folio.order.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsElectronicTest extends MarcUtilsBaseTest { 
	 
    boolean debug = false;
    
	@Test
	public void testGetElectronic() {
		String fname = file2; 
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				DataField nineEighty = (DataField) record.getVariableField("980");
				String electronic = marcUtils.getElectronicIndicator(nineEighty);
				 
				if (debug) {
					System.out.println(fname + " - electronic: " + electronic);
				} else {
					assertNotNull(electronic);
					assertTrue(electronic.length() > 0);
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		 
	} 

}
