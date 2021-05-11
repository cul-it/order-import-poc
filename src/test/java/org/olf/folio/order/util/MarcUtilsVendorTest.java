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
public class MarcUtilsVendorTest extends MarcUtilsBaseTest { 
    boolean debug = false; 

    @Test
    public void testGetVendor() {
        String fname = physical;
        try {
            List<Record> records = getRecords(fname);
            for (Record record : records) {
                DataField nineEighty = (DataField) record.getVariableField("980");
                String vendor = marcUtils.getVendorCode(nineEighty);
                if (debug) {
                    System.out.println(fname + " - vendor: " + vendor);
                } else {
                    assertNotNull(vendor);
                    assertTrue(vendor.length() > 0);
                } 
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
         
    } 

}
