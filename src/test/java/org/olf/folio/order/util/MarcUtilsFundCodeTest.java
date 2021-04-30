package org.olf.folio.order.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsFundCodeTest extends MarcUtilsBaseTest {
	
	boolean debug = false;  

	@Test
	public void testGetFundCode() {
		String fname = file1; 
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				DataField nineEighty = (DataField) record.getVariableField("980");
				//System.out.println(nineEighty.toString());
				String fundCode = marcUtils.getFundCode(nineEighty);
				 
				if (debug) {
					System.out.println(fname + " - fundCode: " + fundCode);
				} else {
					assertNotNull(fundCode);
					assertTrue(fundCode.length() > 0);
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		 
	} 

}
