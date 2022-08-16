package org.olf.folio.order.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class MarcUtilsPriceTest extends MarcUtilsBaseTest { 
    
    boolean debug = false;
    protected final Log logger = LogFactory.getLog(getClass());

    
    @Test
    public void testGetPriceValid() {
        if (debug) {
            System.out.println("testGetPriceValid");
        } 
        try {

            MarcFactory factory = MarcFactory.newInstance();
            Record record = factory.newRecord();
            DataField nineEightyOne = factory.newDataField();
            nineEightyOne.setTag("981");
            Subfield sf = factory.newSubfield('i', "1.98");
            nineEightyOne.addSubfield(sf); 

            String price = marcUtils.getPrice(nineEightyOne);
            if (debug) {
                System.out.println("  price: " + price);
            } else {
                assertNotNull(price);
                assertTrue(price.length() > 0);
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
    
    @Test
    public void testGetPriceInValid() {
        if (debug) {
            System.out.println("testGetPriceInValid");
        }
        try {
            
            MarcFactory factory = MarcFactory.newInstance();
            Record record = factory.newRecord();
            DataField nineEightyOne = factory.newDataField();
            nineEightyOne.setTag("981");
            Subfield sf = factory.newSubfield('i', "one");
            nineEightyOne.addSubfield(sf); 

            String price = marcUtils.getPrice(nineEightyOne);
            if (debug) {
                System.out.println("  price: " + price);
            } else { 
                assertTrue(price == null);
            }

        } catch (Exception e) {
            assertTrue(true);
        }

    }
    
    @Test
    public void testGetPriceNullDatafield() {

        try { 
            String price = marcUtils.getPrice(null);
            if (debug) {
                System.out.println("  price: " + price);
            } else {
                assertTrue(price == null);
            }

        } catch (Exception e) {
            assertTrue(true);
        }

    }


}
