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
public class MarcUtilsSeriesFieldsTest extends MarcUtilsBaseTest { 
    
    boolean debug = false; 
    
    @Test
    public void testGetSeriesFields() { 
         
        List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.bksFO);
        
        try {
            for (String fname : myFnames) {
                List<Record> records = getRecords(fname);
                int recNum = 1;
                for (Record record : records) {
                    JSONObject jsonObj = new JSONObject();
                    List<String> seriesFields = marcUtils.getSeriesFields(record);
                     
                    if (debug) {
                        System.out.println("record: " + recNum++);
                        for (String s : seriesFields) {
                            System.out.println(s);
                        }
                        System.out.println();
                    } else {
                        assertNotNull(seriesFields);
                        //assertTrue(seriesFields.size() > 0);
                    }
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    } 
}
