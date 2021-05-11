package org.olf.folio.order.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;


public class MarcUtilsLinksTest extends MarcUtilsBaseTest {
	
	boolean debug = false;

	@Test
	public void testGetLinks() {
		String fname = requestors;
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				JSONArray eresources = marcUtils.getLinks(record);
				if (debug && ! eresources.isEmpty()) {
			       System.out.println(eresources.toString(3));
			       System.out.println("len: "+ eresources.length());
				} else {
					assertNotNull(eresources);
					if (eresources.length() > 0) {
						JSONObject obj = (JSONObject) eresources.get(0);
						assertNotNull(obj);
					}
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}	 

}
