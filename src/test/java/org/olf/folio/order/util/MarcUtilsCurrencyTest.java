package org.olf.folio.order.util;
 
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsCurrencyTest extends MarcUtilsBaseTest { 
	
	boolean debug = false;

	@Test
	public void testGetCurrency() {
		String fname = file7;
		try {
			List<Record> records = getRecords(fname);
			for (Record record : records) {
				DataField nineEightyOne = (DataField) record.getVariableField("981");
				// System.out.println(nineEightyOne.toString());
				String currency = marcUtils.getCurrency(nineEightyOne);
				if (debug) {
					System.out.println(fname + " - currency: " + currency);
				} else {
					assertNotNull(currency);
					assertTrue(currency.length() > 0);
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

}
