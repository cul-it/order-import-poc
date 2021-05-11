package org.olf.folio.order.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsRushTest extends MarcUtilsBaseTest { 
	
	boolean debug = false;

	@Test
	public void testGetRush() {
		String fname = requestors; 
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				DataField nineEightyOne = (DataField) record.getVariableField("981");
				String rush = marcUtils.getRush(nineEightyOne);
				if (debug) {
					System.out.println(fname + " - rush: " + rush);
				} else {
					assertNotNull(rush);
					assertTrue(rush.length() > 0);
				} 
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		 
	} 

}
