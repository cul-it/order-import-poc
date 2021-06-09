package org.olf.folio.order.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.Record;

/**
 * @author jaf30
 *
 */
public class MarcUtilsBuildContributorsTest extends MarcUtilsBaseTest { 
    
    boolean debug = false;
    String personalNameTypeId = "2b94c631-fca9-4892-a730-03ee529ffe2a";
    
    @Test
    public void testBuildContributors() {
        HashMap<String, String> lookupTable = new HashMap<String, String>();
        //lookupTable.put("bkp", "8261054f-be78-422d-bd51-4ed9f33c3422");
         
        List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.amazonFO);
        
        try {
            for (String fname : myFnames) {
                // System.out.println(fname);
                List<Record> records = getRecords(fname);
                for (Record record : records) {
                    JSONObject jsonObj = new JSONObject();
                    JSONArray contributors = marcUtils.buildContributors(record, lookupTable);
                    jsonObj.put("contributors", contributors);
                    if (debug) {
                        System.out.println(jsonObj.toString(3));
                    } else {
                        assertNotNull(contributors);
                        assertTrue(contributors.length() > 0);
                    }
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    } 
}
