package org.olf.folio.order.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsPriceTest extends MarcUtilsBaseTest { 
	
    boolean debug = false;

	@Test
	public void testGetPrice() {
		String fname = file4; 
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				DataField nineEighty = (DataField) record.getVariableField("980");
				DataField nineEightyOne = (DataField) record.getVariableField("981");
				//System.out.println(nineEighty.toString());
				//System.out.println();
				//System.out.println(nineEightyOne.toString());
				String price = marcUtils.getPrice(nineEighty, nineEightyOne);
				if (debug) {
					System.out.println(fname + " - price: " + price);
				} else {
					assertNotNull(price);
					assertTrue(price.length() > 0);
				} 
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		 
	} 

}
