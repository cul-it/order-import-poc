package org.olf.folio.order.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsSelectorTest extends MarcUtilsBaseTest { 
	 
    boolean debug = false;
    
	@Test
	public void testGetSelector() {
		String fname = casalini;
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				DataField nineEighty = (DataField) record.getVariableField("980");
				String selector = marcUtils.getSelector(nineEighty);
				if (debug) {
					System.out.println(fname + " - selector: " + selector);
				} else {
					assertNotNull(selector);
					assertTrue(selector.length() > 0);
				} 
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		 
	} 

}
