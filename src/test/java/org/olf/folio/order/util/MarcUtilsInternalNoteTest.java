package org.olf.folio.order.util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class MarcUtilsInternalNoteTest extends MarcUtilsBaseTest { 
	 
    boolean debug = true;
    
	@Test
	public void testGetInternalNote() {
		String fname = file6;
		try {
			List<Record> records = getRecords(fname);
			for (Record record: records) {
				DataField nineEightyOne = (DataField) record.getVariableField("981");
				String notes = marcUtils.getInternalNotes(nineEightyOne);
				if (debug) {
					System.out.println(fname + " - notes: " + notes);
				} else {
					assertNotNull(notes);
					assertTrue(notes.length() > 0);
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		 
	} 

}