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
public class MarcUtilsBuildIdentifiersTest extends MarcUtilsBaseTest { 
    
    boolean debug = false;

    @Test
    public void testBuildIdentifiers() {
        HashMap<String, String> lookupTable = new HashMap<String, String>();
        lookupTable.put("ISBN", "8261054f-be78-422d-bd51-4ed9f33c3422");
        lookupTable.put("Invalid ISBN", "fcca2643-406a-482a-b760-7a7f8aec640e");
        lookupTable.put("ISSN", "913300b2-03ed-469a-8179-c1092c991227");
        lookupTable.put("Invalid ISSN", "27fd35a6-b8f6-41f2-aa0e-9c663ceb250c");
        lookupTable.put("Linking ISSN", "5860f255-a27f-4916-a830-262aa900a6b9");
        
        List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.singleharrass);
        
        try {
            for (String fname : myFnames) {
                // System.out.println(fname);
                List<Record> records = getRecords(fname);
                for (Record record : records) {
                    JSONObject jsonObj = new JSONObject();
                    JSONArray identifiers = marcUtils.buildIdentifiers(record, lookupTable);
                    jsonObj.put("identifiers", identifiers);
                    if (debug) {
                        System.out.println(jsonObj.toString(3));
                    } else {
                        assertNotNull(identifiers);
                        assertTrue(identifiers.length() > 0);
                    }
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    } 
}
