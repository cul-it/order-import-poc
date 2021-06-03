package org.olf.folio.order.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.Record;

/**
 * @author jaf30
 *
 */
public class MarcUtilsIsbnTest extends MarcUtilsBaseTest { 
    
    boolean debug = false;

    @Test
    public void testGetIsbn() {
         
        List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.harrass);
        
        try {
            for (String fname : myFnames) {
                // System.out.println(fname);
                List<Record> records = getRecords(fname);
                for (Record record : records) {
                    JSONObject jsonObj = new JSONObject();
                    JSONArray productIds = marcUtils.getISBN(record, "8261054f-be78-422d-bd51-4ed9f33c3422");
                    jsonObj.put("productIds", productIds);
                    if (debug) {
                        System.out.println(jsonObj.toString(3));
                    } else {
                        assertNotNull(productIds);
                        assertTrue(productIds.length() > 0);
                    }
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    } 
}
