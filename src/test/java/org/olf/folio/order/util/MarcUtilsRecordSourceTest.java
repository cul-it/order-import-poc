package org.olf.folio.order.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.marc4j.marc.Record;

public class MarcUtilsRecordSourceTest extends MarcUtilsBaseTest {

    boolean debug = false;

    @Test
    public void testGetRecordSourceDefault() {
        List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.amazonFO);
        try {
            for (String fname : myFnames) {
                List<Record> records = getRecords(fname);
                for (Record record : records) {
                    String recordSource = marcUtils.getRecordSource(record);
                    if (debug) {
                        System.out.println(fname + " - record source: " + recordSource);
                    } else {
                        assertEquals("", recordSource);
                    }
                }
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetRecordSourceApprovals() {
        List<String> myFnames = new ArrayList<String>();
        myFnames.add(this.approvals);
        try {
            for (String fname : myFnames) {
                List<Record> records = getRecords(fname);
                for (Record record : records) {
                    String recordSource = marcUtils.getRecordSource(record);
                    if (debug) {
                        System.out.println(fname + " - record source: " + recordSource);
                    } else {
                        assertEquals("appr", recordSource);
                    }
                }
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
