package org.olf.folio.order.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsTitleTest extends MarcUtilsBaseTest { 
	
    boolean debug = false;
    
	@Test
	public void testGetTitle() {
		String fname = physical;
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				DataField twoFourFive = (DataField) record.getVariableField("245");
				String title = marcUtils.getTitle(twoFourFive);
				if (debug) {
					System.out.println(fname + " - title: " + title);
				} else {
					assertNotNull(title);
					assertTrue(title.length() > 0);
				} 
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		 
	} 

}
