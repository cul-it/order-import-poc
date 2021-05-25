package org.olf.folio.order.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
//import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;


/**
 * @author jaf30
 *
 */
public class MarcUtilsPriceTest extends MarcUtilsBaseTest { 
    
    boolean debug = true;

    /**
     * 
     */
    @Test
    public void testGetPrice() {
        String fname = amazonFO; 
        try {
            List<Record> records = getRecords(fname);
            for (Record record : records) {
                DataField nineEightyOne = (DataField) record.getVariableField("981");
                String price = marcUtils.getPrice(nineEightyOne);
                if (debug) {
                    System.out.println(fname + " - price: $" + price);
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
