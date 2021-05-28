package org.olf.folio.order.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

/**
 * MarcUtilsVendorTest.
 * 
 * @author jaf30
 *
 */
public class MarcUtilsVendorIdTest extends MarcUtilsBaseTest { 
    boolean debug = false; 

    @Test
    public void testGetVendorId() {
        String fname = this.singleharrass;
        try {
            List<Record> records = getRecords(fname);
            for (Record record : records) {
                DataField nineSixtyOne = (DataField) record.getVariableField("961");
                String vendorId = marcUtils.getVendorItemId(nineSixtyOne);
                if (debug) {
                    System.out.println(fname + " - vendorId: " + vendorId);
                } else {
                    assertNotNull(vendorId);
                    assertTrue(vendorId.length() > 0);
                } 
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
         
    } 

}
