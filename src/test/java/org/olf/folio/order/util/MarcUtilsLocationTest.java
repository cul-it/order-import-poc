package org.olf.folio.order.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;


/**
 * MarcUtilsLocationTest.
 */
public class MarcUtilsLocationTest extends MarcUtilsBaseTest {
    boolean debug = false;

    @Test
    public void testGetLocation() {
        String fname = amazonFO; 
        try {
            List<Record> records = getRecords(fname);
            for (Record record : records) {
                DataField nineFiveTwo = (DataField) record.getVariableField("952");
                String location = marcUtils.getLocation(nineFiveTwo);
                if (debug) {
                    System.out.println(fname + " - location: " + location);
                } else {
                    assertNotNull(location);
                    assertTrue(location.length() > 0);
                } 
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
         
    } 

}
